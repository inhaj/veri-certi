package com.vericerti.infrastructure.aop;

import com.vericerti.infrastructure.config.FilterProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * 느린 메서드 실행 시간 경고 AOP
 * 
 * 동작 방식:
 * 1. 모든 @Service 클래스의 public 메서드를 자동 모니터링
 * 2. 기본 임계값: application.yml의 app.slow-execution.default-threshold-ms
 * 3. @WarnSlowExecution 어노테이션이 있으면 해당 값으로 오버라이드
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class SlowMethodAspect {

    private final FilterProperties filterProperties;

    /**
     * 모든 @Service 클래스의 public 메서드 자동 모니터링
     */
    @Around("@within(org.springframework.stereotype.Service)")
    public Object monitorServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        return measureExecutionTime(joinPoint);
    }

    /**
     * @WarnSlowExecution 어노테이션이 있는 메서드 (임계값 오버라이드용)
     */
    @Around("@annotation(warnSlowExecution)")
    public Object monitorAnnotatedMethod(ProceedingJoinPoint joinPoint, 
                                         WarnSlowExecution warnSlowExecution) throws Throwable {
        // 어노테이션이 있으면 해당 값 사용, 없으면 기본값
        return measureExecutionTime(joinPoint, warnSlowExecution.thresholdMs());
    }

    private Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        return measureExecutionTime(joinPoint, -1);
    }

    private Object measureExecutionTime(ProceedingJoinPoint joinPoint, long customThreshold) throws Throwable {
        if (!filterProperties.getSlowExecution().isEnabled()) {
            return joinPoint.proceed();
        }

        // 임계값 결정: 커스텀 값 > 기본값
        long threshold = customThreshold > 0 
            ? customThreshold 
            : filterProperties.getSlowExecution().getDefaultThresholdMs();

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = ((MethodSignature) joinPoint.getSignature()).getMethod().getName();
        String fullMethodName = className + "." + methodName;

        long startTime = System.currentTimeMillis();
        
        try {
            return joinPoint.proceed();
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            if (duration > threshold) {
                log.warn("⚠️ Slow execution: {} took {}ms (threshold: {}ms)", 
                         fullMethodName, duration, threshold);
            }
        }
    }
}

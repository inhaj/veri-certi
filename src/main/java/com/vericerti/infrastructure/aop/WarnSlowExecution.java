package com.vericerti.infrastructure.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 메서드 실행 시간이 임계값을 초과하면 WARN 로그를 출력하는 어노테이션
 * 
 * 사용 예:
 * @WarnSlowExecution                    // 기본 임계값 (설정파일에서 로드)
 * @WarnSlowExecution(thresholdMs = 500) // 500ms 초과 시 경고
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WarnSlowExecution {
    
    /**
     * 임계값 (밀리초)
     * -1이면 설정파일의 기본값 사용
     */
    long thresholdMs() default -1;
}

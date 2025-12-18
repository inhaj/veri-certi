package com.vericerti.common;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

/**
 * Self-Validating 기반 클래스
 * 상속받은 클래스는 생성자에서 validateSelf()를 호출하여 자기 검증 수행
 * 
 * 사용 예:
 * public class SignupRequest extends SelfValidating<SignupRequest> {
 *     @NotBlank private final String email;
 *     
 *     public SignupRequest(String email) {
 *         this.email = email;
 *         validateSelf();  // 유효하지 않으면 예외 발생
 *     }
 * }
 */
public abstract class SelfValidating<T> {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private final Validator validator;

    protected SelfValidating() {
        validator = FACTORY.getValidator();
    }

    /**
     * 현재 객체의 유효성 검증
     * 실패 시 ConstraintViolationException 발생
     */
    @SuppressWarnings("unchecked")
    protected void validateSelf() {
        Set<ConstraintViolation<T>> violations = validator.validate((T) this);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}

package com.bigdata.app.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for transaction amounts
 * Ensures amounts are positive and within business limits
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidTransactionAmountValidator.class)
public @interface ValidTransactionAmount {
    String message() default "Transaction amount must be positive and not exceed daily limit of $100,000";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

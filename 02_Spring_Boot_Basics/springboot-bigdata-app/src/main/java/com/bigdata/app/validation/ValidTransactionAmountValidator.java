package com.bigdata.app.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

/**
 * Validator implementation for ValidTransactionAmount annotation
 * Implements business rules for transaction amount validation
 */
public class ValidTransactionAmountValidator 
        implements ConstraintValidator<ValidTransactionAmount, BigDecimal> {
    
    private static final BigDecimal MIN_AMOUNT = BigDecimal.ZERO;
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("100000.00");
    
    @Override
    public void initialize(ValidTransactionAmount constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(BigDecimal amount, ConstraintValidatorContext context) {
        if (amount == null) {
            return false;
        }
        
        if (amount.compareTo(MIN_AMOUNT) <= 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Transaction amount must be greater than zero")
                .addConstraintViolation();
            return false;
        }
        
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Transaction amount cannot exceed $100,000")
                .addConstraintViolation();
            return false;
        }
        
        return true;
    }
}

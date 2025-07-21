package com.bigdata.crypto.util;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {
    
    private List<String> errors;
    private boolean valid;
    
    public ValidationResult() {
        this.errors = new ArrayList<>();
        this.valid = true;
    }
    
    public void addError(String error) {
        this.errors.add(error);
        this.valid = false;
    }
    
    public void addErrors(List<String> errors) {
        this.errors.addAll(errors);
        if (!errors.isEmpty()) {
            this.valid = false;
        }
    }
    
    public boolean isValid() {
        return valid && errors.isEmpty();
    }
    
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }
    
    public String getErrorsAsString() {
        return String.join(", ", errors);
    }
    
    public void clear() {
        this.errors.clear();
        this.valid = true;
    }
    
    @Override
    public String toString() {
        return "ValidationResult{" +
                "valid=" + valid +
                ", errors=" + errors +
                '}';
    }
}

package edu.virginia.lib.findingaid.structure;

public class ValidationResult {

    private String reason;

    private boolean valid;

    public ValidationResult(boolean valid, String reason) {
        this.valid = valid;
        this.reason = reason;
    }

    public String getReason() {
        return this.reason;
    }

    public boolean isValid() {
        return valid;
    }


}

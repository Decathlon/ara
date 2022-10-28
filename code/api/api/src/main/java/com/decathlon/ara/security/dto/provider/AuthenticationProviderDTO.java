package com.decathlon.ara.security.dto.provider;

public class AuthenticationProviderDTO {

    private String displayValue;

    private String type;

    private String name;

    public AuthenticationProviderDTO(String displayValue, String type, String name) {
        this.displayValue = displayValue;
        this.type = type;
        this.name = name;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public void setDisplayValue(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

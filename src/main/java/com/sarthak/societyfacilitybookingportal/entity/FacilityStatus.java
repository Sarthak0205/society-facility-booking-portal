package com.sarthak.societyfacilitybookingportal.entity;

public enum FacilityStatus {
    ACTIVE("Active"),
    MAINTENANCE("Under Maintenance");

    private final String displayName;

    FacilityStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

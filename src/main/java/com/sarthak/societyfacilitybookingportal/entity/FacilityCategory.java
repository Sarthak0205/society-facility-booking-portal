package com.sarthak.societyfacilitybookingportal.entity;

public enum FacilityCategory {
    SPORTS("Sports"),
    FITNESS("Fitness"),
    RECREATION("Recreation"),
    EVENT_SPACE("Event Space");

    private final String displayName;

    FacilityCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

package com.sarthak.societyfacilitybookingportal.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "facilities")
public class Facility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long facilityId;

    @Column(nullable = false)
    private String facilityName;

    @Column(nullable = false)
    private String description;

    @Column
    private Integer capacity;

    @Column
    private String area;

    @Column
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FacilityCategory category = FacilityCategory.RECREATION;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FacilityStatus status = FacilityStatus.ACTIVE;

    public Facility() {
    }

    public Facility(
            Long facilityId,
            String facilityName,
            String description,
            Integer capacity,
            String area,
            String location,
            FacilityCategory category,
            FacilityStatus status) {
        this.facilityId = facilityId;
        this.facilityName = facilityName;
        this.description = description;
        this.capacity = capacity;
        this.area = area;
        this.location = location;
        this.category = category != null ? category : FacilityCategory.RECREATION;
        this.status = status != null ? status : FacilityStatus.ACTIVE;
    }

    public Long getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(Long facilityId) {
        this.facilityId = facilityId;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public FacilityCategory getCategory() {
        return category;
    }

    public void setCategory(FacilityCategory category) {
        this.category = category;
    }

    public FacilityStatus getStatus() {
        return status;
    }

    public void setStatus(FacilityStatus status) {
        this.status = status;
    }
}
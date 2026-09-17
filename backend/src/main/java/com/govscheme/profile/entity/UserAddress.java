package com.govscheme.profile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_addresses")
public class UserAddress {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "CHAR(36)")
    private String id;

    @PrePersist
    public void generateId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    @Column(name = "user_id", nullable = false, columnDefinition = "CHAR(36)")
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false, length = 20)
    private AddressType addressType = AddressType.PERMANENT;

    @Column(name = "is_primary", nullable = false)
    private Boolean primary = false;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "district", length = 100)
    private String district;

    @Column(name = "block", length = 100)
    private String block;

    @Column(name = "village_town", length = 255)
    private String villageTown;

    @Column(name = "pincode", length = 10)
    private String pincode;

    @Enumerated(EnumType.STRING)
    @Column(name = "area_type", length = 20)
    private AreaType areaType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public AddressType getAddressType() { return addressType; }
    public void setAddressType(AddressType addressType) { this.addressType = addressType; }

    public Boolean getPrimary() { return primary; }
    public void setPrimary(Boolean primary) { this.primary = primary; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getBlock() { return block; }
    public void setBlock(String block) { this.block = block; }

    public String getVillageTown() { return villageTown; }
    public void setVillageTown(String villageTown) { this.villageTown = villageTown; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public AreaType getAreaType() { return areaType; }
    public void setAreaType(AreaType areaType) { this.areaType = areaType; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

package com.govscheme.profile.dto;

import com.govscheme.profile.entity.AddressType;
import com.govscheme.profile.entity.AreaType;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AddressRequest {

    private AddressType addressType;

    private Boolean primary;

    @Size(max = 100, message = "State must be at most 100 characters")
    private String state;

    @Size(max = 100, message = "District must be at most 100 characters")
    private String district;

    @Size(max = 100, message = "Block must be at most 100 characters")
    private String block;

    @Size(max = 255, message = "Village/town must be at most 255 characters")
    private String villageTown;

    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Pincode must be a 6-digit Indian pincode")
    private String pincode;

    private AreaType areaType;

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
}

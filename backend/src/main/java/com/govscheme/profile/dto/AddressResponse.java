package com.govscheme.profile.dto;

import com.govscheme.profile.entity.AddressType;
import com.govscheme.profile.entity.AreaType;
import com.govscheme.profile.entity.UserAddress;

public class AddressResponse {

    private String id;
    private AddressType addressType;
    private Boolean primary;
    private String state;
    private String district;
    private String block;
    private String villageTown;
    private String pincode;
    private AreaType areaType;

    public static AddressResponse from(UserAddress address) {
        AddressResponse dto = new AddressResponse();
        dto.setId(address.getId());
        dto.setAddressType(address.getAddressType());
        dto.setPrimary(address.getPrimary());
        dto.setState(address.getState());
        dto.setDistrict(address.getDistrict());
        dto.setBlock(address.getBlock());
        dto.setVillageTown(address.getVillageTown());
        dto.setPincode(address.getPincode());
        dto.setAreaType(address.getAreaType());
        return dto;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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

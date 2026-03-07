package com.community.toolsharing.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank
    private String name;
    @Email
    @NotBlank
    private String email;
    @NotBlank
    @Size(min = 6, message = "must be at least 6 characters")
    private String password;
    private String phoneNumber;
    @NotBlank
    private String communityInviteCode;
    private String block;
    private String floor;
    private String flatNumber;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getCommunityInviteCode() { return communityInviteCode; }
    public void setCommunityInviteCode(String communityInviteCode) { this.communityInviteCode = communityInviteCode; }
    public String getBlock() { return block; }
    public void setBlock(String block) { this.block = block; }
    public String getFloor() { return floor; }
    public void setFloor(String floor) { this.floor = floor; }
    public String getFlatNumber() { return flatNumber; }
    public void setFlatNumber(String flatNumber) { this.flatNumber = flatNumber; }
}
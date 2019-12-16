package com.example.phonebook;

public class PhoneDto {
    private String name;        //联系人姓名
    private String telPhone;    //电话号码
    private String image;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTelPhone() {
        return telPhone;
    }

    public void setTelPhone(String telPhone) {
        this.telPhone = telPhone;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public PhoneDto() {
    }

    public PhoneDto(String name, String telPhone) {
        this.name = name;
        this.telPhone = telPhone;
    }

    @Override
    public String toString() {
        return String.format(name + " - " + telPhone);
    }
}

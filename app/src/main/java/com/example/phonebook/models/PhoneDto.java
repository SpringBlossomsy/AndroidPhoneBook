package com.example.phonebook.models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.example.phonebook.utils.Constants;

import java.net.URL;
import java.net.URLConnection;

public class PhoneDto {
    private String name;
    private String telPhone;
    private String image;
    private Bitmap imageBmp;
    private String rawContactsId;
    private Long photoId;


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

    public Bitmap getImageBmp() {
        return imageBmp;
    }

    public void setImageBmp(Bitmap imageBmp) {
        this.imageBmp = imageBmp;
    }

    public String getRawContactsId() {
        return rawContactsId;
    }

    public void setRawContactsId(String rawContactsId) {
        this.rawContactsId = rawContactsId;
    }

    public Long getPhotoId() {
        return photoId;
    }

    public void setPhotoId(Long photoId) {
        this.photoId = photoId;
    }

    public PhoneDto() {
    }

    public PhoneDto(String name, String telPhone) {
        this.name = name;
        this.telPhone = telPhone;
    }

    public PhoneDto(String name, String telPhone, String image, Bitmap imageBmp) {
        this.name = name;
        this.telPhone = telPhone;
        this.image = image;
        this.imageBmp = imageBmp;
    }

    public void updatePhoneDto(String name, String telPhone, final String image, final Bitmap newImageBmp) {
        this.name = name;
        this.telPhone = telPhone;
        this.image = image;
        this.imageBmp = newImageBmp;
        if (newImageBmp == null) {
            Thread thread = new Thread() {

                @Override
                public void run() {
                    Bitmap tempImageBmp;
                    try {
                        URL imageUrl = new URL(Constants.URL_DOMAIN + image);
                        tempImageBmp = BitmapFactory.decodeStream(imageUrl.openConnection().getInputStream());
                    } catch (Exception e){
                        tempImageBmp = null;
                    }
                    imageBmp = tempImageBmp;
                }
            };
            thread.start();
        }
    }

    public void updateBitmap() {
        if (this.image != null) {
            Thread thread = new Thread() {

                @Override
                public void run() {
                    Bitmap tempImageBmp;
                    try {
                        URL imageUrl = new URL(Constants.URL_DOMAIN + image);
                        URLConnection conn = imageUrl.openConnection();
                        conn.connect();
                        tempImageBmp = BitmapFactory.decodeStream(conn.getInputStream());
                    } catch (Exception e){
                        e.printStackTrace();
                        tempImageBmp = null;
                    }
                    imageBmp = tempImageBmp;
                }
            };
            thread.start();
        }
    }

    @Override
    public String toString() {
        return String.format(name + " - " + telPhone);
    }
}

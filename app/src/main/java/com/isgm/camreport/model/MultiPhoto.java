package com.isgm.camreport.model;

public class MultiPhoto {

    private String photoName;
    private String photoDate;
    private String photoType;
    private String photoRoute;
    private int imageId;
    private boolean isSelected = false;

    public MultiPhoto(String photoName, String photoDate, String photoType, String photoRoute, int imageId, boolean isSelected) {
        this.photoName = photoName;
        this.photoDate = photoDate;
        this.photoType = photoType;
        this.photoRoute = photoRoute;
        this.imageId = imageId;
        this.isSelected = isSelected;
    }

    public String getPhotoName() {
        return photoName;
    }

    public void setPhotoName(String photoName) {
        this.photoName = photoName;
    }

    public String getPhotoDate() {
        return photoDate;
    }

    public void setPhotoDate(String photoDate) {
        this.photoDate = photoDate;
    }

    public String getPhotoType() {
        return photoType;
    }

    public void setPhotoType(String photoType) {
        this.photoType = photoType;
    }

    public String getPhotoRoute() {
        return photoRoute;
    }

    public void setPhotoRoute(String photoRoute) {
        this.photoRoute = photoRoute;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
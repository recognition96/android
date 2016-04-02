package me.opklnm102.studyexampleproject;

/**
 * Created by Administrator on 2016-04-02.
 */
public class Contact {

    Integer profileImg;
    String name;
    String phoneNumber;

    public Contact() {
        this.profileImg = null;
        this.name = null;
        this.phoneNumber = null;
    }

    public Contact(Integer profileImg, String name, String phoneNumber) {
        this.profileImg = profileImg;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public Integer getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(Integer profileImg) {
        this.profileImg = profileImg;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
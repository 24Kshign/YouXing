package com.share.jack.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by 程 on 2016/4/17.
 */
public class PersonalBean implements Parcelable {

    public String userHead;
    public String userNick;
    public String userId;
    public String userSex;
    public String userLocation;

    public String getUserHead() {
        return userHead;
    }

    public void setUserHead(String userHead) {
        this.userHead = userHead;
    }

    public String getUserNick() {
        return userNick;
    }

    public void setUserNick(String userNick) {
        this.userNick = userNick;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserSex() {
        return userSex;
    }

    public void setUserSex(String userSex) {
        this.userSex = userSex;
    }

    public String getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }

    public static Creator<PersonalBean> getCREATOR() {
        return CREATOR;
    }

    /**
     * 序列化实体类
     */
    public static final Parcelable.Creator<PersonalBean> CREATOR = new Creator<PersonalBean>() {
        public PersonalBean createFromParcel(Parcel source) {
            PersonalBean personPar = new PersonalBean();
            personPar.userHead = source.readString();
            personPar.userNick = source.readString();
            personPar.userId = source.readString();
            personPar.userSex = source.readString();
            personPar.userLocation = source.readString();
            return personPar;
        }

        public PersonalBean[] newArray(int size) {
            return new PersonalBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userHead);
        dest.writeString(userNick);
        dest.writeString(userId);
        dest.writeString(userSex);
        dest.writeString(userLocation);
    }
}

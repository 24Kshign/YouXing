package com.share.jack.bean;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by 程 on 2016/4/3.
 */
public class ShopBean implements Parcelable {

    public String nickName;
    public String userId;
    public String userHead;
    public String mainImage;
    public String otherImage;
    public String content;
    public String title;
    public String readNum;
    public String time;
    public String location;
    public String longitude;
    public String latitude;
    public String recommentId;
    public String praiseNum;
    public String commentNum;

    public String getRecommentId() {
        return recommentId;
    }

    public void setRecommentId(String recommentId) {
        this.recommentId = recommentId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserHead() {
        return userHead;
    }

    public void setUserHead(String userHead) {
        this.userHead = userHead;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public String getOtherImage() {
        return otherImage;
    }

    public void setOtherImage(String otherImage) {
        this.otherImage = otherImage;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReadNum() {
        return readNum;
    }

    public void setReadNum(String readNum) {
        this.readNum = readNum;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getPraiseNum() {
        return praiseNum;
    }

    public void setPraiseNum(String praiseNum) {
        this.praiseNum = praiseNum;
    }

    public String getCommentNum() {
        return commentNum;
    }

    public void setCommentNum(String commentNum) {
        this.commentNum = commentNum;
    }

    public static Creator<ShopBean> getCREATOR() {
        return CREATOR;
    }

    /**
     * 序列化实体类
     */
    public static final Parcelable.Creator<ShopBean> CREATOR = new Creator<ShopBean>() {
        public ShopBean createFromParcel(Parcel source) {
            ShopBean personPar = new ShopBean();
            personPar.nickName = source.readString();
            personPar.userId = source.readString();
            personPar.userHead = source.readString();
            personPar.mainImage = source.readString();
            personPar.otherImage = source.readString();
            personPar.content = source.readString();
            personPar.title = source.readString();
            personPar.readNum = source.readString();
            personPar.location = source.readString();
            personPar.time = source.readString();
            personPar.longitude = source.readString();
            personPar.latitude = source.readString();
            personPar.recommentId = source.readString();
            personPar.praiseNum = source.readString();
            personPar.commentNum = source.readString();
            return personPar;
        }

        public ShopBean[] newArray(int size) {
            return new ShopBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nickName);
        dest.writeString(userId);
        dest.writeString(userHead);
        dest.writeString(mainImage);
        dest.writeString(otherImage);
        dest.writeString(content);
        dest.writeString(title);
        dest.writeString(readNum);
        dest.writeString(time);
        dest.writeString(location);
        dest.writeString(longitude);
        dest.writeString(latitude);
        dest.writeString(recommentId);
        dest.writeString(praiseNum);
        dest.writeString(commentNum);
    }
}

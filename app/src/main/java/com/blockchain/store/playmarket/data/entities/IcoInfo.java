package com.blockchain.store.playmarket.data.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class IcoInfo implements Parcelable {

    public IcoFiles files;
    public String youtubeID;
    public String email;
    @SerializedName("team")
    public ArrayList<IcoTeam> team;
    @SerializedName("advisors")
    public ArrayList<IcoTeam> advisors;
    public String description;
    public String advantages;

    public String tokenSold;
    public String usdRaised;
    public String currentStage;
    public ArrayList<String> pictures;
    @SerializedName("icon")
    public String iconUrl;


    public IcoInfo() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.files, flags);
        dest.writeString(this.youtubeID);
        dest.writeString(this.email);
        dest.writeTypedList(this.team);
        dest.writeTypedList(this.advisors);
        dest.writeString(this.description);
        dest.writeString(this.advantages);
        dest.writeString(this.tokenSold);
        dest.writeString(this.usdRaised);
        dest.writeString(this.currentStage);
        dest.writeStringList(this.pictures);
        dest.writeString(this.iconUrl);
    }

    protected IcoInfo(Parcel in) {
        this.files = in.readParcelable(IcoFiles.class.getClassLoader());
        this.youtubeID = in.readString();
        this.email = in.readString();
        this.team = in.createTypedArrayList(IcoTeam.CREATOR);
        this.advisors = in.createTypedArrayList(IcoTeam.CREATOR);
        this.description = in.readString();
        this.advantages = in.readString();
        this.tokenSold = in.readString();
        this.usdRaised = in.readString();
        this.currentStage = in.readString();
        this.pictures = in.createStringArrayList();
        this.iconUrl = in.readString();
    }

    public static final Creator<IcoInfo> CREATOR = new Creator<IcoInfo>() {
        @Override
        public IcoInfo createFromParcel(Parcel source) {
            return new IcoInfo(source);
        }

        @Override
        public IcoInfo[] newArray(int size) {
            return new IcoInfo[size];
        }
    };
}

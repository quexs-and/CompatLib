package com.quexs.compatlib.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class PString implements Parcelable {

    private String msg;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public PString(String msg) {
        this.msg = msg;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.msg);
    }

    public void readFromParcel(Parcel source) {
        this.msg = source.readString();
    }

    public PString() {
    }

    protected PString(Parcel in) {
        this.msg = in.readString();
    }

    public static final Parcelable.Creator<PString> CREATOR = new Parcelable.Creator<PString>() {
        @Override
        public PString createFromParcel(Parcel source) {
            return new PString(source);
        }

        @Override
        public PString[] newArray(int size) {
            return new PString[size];
        }
    };
}

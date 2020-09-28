package me.minetsh.imaging;

import android.os.Parcel;
import android.os.Parcelable;

public class IMGConfig implements Parcelable {

    private boolean roundClip = false;

    public boolean isRoundClip() {
        return roundClip;
    }

    public static class Builder {
        private IMGConfig config = new IMGConfig();

        /**
         * 圆形裁剪框
         */
        public Builder roundClip(boolean roundClip) {
            config.roundClip = roundClip;
            return this;
        }

        public IMGConfig build() {
            return config;
        }
    }

    protected IMGConfig() {
    }

    protected IMGConfig(Parcel in) {
        roundClip = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (roundClip ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<IMGConfig> CREATOR = new Creator<IMGConfig>() {
        @Override
        public IMGConfig createFromParcel(Parcel in) {
            return new IMGConfig(in);
        }

        @Override
        public IMGConfig[] newArray(int size) {
            return new IMGConfig[size];
        }
    };
}

package me.minetsh.imaging.gallery.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by felix on 2018/1/4 下午2:28.
 */

public class IMGChooseMode implements Parcelable {

    private boolean isOriginal = false;

    private boolean isOriginalChangeable = true;

    private boolean isSingleChoose = false;

    private int maxChooseCount = 9;

    public IMGChooseMode() {

    }

    protected IMGChooseMode(Parcel in) {
        isOriginal = in.readByte() != 0;
        isOriginalChangeable = in.readByte() != 0;
        isSingleChoose = in.readByte() != 0;
        maxChooseCount = in.readInt();
    }

    public static final Creator<IMGChooseMode> CREATOR = new Creator<IMGChooseMode>() {
        @Override
        public IMGChooseMode createFromParcel(Parcel in) {
            return new IMGChooseMode(in);
        }

        @Override
        public IMGChooseMode[] newArray(int size) {
            return new IMGChooseMode[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isOriginal ? 1 : 0));
        dest.writeByte((byte) (isOriginalChangeable ? 1 : 0));
        dest.writeByte((byte) (isSingleChoose ? 1 : 0));
        dest.writeInt(maxChooseCount);
    }

    public boolean isOriginal() {
        return isOriginal;
    }

    public boolean isOriginalChangeable() {
        return isOriginalChangeable;
    }

    public boolean isSingleChoose() {
        return isSingleChoose;
    }

    public int getMaxChooseCount() {
        return maxChooseCount;
    }

    public static class Builder {

        IMGChooseMode mode;

        public Builder() {
            mode = new IMGChooseMode();
        }

        public Builder setOriginal(boolean original) {
            mode.isOriginal = original;
            return this;
        }

        public Builder setOriginalChangeable(boolean originalChangeable) {
            mode.isOriginalChangeable = originalChangeable;
            return this;
        }

        public Builder setSingleChoose(boolean single) {
            mode.isSingleChoose = single;
            if (single) {
                mode.maxChooseCount = 1;
            }
            return this;
        }

        public Builder setMaxChooseCount(int maxChooseCount) {
            mode.maxChooseCount = maxChooseCount;
            if (mode.isSingleChoose) {
                mode.maxChooseCount = Math.min(1, maxChooseCount);
            }
            return this;
        }

        public IMGChooseMode build() {
            return mode;
        }
    }
}

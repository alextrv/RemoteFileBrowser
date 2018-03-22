package org.trv.alex.remotefilemanager;

import android.os.Parcel;
import android.os.Parcelable;

public class FileProperties implements Parcelable {
    private final String mURL;
    private final String mName;
    private final String mModified;
    private final String mSize;
    private final String mType;

    public FileProperties(String url, String name, String modified, String size, String type) {
        mURL      = url;
        mName     = name;
        mModified = modified;
        mSize     = size;
        mType     = type;
    }

    public FileProperties(Parcel in) {
        mURL      = in.readString();
        mName     = in.readString();
        mModified = in.readString();
        mSize     = in.readString();
        mType     = in.readString();
    }

    public String getURL() {
        return mURL;
    }

    public String getName() {
        return mName;
    }

    public String getModified() {
        return mModified;
    }

    public String getSize() {
        return mSize;
    }

    public String getType() {
        return mType;
    }

    public boolean isDirectory() {
        return "Directory".equals(mType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileProperties that = (FileProperties) o;

        if (!mURL.equals(that.mURL)) return false;
        if (!mName.equals(that.mName)) return false;
        if (!mModified.equals(that.mModified)) return false;
        if (!mSize.equals(that.mSize)) return false;
        return mType.equals(that.mType);
    }

    @Override
    public int hashCode() {
        int result = mURL.hashCode();
        result = 31 * result + mName.hashCode();
        result = 31 * result + mModified.hashCode();
        result = 31 * result + mSize.hashCode();
        result = 31 * result + mType.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return mName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mURL);
        dest.writeString(mName);
        dest.writeString(mModified);
        dest.writeString(mSize);
        dest.writeString(mType);
    }

    public static final Parcelable.Creator<FileProperties> CREATOR = new Creator<FileProperties>() {
        @Override
        public FileProperties createFromParcel(Parcel source) {
            return new FileProperties(source);
        }

        @Override
        public FileProperties[] newArray(int size) {
            return new FileProperties[size];
        }
    };
}

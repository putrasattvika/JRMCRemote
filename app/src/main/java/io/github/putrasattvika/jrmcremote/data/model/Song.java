package io.github.putrasattvika.jrmcremote.data.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.litesuits.orm.db.annotation.Ignore;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.annotation.Unique;
import com.litesuits.orm.db.enums.AssignType;

/**
 * Created with Android Studio.
 * User: ryan.hoo.j@gmail.com
 * Date: 9/2/16
 * Time: 4:01 PM
 * Desc: Song
 */
@Table("song")
public class Song implements Parcelable, Cloneable {

    @PrimaryKey(AssignType.AUTO_INCREMENT)
    private int id;

    @Ignore
    private int jrmcID;

    private String title;

    private String displayName;

    private String artist;

    private String album;

    @Unique
    private String path;

    private int duration;

    private int size;

    private boolean favorite;

    @Ignore
    private Bitmap albumArt;

    @Ignore
    private boolean albumArtRetrieved;

    public Song() {
        albumArt = null;
        albumArtRetrieved = false;
    }

    public Song(Parcel in) {
        readFromParcel(in);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.title);
        dest.writeString(this.displayName);
        dest.writeString(this.artist);
        dest.writeString(this.album);
        dest.writeString(this.path);
        dest.writeInt(this.duration);
        dest.writeInt(this.size);
        dest.writeInt(this.jrmcID);
//        dest.writeInt(this.favorite ? 1 : 0);
    }

    public void readFromParcel(Parcel in) {
        this.id = in.readInt();
        this.title = in.readString();
        this.displayName = in.readString();
        this.artist = in.readString();
        this.album = in.readString();
        this.path = in.readString();
        this.duration = in.readInt();
        this.size = in.readInt();
        this.jrmcID = in.readInt();
//        this.favorite = in.readInt() == 1;
    }

    public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel source) {
            return new Song(source);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public int getJrmcID() {
        return jrmcID;
    }

    public void setJrmcID(int jrmcID) {
        this.jrmcID = jrmcID;
    }

    public Bitmap getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(Bitmap albumArt) {
        this.albumArt = albumArt;
        this.albumArtRetrieved = true;
    }

    public boolean isAlbumArtRetrieved() {
        return albumArtRetrieved;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Song)) {
            return false;
        }

        return this.jrmcID == ((Song) obj).getJrmcID();
    }

    public Song clone() {
        Song c;

        try {
            c = (Song) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }

        c.jrmcID = this.jrmcID;
        c.title = this.title;
        c.displayName = this.displayName;
        c.artist = this.artist;
        c.album = this.album;
        c.path = this.path;
        c.duration = this.duration;
        c.size = this.size;
        c.favorite = this.favorite;
        c.albumArt = this.albumArt;
        c.albumArtRetrieved = this.albumArtRetrieved;

        return c;
    }
}

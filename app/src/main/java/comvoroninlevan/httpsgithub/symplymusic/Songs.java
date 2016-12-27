package comvoroninlevan.httpsgithub.symplymusic;


import android.graphics.Bitmap;

/**
 * Created by Levan on 06.11.2016.
 */

public class Songs {

    private long mSongID;
    private String mSongTitle;
    private String mSongArtist;
    private String mData;

    public Songs(long id, String title, String artist, String data){
        mSongID = id;
        mSongTitle = title;
        mSongArtist = artist;
        mData = data;
    }

    public long getSongID(){
        return mSongID;
    }
    public String getSongTitle(){
        return mSongTitle;
    }
    public String getSongArtist(){
        return mSongArtist;
    }
    public String getData(){
        return mData;
    }
}

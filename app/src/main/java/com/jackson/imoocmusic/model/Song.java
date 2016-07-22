package com.jackson.imoocmusic.model;

/**
 * Created by 90720 on 2016/7/17.
 */
public class Song {

    //歌曲名称
    private String mSongName;
    //歌曲的文件名

    private String mSongFileName;
    //歌曲名字的长度
    private int mNameLongth;

    public char[] getNameCharacters(){
        return  mSongName.toCharArray();
    }

    public String getSongFileName() {
        return mSongFileName;
    }

    public void setSongFileName(String songFileName) {
        this.mSongFileName = songFileName;


    }

    public int getNameLongth() {
        return mNameLongth;
    }



    public String getSongName() {
        return mSongName;
    }

    public void setSongName(String songName) {
        this.mSongName = songName;

        this.mNameLongth=songName.length();
    }


}

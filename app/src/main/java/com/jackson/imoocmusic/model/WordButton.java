package com.jackson.imoocmusic.model;

import android.widget.Button;

/**
 * 文字按钮
 * @author  jack
 * Created by 90720 on 2016/7/16.
 */
public class WordButton {
    public int mIndex;
    public boolean mIsVisiable;
    public String mWordString;
    public Button mViewButton;

    public WordButton(){
        mIsVisiable=true;
        mWordString="";
    }

    public Button getViewButton() {
        return mViewButton;
    }
}

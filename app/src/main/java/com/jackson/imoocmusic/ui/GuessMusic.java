package com.jackson.imoocmusic.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jackson.imoocmusic.R;
import com.jackson.imoocmusic.data.Const;
import com.jackson.imoocmusic.model.IAlertDialogButtonListener;
import com.jackson.imoocmusic.model.IWordButtonClickListener;
import com.jackson.imoocmusic.model.Song;
import com.jackson.imoocmusic.model.WordButton;
import com.jackson.imoocmusic.myui.MyGridView;
import com.jackson.imoocmusic.util.MyLog;
import com.jackson.imoocmusic.util.MyPlayer;
import com.jackson.imoocmusic.util.Util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by 90720 on 2016/7/16.
 */
public class GuessMusic extends AppCompatActivity implements IWordButtonClickListener {
    Random random=new Random();
    //唱片相关动画,动画插值器
    private Animation mPanAnim;
    private LinearInterpolator mPanLin;

    private Animation mBarInAnim;
    private LinearInterpolator mBarInLin;

    private Animation mBarOutAnim;
    private LinearInterpolator mBarOutLin;

    private ImageView mViewPan;
    private ImageView mViewPanbar;
    //Play 按键事件
    private ImageButton mBtnPlayStart;

    //判断盘片是否正在旋转
    private boolean mIsRunning=false;
    //文字框容器
    private ArrayList<WordButton> mAllWords;

    private ArrayList<WordButton> mBtnSelectWords;

    //已选择文字框UI容器
    private LinearLayout mViewWordContainer;

    private MyGridView mMyGridView;

    //过关界面view
    private View mPassView;
    //当前歌曲
    private Song mCurrentSong;
    //当前关的索引
    private int mCurrentStageIndex=-1;

    private TextView mCurrentStageView;

    public static final String TAG="GuessMusic";

    //错误答案闪烁次数
    public static final int SPASH_TIMES=6;

    //三种答案状态:正常错误不完整
    public static final int STATUS_ANSWER_RIGHT=1;
    public static final int STATUS_ANSWER_WRONG=2;
    public static final int STATUS_ANSWER_LACK=3;

    //当前金币的数量--初始值
    private int mCurrentCoins=Const.TOTAL_COINS;

    //金币数量view
    private TextView mViewCurrentCoins;

    //显示当前关的索引
    private TextView mCurrentStagePassView;

    //显示当前关的答案
    private TextView mCurrentSongNamePassView;

    private static final int ID_DIALOG_DELETE_WORD=5;
    private static final int ID_DIALOG_TIP_ANSWER=6;
    private static final int ID_DIALOG_LOCK_COINS=7;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess_music);

        //读取游戏数据
        int[] datas=Util.loadData(this);
        mCurrentStageIndex=datas[Const.INDEX_LOAD_DATA_STAGE];
        mCurrentCoins=datas[Const.INDEX_LOAD_DATA_COINS];

        mViewCurrentCoins= (TextView) findViewById(R.id.text_bar_coins);
        mViewCurrentCoins.setText(mCurrentCoins+"");
        mViewPan= (ImageView) findViewById(R.id.imageView1);
        mViewPanbar= (ImageView) findViewById(R.id.imageView2);

        mMyGridView=(MyGridView)findViewById(R.id.gridView);
        //注册wordButton监听事件
        mMyGridView.registOnWordButtonClick(this);
        mViewWordContainer= (LinearLayout) findViewById(R.id.word_select_container);


        //初始化动画
        mPanAnim= AnimationUtils.loadAnimation(this,R.anim.rotate);
        mPanLin=new LinearInterpolator();
        mPanAnim.setInterpolator(mPanLin);
        mPanAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mViewPanbar.startAnimation(mBarOutAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mBarInAnim= AnimationUtils.loadAnimation(this,R.anim.rotate_45);
        mBarInLin=new LinearInterpolator();
        mBarInAnim.setFillAfter(true);
        mBarInAnim.setInterpolator(mBarInLin);
        mBarInAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mViewPan.startAnimation(mPanAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mBarOutAnim= AnimationUtils.loadAnimation(this,R.anim.rotate_d_45);
        mBarOutLin=new LinearInterpolator();
        mBarOutAnim.setFillAfter(true);
        mBarOutAnim.setInterpolator(mBarOutLin);
        mBarOutAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                MyPlayer.stopTheSong(GuessMusic.this);
                mIsRunning=false;
                mBtnPlayStart.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mBtnPlayStart= (ImageButton) findViewById(R.id.btn_play_start);
        mBtnPlayStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handlePlayButton();
                //Toast.makeText(GuessMusic.this, "Hello", Toast.LENGTH_SHORT).show();
            }
        });
        //初始化游戏数据
        initCurrentStageData();

        //处理删除按钮事件
        handleDeleteWord();

        //处理答案提示点击事件
        handleTipAnswer();

//        mCurrentStagePassView= (TextView) findViewById(R.id.text_current_stage_pass);
//        if (mCurrentStagePassView!=null){
//            mCurrentStagePassView.setText(mCurrentStageIndex+1+"");
//        }
//
//        mCurrentSongNamePassView= (TextView) findViewById(R.id.text_current_song_name_pass);
//        if (mCurrentSongNamePassView!=null){
//            mCurrentSongNamePassView.setText(mCurrentSong.getSongName());
//        }
    }

    /**
     * 处理圆盘中间 的播放按钮
     */
    private void handlePlayButton(){
        if (mViewPanbar!=null) {
            if (!mIsRunning) {
                mIsRunning = true;
                //开始动画
                mViewPanbar.startAnimation(mBarInAnim);
                mBtnPlayStart.setVisibility(View.INVISIBLE);

                //播放音乐
                MyPlayer.playSong(GuessMusic.this,mCurrentSong.getSongFileName());


            }
        }
    }

    @Override
    protected void onPause() {
        //保存游戏数据
        Util.saveData(GuessMusic.this,mCurrentStageIndex-1,mCurrentCoins);
        //停止动画
        mViewPan.clearAnimation();
        //暂停音乐
        MyPlayer.stopTheSong(GuessMusic.this);
        super.onPause();
    }

    /**
     * 读取当前关的歌曲信息
     */
    private Song loadStageSongInfo(int stageIndex){
        Song song=new Song();
        String[] stage= Const.SONG_INFO[stageIndex];

        song.setSongFileName(stage[Const.INDEX_FILE_NAME]);
        song.setSongName(stage[Const.INDEX_SONG_NAME]);

        return song;
    }

    /**
     * 加载当前关的数据
     */
    private void initCurrentStageData(){
        //读取当前关的歌曲信息
        mCurrentSong=loadStageSongInfo(++mCurrentStageIndex);
        //初始化已选择框
        mBtnSelectWords=initWordSelect();
        ViewGroup.LayoutParams params=new ViewGroup.LayoutParams(100,100);
        //清空原来的答案--LinearLayout
        mViewWordContainer.removeAllViews();
        //增加新的答案块
        for (int i=0;i<mBtnSelectWords.size();i++){
            mViewWordContainer.addView(mBtnSelectWords.get(i).mViewButton,params);
        }
       //更新当前索引
        mCurrentStageView= (TextView) findViewById(R.id.text_current_stage);
        if (mCurrentStageView!=null){
            mCurrentStageView.setText(mCurrentStageIndex+1+"");
        }

        //获得数据
        mAllWords=initAllWord();
        //更新数据
        mMyGridView.updateData(mAllWords);

        //进入就播放音乐
        handlePlayButton();


    }

    /**
     * 初始化待选文字框
     * @return
     */
    private ArrayList<WordButton> initAllWord(){
        ArrayList<WordButton> data=new ArrayList<>();

        //获得所有待选文字
        String [] words=generateWords();
        for (int i=0;i< MyGridView.COUNTS_WORDS;i++){
            WordButton button=new WordButton();
            button.mWordString=words[i];
            data.add(button);
        }


        return data;
    }

    /**
     * 初始化已选文字框
     */
    private ArrayList<WordButton> initWordSelect(){
        ArrayList<WordButton> data=new ArrayList<>();
        for (int i=0;i<mCurrentSong.getNameLongth();i++){
            View view= Util.getView(GuessMusic.this,R.layout.self_ui_gridview_item);
            final WordButton holder=new WordButton();
            holder.mViewButton= (Button) view.findViewById(R.id.item_btn);
            holder.mViewButton.setTextColor(Color.WHITE);
            holder.mViewButton.setText("");
            holder.mIsVisiable=false;
            holder.mViewButton.setBackgroundResource(R.drawable.game_wordblank);
            holder.mViewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cleanTheAnswer(holder);
                }
            });

            data.add(holder);
        }
        return data;
    }

    @Override
    public void onWordButtonClick(WordButton wordButton) {
        setSelectWord(wordButton);

        //获得答案状态
        int checkResult=checkTheAnswer();
        //检查答案
        switch (checkResult){
            case STATUS_ANSWER_RIGHT:
                //过关并获得奖励
                handlePassEvent();
                break;
            case STATUS_ANSWER_WRONG:
                //闪烁文字并提示用户
                sparkTheWord();
                break;
            case STATUS_ANSWER_LACK:
                //答案缺失时设置文字颜色为白色
                for (int i=0;i<mBtnSelectWords.size();i++){
                    mBtnSelectWords.get(i).mViewButton.setTextColor(Color.WHITE);
                }
                break;
        }
        //Toast.makeText(GuessMusic.this, "李杰真棒棒"+wordButton.mIndex, Toast.LENGTH_SHORT).show();
    }

    /**
     * 处理过关逻辑
     */
    private void handlePassEvent(){
        //Toast.makeText(GuessMusic.this, "JACK棒棒", Toast.LENGTH_SHORT).show();
        mPassView=(LinearLayout)this.findViewById(R.id.pass_view);
        mPassView.setVisibility(View.VISIBLE);

        //停止未完成的动画
        mViewPan.clearAnimation();

        //停止正在播放的音乐
        MyPlayer.stopTheSong(GuessMusic.this);

        MyPlayer.playTone(GuessMusic.this,MyPlayer.INDEX_STONE_COIN);
        //当前关的索引
        mCurrentStagePassView= (TextView) findViewById(R.id.text_current_stage_pass);
        if (mCurrentStagePassView!=null){
            mCurrentStagePassView.setText(mCurrentStageIndex+1+"");
        }
        //显示歌曲的名称
        mCurrentSongNamePassView= (TextView) findViewById(R.id.text_current_song_name_pass);
        if (mCurrentSongNamePassView!=null){
            mCurrentSongNamePassView.setText(mCurrentSong.getSongName());
        }

        //下一关按键处理
        ImageButton btnPass= (ImageButton) findViewById(R.id.btn_next);
        btnPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //通关与否
                if (judegAppPassed()){
                    //进入通关界面
                    Util.startActivity(GuessMusic.this,AllPassView.class);

                }else {
                    //开始新一关
                    mPassView.setVisibility(View.GONE);
                    //加载下一关关卡数据
                    initCurrentStageData();
                }
            }
        });
    }

    /**
     * 判断是否通关
     * @return
     */
    private boolean judegAppPassed(){


        return (mCurrentStageIndex==Const.SONG_INFO.length-1);
    }
    /**
     * 清除答案
     * @param wordButton 已选框被点击的button
     */
    private void cleanTheAnswer(WordButton wordButton){
        wordButton.mViewButton.setText("");
        wordButton.mWordString="";
        wordButton.mIsVisiable=false;
        //设置待选框的可见性
        setButtonVisiable(mAllWords.get(wordButton.mIndex),View.VISIBLE);
    }
    /**
     * 设置答案
     * @param wordButton
     */
    private void setSelectWord(WordButton wordButton){
        for (int i=0;i<mBtnSelectWords.size();i++){
            if (mBtnSelectWords.get(i).mWordString.length()==0){
                //设置答案文字框的内容和可见性
                mBtnSelectWords.get(i).mViewButton.setText(wordButton.mWordString);
                mBtnSelectWords.get(i).mIsVisiable=true;
                mBtnSelectWords.get(i).mWordString=wordButton.mWordString;
                //记录索引
                mBtnSelectWords.get(i).mIndex=wordButton.mIndex;


                MyLog.i(TAG,mBtnSelectWords.get(i).mIndex+"");
                //设置待选框的可见性
                setButtonVisiable(wordButton,View.INVISIBLE);

                break;
            }
        }
    }

    /**
     * 设置待选文字框是否可见
     * @param button
     * @param visibility
     */
    private void setButtonVisiable(WordButton button,int visibility){

        button.mViewButton.setVisibility(visibility);
        button.mIsVisiable=(visibility==View.VISIBLE)?true:false;
        MyLog.d(TAG,button.mIsVisiable+"");
    }

    /**
     * 生成所有待选文字
     * @return
     */
    private String[] generateWords(){
        String [] words=new String[MyGridView.COUNTS_WORDS];
        //存入歌名
        for (int i=0;i<mCurrentSong.getNameLongth();i++){
            words[i]=mCurrentSong.getNameCharacters()[i]+"";
        }

        //获取随机文字
        for (int i=mCurrentSong.getNameLongth();i<MyGridView.COUNTS_WORDS;i++){
            words[i]=getRandomChar()+"";
        }
        //打乱文字顺序:首先从所有文字中随机选取一个文字与第一个文字进行交换
        //然后从第二个文字之后选择一个元素与第二个文字交换,直到最后打乱所有顺序
        for (int i=MyGridView.COUNTS_WORDS-1;i>=0;i--){
            int index=random.nextInt(i+1);
            String buf=words[index];
            words[index]=words[i];
            words[i]=buf;
        }
        return words;
    }

    /**
     * 生成随机汉字
     * @return
     */
    private char getRandomChar(){
        String str="";
        int hightPos;
        int lowPos;

        Random random=new Random();
        hightPos=(176+Math.abs(random.nextInt(39)));
        lowPos=(161+Math.abs(random.nextInt(93)));
        byte[] b=new byte[2];
        b[0]=(Integer.valueOf(hightPos)).byteValue();
        b[1]=(Integer.valueOf(lowPos)).byteValue();

        try {
            str=new String(b,"GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str.charAt(0);
    }
    /**
     * 检查答案
     */
    private int checkTheAnswer(){
        //先检查长度
        for (int i=0;i<mBtnSelectWords.size();i++){
            if (mBtnSelectWords.get(i).mWordString.length()==0){
                return STATUS_ANSWER_LACK;
            }
        }
        //答案完整,检查答案正确与否
        StringBuffer sb=new StringBuffer();
        for (int i=0;i<mBtnSelectWords.size();i++){
            sb.append(mBtnSelectWords.get(i).mWordString);
        }
        return (sb.toString().equals(mCurrentSong.getSongName()))?STATUS_ANSWER_RIGHT:STATUS_ANSWER_WRONG;

    }

    /**
     * 闪烁文字:变换文字颜色
     */
    private void sparkTheWord(){
        //定时器相关
        TimerTask task=new TimerTask() {

            Boolean change=false;
            int spardTimes=0;

            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(++spardTimes>SPASH_TIMES){
                            return;
                        }
                        for (int i=0;i<mBtnSelectWords.size();i++){
                            mBtnSelectWords.get(i).mViewButton.setTextColor(change?Color.RED:Color.WHITE);
                        }
                        change=!change;
                    }
                });
            }
        };
        Timer timer=new Timer();
        timer.schedule(task,1,150);
    }

    /**
     * 提示一个文字
     */
    private void tipAnswer(){
        //减少金币数量
        if(!handleCoins(-getTipAnswerCoins())){
            //显示对话框提示用户金币不够
            showConfirmDialog(ID_DIALOG_LOCK_COINS);
        }

            boolean tipWord=false;
            for (int i=0;i<mBtnSelectWords.size();i++){
                if (mBtnSelectWords.get(i).mWordString.length()==0){

                    //根据当前答案框的条件选择对应的文字并填入
                    onWordButtonClick(findIsAnswerWord(i));
                    tipWord=true;
                    break;
                }
            }

        //没有找到可以填充的答案
        if(!tipWord){
            sparkTheWord();
        }
    }

    /**
     * 删除文字
     */
    private void deleteOneWord(){
        //减少金币
        if (!handleCoins(-getDeleteWordCoins())){
            //金币不够,显示对话框
            showConfirmDialog(ID_DIALOG_LOCK_COINS);
        }else{
            //将金币减少,设置文字框不可见
            setButtonVisiable(findNotAnswerWord(),View.INVISIBLE);
        }

    }

    /**
     * 找到一个当前可见,并且不是答案的文字按钮
     * @return
     */
    private WordButton findNotAnswerWord(){
        Random random=new Random();
        WordButton buf=null;
        while (true){
            int index=random.nextInt(MyGridView.COUNTS_WORDS);
            buf=mAllWords.get(index);
            if (buf.mIsVisiable&&!isTheAnswerWord(buf)){
                return buf;
            }
        }

    }

    /**
     * 找到一个答案的文字按钮
     * @return
     */
    private WordButton findIsAnswerWord(int index){

        WordButton buf=null;
       for (int i=0;i<MyGridView.COUNTS_WORDS;i++){
           buf=mAllWords.get(i);
           if (buf.mWordString.equals(""+mCurrentSong.getNameCharacters()[index])){
               return buf;
           }
       }

        return null;
    }

    /**
     * 判断文字是不是答案--是否被包含
     */

    private boolean isTheAnswerWord(WordButton word){
        boolean result=false;
        for (int i=0;i<mCurrentSong.getNameLongth();i++){
            if (word.mWordString.equals(""+mCurrentSong.getNameCharacters()[i])){
                result=true;
                break;
            }
        }
        return result;
    }
    /**
     * 增加或者减少指定数量的金币
     * @return true,操作成功..
     */
    private boolean handleCoins(int data){
        //判断当前总的金币数量可被减少
        if (mCurrentCoins+data>=0){
            mCurrentCoins+=data;
            mViewCurrentCoins.setText(mCurrentCoins+"");
            return true;
        }else {
            //金币不够

            return false;
        }

    }

    /**
     * 从xml文件中读取删除操作所要用的金币
     * @return
     */
    private int getDeleteWordCoins(){
        return this.getResources().getInteger(R.integer.pay_delete_answer);
    }
    /**
     * 从xml文件中读取答案提示所要用到的金币
     * @return
     */
    private int getTipAnswerCoins(){
        return this.getResources().getInteger(R.integer.pay_tip_answer);
    }
    /**
     * 处理删除待选文字
     */
    private void handleDeleteWord(){
        ImageButton button= (ImageButton) findViewById(R.id.btn_delete_word);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    showConfirmDialog(ID_DIALOG_DELETE_WORD);
                    //deleteOneWord();

            }
        });
    }

    /**
     * 处理提示按键事件
     */
    private void handleTipAnswer(){
        ImageButton button= (ImageButton) findViewById(R.id.btn_tip_answer);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showConfirmDialog(ID_DIALOG_TIP_ANSWER);
                //
            }
        });
    }

    //自定义AlertDialog的事件响应
    //删除错误答案
    private IAlertDialogButtonListener mBtnOkDeleteWordListener = new IAlertDialogButtonListener() {
        @Override
        public void onClick() {
            //执行事件
            deleteOneWord();
        }
    };
    //答案提示
    private IAlertDialogButtonListener mBtnOkTipAnswerWordListener = new IAlertDialogButtonListener() {
        @Override
        public void onClick() {
            //执行事件
            tipAnswer();
        }
    };
    //金币不足
    private IAlertDialogButtonListener mBtnOkLockWordListener = new IAlertDialogButtonListener() {
        @Override
        public void onClick() {
            //执行事件
        }
    };

    /**
     * 显示对话框
     * @param id
     */
    private void showConfirmDialog(int id){
        switch (id){
            case ID_DIALOG_DELETE_WORD:
                Util.showDialog(GuessMusic.this,"确认花掉"+getDeleteWordCoins()+"个金币去掉一个错误答案",mBtnOkDeleteWordListener);
                break;
            case ID_DIALOG_TIP_ANSWER:
                Util.showDialog(GuessMusic.this,"确认花掉"+getTipAnswerCoins()+"个金币获得答案提示",mBtnOkTipAnswerWordListener);
                break;
            case ID_DIALOG_LOCK_COINS:
                Util.showDialog(GuessMusic.this,"金币不足,请补充",mBtnOkLockWordListener);
                break;
        }
    }
}

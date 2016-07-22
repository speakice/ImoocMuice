package com.jackson.imoocmusic.myui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import com.jackson.imoocmusic.R;
import com.jackson.imoocmusic.model.IWordButtonClickListener;
import com.jackson.imoocmusic.model.WordButton;
import com.jackson.imoocmusic.util.Util;

import java.util.ArrayList;

/**
 * 自定义GridView以及适配器
 * Created by 90720 on 2016/7/16.
 */
public class MyGridView extends GridView {
    public static final int COUNTS_WORDS=24;
    private ArrayList<WordButton> mArrayList=new ArrayList<>();
    private MyGridAdapter mAdapter;
    private Context mContext;
    private Animation mScaleAnimation;

    private IWordButtonClickListener mWordButtonListener;
    /**
     * MyGridView需要放入布局管理器所以使用两个参数的构造方法
     */

    public MyGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext=context;
        mAdapter=new MyGridAdapter();
        this.setAdapter(mAdapter);
    }

    public void updateData(ArrayList<WordButton> list){
        mArrayList=list;
        //重新设置适配器
        setAdapter(mAdapter);
    }

    class  MyGridAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mArrayList.size();
        }

        @Override
        public Object getItem(int i) {
            return mArrayList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            final WordButton holder;

            if (view==null){
                view= Util.getView(mContext, R.layout.self_ui_gridview_item);
                holder=mArrayList.get(i);

                //动画第二步2.初始化动画
                mScaleAnimation= AnimationUtils.loadAnimation(mContext,R.anim.scale);

                //为动画设置延迟时间
                mScaleAnimation.setStartOffset(i*100);
                holder.mIndex=i;
                //通过if判断解决第一个button点击后不能隐藏的bug
                if (holder.getViewButton() == null) {
                    holder.mViewButton = (Button) view.findViewById(R.id.item_btn);
                    holder.mViewButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mWordButtonListener.onWordButtonClick(holder);
                        }
                    });
                }
                view.setTag(holder);
            }else {
                holder= (WordButton) view.getTag();
            }
            holder.mViewButton.setText(holder.mWordString);

            //动画第三步3.播放动画
            view.startAnimation(mScaleAnimation);
            return view;
        }


    }
    /**
     * 注册监听接口
     * @param listener
     */
    public void registOnWordButtonClick(IWordButtonClickListener listener){
        mWordButtonListener=listener;
    }
}

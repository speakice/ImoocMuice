package com.jackson.imoocmusic.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jackson.imoocmusic.R;
import com.jackson.imoocmusic.data.Const;
import com.jackson.imoocmusic.model.IAlertDialogButtonListener;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.jackson.imoocmusic.data.Const.FILE_NAME_SAVE_DATA;

/**
 * Created by 90720 on 2016/7/16.
 */
public class Util {

    private static AlertDialog mAlertDialog;

    public static View getView(Context context,int layoutId){
        LayoutInflater inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout=inflater.inflate(layoutId,null);
        return layout;
    }

    /**
     * 页面跳转的方法
     * @param context
     * @param desti
     */
    public static void startActivity(Context context,Class desti){
        Intent intent=new Intent();
        intent.setClass(context,desti);
        context.startActivity(intent);

        //关闭当前activity
        ((Activity)context).finish();
    }

    /**
     * 显示自定义对话框
     * @param context
     * @param message
     * @param listener
     */
    public static void showDialog(final Context context, String message, final IAlertDialogButtonListener listener){
        View dialogView=null;
        AlertDialog.Builder builder=new AlertDialog.Builder(context,R.style.Theme_transparent);
        dialogView=getView(context, R.layout.dialog_view);

        ImageButton btnOkView= (ImageButton) dialogView.findViewById(R.id.btn_dialog_ok);
        ImageButton btnCancelView= (ImageButton) dialogView.findViewById(R.id.btn_dialog_cancel);
        TextView textMessageView= (TextView) dialogView.findViewById(R.id.text_dialog_message);
        textMessageView.setText(message);

        btnOkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //关闭对话框
                if (mAlertDialog!=null){
                    mAlertDialog.dismiss();
                }
                //事件回调
                if (listener!=null) {
                    listener.onClick();
                }

                //播放音效
                MyPlayer.playTone(context,MyPlayer.INDEX_STONE_ENTER);
            }
        });

        btnCancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //关闭对话框
                if (mAlertDialog!=null){
                    mAlertDialog.dismiss();
                }
                MyPlayer.playTone(context,MyPlayer.INDEX_STONE_CANCEL);
            }
        });
        //为dialog设置view
        builder.setView(dialogView);
        //创建对话框
        mAlertDialog=builder.create();
        //显示dialog
        mAlertDialog.show();
    }

    /**
     * 保存游戏数据
     * @param context
     * @param stageIndex
     */
    public static void saveData(Context context,int stageIndex,int coins){

        FileOutputStream fis=null;
        try {
            fis=context.openFileOutput(Const.FILE_NAME_SAVE_DATA,Context.MODE_PRIVATE);
            //将节点流套到处理流上
            DataOutputStream dos=new DataOutputStream(fis);
            dos.writeInt(stageIndex);
            dos.writeInt(coins);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (fis!=null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 读取游戏数据
     * @param context
     * @return
     */
    public static int[] loadData(Context context){
        FileInputStream fis=null;
        int[] datas={-1,Const.TOTAL_COINS};
        try {
            fis = context.openFileInput(Const.FILE_NAME_SAVE_DATA);
            DataInputStream dis=new DataInputStream(fis);
            datas[Const.INDEX_LOAD_DATA_STAGE]=dis.readInt();
            datas[Const.INDEX_LOAD_DATA_COINS]=dis.readInt();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis!=null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return datas;
    }
}

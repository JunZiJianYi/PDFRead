package com.example.pdf;


import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.artifex.mupdfdemo.DrawingLoadUtils;
import com.artifex.mupdfdemo.MuPDFReaderView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * <pre>
 *     author : 孙伟
 *     e-mail : sw610896314@outlook.com
 *     time   : 2018/08/30
 *     desc   :pdf预览
 *     version: 1.0
 * </pre>
 */

public class MainActivity extends AppCompatActivity{

    private RelativeLayout mDrawingShow;

    MuPDFReaderView muPDFReaderView;
    //调用工具类
    DrawingLoadUtils drawingLoadUtils = new DrawingLoadUtils();


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawingShow = findViewById(R.id.rlDrawingShow);

        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }


        //将asset文件写入到本地
       if(copyAssetAndWrite("1.pdf"))
       {
           // 加载图纸
           initDrawingUrl();
       }


        //长按监听按钮
        muPDFReaderView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Map<String, String> getDrawingXYMap =  new HashMap<>();
                getDrawingXYMap =  muPDFReaderView.getDrawingXY(view,motionEvent);
                String d =  String.valueOf( getDrawingXYMap.get("XValue"));
                if(!d.equals("null"))
                {
                    Log.e("测试","X轴坐标" + String.valueOf( getDrawingXYMap.get("XValue") ));
                    Log.e("测试","Y轴坐标" + String.valueOf( getDrawingXYMap.get("YValue") ));
                }
                return false;
            }
        });

    }

    /**
     * 本地路径格式图纸
     */
    public void initDrawingUrl() {

        Uri uri = Uri.parse(getCacheDir() +"/"+"1.pdf");

        muPDFReaderView = drawingLoadUtils.drawDrawingUri(uri,this);
        mDrawingShow.addView(muPDFReaderView);

    }


    /**
     * byte数组格式图纸
     */
    public void initDrawingByte() {

        Uri uri = Uri.parse(getCacheDir() +"/"+"1.pdf");

        byte buffer[] = null;

        try {
            File file =new File(uri.getPath());
            FileInputStream in = new FileInputStream(file);
            long inSize = in.getChannel().size();//判断FileInputStream中是否有内容
            if (inSize == 0) {
                Log.d(TAG,"The FileInputStream has no content!");
            }
            buffer = new byte[in.available()];//in.available() 表示要读取的文件中的数据长度
            in.read(buffer);  //将文件中的数据读到buffer中
            in.close();
        }
        catch (OutOfMemoryError e) {
            System.out.println("Out of memory during buffer reading");
        }
        catch (Exception e) {

        }

        muPDFReaderView = drawingLoadUtils.drawDrawingByte(buffer,this);
        mDrawingShow.addView(muPDFReaderView);

    }


    /**
     * 将asset文件写入到本地
     */
    private boolean copyAssetAndWrite(String fileName){
        try {
            File cacheDir=getCacheDir();
            if (!cacheDir.exists()){
                cacheDir.mkdirs();
            }
            File outFile =new File(cacheDir,fileName);
            if (!outFile.exists()){
                boolean res=outFile.createNewFile();
                if (!res){
                    return false;
                }
            }else {
                if (outFile.length()>10){//表示已经写入一次
                    return true;
                }
            }
            InputStream is=getAssets().open(fileName);
            FileOutputStream fos = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int byteCount;
            while ((byteCount = is.read(buffer)) != -1) {
                fos.write(buffer, 0, byteCount);
            }
            fos.flush();
            is.close();
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

}
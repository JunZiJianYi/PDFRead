package com.artifex.mupdfdemo;

import android.content.Context;
import android.net.Uri;


/**
 * Created by 孙伟 on 2018/8/28.
 */
//图纸加载工具
public class DrawingLoadUtils {


    public MuPDFCore core;
    public MuPDFReaderView mDocView;


    //传入uri地址方式加载图纸
    public  MuPDFReaderView drawDrawingUri(Uri uri, Context context) {
        try {
            core = new MuPDFCore(context.getApplicationContext(), uri.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (core != null && core.countPages() == 0) {
            core = null;
        }
        if (core == null || core.countPages() == 0 || core.countPages() == -1) {
        }
        if (core != null) {
            mDocView = new MuPDFReaderView(context.getApplicationContext()) {
                @Override
                protected void onMoveToChild(int i) {
                    if (core == null)
                        return;
                    super.onMoveToChild(i);
                }
            };
            mDocView.setAdapter(new MuPDFPageAdapter(context.getApplicationContext(), new FilePicker.FilePickerSupport() {
                @Override
                public void performPickFor(FilePicker picker) {

                }
            }, core));
        }
        return mDocView;
    }

    //传入byte数组的方式加载图纸
    public  MuPDFReaderView drawDrawingByte( byte buffer[], Context context) {
        try {
            core = new MuPDFCore(context.getApplicationContext(), buffer,"");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (core != null && core.countPages() == 0) {
            core = null;
        }
        if (core == null || core.countPages() == 0 || core.countPages() == -1) {
        }
        if (core != null) {
            mDocView = new MuPDFReaderView(context.getApplicationContext()) {
                @Override
                protected void onMoveToChild(int i) {
                    if (core == null)
                        return;
                    super.onMoveToChild(i);
                }
            };
            mDocView.setAdapter(new MuPDFPageAdapter(context.getApplicationContext(), new FilePicker.FilePickerSupport() {
                @Override
                public void performPickFor(FilePicker picker) {

                }
            }, core));
        }
        return mDocView;
    }









}
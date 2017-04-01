package com.samulle.plugin.openfile;

import java.io.File;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONException;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

/**
 * 用于打开手机本地文件
 * @author admin
 *
 */
public class OpenFile extends CordovaPlugin {
    private static Context context;
    private static CallbackContext callbackContext;
    private static String path;
    protected final static String[] permissions = { Manifest.permission.READ_EXTERNAL_STORAGE };

    public static final int SAVE_TO_ALBUM_SEC = 1;
    public static final int PERMISSION_DENIED_ERROR = 20;

    @SuppressLint("DefaultLocale")
    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) {

        try {
            this.context = cordova.getActivity().getApplicationContext();
            this.callbackContext = callbackContext;
            //文件路径
            this.path = args.getString(0).toLowerCase();
            //权限
            if(!PermissionHelper.hasPermission(this, permissions[0])) {
                PermissionHelper.requestPermission(this, SAVE_TO_ALBUM_SEC, permissions[0]);
            }else{
                openFile(path);
            }

        } catch (Exception e) {
            LOG.e("error", e.getLocalizedMessage());
        }
        return true;
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        for(int r:grantResults)
        {
            if(r == PackageManager.PERMISSION_DENIED)
            {
                this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR));
                return;
            }
        }
        switch(requestCode)
        {
            case SAVE_TO_ALBUM_SEC:
                openFile(path);
                break;
        }
    }

    private static void openFile(String path) {
        int len = path.length();
        String lastThree = path.substring(len-3,len);//路径后3位
        String lastFour = path.substring(len-4,len);//路径后4位
        //判断文件类型
        //doc或docx文件
        if("doc".equals(lastThree) || "docx".equals(lastFour)){
            Intent intent = getWordFileIntent(path);
            context.startActivity(intent);
        }
        //excel
        else if("xls".equals(lastThree) || "xlsx".equals(lastThree)){
            Intent intent = getExcelFileIntent(path);
            context.startActivity(intent);
        }
        //ppt
        else if(lastThree.equals("ppt") || lastFour.equals("pptx")){
            Intent i = getPptFileIntent(path);
            context.startActivity(i);
        }
        //pdf
        else if(lastThree.equals("pdf")){
            Intent i = getPdfIntent(path);
            context.startActivity(i);
        }
        //图片
        else if(lastThree.equals("jpg") || lastThree.equals("png")
                || lastThree.equals("gif") || lastThree.equals("bmp")
                || lastFour.equals("jpeg")){
            Intent i = getImageIntent(path);
            context.startActivity(i);
        }
        //文本
        else if(lastThree.equals("txt")){
            Intent i = getTextFileIntent(path, false);
            context.startActivity(i);
        }
        //html
        else if(lastThree.equals("htm") || lastFour.equals("html")){
            Intent i = getHtmlFileIntent(path);
            context.startActivity(i);
        }
        //chm
        else if(lastThree.equals("chm")){
            Intent i = getChmFileIntent(path);
            context.startActivity(i);
        }
        //音频
        else if(lastThree.equals("mp3") || lastThree.equals("wav")
                || lastThree.equals("wma") || lastThree.equals("ogg")
                || lastThree.equals("ape") || lastThree.equals("acc")){
            Intent i = getAudioFileIntent(path);
            context.startActivity(i);
        }
        //视频
        else if(lastThree.equals("avi") || lastThree.equals("mov")
                || lastThree.equals("asf") || lastThree.equals("wmv")
                || lastThree.equals("navi") || lastThree.equals("3gp")
                || lastThree.equals("ram") || lastThree.equals("mkv")
                || lastThree.equals("flv") || lastThree.equals("mp4")
                || lastFour.equals("rmvb") || lastThree.equals("mpg")){
            Intent i = getVideoFileIntent(path);
            context.startActivity(i);
        }
        else{
            callbackContext.error("无法打开该文件！");
        }
    }

    //获取一个用于打开word文档的intent
    public static Intent getWordFileIntent(String path) {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Uri uri = getUri(path);
        intent.setDataAndType(uri, "application/msword");

        return intent;
    }

    //获取一个用于打开Excel文件的intent
    public static Intent getExcelFileIntent(String path) {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Uri uri = getUri(path);
        intent.setDataAndType(uri, "application/vnd.ms-excel");

        return intent;

    }

    //获取一个用于打开HTML的intent
    public static Intent getHtmlFileIntent(String path) {
        Uri uri = Uri.parse(path).buildUpon().encodedAuthority("com.android.htmlfileprovider").scheme("content").encodedPath(path).build();

        Intent intent = new Intent(Intent.ACTION_VIEW);

        intent.setDataAndType(uri, "text/html");

        return intent;
    }

    //获取一个用于打开图片的intent
    public static Intent getImageIntent(String path) {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

//        Uri uri = Uri.fromFile(new File(path));
        Uri uri = getUri(path);
        intent.setDataAndType(uri, "image/*");

        return intent;
    }

    //获取一个用于打开PDF的intent
    public static Intent getPdfIntent(String path) {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Uri uri = getUri(path);
        intent.setDataAndType(uri, "application/pdf");

        return intent;
    }

    //获取一个用于打开文本文件的intent
    public static Intent getTextFileIntent( String path, boolean paramBoolean){

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = getUri(path);
        intent.setDataAndType(uri, "text/plain");

        return intent;

    }

    //android获取一个用于打开音频文件的intent
    public static Intent getAudioFileIntent( String param ){

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);

        Uri uri = getUri(param);
        intent.setDataAndType(uri, "audio/*");

        return intent;

    }

    //android获取一个用于打开视频文件的intent
    public static Intent getVideoFileIntent( String param ){

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);

        Uri uri = getUri(param);
        intent.setDataAndType(uri, "video/*");

        return intent;

    }

    //android获取一个用于打开CHM文件的intent
    public static Intent getChmFileIntent( String param ){

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Uri uri = getUri(param);
        intent.setDataAndType(uri, "application/x-chm");

        return intent;

    }

    //android获取一个用于打开PPT文件的intent
    public static Intent getPptFileIntent( String param ){

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Uri uri = getUri(param);
        intent.setDataAndType(uri, "application/vnd.ms-powerpoint");

        return intent;

    }

    public static Uri getUri(String UrlStr) {
        File file = new File(UrlStr);
        Uri uri = null;
        //判读版本是否在7.0以上
        if (Build.VERSION.SDK_INT >= 24) {
            //provider authorities
            uri = FileProvider.getUriForFile(context, context.getPackageName()+".provider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

}

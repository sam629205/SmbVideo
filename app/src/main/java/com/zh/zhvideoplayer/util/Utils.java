package com.zh.zhvideoplayer.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import com.zh.zhvideoplayer.MyApplication;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 播放器工具栏出现和隐藏动画
 */
public class Utils {
	/**
     * 位移动画
     *
     * @param view
     * @param xFrom
     * @param xTo
     * @param yFrom
     * @param yTo
     * @param duration
     */
    public static void translateAnimation(View view, float xFrom, float xTo,
                                          float yFrom, float yTo, long duration) {

        TranslateAnimation translateAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, xFrom, Animation.RELATIVE_TO_SELF, xTo,
                Animation.RELATIVE_TO_SELF, yFrom, Animation.RELATIVE_TO_SELF, yTo);
        translateAnimation.setFillAfter(false);
        translateAnimation.setDuration(duration);
        view.startAnimation(translateAnimation);
        translateAnimation.startNow();
    }
    public static Drawable getDrawable(String imgName){
        Context context = MyApplication.getInstance().getContext();
        int imgId = context.getResources().getIdentifier(imgName,"drawable",context.getPackageName());
        return context.getResources().getDrawable(imgId);
    }
    /*
     *timestamp change
     */
    public static String dateToTimeStamp(String date) throws ParseException {
        SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd");
        Date dat = format.parse(date);
        return dat.getTime()+"";
    }
    /*
    *timestamp change
    * type = 0 format yyyy-MM-dd
    * type = 1 format MM/dd
    */
    public static String timeStampToDate(String timeStamp, int type){
        if (timeStamp.isEmpty() || timeStamp.equals("null")){
            return "";
        }
        long time = Long.parseLong(timeStamp);
        String date = null;
        switch (type){
            case 0:
                date = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date(time));
                break;
            case 1:
                date = new java.text.SimpleDateFormat("MM/dd HH:mm").format(new Date(time));
                break;
            case 2:
                date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time));
                break;
            case 3:
                date = new java.text.SimpleDateFormat("yyyyMMdd").format(new Date(time));
                break;
            default:
                break;
        }
        Long timestamp = Long.parseLong(timeStamp)*1000;

        return date;
    }
    public static Bitmap createRightHalfImage(Bitmap paramBitmap) {
        int i = 0;
        int j = 0;
        if (paramBitmap!=null) {
            i = paramBitmap.getWidth();
            j = paramBitmap.getHeight();
        }
        Matrix localMatrix = new Matrix();
        localMatrix.postScale(1, 1);
        Bitmap localBitmap1 = Bitmap.createBitmap(paramBitmap, paramBitmap.getWidth()/2, 0, paramBitmap.getWidth()/2, paramBitmap.getHeight(), localMatrix, true);
        return localBitmap1;
    }
    public static boolean isVideoType(String path){
        if (path.indexOf(".avi")!=-1||path.indexOf(".mp4")!=-1||path.indexOf(".rmvb")!=-1||path.indexOf(".mkv")!=-1||path.indexOf(".mpg")!=-1||path.indexOf(".wmv")!=-1||path.indexOf(".asf")!=-1){
            return true;
        }else {
            return false;
        }
    }
    public static int dp2px(Context context, int dpVal) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpVal * scale + 0.5f);
    }

    public static int sp2px(Context context, int spVal) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spVal * fontScale + 0.5f);
    }
}

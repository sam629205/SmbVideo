package com.zh.zhvideoplayer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;
import com.zh.zhvideoplayer.player.MyMediaController;

/**
 * Created by Adolf Li on 2018/1/29.
 */

public class MyGSYPlayer extends StandardGSYVideoPlayer{
    private ImageView forwardSeekIv;
    private MyMediaController myMediaController;
    public MyGSYPlayer(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public MyGSYPlayer(Context context) {
        super(context);
    }

    public MyGSYPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    public GSYBaseVideoPlayer startWindowFullscreen(Context context, boolean actionBar, boolean statusBar) {
        MyGSYPlayer video = (MyGSYPlayer) super.startWindowFullscreen(context, actionBar, statusBar);
        this.forwardSeekIv.setVisibility(VISIBLE);
        return video;
    }


    @Override
    protected void resolveNormalVideoShow(View oldF, ViewGroup vp, GSYVideoPlayer gsyVideoPlayer) {
        super.resolveNormalVideoShow(oldF, vp, gsyVideoPlayer);
        this.forwardSeekIv.setVisibility(INVISIBLE);
    }

    @Override
    protected void resolveFullVideoShow(Context context, GSYBaseVideoPlayer gsyVideoPlayer, FrameLayout frameLayout) {
        super.resolveFullVideoShow(context, gsyVideoPlayer, frameLayout);
        this.forwardSeekIv.setVisibility(VISIBLE);
    }

    public void setMediaController(MyMediaController myMediaController){
        this.myMediaController = myMediaController;
    }
}

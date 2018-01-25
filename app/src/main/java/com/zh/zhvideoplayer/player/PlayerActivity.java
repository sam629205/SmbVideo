package com.zh.zhvideoplayer.player;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.devbrackets.android.exomedia.listener.OnBufferUpdateListener;
import com.devbrackets.android.exomedia.listener.OnCompletionListener;
import com.devbrackets.android.exomedia.listener.OnErrorListener;
import com.devbrackets.android.exomedia.listener.OnPreparedListener;
import com.devbrackets.android.exomedia.listener.OnSeekCompletionListener;
import com.devbrackets.android.exomedia.ui.widget.VideoView;
import com.zh.zhvideoplayer.R;
import com.zh.zhvideoplayer.bean.LocalVideoBean;
import com.zh.zhvideoplayer.bean.Resources;
import com.zh.zhvideoplayer.ui.title.TitleBuilder;
import com.zh.zhvideoplayer.util.Constant;
import com.zh.zhvideoplayer.util.FileItem;
import com.zh.zhvideoplayer.util.FileUtil;
import com.zh.zhvideoplayer.util.Preference;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import io.vov.vitamio.Vitamio;


/**
 * 视频播放界面
 */
public class PlayerActivity extends AppCompatActivity implements OnClickListener, OnCompletionListener,
		OnPreparedListener, OnErrorListener,OnSeekCompletionListener,OnBufferUpdateListener,MyMediaController.SetVideoURLListener {
	private MyMediaController mMediaController;
	private View mContentView;
	
	private View mTitleLayout;//顶部标题栏布局
	private View mRl_PlayView;//播放器所在布局
	private View mLoadingView;// 加载中的进度条
	private VideoView mVideoView;// vitmio视频播放控件
	private RelativeLayout mTopTools;// 播放器顶部工具栏
	private ImageView mLockScreenSwitch;// 锁定或解锁屏幕控件
	private ImageView mOrientationSwitch;// 全屏或非全屏开关
	private ImageView forwardSeekIv;
//	private ImageView mVideoShotSwitch;// 截屏开关
	private SeekBar mPlayerSeekbar;// 视频播放进度控件
	public  boolean isPlayComplete = false;// 是否播放完成
	
	private Resources mVideoRes;
	private WebView wv;
	public  boolean isPlayPause = false;// 是否手动播放暂停
	public  boolean isFirstPlay = true;// 是否第一次播放
	public  boolean isPlayLocalVideo = false;// 是否是在播放本地视频
	private boolean mIsPrepared = false;// 是否已经准备好播放
	public static String SMB_ACCOUNT_PWD = "adolf:adolf";
	public PlayerActivity getPlayerActivity(){
		return this;
	}
	private int dlSpeed;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Vitamio.isInitialized(getApplicationContext());
		Vitamio.isInitialized(PlayerActivity.this);
		mContentView = View.inflate(this, R.layout.player_activity_play, null);
		setContentView(mContentView);
		initTitle();
		initViews();
		initDatas();
		invalidateOptionsMenu();
		mMediaController.getPlayerCenterPlaySwitch().setVisibility(View.VISIBLE);
		mLoadingView.setVisibility(View.GONE);

	}
	
	private void initTitle() {
		// 1.设置左边的图片按钮显示，以及事件 2.设置中间TextView显示的文字 3.设置右边的图片按钮显示，并设置事件
		new TitleBuilder(this).setLeftImageRes(true, 0)
				.setLeftTextOrImageListener(true, null)
				.setMiddleTitleText("视频详情");
	}
	
	private void initViews() {

		wv = (WebView) findViewById(R.id.wv);
		mMediaController = (MyMediaController) findViewById(R.id.MyMediaController);
		mTitleLayout =  findViewById(R.id.titlebar);
		mVideoView = (VideoView) findViewById(R.id.vitamio_videoview);
		mLoadingView = findViewById(R.id.video_loading);
		mRl_PlayView = findViewById(R.id.id_ViewLayout);
		forwardSeekIv = (ImageView) findViewById(R.id.forward_seek_btn);
		mTopTools = (RelativeLayout)findViewById(R.id.player_top_bar);
		mLockScreenSwitch = (ImageView)findViewById(R.id.player_iv_lock_screen);
		mOrientationSwitch = (ImageView)findViewById(R.id.orientation_switch);
		mPlayerSeekbar = (SeekBar)findViewById(R.id.player_seekbar);
		mVideoView.setOnCompletionListener(this);
		mVideoView.setOnPreparedListener(this);
		mVideoView.setOnErrorListener(this);
		mVideoView.setOnBufferUpdateListener(this);
		mVideoView.setOnSeekCompletionListener(this);
	}

	private void initDatas() {
		int videoType = getIntent().getIntExtra("videoType", -1);
		if(videoType == 0){
			//在线视频
			mVideoRes = (Resources) getIntent()
	                .getParcelableExtra("video");
		}else if(videoType == 1){
			//本地视频
			isPlayLocalVideo = true;
			LocalVideoBean video = (LocalVideoBean) getIntent()
	                .getParcelableExtra("video");
			mVideoRes = new Resources();
			mVideoRes.setTitle(video.title);
			mVideoRes.setDescription("");
			mVideoRes.setLink(video.path);
			Log.d("zh","video.title:" + video.title);
			Log.d("zh","video.title:" + video.path);
		}
		
		ResetVideoPlayer();
		loadDetailWeb();
		mMediaController.setSetVideoURLListener(this);
	}
	private void loadDetailWeb(){
		String videoLink = mVideoRes.getLink();
		String[] strs = videoLink.split("/");
		String videoName = strs[strs.length-2];
		if (videoName.indexOf("-")==-1){
			String num = videoName.substring(videoName.length()-3,videoName.length());
			String letters = videoName.substring(0,videoName.length()-3);
			videoName = letters+"-"+num;
		}
		String url = Preference.getString(Constant.PREFERENCE_JAVBUS_URL);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.loadUrl(url+videoName);
	}
	@Override
	public void setVideoURI() {
		if(mVideoRes.getLink().indexOf("smb")!=-1){
			FileItem item = new FileItem("",mVideoRes.getLink(),true);
			playSmbVideo(item);
		}else {
			mVideoView.setVideoURI(Uri.parse(mVideoRes.getLink()));
		}
		mVideoView.start();
		mMediaController.getPlayerCenterPlaySwitch().setVisibility(View.GONE);
		mLoadingView.setVisibility(View.VISIBLE);
	}



	private void playSmbVideo(FileItem item){
		String mPath = item.getPath();
		String[] strs = mPath.split("@");
		item.setPath("smb://"+SMB_ACCOUNT_PWD+"@"+strs[1]);
		if (item.isFile())
		{
			String ipVal = FileUtil.ip;
			int portVal = FileUtil.port;
			String path = item.getPath();
			String httpReq = "http://" + ipVal + ":" + portVal + "/smb=";
			Log.e("", "" + FileUtil.ip + ":" + FileUtil.port + " " + path);
			if (path.endsWith(".mp3"))
			{
				path = path.substring(6);
				try
				{
					path = URLEncoder.encode(path, "UTF-8");
				}
				catch (UnsupportedEncodingException e)
				{
					e.printStackTrace();
				}

				String url = httpReq + path;
				Log.e("", "url: "+url);
				Intent it = new Intent(Intent.ACTION_VIEW);
				Uri uri = Uri.parse(url);
				it.setDataAndType(uri, "video/mp4");
				//it.setComponent(new ComponentName("com.android.music","com.android.music.MediaPlaybackActivity"));
				startActivity(it);


			}
			else
			{
				path = path.substring(6);
				try
				{
					path = URLEncoder.encode(path, "UTF-8");
				}
				catch (UnsupportedEncodingException e)
				{
					e.printStackTrace();
				}

				String url = httpReq + path;
				Log.e("", "url: "+url);
				Uri uri = Uri.parse(url);
				mVideoView.setVideoURI(uri);
			}

		}
		else
		{

		}
	}



	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		ResetVideoPlayer();
		super.onConfigurationChanged(newConfig);
	}
	
	private void ResetVideoPlayer(){
		// 设置显示名称
		mMediaController.initControllerTools(this, mContentView, mVideoView,this);
		mMediaController.setMediaPlayer(mVideoView);
		mMediaController.setFileName(mVideoRes.getVideoTitle());
		
		int mCurrentOrientation = getResources().getConfiguration().orientation;
		if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
			SetfullScreen(PlayerActivity.this, false);
			mMediaController.isLockedTools = false;
			
			mTitleLayout.setVisibility(View.VISIBLE);
			mTopTools.setVisibility(View.GONE);
			mLockScreenSwitch.setVisibility(View.GONE);
			mOrientationSwitch.setImageResource(R.mipmap.player_fill);
			mRl_PlayView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, dp2px(PlayerActivity.this, 200)));
			if (mVideoView != null && mIsPrepared){
//				mVideoView.setVideoLayout(-1, 0);
			}
		} else if (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
			SetfullScreen(PlayerActivity.this,true);
			mTitleLayout.setVisibility(View.GONE);
			mTopTools.setVisibility(View.VISIBLE);
			mLockScreenSwitch.setVisibility(View.VISIBLE);
			mOrientationSwitch.setImageResource(R.mipmap.player_btn_scale);
			mRl_PlayView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			if (mVideoView != null && mIsPrepared){
//				mVideoView.setVideoLayout(-1, 0);
			}
		}
		mVideoView.requestFocus();
	}
	
	
	/**
	 * 设置是否进入全屏
	 * @param activity
	 * @param enable
	 */
	public void SetfullScreen(Activity activity,boolean enable) {
	    if (enable) {
	        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
	        lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
	        activity.getWindow().setAttributes(lp);
	        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
	        hideVirtualButtons();
	    } else {
	        WindowManager.LayoutParams attr = activity.getWindow().getAttributes();
	        attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
	        activity.getWindow().setAttributes(attr);
	        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
	    }
	}
	
	/**
	 * 隐藏虚拟导航键，使用于api 19+
	 */
	@TargetApi(11)
	private void hideVirtualButtons() {
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
    }

	/**
	 * 播放器状态变化
	 */
//	@Override
//	public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
//		switch (arg1) {
//		case MediaPlayer.MEDIA_INFO_BUFFERING_START:
//			// 开始缓存，暂停播放
//			if (isPlaying()) {
//				//System.out.println("zh::::MEDIA_INFO_BUFFERING_START");
//				stopPlayer();
//			}
//			mLoadingView.setVisibility(View.VISIBLE);
//			break;
//		case MediaPlayer.MEDIA_INFO_BUFFERING_END:
//			// 缓存完成，继续播放
//			// System.out.println("zh::::MEDIA_INFO_BUFFERING_ENDMEDIA_INFO_BUFFERING_END");
//			if(!isPlayPause){
//				startPlayer();
//				isPlayPause = false;
//			}
//			mLoadingView.setVisibility(View.GONE);
//			break;
//		case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
//			// 显示 下载速度
//			// System.out.println("zh::::901");
//			dlSpeed = arg2;
//			if(!isPlayPause){
//				startPlayer();
//				isPlayPause = false;
//			}
//			mLoadingView.setVisibility(View.GONE);
//			break;
//		}
//		return true;
//	}
	public int getDLSpeed(){
		return dlSpeed;
	}
	
	/**
	 * 处理点击事件 
	 */
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
	}
	
	@Override
	protected void onDestroy() {
		if(mMediaController != null && mMediaController.mHandler != null){
			//播放器所在界面销毁后，停止更新进度条
			mMediaController.mHandler.removeMessages(2);
		}
		super.onDestroy();
	}
	
	/**
	 * 停止播放
	 */
	private void stopPlayer() {
		if (mVideoView != null && mVideoView.isPlaying()){
			mVideoView.pause();
		}
	}

	private boolean isFirstIn = true;
	/**
	 * 开始播放
	 */
	private void startPlayer() {
		if (mVideoView != null && ! mVideoView.isPlaying()){
			mVideoView.requestFocus();
			mVideoView.start();
			mMediaController.updatePausePlay();
			if(isFirstIn){
				mMediaController.mHandler.sendEmptyMessage(2);
				mMediaController.mHandler.sendMessageDelayed(mMediaController.mHandler.obtainMessage(1), 3000);
				isFirstIn = false;
				isFirstPlay = false;
			}
		}
	}

	/**
	 * 播放器是否正在播放
	 * @return
	 */
	private boolean isPlaying() {
		return mVideoView != null && mVideoView.isPlaying();
	}
	
	/**
	 * dp转px
	 * @param context
	 * @param dpVal
	 * @return
	 */
	public static int dp2px(Context context, float dpVal) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				dpVal, context.getResources().getDisplayMetrics());
	}
	
	/**
	 * 显示非WIFI网络播放视频提示框
	 */
	public void ShowSetNetDialog() {
//		Toast.makeText(getActivity(), "当前已是最新版.", Toast.LENGTH_SHORT).show();
		//要用 android.support.v7.app.AlertDialog 并且设置主题
		AlertDialog dialog = new  AlertDialog.Builder(this)
			.setTitle("温馨提示")
			.setMessage("您正在使用非WIFI网络播放视频，会消耗手机流量，如执意使用请在设置内打开开关！")
			.setPositiveButton("确定", null)
			.create();
		dialog.show();
		WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
		params.width = getWindowManager().getDefaultDisplay().getWidth() * 5 / 6 ;
		//	params.height = 200 ;
		dialog.getWindow().setAttributes(params);
	}
	
	/**
	 * 显示设置网络对话框
	 */
	public void ShowCheckNetDialog() {
//		Toast.makeText(getActivity(), "当前已是最新版.", Toast.LENGTH_SHORT).show();
		//要用 android.support.v7.app.AlertDialog 并且设置主题
		AlertDialog dialog = new  AlertDialog.Builder(this)
			.setTitle("温馨提示")
			.setMessage("网络不给力，请检查您的网络！")
			.setPositiveButton("确定", null)
			.create();
		dialog.show();
		WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
		params.width = getWindowManager().getDefaultDisplay().getWidth() * 5 / 6 ;
		//	params.height = 200 ;
		dialog.getWindow().setAttributes(params);
	}

	@Override
	public void onCompletion() {
        mMediaController.updatePausePlay();
        mPlayerSeekbar.setProgress(mPlayerSeekbar.getMax());
        isPlayComplete = true;
        mMediaController.mHandler.removeMessages(2);
        mMediaController.getPlayerCenterPlaySwitch().setVisibility(View.VISIBLE);
	}

	@Override
	public boolean onError(Exception e) {
		mLoadingView.setVisibility(View.GONE);
		return false;
	}

	@Override
	public void onPrepared() {
        mIsPrepared = true;
	}

    @Override
    public void onSeekComplete() {
        mMediaController.mHandler.sendEmptyMessage(2);
    }

    @Override
    public void onBufferingUpdate(int percent) {
	    if (percent>98){
            mLoadingView.setVisibility(View.INVISIBLE);
        }else {
            mLoadingView.setVisibility(View.VISIBLE);
        }
    }
}

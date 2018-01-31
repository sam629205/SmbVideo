package com.zh.zhvideoplayer;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.zh.zhvideoplayer.adapter.LocalVideoListAdapter;
import com.zh.zhvideoplayer.bean.Resources;
import com.zh.zhvideoplayer.model.VideoModel;
import com.zh.zhvideoplayer.player.GetLocalVideosTask;
import com.zh.zhvideoplayer.player.GetSmbVideosTask;
import com.zh.zhvideoplayer.player.PlayerActivity;
import com.zh.zhvideoplayer.service.PlayFileService;
import com.zh.zhvideoplayer.ui.title.TitleBuilder;
import com.zh.zhvideoplayer.util.Constant;
import com.zh.zhvideoplayer.util.FileUtil;
import com.zh.zhvideoplayer.util.Preference;
import com.zh.zhvideoplayer.util.ScreenUtils;
import com.zh.zhvideoplayer.util.Utils;
import com.zh.zhvideoplayer.view.stateview.StateView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener, GetLocalVideosTask.OnSuccessListener,GetSmbVideosTask.OnSmbRefreshListener {
    private GridView mGridView;
    private List<VideoModel> mVideoList;
    private LocalVideoListAdapter mListAdapter;
    private GetLocalVideosTask mGetVideoTask;
    private StateView mStateView;// 加载状态控件，加载中、失败、成功
    private LinearLayout emptyView = null;
    private PullToRefreshGridView mPullToRefreshListview;
    private Button mToTopBtn;// 返回顶部的按钮
    public static boolean isPicWall;
    private List<String> videoPaths = new ArrayList<>();
    private int smbVideosInfoCount;
    private int noInfoVideosSize;
    private int scrollPosition = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View mView = View.inflate(getBaseContext(),
                R.layout.activity_main, null);
        setContentView(mView);
        initView(mView);
        initTitle();
        Uri uri = getIntent().getData();
        if (uri!=null){
            Intent intent = new Intent();
            intent.setClass(MainActivity.this,PlayerActivity.class);
            Resources r = new Resources();
            r.setLink(uri.toString());
            intent.putExtra("video",r);
            intent.putExtra("videoType",0);
            startActivity(intent);
        }
        initListener();
        Intent intent = new Intent(this, PlayFileService.class);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPicWall){
            mGridView.setNumColumns(2);
        }
        initDatas();
    }

    private void initTitle() {
        // 1.设置左边的图片按钮显示，以及事件 2.设置中间TextView显示的文字 3.设置右边的图片按钮显示，并设置事件
        new TitleBuilder(this).setLeftImageRes(false, 0)
                .setLeftTextOrImageListener(true, null)
                .setMiddleTitleText("我的视频");
    }

    /**
     * 初始化views
     *
     * @param mView
     */
    private void initView(final View mView) {
        mStateView = (StateView) mView.findViewById(R.id.mStateView);
        mToTopBtn = (Button) mView.findViewById(R.id.btn_top);
        mPullToRefreshListview = (PullToRefreshGridView) mView
                .findViewById(R.id.PullToRefreshListView);
        mPullToRefreshListview.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mPullToRefreshListview.setPullToRefreshOverScrollEnabled(false);
        // 得到真正的listview,我们在给listview设置adapter时或者设置onItemClick事件必须通过它，而不能用ptrlv_test
        mGridView = mPullToRefreshListview.getRefreshableView();
        // 给ListView添加EmptyView(用于提示上拉加载数据，加载中，没有更多数据了)
        AbsListView.LayoutParams emptyViewlayoutParams = new AbsListView.LayoutParams(
                AbsListView.LayoutParams.MATCH_PARENT,
                AbsListView.LayoutParams.MATCH_PARENT);
        emptyView = (LinearLayout) LayoutInflater.from(this).inflate(
                R.layout.stateview_empty_view, mPullToRefreshListview, false);
        emptyView.setLayoutParams(emptyViewlayoutParams);
        emptyView.setGravity(Gravity.CENTER);
//				mListView.setEmptyView(emptyView);
        mPullToRefreshListview.setEmptyView(emptyView);
        ((TextView)mView.findViewById(R.id.title_right_textview)).setText("设置");
        ((TextView)mView.findViewById(R.id.title_right_textview)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,SettingsActivity.class);
                startActivity(intent);
            }
        });
        ((TextView)mView.findViewById(R.id.title_left_textview)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPicWall = !isPicWall;
                int position = mGridView.getLastVisiblePosition();
                if (isPicWall){
                   ((TextView)mView.findViewById(R.id.title_left_textview)).setText("图鉴");
                   mGridView.setNumColumns(2);
                    position = position/2;
               }else {
                   ((TextView)mView.findViewById(R.id.title_left_textview)).setText("列表");
                   mGridView.setNumColumns(1);
                   position = position*2;
               }
                mListAdapter.notifyDataSetChanged();
                setListViewPos(position);
            }
        });
    }
    private void toOtherPlayer(VideoModel info){
        try {
            String[] strs = info.getPath().split(";");
            videoPaths.clear();
            List<String> names = new ArrayList<>();
            for (String item : strs)
            {
                if (Utils.isVideoType(item))
                {
                    String mName = item.substring(item.lastIndexOf("/")+1,item.length());
                    String mPath = "";
                    if (isDownloadedToLocal(info.getNumber())){
                        mPath = Environment.getExternalStorageDirectory()+"/Download/"+info.getNumber()+"/"+mName;
                    }else {
                        mPath = item;
                    }
                    videoPaths.add(mPath);
                    names.add(mName);
                }
            }
            if (names.size()>1){
                if (isDownloadedToLocal(info.getNumber())){
                    showVideoSelectDialog(names,false);
                }else {
                    showVideoSelectDialog(names,true);
                }
            }else {
                if (isDownloadedToLocal(info.getNumber())){
                    showPlayers(videoPaths.get(0));
                }else {
                    String ipVal = FileUtil.ip;
                    int portVal = FileUtil.port;
                    String httpReq = "http://" + ipVal + ":" + portVal + "/smb="+videoPaths.get(0);
                    showPlayers(httpReq);
                }
            }
        } catch (Exception e){
            Log.e("",e.getMessage());
        }

    }
    private void showPlayers(String path){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(path), "video/mp4");
        startActivity(intent);
    }
    private void showDeleteDialog(final int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("确定要删除这条记录吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Realm mRealm=MyApplication.getInstance().getRealm();
                VideoModel result = mRealm.where(VideoModel.class).equalTo("path",mVideoList.get(position).getPath()).findFirst();
                mRealm.beginTransaction();
                result.deleteFromRealm();
                mRealm.commitTransaction();
                mVideoList.remove(position);
                mListAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void showVideoSelectDialog(List<String> names, final boolean isSmb){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("选择视频");
        String[] nameStrs = names.toArray(new String[0]);
        builder.setItems(nameStrs, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isSmb){
                    String ipVal = FileUtil.ip;
                    int portVal = FileUtil.port;
                    String httpReq = "http://" + ipVal + ":" + portVal + "/smb="+videoPaths.get(which);
                    showPlayers(httpReq);
                }else {
                    showPlayers(videoPaths.get(which));
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }
    private void showVideoMenuDialog(final int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        String[] menuStrs = {"查看详情","删除记录"};
        builder.setItems(menuStrs, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        Intent intent= new Intent();
                        intent.setAction("android.intent.action.VIEW");
                        Uri content_url = Uri.parse(Preference.getString(Constant.PREFERENCE_JAVBUS_URL)+mVideoList.get(position).getNumber());
                        intent.setData(content_url);
                        startActivity(intent);
                        break;
                    case 1:
                        showDeleteDialog(position);
                        break;
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }
    private boolean isDownloadedToLocal(String number){
        File file = new File(Environment.getExternalStorageDirectory()+"/Download/"+number+"/");
        return file.exists();
    }
    private void initListener() {
        mToTopBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                setListViewPos(0);
            }
        });

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                toOtherPlayer(mVideoList.get(position));
            }
        });
        mGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showVideoMenuDialog(position);
                return false;
            }
        });


        mPullToRefreshListview.setOnScrollListener(new AbsListView.OnScrollListener() {
            int itemHeight = 0;
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
//				// 当开始滑动且ListView底部的Y轴点超出屏幕最大范围时，显示或隐藏顶部按钮
                View c = view.getChildAt(0);
                if (c == null) {
                    return ;
                }
                if(itemHeight < c.getHeight()){
                    itemHeight = c.getHeight();
                }
                int height =  (firstVisibleItem + visibleItemCount -1) * itemHeight;
                if (height >= 1.5 * ScreenUtils.getScreenHeight(MainActivity.this)) {
                    mToTopBtn.setVisibility(View.VISIBLE);
                }else {
                    mToTopBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }
        });

        // 设置刷新监听器
        mPullToRefreshListview
                .setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<GridView>() {
                    @Override
                    public void onRefresh(
                            PullToRefreshBase<GridView> refreshView) {
                        if (mPullToRefreshListview.isRefreshing()) {
                            GetSmbVideosTask task = new GetSmbVideosTask();
                            task.setOnRefreshListener(MainActivity.this);
                            task.execute();
                        }
                    }
                });
    }

    private void initDatas() {
        mStateView.setCurrentState(StateView.STATE_LOADING);
        mVideoList = new ArrayList<VideoModel>();
        mListAdapter = new LocalVideoListAdapter(mVideoList, this);
        mGridView.setAdapter(mListAdapter);
        getDatas();
    }

    public void showSmbInfoProgress(){
        smbVideosInfoCount ++;
        Toast.makeText(MainActivity.this,"资料刷新"+smbVideosInfoCount+"/"+noInfoVideosSize,Toast.LENGTH_SHORT).show();
    }

    private void getDatas() {
        mGetVideoTask = new GetLocalVideosTask();
        mGetVideoTask.setOnSuccessListener(this);
        mGetVideoTask.execute(this.getContentResolver());
    }

    @Override
    public void onSuccess(List<VideoModel> videos) {
        loadVideos(videos,false);
    }
    private void loadVideos(List<VideoModel> videos,boolean isRefreshInfo){
        if (videos.size() > 0) {
            mVideoList.clear();
            mVideoList.addAll(videos);
            mListAdapter.reloadData(isRefreshInfo);
            setListViewPos(scrollPosition);
        }
        ShowContent();
        // 我们可以 延迟 1秒左右，在调用onRefreshComplete 方法，可以解决该问题
        mGridView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPullToRefreshListview.setPullLabel("刷新完成");
                mPullToRefreshListview.onRefreshComplete();
                mPullToRefreshListview.setPullLabel("下拉刷新");
            }
        }, 1000);
    }

    @Override
    public void onClick(View arg0) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        scrollPosition = mGridView.getLastVisiblePosition();
    }

    private void ShowContent() {
        mStateView.setCurrentState(StateView.STATE_CONTENT);
        if (mVideoList.size() == 0 && emptyView != null) {
            emptyView.setVisibility(View.GONE);
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    emptyView.setVisibility(View.VISIBLE);
                }
            }, 20);
        }
    }

    /**
     * 获取视频文件截图
     *
     * @param path
     *            视频文件的路径
     * @return Bitmap 返回获取的Bitmap
     */
    public static Bitmap getVideoThumb(String path) {
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        media.setDataSource(path);
        return media.getFrameAtTime();
    }

    /**
     * 获取视频文件缩略图 API>=8(2.2)
     *
     * @param path
     *            视频文件的路径
     * @param kind
     *            缩略图的分辨率：MINI_KIND、MICRO_KIND、FULL_SCREEN_KIND
     * @return Bitmap 返回获取的Bitmap
     */
    public static Bitmap getVideoThumb2(String path, int kind) {
        return ThumbnailUtils.createVideoThumbnail(path, kind);
    }

    public static Bitmap getVideoThumb2(String path) {
        return getVideoThumb2(path,
                MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
    }

    /**
     * 滚动ListView到指定位置
     *
     * @param pos
     */
    private void setListViewPos(int pos) {
        if (android.os.Build.VERSION.SDK_INT >= 8) {
            mGridView.smoothScrollToPosition(pos);
        } else {
            mGridView.setSelection(pos);
        }
    }

    @Override
    public void onRefreshSuccess(List<VideoModel> videos) {
        smbVideosInfoCount = 0;
        Realm mRealm = MyApplication.getInstance().getRealm();
        noInfoVideosSize = videos.size() - mRealm.where(VideoModel.class).findAll().size();
        loadVideos(videos,true);
    }
}

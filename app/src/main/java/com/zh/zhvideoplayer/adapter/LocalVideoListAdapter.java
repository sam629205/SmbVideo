package com.zh.zhvideoplayer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore.Video.Thumbnails;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloadQueueSet;
import com.liulishuo.filedownloader.FileDownloader;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.zh.zhvideoplayer.MainActivity;
import com.zh.zhvideoplayer.MyApplication;
import com.zh.zhvideoplayer.R;
import com.zh.zhvideoplayer.bean.LocalVideoBean;
import com.zh.zhvideoplayer.model.StarModel;
import com.zh.zhvideoplayer.model.VideoModel;
import com.zh.zhvideoplayer.player.LocalVideoImageLoader;
import com.zh.zhvideoplayer.util.Constant;
import com.zh.zhvideoplayer.util.FileUtil;
import com.zh.zhvideoplayer.util.Preference;
import com.zh.zhvideoplayer.util.Utils;
import com.zh.zhvideoplayer.view.CircleProgressView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * 本地视频列表的适配器
 * 
 */
public class LocalVideoListAdapter extends BaseAdapter {

	private List<VideoModel> mVideoList;
	private Context mContext;
	private LocalVideoImageLoader mVideoImageLoader;
	private ImageLoader imageLoader;
	private DisplayImageOptions options;
	private boolean isRefreshInfo = false;
	
	public LocalVideoListAdapter(List<VideoModel> videoList, Context context) {
		super();
		this.mVideoList = videoList;
		this.mContext = context;
		mVideoImageLoader = new LocalVideoImageLoader(context);// 初始化缩略图载入方法
		options = new DisplayImageOptions.Builder()
				.cacheInMemory(true)
				.cacheOnDisk(true)
				.considerExifParams(true)
                .showImageOnLoading(R.drawable.loading_spinner)
				.build();
		imageLoader = ImageLoader.getInstance();
	}

	@Override
	public int getCount() {
		return mVideoList.size();
	}

	@Override
	public VideoModel getItem(int position) {
		return mVideoList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
        Realm mRealm=MyApplication.getInstance().getRealm();
		VideoModel info = getItem(position);
		if (TextUtils.isEmpty(info.getNumber())&&isRefreshInfo){
			String[] strs = info.getPath().split("/");
			String number =strs[strs.length-2];
			if (number.indexOf("-")==-1){
				String num = number.substring(number.length()-3,number.length());
				String letters = number.substring(0,number.length()-3);
				number = letters+"-"+num;
			}
			RealmResults<VideoModel> dbResult = mRealm.where(VideoModel.class).equalTo("number",number).findAll();
			if (dbResult.size()==0||TextUtils.isEmpty(dbResult.first().getPic())){
				new VideoInfoTask().execute(info.getPath());
			}else {
				info = dbResult.first();
			}
		}
		if(convertView == null){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.video_local_video_list_item, null);
			viewHolder = new ViewHolder(convertView);
			convertView.setTag(viewHolder);			
		} else{
			viewHolder = (ViewHolder) convertView.getTag();
		}
		if (MainActivity.isPicWall){
			viewHolder.image.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,1000));
			viewHolder.factory.setVisibility(View.GONE);
			viewHolder.series.setVisibility(View.GONE);
			viewHolder.downloadImage.setVisibility(View.GONE);
			viewHolder.sv.setVisibility(View.GONE);
			viewHolder.tvTitleBelow.setVisibility(View.VISIBLE);
		}else {
			viewHolder.image.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,300));
			viewHolder.factory.setVisibility(View.VISIBLE);
			viewHolder.series.setVisibility(View.VISIBLE);
			viewHolder.downloadImage.setVisibility(View.VISIBLE);
			viewHolder.sv.setVisibility(View.VISIBLE);
			viewHolder.tvTitleBelow.setVisibility(View.GONE);
		}
		imageLoader.displayImage(info.getPic(), viewHolder.image, options, new MyLoadingListener());
		viewHolder.title.setText(info.getNumber()+" "+info.getName());
		viewHolder.tvTitleBelow.setText(info.getNumber()+" "+info.getName());
		viewHolder.factory.setText("厂商："+info.getFactory());
		viewHolder.series.setText("系列："+info.getSeries());
		String[] stars = {""};
		if (!TextUtils.isEmpty(info.getActors())){
			stars = info.getActors().split(",");
		}
		final RealmResults<StarModel> results = mRealm.where(StarModel.class).in("starJavbusId",stars).findAll();
		List<VideoModel> result = new ArrayList<>();
		final List<StarModel> infoList = mRealm.copyFromRealm(results);
		LinearLayout ll = new LinearLayout(mContext);
		ll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
		ll.setOrientation(LinearLayout.HORIZONTAL);
		TextView mTitle = new TextView(mContext);
		mTitle.setText("出演：");
		ll.addView(mTitle);
		for (StarModel item:infoList){
			TextView tv = new TextView(mContext);
			tv.setText(item.getName()+"/");
			ll.addView(tv);
		}
		if (viewHolder.sv.getChildCount()<1){
			viewHolder.sv.addView(ll);
		}
		final String[] mPaths = info.getPath().split(";");
        final String videoNumber = info.getNumber();
		viewHolder.downloadImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FileDownloader.setup(mContext);
                viewHolder.downloadImage.setVisibility(View.INVISIBLE);
                viewHolder.progressView.setVisibility(View.VISIBLE);
                FileDownloadQueueSet queueSet = new FileDownloadQueueSet(new FileDownloadListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                    }

                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        viewHolder.progressView.setProgress(viewHolder.progressView.getProgress()+1);
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        Toast.makeText(mContext,"已完成下载"+task.getFilename(),Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {

                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {

                    }
                });
                List<BaseDownloadTask> tasks = new ArrayList<>();
                for (int i = 0; i < mPaths.length; i++) {
                    String ipVal = FileUtil.ip;
                    int portVal = FileUtil.port;
                    String httpReq = "http://" + ipVal + ":" + portVal + "/smb="+mPaths[i];
                    String[] strs = mPaths[i].split("/");
                    String fileName = strs[strs.length-1];
                    String desPath = Environment.getExternalStorageDirectory() +"/Download/"+videoNumber+"/"+fileName;
                    tasks.add(FileDownloader.getImpl().create(httpReq).setPath(desPath).setTag(i + 1));
                }
                queueSet.setAutoRetryTimes(1);
                queueSet.downloadSequentially(tasks);
                queueSet.start();
			}
		});

		return convertView;
	}


    public void reloadData(boolean isRefreshInfo) {
		this.isRefreshInfo = isRefreshInfo;
		notifyDataSetChanged();
	}

	private class MyLoadingListener implements ImageLoadingListener{

		@Override
		public void onLoadingStarted(String s, View view) {

		}

		@Override
		public void onLoadingFailed(String s, View view, FailReason failReason) {

		}

		@Override
		public void onLoadingComplete(String s, View view, Bitmap bitmap) {
			if (bitmap == null){
				return;
			}
			((ImageView)view).setImageBitmap(Utils.createRightHalfImage(bitmap));
		}

		@Override
		public void onLoadingCancelled(String s, View view) {

		}
	}
	private class VideoInfoTask extends AsyncTask<String,String,VideoModel>{

		@Override
		protected VideoModel doInBackground(String... params) {
			String path = params[0];
			if (path.indexOf(";")!=-1){
				path = path.split(";")[0];
			}
			String[] strs = path.split("/");
			String videoName = strs[strs.length-2];
			if (videoName.indexOf("-")==-1){
				String num = videoName.substring(videoName.length()-3,videoName.length());
				String letters = videoName.substring(0,videoName.length()-3);
				videoName = letters+"-"+num;
			}
			String url = Preference.getString(Constant.PREFERENCE_JAVBUS_URL);
			try {
				Document doc = Jsoup.connect(url+videoName).followRedirects(true).get();
				VideoModel info = new VideoModel();
				Elements eles0 = doc.getElementsByClass("col-md-9");
				info.setPic(eles0.get(0).getElementsByTag("a").get(0).attr("href"));
				info.setName(eles0.get(0).getElementsByTag("img").get(0).attr("title"));
				Elements eles1 = doc.getElementsByClass("col-md-3");
				Elements eles2 = eles1.get(0).getElementsByTag("p");
				for (Element ele:eles2){
					if (ele.toString().indexOf("識別碼")!=-1){
						info.setNumber(ele.getElementsByTag("span").get(1).text());
					}
					if (ele.toString().indexOf("發行日期")!=-1){
						String date = ele.ownText();
						try {
							info.setPubDate(Utils.dateToTimeStamp(date));
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
					if (ele.toString().indexOf("製作商")!=-1){
						info.setFactory(ele.getElementsByTag("a").get(0).text());
					}
					if (ele.toString().indexOf("系列")!=-1){
						info.setSeries(ele.getElementsByTag("a").get(0).text());
					}
				}
				Elements eles3 = eles1.get(0).getElementsByClass("star-name");
				String actors = "";
                Realm mRealm= MyApplication.getInstance().getRealm();
				mRealm.beginTransaction();
				for (Element ele:eles3){
					Elements subEles = ele.getElementsByTag("a");
					String link = subEles.get(0).attr("href");
					String[] mStrs = link.split("/");
					String id = mStrs[mStrs.length-1];
					StarModel starInfo = new StarModel();
					starInfo.setName(ele.text());
					starInfo.setStarJavbusId(id);
					mRealm.insertOrUpdate(starInfo);
					actors = actors+id+",";
				}
				info.setActors(actors);
				info.setPath(params[0]);
				mRealm.insertOrUpdate(info);
				mRealm.commitTransaction();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(VideoModel videoModel) {
			super.onPostExecute(videoModel);
			((MainActivity)mContext).showSmbInfoProgress();
		}
	}
	private static class ViewHolder{
		private ImageView image;
		private TextView title;
		private TextView factory;
		private TextView series;
		private TextView tvTitleBelow;
		private CircleProgressView progressView;
		private HorizontalScrollView sv;
		private ImageView downloadImage;
		Bitmap bitmap = Bitmap.createBitmap( 10,10, Config.ARGB_4444 );
		
		public ViewHolder(View view){
			image = (ImageView) view.findViewById(R.id.image);
			title = (TextView) view.findViewById(R.id.title);
			factory = (TextView) view.findViewById(R.id.tv_factory);
			series = (TextView) view.findViewById(R.id.tv_series);
			sv = (HorizontalScrollView) view.findViewById(R.id.sv_stars);
			progressView = (CircleProgressView) view.findViewById(R.id.circle_progress);
			downloadImage = (ImageView) view.findViewById(R.id.btn_download);
			tvTitleBelow = (TextView) view.findViewById(R.id.title_below);
		}
		
		public void setData(LocalVideoBean video){
			title.setText("名称：" + video.title);
			image.setImageBitmap(bitmap);
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
		
		public static Bitmap getVideoThumb1(String path) {
			Bitmap  bitmap = ThumbnailUtils.createVideoThumbnail(path,
	                   Thumbnails.MICRO_KIND);//MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96,
//			bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
			return bitmap;
		}
	}
}

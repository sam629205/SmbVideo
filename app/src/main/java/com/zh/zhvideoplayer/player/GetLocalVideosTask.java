package com.zh.zhvideoplayer.player;

import android.os.AsyncTask;
import android.widget.Toast;

import com.zh.zhvideoplayer.MyApplication;
import com.zh.zhvideoplayer.model.SmbModel;
import com.zh.zhvideoplayer.model.VideoModel;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * 异步获取本地视频列表
 */
public class GetLocalVideosTask extends AsyncTask<Object,Integer,List<VideoModel>> {

    private OnSuccessListener onSuccessListener;
    
    @Override
    protected List<VideoModel> doInBackground(Object... params) {
        Realm mRealm=MyApplication.getInstance().getRealm();
        RealmResults<VideoModel> results = mRealm.where(VideoModel.class).findAll().sort("number");
        RealmResults<SmbModel> results2 = mRealm.where(SmbModel.class).equalTo("active","0").findAll();
        List<SmbModel> smbPaths = mRealm.copyFromRealm(results2);
        List<VideoModel> infoList = mRealm.copyFromRealm(results);
        for (int i=infoList.size()-1;i>=0;i--){
            if (pathIsInactive(infoList.get(i).getPath(),smbPaths)){
                infoList.remove(infoList.get(i));
            }
        }
        return infoList;
    }
    private boolean pathIsInactive(String path,List<SmbModel> mPaths){
        boolean result = false;
        for (SmbModel item:mPaths){
            if (path.contains(item.getUrl())){
                result = true;
                break;
            }
        }
        return result;
    }
    @Override
    protected void onPostExecute(List<VideoModel> videos) {
        super.onPostExecute(videos);
        this.onSuccessListener.onSuccess(videos);
    }

    public interface OnSuccessListener{
        void onSuccess(List<VideoModel> videos);
    }
    
    public void setOnSuccessListener(OnSuccessListener onSuccessListener){
        this.onSuccessListener = onSuccessListener;
    }
    private List<VideoModel> searchFile(String path){
        List<VideoModel> result = new ArrayList<>();
        ArrayList<SmbFile> fileList = new ArrayList<SmbFile>();
        try
        {
            SmbFile smbFile = new SmbFile(path);

            SmbFile[] fs = smbFile.listFiles();

            for (SmbFile f : fs)
            {
                if (f.isDirectory())
                {
                    VideoModel info = new VideoModel();
                    info.setPath(f.getPath());
                    result.add(info);
                }
                else
                {
                    fileList.add(f);
                }
            }

        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (SmbException e)
        {
            e.printStackTrace();
            Toast.makeText(MyApplication.getInstance(),e.getMessage(),Toast.LENGTH_SHORT).show();
        }
        return result;
    }
}

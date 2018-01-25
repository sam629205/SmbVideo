package com.zh.zhvideoplayer.player;

import android.os.AsyncTask;
import android.widget.Toast;

import com.zh.zhvideoplayer.MyApplication;
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
        final RealmResults<VideoModel> results = mRealm.where(VideoModel.class).findAll().sort("number");
        List<VideoModel> infoList = mRealm.copyFromRealm(results);
        return infoList;
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

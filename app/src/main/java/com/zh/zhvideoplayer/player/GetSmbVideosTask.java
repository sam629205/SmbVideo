package com.zh.zhvideoplayer.player;

import android.os.AsyncTask;
import android.widget.Toast;

import com.zh.zhvideoplayer.MyApplication;
import com.zh.zhvideoplayer.model.SmbModel;
import com.zh.zhvideoplayer.model.VideoModel;
import com.zh.zhvideoplayer.util.Utils;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * Created by Adolf Li on 2018/1/22.
 */

public class GetSmbVideosTask extends AsyncTask<Object,Integer,List<VideoModel>> {
    private OnSmbRefreshListener onRefreshListener;

    @Override
    protected List<VideoModel> doInBackground(Object... params) {
        Realm mRealm=MyApplication.getInstance().getRealm();
        final RealmResults<SmbModel> results = mRealm.where(SmbModel.class).findAll();
        List<VideoModel> result = new ArrayList<>();
        final List<SmbModel> infoList = mRealm.copyFromRealm(results);
        for (SmbModel info:infoList){
            String path = "smb://"+info.getAccount()+":"+info.getPwd()+"@"+info.getUrl();
            result.addAll(searchFile(path));
        }
        return result;
    }

    @Override
    protected void onPostExecute(List<VideoModel> videos) {
        super.onPostExecute(videos);
        if (videos==null){
            Toast.makeText(MyApplication.getInstance(),"无法连接SMB",Toast.LENGTH_SHORT).show();
        }else {
            this.onRefreshListener.onRefreshSuccess(videos);
        }
    }

    public interface OnSmbRefreshListener{
        void onRefreshSuccess(List<VideoModel> videos);
    }

    public void setOnRefreshListener(OnSmbRefreshListener onRefreshListener){
        this.onRefreshListener = onRefreshListener;
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
                    String pathStr = "";
                    for (SmbFile video:f.listFiles()){
                        if (Utils.isVideoType(video.getPath())){
                            pathStr += video.getPath()+";";
                        }
                    }
                    info.setPath(pathStr);
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

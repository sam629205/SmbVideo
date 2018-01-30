package com.zh.zhvideoplayer.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.zh.zhvideoplayer.MyApplication;
import com.zh.zhvideoplayer.R;
import com.zh.zhvideoplayer.model.SmbModel;
import com.zh.zhvideoplayer.model.VideoModel;
import com.zh.zhvideoplayer.util.Utils;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;


/**
 * Created by Adolf Li on 2017/5/17.
 */

public class SmbUrlAdapter extends BaseAdapter{
    private Context context;
    private List<SmbModel> infos;
    public SmbUrlAdapter(List<SmbModel> infos, Context context){
        this.infos = infos;
        this.context = context;
    }

    @Override
    public int getCount() {
        return infos.size();
    }

    @Override
    public Object getItem(int position) {
        return infos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        final SmbModel info = infos.get(position);
        LayoutInflater inflater = LayoutInflater.from(context);
        if (convertView==null){
            convertView = inflater.inflate(R.layout.smb_url_item,null);
            holder = new ViewHolder();
            holder.tvUrl = (TextView) convertView.findViewById(R.id.tv_url);
            holder.ivAccount = (ImageView) convertView.findViewById(R.id.iv_account);
            holder.ivDel = (ImageView) convertView.findViewById(R.id.iv_del);
            holder.ivActive = convertView.findViewById(R.id.iv_active);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }
        if (TextUtils.isEmpty(info.getAccount())){
            holder.ivAccount.setImageDrawable(Utils.getDrawable("account_gray"));
        }else {
            holder.ivAccount.setImageDrawable(Utils.getDrawable("account_blue"));
        }
        holder.ivActive.setImageResource(info.getActive().equals("1")?R.drawable.active:R.drawable.inactive);
        holder.tvUrl.setText(info.getUrl());
        holder.ivAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAccountDialog(info);

            }
        });
        holder.ivDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Realm mRealm=MyApplication.getInstance().getRealm();
                SmbModel model = mRealm.where(SmbModel.class).equalTo("url",info.getUrl()).findFirst();
                RealmResults<VideoModel> videos = mRealm.where(VideoModel.class).contains("path",info.getUrl()).findAll();
                mRealm.beginTransaction();
                videos.deleteAllFromRealm();
                model.deleteFromRealm();
                infos.remove(position);
                notifyDataSetChanged();
                mRealm.commitTransaction();
            }
        });
        holder.ivActive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Realm mRealm=MyApplication.getInstance().getRealm();
                SmbModel model = mRealm.where(SmbModel.class).equalTo("url",info.getUrl()).findFirst();
                mRealm.beginTransaction();
                if (model.getActive().equals("0")){
                    info.setActive("1");
                    model.setActive("1");
                }else {
                    info.setActive("0");
                    model.setActive("0");
                }
                notifyDataSetChanged();
                mRealm.commitTransaction();
            }
        });
        return convertView;
    }
    public void add(SmbModel info){
        infos.add(info);
        notifyDataSetChanged();
    }
    class ViewHolder{
        TextView tvUrl;
        ImageView ivAccount;
        ImageView ivDel;
        ImageView ivActive;
    }
    private void showAccountDialog(final SmbModel info){

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_dialog_account_pwd,null);
        final EditText etAccount = (EditText) view.findViewById(R.id.et_account);
        final EditText etPwd = (EditText) view.findViewById(R.id.et_pwd);
        etAccount.setText(info.getAccount());
        etPwd.setText(info.getPwd());
        Dialog dialog = new  AlertDialog.Builder(context)
                .setTitle("帐户信息" )
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Realm mRealm=MyApplication.getInstance().getRealm();
                        mRealm.beginTransaction();
                        info.setAccount(etAccount.getText().toString());
                        info.setPwd(etPwd.getText().toString());
                        mRealm.insertOrUpdate(info);
                        mRealm.commitTransaction();
                        notifyDataSetChanged();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}

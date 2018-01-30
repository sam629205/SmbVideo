package com.zh.zhvideoplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zh.zhvideoplayer.adapter.SmbUrlAdapter;
import com.zh.zhvideoplayer.bean.Resources;
import com.zh.zhvideoplayer.model.SmbModel;
import com.zh.zhvideoplayer.player.MyMediaController;
import com.zh.zhvideoplayer.player.PlayerActivity;
import com.zh.zhvideoplayer.ui.title.TitleBuilder;
import com.zh.zhvideoplayer.util.Constant;
import com.zh.zhvideoplayer.util.Preference;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Adolf Li on 2017/5/31.
 */

public class SettingsActivity extends AppCompatActivity{
    private EditText etInterval;
    private EditText etBufferSize;
    private EditText etLink,etSmbUrl;
    private EditText etJAVBus;
    private Button btnOK,btnSmbAdd;
    private ListView lvSmb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        etInterval = (EditText) findViewById(R.id.et_interval);
        etBufferSize = (EditText) findViewById(R.id.et_buffer);
        etJAVBus = (EditText) findViewById(R.id.et_javbus);
        etLink = (EditText) findViewById(R.id.et_link);
        etSmbUrl = (EditText) findViewById(R.id.et_smb_url);
        btnOK = (Button) findViewById(R.id.btn_ok);
        btnSmbAdd = (Button) findViewById(R.id.btn_smb_add);
        lvSmb = (ListView) findViewById(R.id.lv_smb);
        initView();
    }

    private void initView() {
        new TitleBuilder(this).setMiddleTitleText("设置").setLeftBackVisible();
        etInterval.setText(Preference.getInt(MyMediaController.PREFERENCE_FORWARD_INTERVAL)+"");
        etBufferSize.setText(Preference.getInt(MyMediaController.PREFERENCE_BUFFER_SIZE)+"");
        etJAVBus.setText(Preference.getString(Constant.PREFERENCE_JAVBUS_URL));
        ((TextView)findViewById(R.id.title_right_textview)).setText("确定");
        ((TextView)findViewById(R.id.title_right_textview)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Preference.putInt(MyMediaController.PREFERENCE_FORWARD_INTERVAL,Integer.parseInt(etInterval.getText().toString()));
                Preference.putInt(MyMediaController.PREFERENCE_BUFFER_SIZE,Integer.parseInt(etBufferSize.getText().toString()));
                Preference.putString(Constant.PREFERENCE_JAVBUS_URL,etJAVBus.getText().toString());
                Toast.makeText(SettingsActivity.this,"保存完成",Toast.LENGTH_SHORT).show();
            }
        });
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(SettingsActivity.this,PlayerActivity.class);
                Resources r = new Resources();
                r.setLink(etLink.getText().toString());
                intent.putExtra("video",r);
                intent.putExtra("videoType",0);
                startActivity(intent);
                finish();
            }
        });
        btnSmbAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Realm mRealm=MyApplication.getInstance().getRealm();
                SmbModel info = new SmbModel();
                info.setUrl(etSmbUrl.getText().toString());
                info.setActive("1");
                ((SmbUrlAdapter)lvSmb.getAdapter()).add(info);
                mRealm.beginTransaction();
                mRealm.insertOrUpdate(info);
                mRealm.commitTransaction();
            }
        });
        loadSmbUrls();
    }
    private void loadSmbUrls(){
        Realm mRealm=MyApplication.getInstance().getRealm();
        final RealmResults<SmbModel> results = mRealm.where(SmbModel.class).findAll();
        final List<SmbModel> infoList = mRealm.copyFromRealm(results);
        final SmbUrlAdapter adapter = new SmbUrlAdapter(infoList,SettingsActivity.this);
        lvSmb.setAdapter(adapter);
    }
}

package com.zh.zhvideoplayer;

import android.app.Application;

import com.avos.avoscloud.AVOSCloud;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * 全局Application
 */
public class MyApplication extends Application {
	private static MyApplication instance;
	private RealmConfiguration realmConfig;

	/**
	 * 获取Application
	 * 
	 * @return BaseApplication
	 */
	public synchronized static MyApplication getInstance() {
		if (instance == null) {
			instance = new MyApplication();
		}
		return instance;
	}

	/**
	 * 获取上下文
	 * 
	 * @return getInstance()
	 */
	public static MyApplication getContext() {
		return getInstance();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		initRealm();
		// 初始化参数依次为 this, AppId, AppKey
		AVOSCloud.initialize(this,"3PcQbTq01bbBHp4NHnfMDQN7-gzGzoHsz","RPFJGkdB64JpYRWPxgP9qMaB");
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getContext())
				.build();
		ImageLoader.getInstance().init(config);
	}
	private void initRealm(){
		Realm.init(this);
		realmConfig = new RealmConfiguration.Builder()
				.name("flightAdmin.realm").schemaVersion(1).deleteRealmIfMigrationNeeded().build();
	}
	public Realm getRealm(){
		return Realm.getInstance(realmConfig);
	}
}

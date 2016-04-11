package app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import java.io.File;

import activity.LoginActivity;
import bean.User;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;
import config.Constant;
import db.LikesDB;


/**
 * Created by huang.fan on 2016-3-21.
 */
public class BeiWoApplication extends Application{
    private static Context context;
    private static BeiWoApplication mInstance;
    private static LikesDB likesDB;


    public static synchronized BeiWoApplication getInstance(){
        if(mInstance == null){
            mInstance = new BeiWoApplication();
        }
        return  mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        mInstance = this;

        initImageLoader();
        //初始化Bmob
        Bmob.initialize(this, Constant.BMOB_APP_ID);

//        // 使用推送服务时的初始化操作
//        BmobInstallation.getCurrentInstallation(this).save();
//        // 启动推送服务
//        BmobPush.startWork(this, Constant.BMOB_APP_ID);
    }

    /**
     * 获取全局上下文
     * @return
     */
    public Context getContext() {
        return context;
    }

    /**
     * 初始化ImageLoader
     */
    private void initImageLoader(){
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(mInstance)
                .memoryCacheExtraOptions(480, 800)
                .threadPoolSize(3)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCache(new UnlimitedDiskCache(new File(Constant.IMAGE_CACHE_PATH)))
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .diskCacheSize(50 * 1024 * 1024)
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .imageDownloader(new BaseImageDownloader(mInstance, 5 * 1000, 30 * 1000))
                .build();
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
    }

    /**
     * 获取缓存用户信息
     * @return
     */
    public User getCurrentUser() {
        return BmobUser.getCurrentUser(context, User.class);
    }

    /**
     * 注销
     * @param isOffsite 是否显示异地登录
     */
    public void logout(boolean isOffsite) {
        // 关闭数据库
        closedDB();
        // 注销登录设备
        //PushUtil.logoutInstallation(context);
        // 用户缓存注销
        BmobUser.logOut(context);
        // 清空缓存用户配置
       // userConfig = null;
        // 清空所有界面
        AppManager.getAppManager().finishAllActivity();
        // 跳转至登录页
        Intent intent = new Intent(context, LoginActivity.class);
        //intent.putExtra("isOffsite", isOffsite);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 获取用户设置信息
     * @return
     */
    public synchronized LikesDB getLikesDB() {
        if (likesDB == null) {
            likesDB = new LikesDB(context);
        }
        return likesDB;
    }

    private void closedDB() {
        likesDB.closedDB();
        likesDB = null;
    }
}

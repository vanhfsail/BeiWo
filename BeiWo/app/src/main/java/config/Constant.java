package config;

import android.os.Environment;

import java.io.File;

/**
 * Created by huang.fan on 2016-3-21.
 */
public class Constant {
    public static final int PAGE_SIZE = 10;
    public static final int INPUT_LIMITED_LENGTH = 200;
    public static final int REQUEST_CODE_IMAGE_NATIVE = 0;
    public static final int REQUEST_CODE_IMAGE_CAMERA = 1;
    public static final String MOB_SMS_APP_KEY = "10bff8d9b0238";
    public static final String MOB_SMS_APP_SECRET = "64e8f231882bc8289fe3ff3820ff4b93";
    public static final String BMOB_APP_ID ="b760aa42ca3d96a29257ed0eb00f083f";
    public static final String IMAGE_CACHE_PATH = Environment.getExternalStorageDirectory()
            + File.separator + "BeiWo"+ File.separator + "Cache";

    /*  数据缓存 目录，object */
    public static final String DATA_CACHE_FILDER = "data_cache";
    /*  数据缓存 文件，用户粉丝列表 */
    public static final String USER_FANS_LIST = "USER_FANS_LIST";
    /*  数据缓存 文件，用户关注列表 */
    public static final String USER_FOLLOW_LIST = "USER_FOLLOW_LIST";
}

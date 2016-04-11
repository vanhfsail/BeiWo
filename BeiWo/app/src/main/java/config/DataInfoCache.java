package config;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import bean.User;

/**
 * Created by huang.fan on 2016-3-30.
 */
public class DataInfoCache {
    /**
     * @param ctx
     * @param data 保存用户粉丝列表集合对象
     */
    public static void saveUserFansList(Context ctx, ArrayList<User> data) {
        new DataCache<User>().saveGlobal(ctx, data, Constant.USER_FANS_LIST);
    }

    /**
     * @param ctx
     * @return 加载保存的用户粉丝列表集合对象
     */
    public static ArrayList<User> loadUserFansList(Context ctx) {
        return new DataCache<User>().loadGlobal(ctx, Constant.USER_FANS_LIST);
    }

    /**
     * @param ctx
     * @param data 保存用户关注列表集合对象
     */
    public static void saveUserFollowList(Context ctx, ArrayList<User> data) {
        new DataCache<User>().saveGlobal(ctx, data, Constant.USER_FOLLOW_LIST);
    }

    /**
     * @param ctx
     * @return 加载保存的用户关注列表集合对象
     */
    public static ArrayList<User> loadUserFollowList(Context ctx) {
        return new DataCache<User>().loadGlobal(ctx, Constant.USER_FOLLOW_LIST);
    }
    /**
     * @param <T> 数据缓存 save or load
     */
    static class DataCache<T> {
        public void save(Context ctx, ArrayList<T> data, String name) {
            save(ctx, data, name, "");
        }

        public void saveGlobal(Context ctx, ArrayList<T> data, String name) {
            save(ctx, data, name, Constant.DATA_CACHE_FILDER);
        }

        private void save(Context ctx, ArrayList<T> data, String name,
                          String folder) {
            if (ctx == null) {
                return;
            }
            File file;
            if (!folder.isEmpty()) {
                File fileDir = new File(ctx.getFilesDir(), folder);
                if (!fileDir.exists() || !fileDir.isDirectory()) {
                    fileDir.mkdir();
                }
                file = new File(fileDir, name);
            } else {
                file = new File(ctx.getFilesDir(), name);
            }
            if (file.exists()) {
                file.delete();
            }
            try {
                ObjectOutputStream oos = new ObjectOutputStream(
                        new FileOutputStream(file));
                oos.writeObject(data);
                oos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public ArrayList<T> load(Context ctx, String name) {
            return load(ctx, name, "");
        }

        public ArrayList<T> loadGlobal(Context ctx, String name) {
            return load(ctx, name, Constant.DATA_CACHE_FILDER);
        }

        private ArrayList<T> load(Context ctx, String name, String folder) {
            ArrayList<T> data = null;

            File file;
            if (!folder.isEmpty()) {
                File fileDir = new File(ctx.getFilesDir(), folder);
                if (!fileDir.exists() || !fileDir.isDirectory()) {
                    fileDir.mkdir();
                }
                file = new File(fileDir, name);
            } else {
                file = new File(ctx.getFilesDir(), name);
            }
            if (file.exists()) {
                try {
                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                    data = (ArrayList<T>) ois.readObject();
                    ois.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (data == null) {
                data = new ArrayList<T>();
            }
            return data;
        }
    }
}


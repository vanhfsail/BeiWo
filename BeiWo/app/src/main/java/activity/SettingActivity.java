package activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bmob.BmobProFile;
import com.bmob.btp.callback.UploadListener;
import com.huangfan.beiwo.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;

import bean.User;
import butterknife.Bind;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.UpdateListener;
import config.Constant;
import config.ImageLoaderOptions;
import de.hdodenhof.circleimageview.CircleImageView;
import dialog.ChooseSexDialog;
import dialog.EditTextDialog;
import dialog.SelectPictureDialog;
import util.FileUtil;
import util.PhotoUtil;
import util.ToastUtil;

/**
 * Created by huang.fan on 2016-3-23.
 */
public class SettingActivity extends BaseActivity implements View.OnClickListener {
    private String localCameraPath;// 拍照后得到的图片地址
    private String imagePath;// 上传的图片地址

    @Bind(R.id.tv_title)
    TextView title;
    @Bind(R.id.rl_bar_left)
    RelativeLayout back;
    @Bind(R.id.rl_setting_avatar)
    RelativeLayout settingAvatar;
    @Bind(R.id.rl_setting_name)
    RelativeLayout settingName;
    @Bind(R.id.rl_setting_sex)
    RelativeLayout settingSex;
    @Bind(R.id.rl_setting_age)
    RelativeLayout settingAge;
    @Bind(R.id.rl_setting_signature)
    RelativeLayout settingSignature;
    @Bind(R.id.rl_setting_clear_cache)
    RelativeLayout settingClearCache;
    @Bind(R.id.rl_setting_about)
    RelativeLayout settingAbout;

    @Bind(R.id.setting_avatar)
    CircleImageView avatar;
    @Bind(R.id.setting_name)
    TextView name;
    @Bind(R.id.setting_sex)
    TextView sexy;
    @Bind(R.id.setting_age)
    TextView age;
    @Bind(R.id.setting_signature)
    TextView signature;
    @Bind(R.id.setting_cache_size)
    TextView cacheSize;

    @Bind(R.id.btn_logout)
    Button logout;

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.activity_setting;
    }

    @Override
    protected void initViewsAndEvents() {
        title.setText("个人设置");
        initUserInfo();
        back.setOnClickListener(this);
        settingAvatar.setOnClickListener(this);
        settingName.setOnClickListener(this);
        settingSex.setOnClickListener(this);
        settingAge.setOnClickListener(this);
        settingSignature.setOnClickListener(this);
        settingClearCache.setOnClickListener(this);
        settingAbout.setOnClickListener(this);
        logout.setOnClickListener(this);
        cacheSize.setText(FileUtil.getFormatSize(FileUtil.getFolderSize(new File(Constant.IMAGE_CACHE_PATH))));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rl_bar_left:
                finish();
                break;
            case R.id.rl_setting_avatar:
                final SelectPictureDialog selectPictureDialog = new SelectPictureDialog(this, new SelectPictureDialog.OnSelectedListener() {
                    @Override
                    public void onSelected(int type) {
                        switch (type) {
                            case SelectPictureDialog.CAMERA:
                                selectImageFromCamera();
                                break;
                            case SelectPictureDialog.NATIVE:
                                selectImageFromLocal();
                                break;
                        }
                    }
                });
                selectPictureDialog.show();
                break;
            case R.id.rl_setting_name:
                EditTextDialog editNameDialog = new EditTextDialog(this,
                        "昵称", InputType.TYPE_CLASS_TEXT,
                        application.getCurrentUser().getNickname(), 16,
                        new EditTextDialog.OnDoneListener() {
                            @Override
                            public void onDone(final String contentStr) {
                                loadingDialog.show();
                                User user = new User();
                                user.setNickname(contentStr);
                                updateUser(user, new UpdateListener() {
                                    @Override
                                    public void onSuccess() {
                                        name.setText(contentStr);
                                        loadingDialog.dismiss();
                                    }

                                    @Override
                                    public void onFailure(int i, String s) {
                                        ToastUtil.showShort("更新用户信息失败:" + s);
                                        loadingDialog.dismiss();
                                    }
                                });
                            }
                        });
                editNameDialog.show();
                break;
            case R.id.rl_setting_sex:
                ChooseSexDialog chooseSexDialog = new ChooseSexDialog(this,
                        application.getCurrentUser().getSex(), new ChooseSexDialog.OnDoneListener(){
                    @Override
                    public void onDone(final Integer sex) {
                        loadingDialog.show();
                        User user = new User();
                        user.setSex(sex);
                        updateUser(user, new UpdateListener() {
                            @Override
                            public void onSuccess() {
                                sexy.setText(sex.intValue() == 0 ? "男" : "女");
                                loadingDialog.dismiss();
                            }
                            @Override
                            public void onFailure(int i, String s) {
                                ToastUtil.showShort("更新用户信息失败:" + s);
                                loadingDialog.dismiss();
                            }
                        });
                    }
                });
                chooseSexDialog.show();
                break;
            case R.id.rl_setting_age:
                EditTextDialog editAgeDialog = new EditTextDialog(this,
                        "年龄", InputType.TYPE_CLASS_NUMBER,
                        String.valueOf(application.getCurrentUser().getAge()), 3,
                        new EditTextDialog.OnDoneListener() {
                            @Override
                            public void onDone(final String contentStr) {
                                loadingDialog.show();
                                User user = new User();
                                user.setAge(Integer.parseInt(contentStr));
                                updateUser(user, new UpdateListener() {
                                    @Override
                                    public void onSuccess() {
                                        age.setText(contentStr);
                                        loadingDialog.dismiss();
                                    }
                                    @Override
                                    public void onFailure(int i, String s) {
                                        ToastUtil.showShort("更新用户信息失败:" + s);
                                        loadingDialog.dismiss();
                                    }
                                });
                            }
                        });
                editAgeDialog.show();
                break;
            case R.id.rl_setting_signature:
                EditTextDialog editSignatureDialog = new EditTextDialog(this,
                        "个性签名",InputType.TYPE_CLASS_TEXT,
                        application.getCurrentUser().getSign(), 50,
                        new EditTextDialog.OnDoneListener() {
                            @Override
                            public void onDone(final String contentStr) {
                                loadingDialog.show();
                                User user = new User();
                                user.setSign(contentStr);
                                updateUser(user, new UpdateListener() {
                                    @Override
                                    public void onSuccess() {
                                        signature.setText(contentStr);
                                        loadingDialog.dismiss();
                                    }
                                    @Override
                                    public void onFailure(int i, String s) {
                                        ToastUtil.showShort("更新用户信息失败:" + s);
                                        loadingDialog.dismiss();
                                    }
                                });
                            }
                        });
                editSignatureDialog.show();
                break;
            case R.id.rl_setting_clear_cache:
                new ClearCacheTask().execute();
                break;
            case R.id.rl_setting_about:
                //startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.btn_logout:
                application.logout(false);
                break;
        }
    }

    /**
     * 初始化用户信息设置
     */
    private void initUserInfo(){
        User user = application.getCurrentUser();
        if (user.getAvatar() != null) {
            ImageLoader.getInstance().displayImage(user.getAvatar().getUrl(),
                    avatar, ImageLoaderOptions.getOptions());
        } else {
            avatar.setImageResource(R.mipmap.user_logo_default);
        }
        name.setText(TextUtils.isEmpty(user.getNickname()) ?
                "点击填写" : user.getNickname().toString());
        sexy.setText(user.getSex().intValue() == 0 ? "男" : "女");
        age.setText(user.getAge().toString());
        signature.setText(TextUtils.isEmpty(user.getSign()) ?
                "点击填写" : user.getSign().toString());

    }

    /**
     * 启动相机拍照
     */
    public void selectImageFromCamera() {
        File dir = new File(Constant.IMAGE_CACHE_PATH + File.separator);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, String.valueOf(System.currentTimeMillis()));
        localCameraPath = file.getPath();
        Uri imageUri = Uri.fromFile(file);
        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(openCameraIntent, Constant.REQUEST_CODE_IMAGE_CAMERA);
    }

    /**
     * 获取本地图片
     */
    public void selectImageFromLocal() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
        } else {
            intent = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent, Constant.REQUEST_CODE_IMAGE_NATIVE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Constant.REQUEST_CODE_IMAGE_CAMERA:
                    // 获取拍照的压缩图片
                    String cameraPath = Constant.IMAGE_CACHE_PATH + File.separator + String.valueOf(System.currentTimeMillis());
                    Bitmap cameraBitmap = PhotoUtil.compressImage(localCameraPath, cameraPath, true);
                    imagePath = cameraPath;
                    // 更新头像
                    uploadAvatar(imagePath, cameraBitmap);
                    break;
                case Constant.REQUEST_CODE_IMAGE_NATIVE:
                    if (data != null) {
                        Uri selectedImage = data.getData();
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(
                                    selectedImage, null, null, null, null);
                            cursor.moveToFirst();
                            int columnIndex = cursor.getColumnIndex("_data");
                            String localSelectPath = cursor.getString(columnIndex);
                            cursor.close();
                            if (localSelectPath == null || localSelectPath.equals("null")) {
                                ToastUtil.showShort("未取到图片！");
                                return;
                            }
                            Bitmap nativeBitmap = null;
                            File localFile = new File(localSelectPath);
                            // 若此文件小于100KB，直接使用。为了减轻缓存容量
                            if (localFile.length() < 102400) {
                                nativeBitmap = BitmapFactory.decodeFile(localSelectPath);
                                imagePath = localSelectPath;
                            } else {
                                String nativePath = Constant.IMAGE_CACHE_PATH  + File.separator + String.valueOf(System.currentTimeMillis());
                                nativeBitmap = PhotoUtil.compressImage(localSelectPath, nativePath, false);
                                imagePath = nativePath;
                            }
                            // 更新头像
                            uploadAvatar(imagePath, nativeBitmap);
                        }
                    }
                    break;
            }
        }
    }

    /**
     * 上传用户头像
     * @param imagePath
     */
    private void uploadAvatar(String imagePath, final Bitmap bitmap) {
        if (TextUtils.isEmpty(imagePath)) {
            return;
        }
        BmobProFile.getInstance(this).upload(imagePath, new UploadListener() {
            @Override
            public void onSuccess(String s, String s1, BmobFile bmobFile) {
                User user = new User();
                user.setAvatar(bmobFile);
                updateUser(user, new UpdateListener() {
                    @Override
                    public void onSuccess() {
                        avatar.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onFailure(int i, String s) {
                        ToastUtil.showShort("更新用户信息失败:" + s);
                    }
                });
            }

            @Override
            public void onProgress(int i) {
            }

            @Override
            public void onError(int i, String s) {
            }
        });
    }

    /**
     * 更新user信息
     * @param newUser
     * @param listener
     */
    private void updateUser(User newUser, UpdateListener listener) {
        if (newUser == null) {
            return;
        }
        User currentUser = application.getCurrentUser();
        newUser.update(this, currentUser.getObjectId(), listener);
    }

    /**
     * 清除缓存异步任务
     */
    class ClearCacheTask extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            File file = new File(Constant.IMAGE_CACHE_PATH);
            FileUtil.clearCache(file);
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            ToastUtil.showShort("缓存清除完毕");
            cacheSize.setText(null);
        }

    }

}

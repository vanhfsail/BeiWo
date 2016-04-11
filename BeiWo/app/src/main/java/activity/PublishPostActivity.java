package activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bmob.BmobProFile;
import com.bmob.btp.callback.UploadListener;
import com.huangfan.beiwo.R;

import java.io.File;

import bean.Post;
import bean.User;
import butterknife.Bind;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.SaveListener;
import config.Constant;
import util.PhotoUtil;
import util.ToastUtil;

/**
 * Created by huang.fan on 2016-3-21.
 */
public class PublishPostActivity extends BaseActivity implements View.OnClickListener {
    @Bind(R.id.tv_title)
    TextView title;

    @Bind(R.id.rl_bar_right)
    RelativeLayout rightBar;

    @Bind(R.id.iv_bar_right)
    ImageView rightImage;

    @Bind(R.id.publish_post_content)
    EditText postContent;

    @Bind(R.id.publish_post_image_layout)
    RelativeLayout imageLayout;

    @Bind(R.id.publish_post_image)
    ImageView postImage;

    @Bind(R.id.publish_delete_image)
    ImageView deleteImage;

    @Bind(R.id.publish_add_camera)
    ImageView addCamera;

    @Bind(R.id.publish_add_picture)
    ImageView addPicture;

    @Bind(R.id.publish_word_num)
    TextView wordNum;

    @Bind(R.id.rl_bar_left)
    RelativeLayout leftbar;

    private String localCameraPath;// 拍照后得到的图片地址
    private String imagePath;// 上传的图片地址
    @Override
    protected int getContentViewLayoutId() {
        return R.layout.activity_publish_post;
    }

    @Override
    protected void initViewsAndEvents() {
        title.setText("发布");
        leftbar.setOnClickListener(this);
        rightBar.setVisibility(View.VISIBLE);
        rightBar.setOnClickListener(this);
        rightImage.setImageResource(R.mipmap.icon_correct);
        deleteImage.setOnClickListener(this);
        addCamera.setOnClickListener(this);
        addPicture.setOnClickListener(this);
        postContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int length = Constant.INPUT_LIMITED_LENGTH - postContent.getText().toString().length();
                wordNum.setText(String.valueOf(length));
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rl_bar_left:
                finish();
                break;
            case R.id.publish_add_picture:
                selectImageFromLocal();
                break;
            case  R.id.publish_add_camera:
                selectImageFromCamera();
                break;
            case R.id.publish_delete_image:
                localCameraPath = "";
                imagePath = "";
                imageLayout.setVisibility(View.GONE);
                postImage.setImageDrawable(null);
                break;
            case R.id.rl_bar_right:
                post();
                break;
        }

    }

    /**
     * 发表前上传图片
     */
    private void post() {
        loadingDialog.show();
        if (TextUtils.isEmpty(imagePath)) {
            // 没有图片，直接发表
            publishPost(null);
            return;
        }
        // 有图片，上传图片
        BmobProFile.getInstance(this).upload(imagePath, new UploadListener() {
            @Override
            public void onSuccess(String s, String s1, BmobFile bmobFile) {
                Log.d("bmob", "bmobFile-Url：" + bmobFile.getUrl());
                publishPost(bmobFile);
            }

            @Override
            public void onProgress(int i) {
                Log.d("bmob", "onProgress：" + i);
            }

            @Override
            public void onError(int i, String s) {
                loadingDialog.dismiss();
                ToastUtil.showShort("发表失败：" + s);
            }
        });
    }

    /**
     * 发表
     * @param imageFile
     */
    private void publishPost(BmobFile imageFile) {
        String content = postContent.getText().toString();
        // 若没有图片，则发表内容不能为空
        if (imageFile == null && TextUtils.isEmpty(content)) {
            loadingDialog.dismiss();
            ToastUtil.showShort("请输入内容或添加图片！");
            return;
        }
        User user = application.getCurrentUser();
        Post post = new Post();
        post.setAuthor(user);
        post.setContent(content);
        post.setImage(imageFile);
        post.setCommentNum(0);
        post.setLikeNum(0);
        post.setDevice(Build.MODEL);
        post.save(this, new SaveListener() {
            @Override
            public void onSuccess() {
                loadingDialog.dismiss();
                ToastUtil.showShort("发表成功！");
                finish();
            }

            @Override
            public void onFailure(int i, String s) {
                loadingDialog.dismiss();
                ToastUtil.showShort("发表失败：" + s);
            }
        });
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
                    // 界面显示
                    imageLayout.setVisibility(View.VISIBLE);
                    postImage.setImageBitmap(cameraBitmap);
                    imagePath = cameraPath;
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
                            // 界面显示
                            imageLayout.setVisibility(View.VISIBLE);
                            postImage.setImageBitmap(nativeBitmap);
                        }
                    }
                    break;
            }
        }
    }
}

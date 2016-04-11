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

import bean.Comment;
import bean.Post;
import bean.User;
import butterknife.Bind;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import config.Constant;
import util.PhotoUtil;
import util.ToastUtil;

/**
 * Created by huang.fan on 2016-3-28.
 */
public class PublishCommentActivity  extends BaseActivity implements View.OnClickListener{
    @Bind(R.id.tv_title)
    TextView title;

    @Bind(R.id.rl_bar_left)
    RelativeLayout back;

    @Bind(R.id.rl_bar_right)
    RelativeLayout rightBar;

    @Bind(R.id.iv_bar_right)
    ImageView rightImage;

    @Bind(R.id.publish_comment_content)
    EditText commentContent;

    @Bind(R.id.publish_comment_image_layout)
    RelativeLayout imageLayout;

    @Bind(R.id.publish_comment_image)
    ImageView commentImage;

    @Bind(R.id.comment_delete_image)
    ImageView deleteImage;

    @Bind(R.id.comment_add_camera)
    ImageView addCamera;

    @Bind(R.id.comment_add_picture)
    ImageView addPicture;

    @Bind(R.id.comment_word_num)
    TextView wordNum;

    private String localCameraPath;// 拍照后得到的图片地址
    private String imagePath;// 上传的图片地址
    private Post mPost;
    private int commentNum;
    @Override
    protected int getContentViewLayoutId() {
        return R.layout.activity_publish_comment;
    }

    @Override
    protected void initViewsAndEvents() {
        title.setText("发布评论");
        back.setOnClickListener(this);
        rightBar.setVisibility(View.VISIBLE);
        rightBar.setOnClickListener(this);
        rightImage.setImageResource(R.mipmap.icon_correct);
        deleteImage.setOnClickListener(this);
        addCamera.setOnClickListener(this);
        addPicture.setOnClickListener(this);
        commentContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int length = Constant.INPUT_LIMITED_LENGTH - commentContent.getText().toString().length();
                wordNum.setText(String.valueOf(length));
            }
        });
        initPost();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rl_bar_left:
                finish();
                break;
            case R.id.comment_add_picture:
                selectImageFromLocal();
                break;
            case  R.id.comment_add_camera:
                selectImageFromCamera();
                break;
            case R.id.comment_delete_image:
                localCameraPath = "";
                imagePath = "";
                imageLayout.setVisibility(View.GONE);
                commentImage.setImageDrawable(null);
                break;
            case R.id.rl_bar_right:
                hideSoftInputView();
                comment();
                break;
        }
    }

    /**
     * 初始化post数据
     */
    private void initPost() {
        BmobQuery<Post> query = new BmobQuery<Post>();
        String postId = getIntent().getStringExtra("postId");
        if (TextUtils.isEmpty(postId)) {
            ToastUtil.showShort("获取数据失败");
            return;
        }
        query.include("author");
        query.getObject(this, postId, new GetListener<Post>() {
            @Override
            public void onSuccess(Post object) {
                mPost = object;
            }

            @Override
            public void onFailure(int code, String arg0) {
                ToastUtil.showShort("获取数据失败");
            }
        });
    }

    /**
     * 发布前上传图片
     */

    private void comment(){
        loadingDialog.show();
        if (TextUtils.isEmpty(imagePath)) {
            // 没有图片，直接发表
            publishComment(null);
            return;
        }
        // 有图片，上传图片
        BmobProFile.getInstance(this).upload(imagePath, new UploadListener() {
            @Override
            public void onSuccess(String s, String s1, BmobFile bmobFile) {
                Log.d("bmob", "bmobFile-Url：" + bmobFile.getUrl());
                publishComment(bmobFile);
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
     * 发布评论
     * @param imageFile
     */
    private void publishComment(BmobFile imageFile) {
        String content = commentContent.getText().toString();
        // 若没有图片，则发表内容不能为空
        if (imageFile == null && TextUtils.isEmpty(content)) {
            loadingDialog.dismiss();
            ToastUtil.showShort("请输入内容或添加图片！");
            return;
        }
        // 发送
        User user = application.getCurrentUser();
        commentNum = mPost.getCommentNum().intValue();
        final Comment comment = new Comment();
        comment.setContent(content);
        comment.setPost(mPost);
        comment.setUser(user);
        comment.setImage(imageFile);
        comment.save(this, new SaveListener() {
            @Override
            public void onSuccess() {
                commentNum++;
                Post post = new Post();
                post.setCommentNum(commentNum);
                post.update(PublishCommentActivity.this, mPost.getObjectId(), new UpdateListener() {
                    @Override
                    public void onSuccess() {
                        loadingDialog.dismiss();
                        ToastUtil.showShort("评论成功！");
                        finish();
                        // 发送消息
                        //sendMessage(comment.getContent());
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        ToastUtil.showShort("评论失败：" + msg);
                    }
                });
            }

            @Override
            public void onFailure(int code, String msg) {
                ToastUtil.showShort("评论失败：" + msg);
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
                    commentImage.setImageBitmap(cameraBitmap);
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
                            commentImage.setImageBitmap(nativeBitmap);
                        }
                    }
                    break;
            }
        }
    }
}

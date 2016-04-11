package adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.huangfan.beiwo.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import activity.UserInfoActivity;
import app.BeiWoApplication;
import bean.Like;
import bean.Post;
import bean.User;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.listener.UpdateListener;
import config.ImageLoaderOptions;
import db.LikesDB;
import de.hdodenhof.circleimageview.CircleImageView;
import dialog.ImageDialog;
import util.TimeUtil;
import util.ToastUtil;
import util.ViewHolder;

/**
 * Created by huang.fan on 2016-3-23.
 */
public class PostListAdapter extends BaseAdapter {
    private Context context;
    private List<Post> list;
    private LayoutInflater inflater;
    private LikesDB likesDB;

    public PostListAdapter(Context context, List<Post> list) {
        super();
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
        likesDB = BeiWoApplication.getInstance().getLikesDB();
    }

    public void setList(List<Post> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size() == 0 ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_post, null);
        }
        CircleImageView avatar = ViewHolder.get(convertView,R.id.post_item_avatar);
        TextView name = ViewHolder.get(convertView,R.id.post_item_nickname);
        TextView time = ViewHolder.get(convertView,R.id.post_item_time);
        TextView content = ViewHolder.get(convertView,R.id.post_item_content);
        ImageView contentImage = ViewHolder.get(convertView,R.id.post_item_image);
        TextView commentNum = ViewHolder.get(convertView,R.id.post_comment_num);
        final  ImageView postlike = ViewHolder.get(convertView,R.id.post_like);
        final  TextView likeNum = ViewHolder.get(convertView,R.id.post_like_num);
        TextView device = ViewHolder.get(convertView,R.id.post_item_device);
        final Post post = list.get(position);
        if (post.getAuthor().getAvatar() != null) {
            ImageLoader.getInstance().displayImage(post.getAuthor().getAvatar().getUrl(),
                    avatar, ImageLoaderOptions.getOptions());
        } else {
            avatar.setImageResource(R.mipmap.user_logo_default);
        }
        //如果是个人中心页面传入，则头像不可点击
        if(context.getClass().equals(UserInfoActivity.class)){
            avatar.setEnabled(false);
        }
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到个人主页。。。
                Intent intent = new Intent();
                intent.putExtra("user", post.getAuthor());
                intent.setClass(context, UserInfoActivity.class);
                context.startActivity(intent);
            }
        });

        String nickname = TextUtils.isEmpty(post.getAuthor().getNickname())
                ? post.getAuthor().getUsername()
                : post.getAuthor().getNickname();
        name.setText(nickname);
        String createAt = post.getCreatedAt();
        time.setText(TimeUtil.getDescriptionTimeFromTimestamp(TimeUtil.stringToLong(createAt, TimeUtil.FORMAT_DATE_TIME_SECOND)));
        if (!TextUtils.isEmpty(post.getContent())) {
            content.setVisibility(View.VISIBLE);
            content.setText(post.getContent());
        } else {
            content.setVisibility(View.GONE);
        }
        if (post.getImage() != null) {
            contentImage.setVisibility(View.VISIBLE);
            ImageLoader.getInstance().displayImage(post.getImage().getUrl(),
                    contentImage, ImageLoaderOptions.getOptions());
            contentImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageDialog imageDialog = new ImageDialog(context, post.getImage().getUrl());
                    imageDialog.show();
                }
            });
        } else {
            contentImage.setVisibility(View.GONE);
        }

        String deviceinfo = post.getDevice();
        if (!deviceinfo.isEmpty()) {
            final String format = "来自 %s";
            deviceinfo = String.format(format, deviceinfo);
        }
        device.setText(deviceinfo);
        commentNum.setText(post.getCommentNum().toString());
        likeNum.setText(post.getLikeNum().toString());
        String uId = BeiWoApplication.getInstance().getCurrentUser().getObjectId();
        Like like = new Like(uId, post.getObjectId());
        postlike.setSelected(likesDB.isLike(like));
        postlike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = BmobUser.getCurrentUser(context, User.class);
                // 若是自己发布，则不可点
                if (user.getObjectId().equals(list.get(position).getAuthor().getObjectId())) {
                    return;
                }
                postlike.setClickable(false);
                Post post = new Post();
                post.setObjectId(list.get(position).getObjectId());
                BmobRelation relation = new BmobRelation();
                final Like like = new Like(user.getObjectId(), list.get(position).getObjectId());
                if (postlike.isSelected()) {
                    // 取消点赞
                    ToastUtil.showShort("取消点赞");
                    relation.remove(user);
                    post.setLikes(relation);
                    post.increment("likeNum", -1);
                    post.update(context, new UpdateListener() {
                        @Override
                        public void onSuccess() {
                            postlike.setSelected(false);
                            likeNum.setText(String.valueOf(Integer.parseInt(likeNum.getText().toString()) - 1));
                            likesDB.delete(like);
                            postlike.setClickable(true);
                        }

                        @Override
                        public void onFailure(int arg0, String arg1) {
                        }
                    });
                    return;
                } else {
                    // 点赞
                    ToastUtil.showShort("点赞成功");
                    relation.add(user);
                    post.setLikes(relation);
                    post.increment("likeNum", 1);
                    post.update(context, new UpdateListener() {
                        @Override
                        public void onSuccess() {
                            postlike.setSelected(true);
                            likeNum.setText(String.valueOf(Integer.parseInt(likeNum.getText().toString()) + 1));
                            likesDB.addOne(like);
                            postlike.setClickable(true);
                        }

                        @Override
                        public void onFailure(int i, String s) {
                        }
                    });
                }
            }
        });
        return convertView;
    }
}

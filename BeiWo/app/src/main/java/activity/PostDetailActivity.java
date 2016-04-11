package activity;

import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.huangfan.beiwo.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import adapter.CommentListAdapter;
import adapter.LikeListAdapter;
import bean.Comment;
import bean.Like;
import bean.Post;
import bean.User;
import butterknife.Bind;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.UpdateListener;
import config.ImageLoaderOptions;
import db.LikesDB;
import de.hdodenhof.circleimageview.CircleImageView;
import dialog.ImageDialog;
import util.TimeUtil;
import util.ToastUtil;
import widget.ListViewForScrollView;

/**
 * Created by huang.fan on 2016-3-28.
 */
public class PostDetailActivity extends BaseActivity implements View.OnClickListener{
    @Bind(R.id.tv_title)
    TextView title;

    @Bind(R.id.scrollview)
    ScrollView scrollView;

    @Bind(R.id.show_comment_list)
    TextView showComment;

    @Bind(R.id.show_like_list)
    TextView showLike;

    @Bind(R.id.post_detail_comment_list)
    ListViewForScrollView commentListView;

    @Bind(R.id.post_detail_like_list)
    ListViewForScrollView likeListView;

    @Bind(R.id.post_detail_tips)
    TextView tips;

    @Bind(R.id.post_detail_comment)
    LinearLayout commentClick;

    @Bind(R.id.post_detail_like)
    ImageView likeClick;

    @Bind(R.id.post_detail_item_avatar)
    CircleImageView avatar;

    @Bind(R.id.post_detail_item_nickname)
    TextView nickname;

    @Bind(R.id.post_detail_item_time)
    TextView time;

    @Bind(R.id.post_detail_item_content)
    TextView content;

    @Bind(R.id.post_detail_item_image)
    ImageView image;

    @Bind(R.id.rl_bar_left)
    RelativeLayout back;

    @Bind(R.id.post_detail_progress)
    ProgressBar progressBar;

    private LikesDB likesDB;
    private static int PAGE_SIZE = 10;
    private int currentCommentNum;
    private int currentLikeNum;
    private int currentPageIndex;
    private String postId;

    private Post mPost;
    private List<Comment> commentList;
    private List<User> likelist;
    private CommentListAdapter commentListAdapter;
    private LikeListAdapter likeListAdapter;

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.activity_post_detail;
    }

    @Override
    protected void initViewsAndEvents() {
        title.setText("贴子详情");
        progressBar.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.INVISIBLE);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                //execute the task
                initPost();
                currentPageIndex = 0;
                scrollView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
            }
        }, 2000);
        likesDB = application.getLikesDB();
        commentList = new ArrayList<Comment>();
        commentListAdapter = new CommentListAdapter(this,commentList);
        commentListView.setAdapter(commentListAdapter);

        likelist = new ArrayList<User>();
        likeListAdapter = new LikeListAdapter(this,likelist);
        likeListView.setAdapter(likeListAdapter);

        avatar.setOnClickListener(this);
        back.setOnClickListener(this);
        showComment.setOnClickListener(this);
        showLike.setOnClickListener(this);
        commentClick.setOnClickListener(this);
        likeClick.setOnClickListener(this);
        tips.setOnClickListener(this);
        postId= getIntent().getStringExtra("postId");

    }

    private void initData(){
        if (mPost.getAuthor().getAvatar() != null) {
            ImageLoader.getInstance().displayImage(mPost.getAuthor().getAvatar().getUrl(),
                    avatar, ImageLoaderOptions.getOptions());
        }
        String name = TextUtils.isEmpty(mPost.getAuthor().getNickname())
                ? mPost.getAuthor().getUsername()
                : mPost.getAuthor().getNickname();
        nickname.setText(name);
        String createAt = mPost.getCreatedAt();
        time.setText(TimeUtil.getDescriptionTimeFromTimestamp(TimeUtil.stringToLong(createAt, TimeUtil.FORMAT_DATE_TIME_SECOND)));
        if (!TextUtils.isEmpty(mPost.getContent())) {
            content.setVisibility(View.VISIBLE);
            content.setText(mPost.getContent());
        } else {
            content.setVisibility(View.GONE);
        }
        if (mPost.getImage() != null) {
            image.setVisibility(View.VISIBLE);
            ImageLoader.getInstance().displayImage(mPost.getImage().getUrl(),
                    image, ImageLoaderOptions.getOptions());
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageDialog imageDialog = new ImageDialog(PostDetailActivity.this, mPost.getImage().getUrl());
                    imageDialog.show();
                }
            });
        } else {
            image.setVisibility(View.GONE);
        }

        currentCommentNum = mPost.getCommentNum().intValue();
        currentLikeNum = mPost.getLikeNum().intValue();
        showComment.setText("评论" + " " + mPost.getCommentNum().toString());
        showComment.setSelected(true);
        showLike.setText("赞" + " " + mPost.getLikeNum().toString());

        Like like = new Like(application.getCurrentUser().getObjectId(), mPost.getObjectId());
        likeClick.setSelected(likesDB.isLike(like));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rl_bar_left:
                finish();
                break;
            case R.id.post_detail_like:
                likeOrNot();
                break;
            case R.id.show_comment_list:
                if (!showComment.isSelected()) {
                    // 切换选中状态
                    showComment.setSelected(true);
                    showLike.setSelected(false);
                    // 显示
                    commentListView.setVisibility(View.VISIBLE);
                    likeListView.setVisibility(View.GONE);
                    tips.setText("正在加载...");
                    // 加载数据
                    currentPageIndex = 0;
                    loadComments();
                }
                break;
            case R.id.show_like_list:
                if (!showLike.isSelected()) {
                    // 切换选中状态
                    showComment.setSelected(false);
                    showLike.setSelected(true);
                    // 显示
                    commentListView.setVisibility(View.GONE);
                    likeListView.setVisibility(View.VISIBLE);
                    tips.setText("正在加载...");
                    // 加载数据
                    currentPageIndex = 0;
                    loadLikes();
                }
                break;
            case R.id.post_detail_tips:
                if (showComment.isSelected()) {
                    loadComments();
                } else {
                    loadLikes();
                }
                break;
            case R.id.post_detail_comment:
                Intent intent1 = new Intent(this,PublishCommentActivity.class);
                intent1.putExtra("postId", postId);
                startActivity(intent1);
                break;
            case R.id.post_detail_item_avatar:
                Intent intent2 = new Intent();
                intent2.putExtra("user", mPost.getAuthor());
                intent2.setClass(this, UserInfoActivity.class);
                startActivity(intent2);
        }
    }
    
    private void likeOrNot(){
        final User user = application.getCurrentUser();
        // 若是自己发布，则不可点
        if (user.getObjectId().equals(mPost.getAuthor().getObjectId())) {
            return;
        }
        likeClick.setClickable(false);
        Post post = new Post();
        post.setObjectId(mPost.getObjectId());
        BmobRelation relation = new BmobRelation();
        final Like like = new Like(user.getObjectId(), mPost.getObjectId());
        if (likeClick.isSelected()) {
            // 取消点赞
            relation.remove(user);
            post.setLikes(relation);
            post.increment("likeNum", -1);
            post.update(this, new UpdateListener() {
                @Override
                public void onSuccess() {
                    currentLikeNum--;
                    likeClick.setSelected(false);
                    likesDB.delete(like);
                    showLike.setText("赞" + " " + currentLikeNum);
                    if (likelist.size() < PAGE_SIZE * currentPageIndex) {
                        for (int i = 0; i < likelist.size(); i++) {
                            if (likelist.get(i).getObjectId().equalsIgnoreCase(user.getObjectId())) {
                                likelist.remove(i);
                                break;
                            }
                        }
                        likeListAdapter.notifyDataSetChanged();
                    }
                    likeClick.setClickable(true);
                }
                @Override
                public void onFailure(int arg0, String arg1) {
                }
            });
            return;
        } else {
            // 点赞
            // 切换选中状态
            showComment.setSelected(false);
            showLike.setSelected(true);
            // 显示
            commentListView.setVisibility(View.GONE);
            likeListView.setVisibility(View.VISIBLE);
            tips.setText("正在加载...");
            // 加载数据
            currentPageIndex = 0;
            loadLikes();
            relation.add(user);
            post.setLikes(relation);
            post.increment("likeNum", 1);
            post.update(this, new UpdateListener() {
                @Override
                public void onSuccess() {
                    currentLikeNum++;
                    likeClick.setSelected(true);
                    likesDB.addOne(like);
                    showLike.setText("赞" + " " + currentLikeNum);
                    if (likelist.size() < PAGE_SIZE * currentPageIndex) {
                        likelist.add(user);
                        likeListAdapter.notifyDataSetChanged();
                    } else {
                        loadLikes();
                    }
                    likeClick.setClickable(true);
                }
                @Override
                public void onFailure(int i, String s) {
                }
            });
        }
    }

    /**
     * 点赞查询
     */
    private void loadLikes(){
        BmobQuery<User> query = new BmobQuery<User>();
        Post post = new Post();
        post.setObjectId(mPost.getObjectId());
        query.addWhereRelatedTo("likes", new BmobPointer(post));
        query.order("createdAt");
        query.setLimit(PAGE_SIZE);
        query.setSkip(PAGE_SIZE * currentPageIndex);
        query.findObjects(this, new FindListener<User>() {
            @Override
            public void onSuccess(List<User> object) {
                // 若是起始页，则删除列表
                if (currentPageIndex == 0) {
                    likelist.clear();
                    if (object == null || object.size() == 0) {
                        tips.setText("暂无点赞");
                        tips.setClickable(false);
                        return;
                    }
                }
                currentPageIndex++;
                likelist.addAll(object);
                likeListAdapter.notifyDataSetChanged();

                if (likelist.size() < PAGE_SIZE * currentPageIndex) {
                    tips.setText("无更多点赞");
                    tips.setClickable(false);
                } else {
                    tips.setText("加载更多");
                    tips.setClickable(true);
                }
            }

            @Override
            public void onError(int code, String msg) {
                tips.setText("暂无点赞");
            }
        });
    }

    /**
     * 评论查询
     */
    private void loadComments(){
        BmobQuery<Comment> query = new BmobQuery<Comment>();
        Post post = new Post();
        post.setObjectId(mPost.getObjectId());
        query.addWhereEqualTo("post", new BmobPointer(post));
        query.include("user");
        query.order("createdAt");
        query.setLimit(PAGE_SIZE);
        query.setSkip(PAGE_SIZE * currentPageIndex);
        query.findObjects(this, new FindListener<Comment>() {
            @Override
            public void onSuccess(List<Comment> object) {
                // 若是起始页，则删除列表
                if (currentPageIndex == 0) {
                    commentList.clear();
                    if (object == null || object.size() == 0) {
                        tips.setText("暂无评论");
                        tips.setClickable(false);
                        return;
                    }
                }
                currentPageIndex++;
                commentList.addAll(object);
                commentListAdapter.notifyDataSetChanged();

                if (commentList.size() < PAGE_SIZE * currentPageIndex) {
                    tips.setText("无更多评论");
                    tips.setClickable(false);
                } else {
                    tips.setText("加载更多");
                    tips.setClickable(true);
                }
            }

            @Override
            public void onError(int code, String msg) {
                tips.setText("暂无评论");
            }
        });
    }

    /**
     * 初始化post数据
     */
    private void initPost() {
        BmobQuery<Post> query = new BmobQuery<Post>();
        if (TextUtils.isEmpty(postId)) {
            ToastUtil.showShort("获取数据失败");
            return;
        }
        query.include("author");
        query.getObject(this, postId, new GetListener<Post>() {
            @Override
            public void onSuccess(Post object) {
                mPost = object;
                // 获取到数据后，刷新页面
                initData();
                // 加载评论
                currentPageIndex = 0;
                loadComments();
            }

            @Override
            public void onFailure(int code, String arg0) {
                ToastUtil.showShort("获取数据失败");
            }
        });
    }


}

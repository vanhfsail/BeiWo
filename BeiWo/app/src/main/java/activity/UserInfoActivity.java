package activity;

import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.huangfan.beiwo.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import adapter.PostListAdapter;
import bean.Post;
import bean.User;
import butterknife.Bind;
import cn.bmob.v3.AsyncCustomEndpoints;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.listener.CloudCodeListener;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;
import config.DataInfoCache;
import config.ImageLoaderOptions;
import de.hdodenhof.circleimageview.CircleImageView;
import util.LogUtil;
import util.ToastUtil;
import widget.ListViewForScrollView;

/**
 * Created by huang.fan on 2016-3-29.
 */
public class UserInfoActivity extends BaseActivity implements View.OnClickListener {
    @Bind(R.id.tv_title)
    TextView title;

    @Bind(R.id.rl_bar_left)
    RelativeLayout back;

    @Bind(R.id.user_info_follow)
    RelativeLayout followZone;

    @Bind(R.id.user_info_follow_text)
    TextView followText;

    @Bind(R.id.user_info_avatar)
    CircleImageView avatar;

    @Bind(R.id.user_info_name)
    TextView nickname;

    @Bind(R.id.user_info_signature_text)
    TextView signature;

    @Bind(R.id.user_info_post_list)
    ListViewForScrollView mListView;

    @Bind(R.id.user_info_tips)
    TextView loadMore;

    @Bind(R.id.user_info_scrollview)
    ScrollView scrollView;

    @Bind(R.id.user_info_fans_num)
    TextView fansNum;

    @Bind(R.id.user_info_follow_num)
    TextView followNum;

    @Bind(R.id.user_info_progress)
    ProgressBar progressBar;

    @Bind(R.id.user_info_following_zone)
    RelativeLayout seeFollow;

    @Bind(R.id.user_info_fans_zone)
    RelativeLayout seeFans;

    private ArrayList<Post> postList = new ArrayList<Post>();
    private PostListAdapter postListAdapter;

    private static int PAGE_SIZE = 10;
    private int currentPageIndex;

    private User user;

    private boolean isFollowed = false;
    private List<User> followList;

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.activity_user_info;
    }

    @Override
    protected void initViewsAndEvents() {
        currentPageIndex = 0;
        progressBar.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.INVISIBLE);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                //execute the task
                initUserInfo();
                currentPageIndex = 0;
                loadPosts();
                scrollView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
            }
        }, 2000);
        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("user");
        followList = new ArrayList<User>();
        followList = DataInfoCache.loadUserFollowList(this);
        followZone.setVisibility(View.GONE);
        if(!user.getObjectId().equals(application.getCurrentUser().getObjectId())){
            followZone.setVisibility(View.VISIBLE);
            followZone.setClickable(true);
        }
        followZone.setOnClickListener(this);
        //  设置关注状态
        if (followList != null && followList.size() > 0) {
            if (userIsFollow(followList)) {
                updateFollowView(true);
            } else {
                updateFollowView(false);
            }
        }

        title.setText(user.getNickname());
        postList = new ArrayList<Post>();
        postListAdapter = new PostListAdapter(this, postList);
        mListView.setAdapter(postListAdapter);
        back.setOnClickListener(this);
        loadMore.setOnClickListener(this);
        seeFans.setOnClickListener(this);
        seeFollow.setOnClickListener(this);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(UserInfoActivity.this, PostDetailActivity.class);
                intent.putExtra("postId", postList.get(position).getObjectId());
                startActivity(intent);
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_bar_left:
                finish();
                break;
            case R.id.user_info_tips:
                loadPosts();
                break;
            case R.id.user_info_follow:
                if (isFollowed) {
                    cancelFollow(user);
                } else {
                    followOther(user);
                }
                break;
            case R.id.user_info_following_zone:
                Intent followIntent = new Intent(this, UserListActivity.class);
                followIntent.putExtra("user",user);
                followIntent.putExtra("userListType","follow");
                startActivity(followIntent);
                break ;
            case R.id.user_info_fans_zone:
                Intent fansIntent = new Intent(this, UserListActivity.class);
                fansIntent.putExtra("user",user);
                fansIntent.putExtra("userListType","fans");
                startActivity(fansIntent);
                break ;
        }
    }

    /**
     * 加载用户信息
     */
    private void initUserInfo() {
        if (user.getAvatar() != null) {
            ImageLoader.getInstance().displayImage(user.getAvatar().getUrl(),
                    avatar, ImageLoaderOptions.getOptions());
        }
        String name = TextUtils.isEmpty(user.getNickname())
                ? user.getUsername()
                : user.getNickname();
        nickname.setText(name);
        String sign = TextUtils.isEmpty(user.getSign())
                ? "什么都没有留下~"
                : user.getSign();
        signature.setText(sign);
        getFansNum(user);
        getFollowNum(user);
    }

    /**
     * 加载帖子信息
     */
    private void loadPosts() {
        BmobQuery<Post> query = new BmobQuery<Post>();
        query.order("-createdAt");
        query.include("author");
        query.addWhereEqualTo("author", new BmobPointer(user));
        query.setLimit(PAGE_SIZE);
        query.setSkip(PAGE_SIZE * currentPageIndex);
        query.findObjects(this, new FindListener<Post>() {
            @Override
            public void onSuccess(List<Post> object) {
                // 若是起始页，则删除列表
                if (currentPageIndex == 0) {
                    postList.clear();
                    if (object == null || object.size() == 0) {
                        loadMore.setText("暂无发布的内容");
                        loadMore.setClickable(false);
                    }
                }
                if (object != null && object.size() != 0) {
                    currentPageIndex++;
                    postList.addAll(object);
                    postListAdapter.notifyDataSetChanged();
                }

                if (postList.size() < PAGE_SIZE * currentPageIndex) {
                    loadMore.setText("无更多内容");
                    loadMore.setClickable(false);
                } else {
                    loadMore.setText("加载更多");
                    loadMore.setClickable(true);
                }
            }

            @Override
            public void onError(int code, String msg) {
                loadMore.setText("暂无发布的内容");
            }
        });
    }


    /**
     * 用户关注事件
     *
     * @param user
     */
    private void followOther(final User user) {
        final User currentUser = application.getCurrentUser();
        //调用云端逻辑 addfans方法，构建双方的follow和fans关系
        AsyncCustomEndpoints endpoints = new AsyncCustomEndpoints();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("followUserId", user.getObjectId());
            jsonObject.put("fansUserId", currentUser.getObjectId());

            endpoints.callEndpoint(this, "addFans", jsonObject, new CloudCodeListener() {

                @Override
                public void onSuccess(Object arg0) {
                    LogUtil.e("关注成功");
                    ToastUtil.showShort("关注成功");
                    getUserFollowList(application.getCurrentUser());
                    getFansNum(user);
                    updateFollowView(true);
                }

                @Override
                public void onFailure(int i, String msg) {
                    LogUtil.e("关注失败---" + i + "---" + msg);
                    ToastUtil.showShort("关注失败");
                }
            });

        } catch (JSONException e) {
            LogUtil.e("关注失败---" + e);
            ToastUtil.showShort("关注失败");
        }
    }

    /**
     * 取消关注事件
     *
     * @param user
     */
    private void cancelFollow(final User user) {
        final User currentUser = application.getCurrentUser();
        //调用云端逻辑 removefans方法，将双方的follow和fans关系取消
        AsyncCustomEndpoints endpoints = new AsyncCustomEndpoints();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("followUserId", user.getObjectId());
            jsonObject.put("fansUserId", currentUser.getObjectId());

            endpoints.callEndpoint(this, "removeFans", jsonObject, new CloudCodeListener() {

                @Override
                public void onSuccess(Object arg0) {
                    LogUtil.e("取关成功");
                    ToastUtil.showShort("取关成功");
                    getUserFollowList(application.getCurrentUser());
                    getFansNum(user);
                    updateFollowView(false);
                }

                @Override
                public void onFailure(int i, String msg) {
                    LogUtil.e("取关失败---" + i + "---" + msg);
                    ToastUtil.showShort("取关失败");
                }
            });

        } catch (JSONException e) {
            LogUtil.e("取关失败---" + e);
            ToastUtil.showShort("取关失败");
        }
    }

    /**
     * 更新关注按钮的状态
     *
     * @param followed
     */
    private void updateFollowView(boolean followed) {
        if (followed) {
            isFollowed = true;
            followText.setText("已关注");
            followText.setTextColor(getResources().getColor(R.color.content_back));
            followText.setBackgroundResource(R.drawable.following_btn_back);
        } else {
            isFollowed = false;
            followText.setText("关注");
            followText.setTextColor(getResources().getColor(R.color.pretty_green));
            followText.setBackgroundResource(R.drawable.unfollow_btn_back);
        }
    }


    /**
     * 当前用户是否关注此用户
     *
     * @return
     */
    private boolean userIsFollow(List<User> list) {
        boolean isFollow = false;
        for (int i = 0; i < list.size(); i++) {
            if (user.getObjectId().equals(list.get(i).getObjectId())) {
                isFollow = true;
                break;
            }
        }
        return isFollow;
    }

    /**
     * 获取用户关注列表
     *
     * @param user
     */
    private void getUserFollowList(User user) {
        BmobQuery<User> query = new BmobQuery<User>();
        query.addWhereRelatedTo("follow", new BmobPointer(user));
        query.findObjects(this, new FindListener<User>() {

            @Override
            public void onSuccess(List<User> users) {
                DataInfoCache.saveUserFollowList(UserInfoActivity.this, (ArrayList) users);
            }

            @Override
            public void onError(int i, String s) {
                LogUtil.e("获取用户关注列表失败--" + i + s);
            }
        });
    }

    /**
     * 获取用户关注数
     *
     * @param user
     */
    private void getFollowNum(User user) {
        BmobQuery<User> query = new BmobQuery<User>();
        query.addWhereRelatedTo("follow", new BmobPointer(user)); // 条件：查询当前用户关注的人
        query.count(this, User.class, new CountListener() {

            @Override
            public void onSuccess(int count) {
                //设置关注数为count
                followNum.setText("" + count);
            }

            @Override
            public void onFailure(int i, String msg) {
                //获取关注数失败
                LogUtil.e("获取关注数失败---" + i + msg);
            }
        });
    }

    /**
     * 获取用户粉丝 数
     *
     * @param user
     */
    private void getFansNum(User user) {
        BmobQuery<User> query = new BmobQuery<User>();
        query.addWhereRelatedTo("fans", new BmobPointer(user)); // 条件：查询当前用户粉丝
        query.count(this, User.class, new CountListener() {

            @Override
            public void onSuccess(int count) {
                //设置粉丝数为count
                fansNum.setText("" + count);
            }

            @Override
            public void onFailure(int i, String msg) {
                //获取粉丝数失败
                LogUtil.e("获取粉丝数失败---" + i + msg);
            }
        });
    }
}

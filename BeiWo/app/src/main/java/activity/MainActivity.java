package activity;

import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huangfan.beiwo.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import adapter.PostListAdapter;
import app.AppManager;
import bean.Post;
import bean.User;
import butterknife.Bind;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.listener.FindListener;
import config.Constant;
import config.DataInfoCache;
import config.ImageLoaderOptions;
import de.hdodenhof.circleimageview.CircleImageView;
import util.LogUtil;
import util.ToastUtil;
import widget.RefreshLayout;

/**
 * Created by huang.fan on 2016-3-17.
 * 主页面Activity
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {

    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @Bind(R.id.left_drawer)
    RelativeLayout leftDrawer;

    @Bind(R.id.left_menu_avatar)
    CircleImageView mAvatar;

    @Bind(R.id.left_menu_name)
    TextView nickname;

    @Bind(R.id.left_menu_user_zone)
    LinearLayout userZone;

    @Bind(R.id.left_menu_setting)
    TextView setting;

    @Bind(R.id.rl_bar_left)
    RelativeLayout leftBar;

    @Bind(R.id.iv_bar_left)
    ImageView leftIcon;

    @Bind(R.id.rl_bar_right)
    RelativeLayout rightBar;

    @Bind(R.id.home_listView)
    ListView listView;

    @Bind(R.id.swiperefreshlayout)
    RefreshLayout refreshLayout;

    @Bind(R.id.common_title_bar)
    RelativeLayout titleBar;

    @Bind(R.id.main_progress)
    ProgressBar mainProgress;

    private PostListAdapter adapter;

    private List<Post> mlist;

    private int currentPageIndex;

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViewsAndEvents() {
        initLeftDrawer();
        getUserFollowList(application.getCurrentUser());
        currentPageIndex = 0;
        mainProgress.setVisibility(View.VISIBLE);
        refreshLayout.postDelayed(new Runnable() {

            @Override
            public void run() {
                //loaddata
                currentPageIndex = 0;
                loadPost();
                //关闭动画
                mainProgress.setVisibility(View.INVISIBLE);
                refreshLayout.setRefreshing(false);
            }
        }, 1000);
        leftIcon.setImageResource(R.mipmap.icon_menu);
        titleBar.setOnClickListener(this);
        leftBar.setOnClickListener(this);
        rightBar.setVisibility(View.VISIBLE);
        rightBar.setOnClickListener(this);
        setting.setOnClickListener(this);
        userZone.setOnClickListener(this);
        mlist = new ArrayList<Post>();
        adapter = new PostListAdapter(this, mlist);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this,PostDetailActivity.class);
                intent.putExtra("postId",mlist.get(position).getObjectId());
                startActivity(intent);
            }
        });
        refreshLayout.setColorSchemeResources(R.color.pretty_green);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                refreshLayout.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        //loaddata
                        currentPageIndex = 0;
                        loadPost();
                        //关闭动画
                        refreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });

        //上拉加载
        refreshLayout.setOnLoadListener(new RefreshLayout.OnLoadListener() {

            @Override
            public void onLoad() {
                refreshLayout.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        //loaddata
                        loadPost();
                        refreshLayout.setLoading(false);
                    }
                }, 1000);
            }
        });
    }

    private void initLeftDrawer() {
        User user = application.getCurrentUser();
        if (user.getAvatar() != null) {
            ImageLoader.getInstance().displayImage(user.getAvatar().getUrl(),
                    mAvatar, ImageLoaderOptions.getOptions());
        }
        if (user.getNickname() == null) {
            nickname.setText("无名氏");
        }
        nickname.setText(user.getNickname());
    }

    @Override
    protected void onResume() {
        super.onResume();
        initLeftDrawer();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_bar_left:
                if (!mDrawerLayout.isDrawerOpen(leftDrawer)) {
                    mDrawerLayout.openDrawer(leftDrawer);
                }
                break;
            case R.id.left_menu_setting:
                Intent intent1 = new Intent(this, SettingActivity.class);
                startActivity(intent1);
                break;
            case R.id.rl_bar_right:
                Intent intent2 = new Intent(this, PublishPostActivity.class);
                startActivity(intent2);
                break;
            case R.id.common_title_bar:
                //单击titlebar返回顶部
                if (!listView.isStackFromBottom()) {
                    listView.setStackFromBottom(true);
                }
                listView.setStackFromBottom(false);
                break;
            case R.id.left_menu_user_zone:
                Intent intent = new Intent();
                intent.putExtra("user", application.getCurrentUser());
                intent.setClass(this, UserInfoActivity.class);
                startActivity(intent);
        }
    }


    /**
     * 分页加载数据
     */
    private void loadPost() {
        BmobQuery<Post> query = new BmobQuery<Post>();
        query.order("-createdAt");
        query.include("author");// 希望在查询帖子信息的同时也把发布人的信息查询出来
        query.setLimit(Constant.PAGE_SIZE);
        query.setSkip(Constant.PAGE_SIZE * currentPageIndex);
        query.findObjects(this, new FindListener<Post>() {
            @Override
            public void onSuccess(List<Post> object) {
                // 若是起始页，则删除列表
                if (currentPageIndex == 0) {
                    mlist.clear();
                    if (object == null || object.size() == 0) {
                        //tipsView.setText("还没帖子，赶紧发布吧！");
                        ToastUtil.showShort("还没帖子，赶紧发布吧！");
                    } else {
                        //tipsView.setVisibility(View.GONE);
                    }
                }
                if (object != null && object.size() != 0) {
                    currentPageIndex++;
                    mlist.addAll(object);
                    adapter.setList(mlist);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(int code, String msg) {
                //ToastUtil.showShort("查询失败！" + msg);
                ToastUtil.showShort("还没帖子，赶紧发布吧！");
                //tipsView.setText("还没帖子，赶紧发布吧！");
            }
        });
    }

    private static long firstTime;

    /**
     * 连续按两次返回键就退出
     */
    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(leftDrawer)) {
            mDrawerLayout.closeDrawer(leftDrawer);
            return;
        }
        if (firstTime + 2000 > System.currentTimeMillis()) {
            AppManager.getAppManager().finishAllActivity();
            super.onBackPressed();
        } else {
            ToastUtil.showShort("再按一次退出程序");
        }
        firstTime = System.currentTimeMillis();
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
                DataInfoCache.saveUserFollowList(MainActivity.this, (ArrayList) users);
            }

            @Override
            public void onError(int i, String s) {
                LogUtil.e("获取用户关注列表失败--" + i + s);
            }
        });
    }
}

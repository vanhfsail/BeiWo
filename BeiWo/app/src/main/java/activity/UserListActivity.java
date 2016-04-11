package activity;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huangfan.beiwo.R;

import java.util.ArrayList;
import java.util.List;

import adapter.UserListAdapter;
import bean.User;
import butterknife.Bind;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.listener.FindListener;
import config.Constant;
import util.ToastUtil;
import widget.RefreshLayout;

/**
 * Created by huang.fan on 2016-3-31.
 */
public class UserListActivity extends BaseActivity implements View.OnClickListener {
    @Bind(R.id.tv_title)
    TextView title;

    @Bind(R.id.rl_bar_left)
    RelativeLayout back;

    @Bind(R.id.user_list_swiperefreshlayout)
    RefreshLayout refreshLayout;

    @Bind(R.id.user_listView)
    ListView userListView;

    @Bind(R.id.user_list_progress)
    ProgressBar progressBar;

    private List<User> userList;

    private UserListAdapter userListAdapter;

    private int currentPageIndex;
    
    private User user;

    private String userListType;

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.activity_user_list;
    }

    @Override
    protected void initViewsAndEvents() {
        Intent intent = getIntent() ;
        user = (User) intent.getSerializableExtra("user");
        userListType = intent.getStringExtra("userListType");
        currentPageIndex = 0;
        progressBar.setVisibility(View.VISIBLE);
        refreshLayout.postDelayed(new Runnable() {

            @Override
            public void run() {
                //loaddata
                currentPageIndex = 0;
                if(userListType.equals("fans")){
                    getUserFansList();
                    title.setText("粉丝列表");
                }else if(userListType.equals("follow")){
                    getUserFollowList();
                    title.setText("关注列表");
                }
                //关闭动画
                progressBar.setVisibility(View.INVISIBLE);
                refreshLayout.setRefreshing(false);
            }
        }, 1000);

        back.setOnClickListener(this);
        userList = new ArrayList<User>();
        userListAdapter = new UserListAdapter(this, userList);
        userListView.setAdapter(userListAdapter);
        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //
                Intent intent = new Intent() ;
                User user = userList.get(position) ;
                    intent.putExtra("user", user);
                    intent.setClass(UserListActivity.this, UserInfoActivity.class);
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
                        if(userListType.equals("fans")){
                            getUserFansList();
                        }else if(userListType.equals("follow")){
                            getUserFollowList();
                        }
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
                        if(userListType.equals("fans")){
                            getUserFansList();
                        }else if(userListType.equals("follow")){
                            getUserFollowList();
                        }
                        refreshLayout.setLoading(false);
                    }
                }, 1000);
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_bar_left:
                finish();
                break;
        }
    }

    /**
     * 获取用户粉丝列表
     */
    public void getUserFansList(){
        BmobQuery<User> query = new BmobQuery<User>() ;
        query.order("-createdAt") ;
        query.setLimit(Constant.PAGE_SIZE);
        query.setSkip(Constant.PAGE_SIZE*currentPageIndex);
        query.addWhereRelatedTo("fans", new BmobPointer(user));
        query.findObjects(this, new FindListener<User>() {

            @Override
            public void onSuccess(List<User> users) {
                // 若是起始页，则删除列表
                if (currentPageIndex == 0) {
                    userList.clear();
                    if (users == null || users.size() == 0) {
                        ToastUtil.showShort("还没有粉丝哦！");
                    } else {
                        //tipsView.setVisibility(View.GONE);
                    }
                }
                if (users != null && users.size() != 0) {
                    currentPageIndex++;
                    userList.addAll(users);
                    userListAdapter.setList(userList);
                    userListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(int code, String msg) {
                ToastUtil.showShort("还没有粉丝哦！");
            }
        });
    }

    /**
     * 获取用户关注列表
     */
    public void getUserFollowList(){
        BmobQuery<User> query = new BmobQuery<User>() ;
        query.order("-createdAt") ;
        query.setLimit(Constant.PAGE_SIZE);
        query.setSkip(Constant.PAGE_SIZE*currentPageIndex);
        query.addWhereRelatedTo("follow", new BmobPointer(user));
        query.findObjects(this, new FindListener<User>() {

            @Override
            public void onSuccess(List<User> users) {
                // 若是起始页，则删除列表
                if (currentPageIndex == 0) {
                    userList.clear();
                    if (users == null || users.size() == 0) {
                        ToastUtil.showShort("还没有关注别人哦！");
                    } else {
                        //tipsView.setVisibility(View.GONE);
                    }
                }
                if (users != null && users.size() != 0) {
                    currentPageIndex++;
                    userList.addAll(users);
                    userListAdapter.setList(userList);
                    userListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(int code, String msg) {
                ToastUtil.showShort("还没有关注别人哦！");
            }
        });
    }

}

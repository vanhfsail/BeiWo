package activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.huangfan.beiwo.R;

import bean.User;

/**
 * Created by huang.fan on 2016-3-24.
 */
public class SplashActivity extends BaseActivity implements View.OnClickListener {

    private static final int GO_HOME = 100;
    private static final int GO_LOGIN = 200;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GO_HOME:
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    break;
                case GO_LOGIN:
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    break;
            }
        }
    };
    @Override
    protected int getContentViewLayoutId() {
        return R.layout.activity_splash;
    }

    @Override
    protected void initViewsAndEvents() {
        // 自动登录
        User user = application.getCurrentUser();
        if (user != null) {
            mHandler.sendEmptyMessageDelayed(GO_HOME, 1000);
        } else {
            mHandler.sendEmptyMessageDelayed(GO_LOGIN, 1000);
        }
    }

    @Override
    public void onClick(View v) {

    }
}

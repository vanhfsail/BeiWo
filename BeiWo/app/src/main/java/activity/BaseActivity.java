package activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import app.AppManager;
import app.BeiWoApplication;
import dialog.LoadingDialog;
import util.LogUtil;

import butterknife.ButterKnife;

/**
 * Created by huang.fan on 2016-3-17.
 * Activity基类
 */
public abstract class BaseActivity extends FragmentActivity {

    public static String TAG = null;

    protected BeiWoApplication application;
    protected LoadingDialog loadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = BeiWoApplication.getInstance();
        loadingDialog = new LoadingDialog(this);

        // 添加Activity到堆栈
        AppManager.getAppManager().addActivity(this);
        TAG = this.getClass().getSimpleName();

        if (getContentViewLayoutId() != 0) {
            setContentView(getContentViewLayoutId());
        } else {
            throw new IllegalArgumentException("You must return a right contentView layout resource Id");
        }
        //添加注解
        ButterKnife.bind(this);
        initViewsAndEvents();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.d("onPause---------->>");

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        LogUtil.d("onRestart---------->>");
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.d("onResume---------->>");
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtil.d("onStart---------->>");
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtil.d("onStop---------->>");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 结束Activity&从堆栈中移除
        AppManager.getAppManager().finishActivity(this);
        //解除注解
        ButterKnife.unbind(this);
        LogUtil.d("onDestroy---------->>");
    }

    /**
     * 隐藏软键盘
     */
    public void hideSoftInputView() {
        InputMethodManager manager = ((InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE));
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /***
     * 绑定资源文件ID
     *
     * @return
     */
    protected abstract int getContentViewLayoutId();

    /***
     * 绑定视图组件
     */
    protected abstract void initViewsAndEvents();


}


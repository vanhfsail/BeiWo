package activity;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huangfan.beiwo.R;

import bean.User;
import butterknife.Bind;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.LogInListener;
import util.EncryptUtil;
import util.ToastUtil;

/**
 * Created by huang.fan on 2016-3-18.
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private static final int REQUEST_CODE_REGIST = 0;

    @Bind(R.id.tv_title)
    TextView title;

    @Bind(R.id.rl_bar_left)
    RelativeLayout back;

    @Bind(R.id.tv_gofor_register)
    TextView goforRegister;

    @Bind(R.id.et_login_phone)
    EditText phoneText;

    @Bind(R.id.et_login_password)
    EditText passwordText;

    @Bind(R.id.btn_login)
    Button loginButton;

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void initViewsAndEvents() {
        title.setText("登录");
        goforRegister.setOnClickListener(this);
        loginButton.setOnClickListener(this);
        back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_bar_left:
                finish();
                break;
            case R.id.tv_gofor_register:
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_login:
                String phone = phoneText.getText().toString().trim();
                String password = passwordText.getText().toString().trim();
                if (!TextUtils.isEmpty(phone) && !TextUtils.isEmpty(password)) {
                    // 加密
                    String SHA_password = EncryptUtil.SHA1(password.trim()) ;
                    login(phone, SHA_password);
                } else {
                    ToastUtil.showShort("请填写手机号或密码！");
                }
                break;
        }
    }

    /**
     * 登录
     * @param phone
     * @param password
     */
    private void login(String phone,String password){
        hideSoftInputView();
        loginButton.setEnabled(false);
        loginButton.setText("登录中...");
        BmobUser.loginByAccount(this, phone, password, new LogInListener<User>() {
            @Override
            public void done(User user, BmobException e) {
                if (user != null) {
                    // 检查异地登录，并更新登录信息
                    //PushUtil.notifyOffsite(LoginActivity.this, user.getObjectId());
                    // 跳转
                    //openActivityAndClose(new Intent(LoginActivity.this, MainActivity.class));
                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    startActivity(intent);
                } else {
                    ToastUtil.showShort("账号或密码错误！");
                    loginButton.setEnabled(true);
                    loginButton.setText("登录");
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 注册成功回调
        if (requestCode == REQUEST_CODE_REGIST && resultCode == RESULT_OK) {
            phoneText.setText(data.getStringExtra("username"));
            passwordText.requestFocus();
        }
    }
}

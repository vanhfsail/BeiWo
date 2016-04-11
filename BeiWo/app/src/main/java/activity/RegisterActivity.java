package activity;

import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huangfan.beiwo.R;

import bean.User;
import butterknife.Bind;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.SaveListener;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import config.Constant;
import util.EncryptUtil;
import util.ToastUtil;

import static com.mob.tools.utils.R.getStringRes;

/**
 * Created by huang.fan on 2016-3-18.
 */
public class RegisterActivity extends BaseActivity implements View.OnClickListener {
    @Bind(R.id.tv_title)
    TextView title;

    @Bind(R.id.rl_bar_left)
    RelativeLayout back;

    @Bind(R.id.et_register_phone)
    EditText phoneNum;

    @Bind(R.id.et_verification_code)
    EditText code;

    @Bind(R.id.et_register_password)
    EditText password;

    @Bind(R.id.btn_count_down_timer)
    Button timer;

    @Bind(R.id.btn_onekey_register)
    Button registerBtn;

    @Bind(R.id.read_user_agreement)
    TextView readUserAgreement;

    @Bind(R.id.cb_accept_agreement)
    CheckBox acceptAgreement;
    private String mPhone, mCode;
    private boolean flag = true;
    private TimeCount time;

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.activity_register;
    }

    @Override
    protected void initViewsAndEvents() {
        initSMS();
        time = new TimeCount(60000, 1000);//构造CountDownTimer对象
        title.setText("手机号注册");
        back.setOnClickListener(this);
        registerBtn.setOnClickListener(this);
        registerBtn.setEnabled(false);
        timer.setOnClickListener(this);
        readUserAgreement.setOnClickListener(this);
        acceptAgreement.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    registerBtn.setEnabled(true);
                }else{
                    registerBtn.setEnabled(false);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_bar_left:
                finish();
                break;
            case R.id.btn_count_down_timer:
                if (!TextUtils.isEmpty(phoneNum.getText().toString().trim())) {
                    if (phoneNum.getText().toString().trim().length() == 11) {
                        if (isMobileNO(phoneNum.getText().toString().trim())) {
                            mPhone = phoneNum.getText().toString().trim();
                            SMSSDK.getVerificationCode("86", mPhone);
                            time.start();// 开始计时
                            code.requestFocus();
                        } else {
                            ToastUtil.showShort("请输入正确的电话号码");
                            phoneNum.requestFocus();
                        }
                    } else {
                        ToastUtil.showShort("请输入完整电话号码");
                        phoneNum.requestFocus();
                    }
                } else {
                    ToastUtil.showShort("请输入您的电话号码");
                    phoneNum.requestFocus();
                }
                break;
            case R.id.btn_onekey_register:
                if (!TextUtils.isEmpty(code.getText().toString().trim())) {
                    if (code.getText().toString().trim().length() == 4) {
                        mCode = code.getText().toString().trim();
                        SMSSDK.submitVerificationCode("86", mPhone, mCode);
                        flag = false;
                    } else {
                        ToastUtil.showShort("请输入完整验证码");
                        code.requestFocus();
                    }
                } else {
                    ToastUtil.showShort("请输入验证码");
                    code.requestFocus();
                }
                break;
            case R.id.read_user_agreement:
                Intent intent = new Intent(this,UserAgreementActivity.class);
                startActivity(intent);

        }
    }

    private void initSMS() {
        SMSSDK.initSDK(this, Constant.MOB_SMS_APP_KEY, Constant.MOB_SMS_APP_SECRET);

        EventHandler eh = new EventHandler() {

            @Override
            public void afterEvent(int event, int result, Object data) {
                Message msg = new Message();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                handler.sendMessage(msg);
            }
        };
        SMSSDK.registerEventHandler(eh); //注册短信回调
    }


    public Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            int event = msg.arg1;
            int result = msg.arg2;
            Object data = msg.obj;
            Log.e("event", "event=" + event);
            if (result == SMSSDK.RESULT_COMPLETE) {
                //短信注册成功后，返回MainActivity,然后提示新好友
                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {//提交验证码成功,验证通过
                    ToastUtil.showShort("验证码校验成功");
                    if(password.getText().toString().trim().isEmpty()){
                        ToastUtil.showShort("密码不能为空");
                    }else{
                        // 加密
                        String SHA_password = EncryptUtil.SHA1(password.getText().toString().trim()) ;
                        signUp(phoneNum.getText().toString().trim(),SHA_password);
                    }
                } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {//服务器验证码发送成功
                    ToastUtil.showShort("验证码已经发送");
                } else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {//返回支持发送验证码的国家列表
                    ToastUtil.showShort("获取国家列表成功");
                }
            } else {
                if (flag) {
                    ToastUtil.showShort("验证码获取失败，请重新获取");
                    phoneNum.requestFocus();
                    time.onFinish();// 结束计时
                } else {
                    ((Throwable) data).printStackTrace();
                    int resId = getStringRes(RegisterActivity.this, "smssdk_network_error");
                    ToastUtil.showShort("验证码错误");
                    code.selectAll();
                    if (resId > 0) {
                        ToastUtil.showIntShort(resId);
                    }
                }

            }

        }

    };

    /**
     * 注册方法
     * @param phoneNum
     * @param password
     */
    public void signUp(final String phoneNum,String password){
        hideSoftInputView();
        loadingDialog.show();
        User user = new User();
        user.setUsername(phoneNum);
        user.setPassword(password);
        user.setMobilePhoneNumber(phoneNum);
        user.setMobilePhoneNumberVerified(true);
        user.setSex(0);
        user.setAge(0);
        user.signUp(this, new SaveListener() {
            @Override
            public void onSuccess() {
                loadingDialog.dismiss();
                ToastUtil.showShort("注册成功！");
                // 缓存清空
                BmobUser.logOut(RegisterActivity.this);
                // 返回登录
                Intent intent = new Intent();
                intent.putExtra("username", phoneNum);
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void onFailure(int i, String s) {
                loadingDialog.dismiss();
                if(s.contains("already taken"))
                    ToastUtil.showShort("手机号已被注册");
                else
                    ToastUtil.showShort(s);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loadingDialog.dismiss();
        SMSSDK.unregisterAllEventHandler();
    }

    /**
     * 验证手机格式
     */
    public static boolean isMobileNO(String mobiles) {
    /*
    移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
    联通：130、131、132、152、155、156、185、186
    电信：133、153、180、189、（1349卫通）
    总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
    */
        String telRegex = "[1][358]\\d{9}";//"[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        if (TextUtils.isEmpty(mobiles))
            return false;
        else
            return mobiles.matches(telRegex);
    }
    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {// 计时完毕
            timer.setText("发送验证码");
            timer.setClickable(true);
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程
            timer.setClickable(false);//防止重复点击
            timer.setText(millisUntilFinished / 1000 + "s");
        }
    }
}
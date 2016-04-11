package activity;

import android.content.res.AssetManager;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huangfan.beiwo.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import butterknife.Bind;

/**
 * Created by huang.fan on 2016-3-22.
 */
public class UserAgreementActivity extends BaseActivity implements View.OnClickListener {
    @Bind(R.id.agreement_content)
    TextView userAgreement;

    @Bind(R.id.tv_title)
    TextView title;

    @Bind(R.id.rl_bar_left)
    RelativeLayout back;

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.activity_user_agreement;
    }

    @Override
    protected void initViewsAndEvents() {
        title.setText("用户协议");
        back.setOnClickListener(this);
        try {
            userAgreement.setText(getAgreement("agreement.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rl_bar_left:
                finish();
                break;
        }
    }

    public String getAgreement(String fileName) throws IOException {
        AssetManager assetManager = getAssets();
        InputStream inputStream = null;
        inputStream = assetManager.open(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bytes = new byte[4096];
        int len = 0;
        while ((len = inputStream.read(bytes)) > 0) {
            byteArrayOutputStream.write(bytes, 0, len);
        }
        return new String(byteArrayOutputStream.toByteArray(), "UTF8");
    }
}

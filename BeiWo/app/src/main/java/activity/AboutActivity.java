package activity;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huangfan.beiwo.R;

import butterknife.Bind;

/**
 * Created by huang.fan on 2016-4-11.
 */
public class AboutActivity extends BaseActivity implements View.OnClickListener {
    @Bind(R.id.tv_title)
    TextView title;

    @Bind(R.id.rl_bar_left)
    RelativeLayout back;

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.activity_about;
    }

    @Override
    protected void initViewsAndEvents() {
        title.setText("关于");
        back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rl_bar_left:
                finish();
                break;
        }
    }
}

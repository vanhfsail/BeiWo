package dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import com.huangfan.beiwo.R;

/**
 * Created by huang.fan on 2016-3-23.
 */
public class LoadingDialog extends Dialog{
    public LoadingDialog(Context context) {
        super(context, R.style.NormalDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
    }
}

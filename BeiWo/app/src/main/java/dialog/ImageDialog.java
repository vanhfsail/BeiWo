package dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.bm.library.PhotoView;
import com.huangfan.beiwo.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import config.ImageLoaderOptions;

/**
 * Created by huang.fan on 2016-3-29.
 */
public class ImageDialog extends Dialog {

    private Context context;
    private PhotoView photoView;
    private String imageSrc;

    public ImageDialog(Context context, String imageSrc) {
        super(context, R.style.NormalDialog);
        this.context = context;
        this.imageSrc = imageSrc;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_image);
        setCancelable(true);
        setCanceledOnTouchOutside(true);

        Window window = getWindow();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.width = width;
        params.height = height;
        params.alpha = 1f;
        window.setAttributes(params);
        window.setBackgroundDrawableResource(R.color.black);
        window.setWindowAnimations(R.style.dialogWindowAnim);

        photoView = (PhotoView) findViewById(R.id.photoView);
        // 启用缩放功能
        photoView.enable();
        ImageLoader.getInstance().displayImage(imageSrc, photoView, ImageLoaderOptions.getOptions());
        // 单击关闭
        photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
    }

}

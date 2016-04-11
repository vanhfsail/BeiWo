package adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.huangfan.beiwo.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import bean.Comment;
import config.ImageLoaderOptions;
import de.hdodenhof.circleimageview.CircleImageView;
import dialog.ImageDialog;
import util.TimeUtil;
import util.ViewHolder;

/**
 * Created by huang.fan on 2016-3-28.
 */
public class CommentListAdapter extends BaseAdapter{

    private Context context;
    private List<Comment> mlist;
    private LayoutInflater inflater;

    public CommentListAdapter(Context context, List<Comment> mlist) {
        super();
        this.context = context;
        this.mlist = mlist;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mlist.size() == 0 ? 0 : mlist.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView = inflater.inflate(R.layout.list_item_comment,null);
        }
        CircleImageView avatar = ViewHolder.get(convertView,R.id.comment_avatar);
        TextView nickname = ViewHolder.get(convertView,R.id.comment_nickname);
        TextView time = ViewHolder.get(convertView,R.id.comment_time);
        TextView content = ViewHolder.get(convertView,R.id.comment_content);
        ImageView contentImage = ViewHolder.get(convertView,R.id.comment_image);

        if(mlist.get(position).getUser().getAvatar()!=null){
            ImageLoader.getInstance().displayImage(mlist.get(position).getUser().getAvatar().getUrl(),
                    avatar, ImageLoaderOptions.getOptions());
        } else {
            avatar.setImageResource(R.mipmap.user_logo_default);
        }
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到个人主页。。。
            }
        });
        String name = TextUtils.isEmpty(mlist.get(position).getUser().getNickname())
                ? mlist.get(position).getUser().getUsername()
                : mlist.get(position).getUser().getNickname();
        nickname.setText(name);

        String createAt = mlist.get(position).getCreatedAt();
        time.setText(TimeUtil.getMessageTime(TimeUtil.stringToLong(createAt, TimeUtil.FORMAT_DATE_TIME_SECOND)));
        if (!TextUtils.isEmpty(mlist.get(position).getContent())) {
            content.setVisibility(View.VISIBLE);
            content.setText(mlist.get(position).getContent());
        } else {
            content.setVisibility(View.GONE);
        }

        if (mlist.get(position).getImage() != null) {
            contentImage.setVisibility(View.VISIBLE);
            ImageLoader.getInstance().displayImage(mlist.get(position).getImage().getUrl(),
                    contentImage, ImageLoaderOptions.getOptions());
            contentImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageDialog imageDialog = new ImageDialog(context, mlist.get(position).getImage().getUrl());
                    imageDialog.show();
                }
            });
        } else {
            contentImage.setVisibility(View.GONE);
        }

        return convertView;
    }
}

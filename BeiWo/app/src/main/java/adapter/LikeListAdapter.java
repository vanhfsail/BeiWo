package adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.huangfan.beiwo.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import bean.User;
import config.ImageLoaderOptions;
import de.hdodenhof.circleimageview.CircleImageView;
import util.ViewHolder;

/**
 * Created by huang.fan on 2016-3-28.
 */
public class LikeListAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;
    private List<User> mlist;

    public LikeListAdapter(Context context, List<User> mlist) {
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
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView = inflater.inflate(R.layout.list_item_like,null);
        }
        CircleImageView avatar = ViewHolder.get(convertView,R.id.like_avatar);
        TextView nickname = ViewHolder.get(convertView,R.id.like_nickname);

        if(mlist.get(position).getAvatar()!=null){
            ImageLoader.getInstance().displayImage(mlist.get(position).getAvatar().getUrl(),
                    avatar, ImageLoaderOptions.getOptions());
        } else {
            avatar.setImageResource(R.mipmap.user_logo_default);
        }
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到个人中心
            }
        });
        String name = TextUtils.isEmpty(mlist.get(position).getNickname())
                ? mlist.get(position).getUsername()
                : mlist.get(position).getNickname();
        nickname.setText(name);

        return convertView;
    }
}

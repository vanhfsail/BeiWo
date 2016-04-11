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
 * Created by huang.fan on 2016-3-31.
 */
public class UserListAdapter extends BaseAdapter{
    private Context mContext;
    private List<User> mlist;
    private LayoutInflater inflater;

    public UserListAdapter(Context context, List<User> list) {
        super();
        this.mContext = context;
        this.mlist = list;
        inflater = LayoutInflater.from(context);
    }

    public void setList(List<User> list) {
        this.mlist = list;
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
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_user, null);
        }
        CircleImageView avatar = ViewHolder.get(convertView,R.id.user_item_avatar);
        TextView nickname = ViewHolder.get(convertView,R.id.user_item_nickname);
        TextView signature = ViewHolder.get(convertView,R.id.user_item_signature);
        User user = mlist.get(position);
        if (user.getAvatar() != null) {
            ImageLoader.getInstance().displayImage(user.getAvatar().getUrl(),
                    avatar, ImageLoaderOptions.getOptions());
        } else {
            avatar.setImageResource(R.mipmap.user_logo_default);
        }

        String name = TextUtils.isEmpty(user.getNickname())
                ? user.getUsername()
                : user.getNickname();
        nickname.setText(name);

        String sign = TextUtils.isEmpty(user.getSign())
                ? "什么都没有留下~"
                : user.getSign();
        signature.setText(sign);

        return convertView;
    }
}

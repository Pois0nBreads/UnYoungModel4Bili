package net.pois0nbread.unyoungmodel4bili.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.pois0nbread.unyoungmodel4bili.R;

import java.util.List;

/**
 * <pre>
 *     author : Pois0nBread
 *     e-mail : pois0nbreads@gmail.com
 *     time   : 2019/12/02
 *     desc   : AppAdapter
 *     version: 1.0
 * </pre>
 */

public class AppAdapter extends BaseAdapter {

    private List<AppInfo> mData;
    private Context mContext;

    public AppAdapter(Context context, List<AppInfo> data) {
        this.mData = data;
        this.mContext = context;
    }

    public void changeData(List<AppInfo> appInfos) {
        this.mData = appInfos;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //返回带数据当前行的Item视图对象
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int index = position;
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_apps, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.apprl = convertView.findViewById(R.id.apps_layout);
            viewHolder.appicon = convertView.findViewById(R.id.apps_icon);
            viewHolder.appname = convertView.findViewById(R.id.apps_name);
            viewHolder.appinfo = convertView.findViewById(R.id.apps_info);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.appicon.setImageDrawable(mData.get(index).getIcon());
        viewHolder.appname.setText(mData.get(index).getAppName());
        viewHolder.appinfo.setText(mData.get(index).getPackageName());
        return convertView;
    }

    private class ViewHolder {
        RelativeLayout apprl;
        ImageView appicon;
        TextView appname;
        TextView appinfo;
    }
}

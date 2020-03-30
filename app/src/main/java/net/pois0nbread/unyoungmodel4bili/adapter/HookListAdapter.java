package net.pois0nbread.unyoungmodel4bili.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *     author : Pois0nBread
 *     e-mail : pois0nbreads@gmail.com
 *     time   : 2030/03/30
 *     desc   : HookListAdapter
 *     version: 1.0
 * </pre>
 */

public class HookListAdapter extends BaseAdapter {

    private List<JSONObject> mData;
    private Context mContext;
    private boolean isNullData = false;
    private List<JSONObject> nullDataUserInfo = new ArrayList<>();

    {
        nullDataUserInfo.add(null);
    }

    public HookListAdapter(Context context, List<JSONObject> list) {
        if (list == null) {
            isNullData = true;
            this.mData = nullDataUserInfo;
        } else if (list.size() == 0) {
            isNullData = true;
            this.mData = nullDataUserInfo;
        } else {
            isNullData = false;
            this.mData = list;
        }
        this.mContext = context;
    }

    public void changeData(List<JSONObject> list) {
        if (list == null) {
            isNullData = true;
            this.mData = nullDataUserInfo;
        } else if (list.size() == 0) {
            isNullData = true;
            this.mData = nullDataUserInfo;
        } else {
            isNullData = false;
            this.mData = list;
        }
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (isNullData) {
            convertView = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1, parent, false);
            ((TextView) convertView.findViewById(android.R.id.text1)).setText("没有数据请添加");
            return convertView;
        }

        final  int index = position;
        final JSONObject mJsonObject = mData.get(index);
        TextView textView;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1, parent, false);
            textView = convertView.findViewById(android.R.id.text1);
            convertView.setTag(textView);
        } else {
            textView = (TextView) convertView.getTag();
        }
        try {
            String AppName = mJsonObject.getString("ApplicationName");
            String PackageName = mJsonObject.getString("PackageName");
            textView.setText("应用名称：" + AppName + "\n包名：" + PackageName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return convertView;
    }
}

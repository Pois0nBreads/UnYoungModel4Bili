package net.pois0nbread.unyoungmodel4bili.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <pre>
 *     author : Pois0nBread
 *     e-mail : pois0nbreads@gmail.com
 *     time   : 2020/03/18
 *     desc   : UserInfoAdapter
 *     version: 1.0
 * </pre>
 */

public class UserInfoAdapter extends BaseAdapter {

    private List<UserInfo> mData;
    private Context mContext;
    private boolean isNullData = false;
    private List<UserInfo> nullDataUserInfo = new ArrayList<>();

    {
        nullDataUserInfo.add(new UserInfo());
    }

    public UserInfoAdapter(Context context, List<UserInfo> userInfos) {
        if (userInfos == null) {
            isNullData = true;
            this.mData = nullDataUserInfo;
        } else if (userInfos.size() == 0) {
            isNullData = true;
            this.mData = nullDataUserInfo;
        } else {
            isNullData = false;
            this.mData = userInfos;
        }
        this.mContext = context;
    }

    public void changeData(List<UserInfo> userInfos) {
        if (userInfos == null) {
            isNullData = true;
            this.mData = nullDataUserInfo;
        } else if (userInfos.size() == 0) {
            isNullData = true;
            this.mData = nullDataUserInfo;
        } else {
            isNullData = false;
            this.mData = userInfos;
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

    //返回带数据当前行的Item视图对象
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (isNullData) {
            convertView = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1, parent, false);
            ((TextView) convertView.findViewById(android.R.id.text1)).setText("没有最近登录的数据");
            return convertView;
        }

        final int index = position;
        final  UserInfo userInfo = mData.get(index);
        final Long l = Long.parseLong(new SimpleDateFormat("yyyyMMdd").format(new Date()));
        TextView textView;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1, parent, false);
            textView = convertView.findViewById(android.R.id.text1);
            convertView.setTag(textView);
        } else {
            textView = (TextView) convertView.getTag();
        }

        textView.setOnClickListener(v -> {
            if ((l - userInfo.getLast_login_time_long()) > 1) {
                new AlertDialog.Builder(mContext)
                        .setTitle("警告")
                        .setMessage("改令牌已超过一天以上没有通过官方登陆器登录，可能无法正常登录。\n是否继续登录")
                        .setNeutralButton("继续登录", (dialog, which) -> onLoginListener(userInfo))
                        .setPositiveButton("取消登录", (dialog, which) -> {})
                        .create().show();
            } else {
                onLoginListener(userInfo);
            }
        });
        textView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(mContext)
                    .setCancelable(false)
                    .setTitle("登录信息详情")
                    .setMessage("B站ID：" + userInfo.getUsername() +
                            "\n令牌用户ID：" + userInfo.getUid() +
                            "\n访问令牌：" + userInfo.getAccess_token() +
                            "\n会话过期时间：" + userInfo.getExpire_times() +
                            "\n刷新令牌：" + userInfo.getRefresh_token() +
                            "\n最后一次通过官方登陆器登陆的时间：" + userInfo.getLast_login_time())
                    .setPositiveButton("关闭", (dialog, which) -> setDialogShowing(dialog, false))
                    .setNeutralButton("删除该登录信息", (dialog, which) -> {
                        setDialogShowing(dialog, true);
                        new AlertDialog.Builder(mContext)
                                .setCancelable(false)
                                .setTitle("是否删除")
                                .setMessage("您正在试图删除B站用户名为\"" + userInfo.getUsername() + "\"的登录信息。\n删除后需要通过官方登陆器进行登录才能重新获取登录信息。")
                                .setNegativeButton("确认", (dialog1, which1) -> {
                                    setDialogShowing(dialog, false);
                                    onDeleteListener(userInfo.getUid());
                                })
                                .setPositiveButton("取消", (dialog1, which1) -> {})
                                .create().show();
                    })
                    .create().show();
            return true;
        });
        textView.setText("B站ID：" + userInfo.getUsername() + "\n最后一次通过官方登陆器登陆的时间：" + userInfo.getLast_login_time());
        if ((l - userInfo.getLast_login_time_long()) > 1) {
            textView.setTextColor(Color.RED);
        } else {
            textView.setTextColor(Color.BLUE);
        }
        return convertView;
    }

    private void setDialogShowing(DialogInterface dialog, boolean b){
        b = !b;
        try {
            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
            field.setAccessible(true);
            field.set(dialog, b);
        } catch (Exception e) {
            e.printStackTrace();;
        }
        if (b) dialog.dismiss();
    }

    protected void onDeleteListener(String uid){}
    protected void onLoginListener(UserInfo userInfo){}
}
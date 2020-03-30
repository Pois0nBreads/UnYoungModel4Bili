package net.pois0nbread.unyoungmodel4bili.adapter;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * <pre>
 *     author : Pois0nBread
 *     e-mail : pois0nbreads@gmail.com
 *     time   : 2030/03/30
 *     desc   : UserInfo
 *     version: 1.0
 * </pre>
 */

public class UserInfo {
    private String uid;
    private String username;
    private String nickname;
    private String access_token;
    private String expire_times;
    private String refresh_token;
    private long last_login_time_long;
    private String last_login_time;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getExpire_times() {
        return expire_times;
    }

    public void setExpire_times(String expire_times) {
        this.expire_times = expire_times;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public long getLast_login_time_long() {
        return last_login_time_long;
    }

    public void setLaste_login_time_long(long last_login_time_long) {
        this.last_login_time_long = last_login_time_long;
    }

    public String getLast_login_time() {
        return last_login_time;
    }

    public void setLaste_login_time(String last_login_time) {
        this.last_login_time = last_login_time;
    }

    public JSONObject toJSONObject(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uid", this.uid);
            jsonObject.put("username", this.username);
            jsonObject.put("nickname", this.nickname);
            jsonObject.put("access_token", this.access_token);
            jsonObject.put("expire_times", this.expire_times);
            jsonObject.put("refresh_token", this.refresh_token);
            jsonObject.put("last_login_time_long", this.last_login_time_long);
            jsonObject.put("last_login_time", this.last_login_time);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  jsonObject;
    }
}

package net.coding.program.model;

import net.coding.program.Global;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by cc191954 on 14-8-7.
 */
public class UserObject implements Serializable {
    public String avatar = "";
    public String slogan = "";
    public String tags = "";
    public String tags_str = "";
    public String company = "";
    public String global_key = "";
    public String id = "";
    public String introduction = "";
    public String job_str = "";
    public String lavatar = "";
    public String location = "";
    public String name = "";
    public String path = "";
    public String phone = "";
    public long birthday;
    public long created_at;
    public int fans_count;
    public boolean follow;  // 别人是否关注我
    public boolean followed;
    public int follows_count;
    public int job;
    public int sex;
    public int status;
    public long last_activity_at;
    public long last_logined_at;
    public long updated_at;
    public int tweets_count;
    public String email;

    public UserObject(JSONObject json) throws JSONException {
        avatar = Global.replaceAvatar(json);
        slogan = json.optString("slogan");
        tags = json.optString("tags");
        tags_str = json.optString("tags_str");
        company = json.optString("company");
        global_key = json.optString("global_key");
        id = json.optString("id");
        introduction = json.optString("introduction");
        job_str = json.optString("job_str");
        lavatar = json.optString("lavatar");
        location = json.optString("location");
        name = json.optString("name");
        path = json.optString("path");
        phone = json.optString("phone");

        try {
            birthday = Global.longFromDay(json.optString("birthday"));
        } catch (Exception e) {
        }

        created_at = json.optLong("created_at");
        last_activity_at = json.optLong("last_activity_at");
        last_logined_at = json.optLong("last_logined_at");
        updated_at = json.optLong("updated_at");

        follow = json.optBoolean("follow");
        followed = json.optBoolean("followed");
        follows_count = json.optInt("follows_count");
        fans_count = json.optInt("fans_count");
        job = json.optInt("job");
        sex = json.optInt("sex");
        status = json.optInt("status");
        tweets_count = json.optInt("tweets_count");
        email = json.optString("email");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserObject that = (UserObject) o;

        if (birthday != that.birthday) return false;
        if (created_at != that.created_at) return false;
        if (fans_count != that.fans_count) return false;
        if (follow != that.follow) return false;
        if (followed != that.followed) return false;
        if (follows_count != that.follows_count) return false;
        if (job != that.job) return false;
        if (last_activity_at != that.last_activity_at) return false;
        if (last_logined_at != that.last_logined_at) return false;
        if (sex != that.sex) return false;
        if (status != that.status) return false;
        if (tweets_count != that.tweets_count) return false;
        if (updated_at != that.updated_at) return false;
        if (avatar != null ? !avatar.equals(that.avatar) : that.avatar != null) return false;
        if (company != null ? !company.equals(that.company) : that.company != null) return false;
        if (email != null ? !email.equals(that.email) : that.email != null) return false;
        if (global_key != null ? !global_key.equals(that.global_key) : that.global_key != null)
            return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (introduction != null ? !introduction.equals(that.introduction) : that.introduction != null)
            return false;
        if (job_str != null ? !job_str.equals(that.job_str) : that.job_str != null) return false;
        if (lavatar != null ? !lavatar.equals(that.lavatar) : that.lavatar != null) return false;
        if (location != null ? !location.equals(that.location) : that.location != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (path != null ? !path.equals(that.path) : that.path != null) return false;
        if (phone != null ? !phone.equals(that.phone) : that.phone != null) return false;
        if (slogan != null ? !slogan.equals(that.slogan) : that.slogan != null) return false;
        if (tags != null ? !tags.equals(that.tags) : that.tags != null) return false;
        if (tags_str != null ? !tags_str.equals(that.tags_str) : that.tags_str != null)
            return false;

        return true;
    }

    public UserObject() {
    }
}

package net.coding.program.common.model;

import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.network.constant.VIP;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by cc191954 on 14-8-7.
 */
public class UserObject implements Serializable, Comparable {

    private static final long serialVersionUID = 399378582466355030L;

    @SerializedName("degree")
    @Expose
    public int degree;
    @SerializedName("school")
    @Expose
    public String school = "";
    @SerializedName("skills")
    @Expose
    public ArrayList<Skill> skills = new ArrayList<>();
    @SerializedName("avatar")
    @Expose
    public String avatar = "";
    @SerializedName("slogan")
    @Expose
    public String slogan = "";
    @SerializedName("tags")
    @Expose
    public String tags = "";
    @SerializedName("tags_str")
    @Expose
    public String tags_str = "";
    @SerializedName("company")
    @Expose
    public String company = "";
    @SerializedName("global_key")
    @Expose
    public String global_key = "";
    @SerializedName("id")
    @Expose
    public int id;
    @SerializedName("introduction")
    @Expose
    public String introduction = "";
    @SerializedName("job_str")
    @Expose
    public String job_str = "";
    @SerializedName("lavatar")
    @Expose
    public String lavatar = "";
    @SerializedName("location")
    @Expose
    public String location = "";
    @SerializedName("name")
    @Expose
    public String name = "";
    @SerializedName("path")
    @Expose
    public String path = "";
    @SerializedName("phone")
    @Expose
    public String phone = "";
    @SerializedName("birthday")
    @Expose
    public String birthday = "";
    @SerializedName("created_at")
    @Expose
    public long created_at;
    @SerializedName("fans_count")
    @Expose
    public int fans_count;
    @SerializedName("follow")
    @Expose
    public boolean follow;  // 别人是否关注我
    @SerializedName("followed")
    @Expose
    public boolean followed;
    @SerializedName("follows_count")
    @Expose
    public int follows_count;
    @SerializedName("job")
    @Expose
    public int job;
    @SerializedName("sex")
    @Expose
    public int sex;
    @SerializedName("status")
    @Expose
    public int status;
    @SerializedName("last_activity_at")
    @Expose
    public long last_activity_at;
    @SerializedName("last_logined_at")
    @Expose
    public long last_logined_at;
    @SerializedName("updated_at")
    @Expose
    public long updated_at;
    @SerializedName("tweets_count")
    @Expose
    public int tweets_count;
    @SerializedName("email")
    @Expose
    public String email = "";
    @SerializedName("points_left")
    @Expose
    public BigDecimal points_left = BigDecimal.ZERO;
    @SerializedName("email_validation")
    @Expose
    public int email_validation = 0;
    @SerializedName("phone_validation")
    @Expose
    public int phone_validation = 0;
    @SerializedName("phone_country_code")
    @Expose
    public String phone_country_code = "+86";
    @SerializedName("pingYin")
    @Expose
    private String pingYin = "";
    @SerializedName("vip")
    @Expose
    public VIP vip = VIP.normal;
    @SerializedName("vip_expired_at")
    @Expose
    public long vipExpiredAt;
    @SerializedName("twofa_enabled")
    @Expose
    public int twofaEnabled;

    public boolean vipNearExpired() {
        // 提前 3 天通知，但因为服务器给的时间是 0 点 0 分，所以用 2
        return vipExpiredAt - System.currentTimeMillis() < 2 * 3600 * 1000 * 24;
    }

    public UserObject(JSONObject json) {
        if (json == null) {
            return;
        }

        avatar = Global.replaceAvatar(json);
        slogan = json.optString("slogan", "");
        tags = json.optString("tags", "");
        tags_str = json.optString("tags_str", "");
        company = json.optString("company", "");
        global_key = json.optString("global_key", "");
        id = json.optInt("id");
        introduction = json.optString("introduction", "");
        job_str = json.optString("job_str", "");
        lavatar = json.optString("lavatar", "");
        location = json.optString("location", "");
        name = json.optString("name", "");
        path = json.optString("path", "");
        phone = json.optString("phone", "");

        birthday = json.optString("birthday", "");

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
        email = json.optString("email", "");
        points_left = new BigDecimal(json.optString("points_left", "0"));
        pingYin = getFirstLetters(name).toUpperCase();
        email_validation = json.optInt("email_validation", 0);
        phone_validation = json.optInt("phone_validation", 0);
        phone_country_code = json.optString("phone_country_code", "+86");
        vip = VIP.Companion.id2Enum(json.optInt("vip", 1));
        vipExpiredAt = json.optLong("vip_expired_at", 0);

        degree = json.optInt("degree");
        school = json.optString("school", "");
        JSONArray jsonSkills = json.optJSONArray("skills");
        if (jsonSkills != null) {
            for (int i = 0; i < jsonSkills.length(); ++i) {
                skills.add(new Skill(jsonSkills.optJSONObject(i)));
            }
        }

        twofaEnabled = json.optInt("twofa_enabled" , 0);
    }

    public boolean getTwofaEnabled() {
        return twofaEnabled == 1;
    }

    public UserObject() {
    }

    public String getUserDegree() {
        String[] userDegree = new String[]{
                "高中及以下",
                "大专",
                "本科",
                "硕士及以上"};

        String degreeString = "";
        int pickDegree = degree - 1;
        if (0 <= pickDegree && pickDegree < userDegree.length) {
            degreeString = userDegree[pickDegree];
        }

        return degreeString;
    }

    public String getUserSkills() {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (Skill item : skills) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(", ");
            }
            sb.append(item.toString());
        }
        return sb.toString();
    }

    public static String getFirstLetters(String chinese) {
        StringBuffer pybf = new StringBuffer();
        char[] arr = chinese.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] > 128) {
                try {
                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(arr[i], defaultFormat);
                    if (temp != null) {
                        pybf.append(temp[0].charAt(0));
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                pybf.append(arr[i]);
            }
        }
        return pybf.toString().replaceAll("\\W", "").trim();
    }

    public boolean isMe() {
        return GlobalData.sUserObject.id == id;
    }

    public void reward() {
        points_left = points_left.subtract(new BigDecimal("0.01"));
    }

    public String getFirstLetter() {
        String letter = pingYin.substring(0, 1).toUpperCase();
        if (0 <= letter.compareTo("A") && letter.compareTo("Z") <= 0) {
            return letter;
        }

        return "#";
    }

    @Override
    public int compareTo(Object another) {
        String otherPY = ((UserObject) another).pingYin;
        boolean selfStart = isLetter(pingYin);
        boolean otherStart = isLetter(otherPY);

        if (selfStart) {
            if (otherStart) {
                return pingYin.compareTo(otherPY);
            } else {
                return -1;
            }
        } else {
            if (otherStart) {
                return 1;
            } else {
                return pingYin.compareTo(otherPY);
            }
        }
    }

    private boolean isLetter(String s) {
        if (TextUtils.isEmpty(s)) {
            return false;
        }
        char c = s.charAt(0);
        return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
    }

    public boolean isEmailValidation() {
        return email_validation != 0;
    }

    public boolean isPhoneValidation() {
        return phone_validation != 0;
    }

    public boolean isFillInfo() {
        if (TextUtils.isEmpty(birthday) ||
                TextUtils.isEmpty(location) ||
                TextUtils.isEmpty(job_str) ||
                TextUtils.isEmpty(getUserDegree()) ||
                TextUtils.isEmpty(school) ||
                phone_validation != 1 ||
                email_validation != 1 ||
                !isSkillsCompleted()) {
            return false;
        }

        return true;
    }

    public boolean isHighLevel() {
        return vip != null && vip.getId() >= VIP.silver.getId();
    }

    public boolean phoneAndEmailValid() {
        return phone_validation == 1 && email_validation == 1;
    }

    public boolean isSkillsCompleted() {
        return skills != null && skills.size() > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserObject)) return false;

        UserObject that = (UserObject) o;

        if (created_at != that.created_at) return false;
        if (fans_count != that.fans_count) return false;
        if (follow != that.follow) return false;
        if (followed != that.followed) return false;
        if (follows_count != that.follows_count) return false;
        if (id != that.id) return false;
        if (job != that.job) return false;
        if (last_activity_at != that.last_activity_at) return false;
        if (last_logined_at != that.last_logined_at) return false;
        if (sex != that.sex) return false;
        if (status != that.status) return false;
        if (tweets_count != that.tweets_count) return false;
        if (updated_at != that.updated_at) return false;
        if (avatar != null ? !avatar.equals(that.avatar) : that.avatar != null) return false;
        if (birthday != null ? !birthday.equals(that.birthday) : that.birthday != null)
            return false;
        if (company != null ? !company.equals(that.company) : that.company != null) return false;
        if (email != null ? !email.equals(that.email) : that.email != null) return false;
        if (global_key != null ? !global_key.equals(that.global_key) : that.global_key != null)
            return false;
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

    @Override
    public int hashCode() {
        int result = avatar != null ? avatar.hashCode() : 0;
        result = 31 * result + (slogan != null ? slogan.hashCode() : 0);
        result = 31 * result + (tags != null ? tags.hashCode() : 0);
        result = 31 * result + (tags_str != null ? tags_str.hashCode() : 0);
        result = 31 * result + (company != null ? company.hashCode() : 0);
        result = 31 * result + (global_key != null ? global_key.hashCode() : 0);
        result = 31 * result + id;
        result = 31 * result + (introduction != null ? introduction.hashCode() : 0);
        result = 31 * result + (job_str != null ? job_str.hashCode() : 0);
        result = 31 * result + (lavatar != null ? lavatar.hashCode() : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (phone != null ? phone.hashCode() : 0);
        result = 31 * result + (birthday != null ? birthday.hashCode() : 0);
        result = 31 * result + (int) (created_at ^ (created_at >>> 32));
        result = 31 * result + fans_count;
        result = 31 * result + (follow ? 1 : 0);
        result = 31 * result + (followed ? 1 : 0);
        result = 31 * result + follows_count;
        result = 31 * result + job;
        result = 31 * result + sex;
        result = 31 * result + status;
        result = 31 * result + (int) (last_activity_at ^ (last_activity_at >>> 32));
        result = 31 * result + (int) (last_logined_at ^ (last_logined_at >>> 32));
        result = 31 * result + (int) (updated_at ^ (updated_at >>> 32));
        result = 31 * result + tweets_count;
        result = 31 * result + (email != null ? email.hashCode() : 0);
        return result;
    }

    public void setPhone(String phone, String countryCode) {
        this.phone = phone;
        this.phone_country_code = countryCode;
        this.phone_validation = 1;
    }
}

package net.coding.program.common.model;

import com.orhanobut.logger.Logger;

import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by chenchao on 2017/1/19.
 */

public class EnterpriseAccount implements Serializable {

    private static final long serialVersionUID = -8068995659716038945L;

    public static final int MAX_TRIAL_DAYS = 15;

    public String balance;
    public int remaindays;
    public long createdat;
    public long suspendedAt;
    public boolean trial;

    public boolean payed; // 是否充值过
    public long estimateDate;


    public EnterpriseAccount(JSONObject json) {
        balance = json.optString("balance");
        remaindays = json.optInt("remain_days");
        createdat = json.optInt("created_at");
        trial = json.optBoolean("trial");
        payed = json.optBoolean("payed");
        estimateDate = json.optLong("estimate_date");
        suspendedAt = json.optLong("suspended_at");
    }

    public int estimateDate() {
        return Math.abs(todayToEnd(estimateDate));
    }

    public int suspendedToToday() {
        return Math.abs(todayToEnd(suspendedAt));
    }

    private int todayToEnd(long endTime) {
        Calendar calendar = Calendar.getInstance();

        long today = calendar.getTimeInMillis();

        calendar.setTimeInMillis(endTime);
        long end = setHour0(calendar);

        long diffTime = end - today;
        int oneDay = 24 * 3600 * 1000;
        long diffDay = diffTime / oneDay;
        if (diffDay % oneDay > 0) {
            diffDay++;
        }
        ++diffDay;

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Logger.d("today %s %s %s", df.format(new Long(today)), df.format(new Long(end)), diffDay);
        return (int) diffDay;
    }

    private long setHour0(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTimeInMillis();
    }

}

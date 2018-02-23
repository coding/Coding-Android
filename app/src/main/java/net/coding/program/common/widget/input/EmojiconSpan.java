package net.coding.program.common.widget.input;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.style.DynamicDrawableSpan;

import net.coding.program.R;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.enter.DrawableTool;

import java.util.HashMap;

/**
 * Created by chaochen on 14-11-12.
 */
public class EmojiconSpan extends DynamicDrawableSpan {
    private final Context mContext;
    private final int mResourceId;
    private Drawable mDrawable;
    private boolean mIsMonkey;

    private static HashMap<String, String> emojiMonkeyMap = new HashMap<>();
    private static HashMap<String, String> textToMonkdyMap = new HashMap<>();

    static {
        emojiMonkeyMap.put("coding_emoji_01", "哈哈");
        emojiMonkeyMap.put("coding_emoji_02", "吐");
        emojiMonkeyMap.put("coding_emoji_03", "压力山大");
        emojiMonkeyMap.put("coding_emoji_04", "忧伤");
        emojiMonkeyMap.put("coding_emoji_05", "坏人");
        emojiMonkeyMap.put("coding_emoji_06", "酷");
        emojiMonkeyMap.put("coding_emoji_07", "哼");
        emojiMonkeyMap.put("coding_emoji_08", "你咬我啊");
        emojiMonkeyMap.put("coding_emoji_09", "内急");
        emojiMonkeyMap.put("coding_emoji_10", "32个赞");
        emojiMonkeyMap.put("coding_emoji_11", "加油");
        emojiMonkeyMap.put("coding_emoji_12", "闭嘴");
        emojiMonkeyMap.put("coding_emoji_13", "wow");
        emojiMonkeyMap.put("coding_emoji_14", "泪流成河");
        emojiMonkeyMap.put("coding_emoji_15", "NO!");
        emojiMonkeyMap.put("coding_emoji_16", "疑问");
        emojiMonkeyMap.put("coding_emoji_17", "耶");
        emojiMonkeyMap.put("coding_emoji_18", "生日快乐");
        emojiMonkeyMap.put("coding_emoji_19", "求包养");
        emojiMonkeyMap.put("coding_emoji_20", "吹泡泡");
        emojiMonkeyMap.put("coding_emoji_21", "睡觉");
        emojiMonkeyMap.put("coding_emoji_22", "惊讶");
        emojiMonkeyMap.put("coding_emoji_23", "Hi");
        emojiMonkeyMap.put("coding_emoji_24", "打发点咯");
        emojiMonkeyMap.put("coding_emoji_25", "呵呵");
        emojiMonkeyMap.put("coding_emoji_26", "喷血");
        emojiMonkeyMap.put("coding_emoji_27", "Bug");
        emojiMonkeyMap.put("coding_emoji_28", "听音乐");
        emojiMonkeyMap.put("coding_emoji_29", "垒码");
        emojiMonkeyMap.put("coding_emoji_30", "我打你哦");
        emojiMonkeyMap.put("coding_emoji_31", "顶足球");
        emojiMonkeyMap.put("coding_emoji_32", "放毒气");
        emojiMonkeyMap.put("coding_emoji_33", "表白");
        emojiMonkeyMap.put("coding_emoji_34", "抓瓢虫");
        emojiMonkeyMap.put("coding_emoji_35", "下班");
        emojiMonkeyMap.put("coding_emoji_36", "冒泡");

        emojiMonkeyMap.put("coding_emoji_38", "2015");
        emojiMonkeyMap.put("coding_emoji_39", "拜年");
        emojiMonkeyMap.put("coding_emoji_40", "发红包");
        emojiMonkeyMap.put("coding_emoji_41", "放鞭炮");
        emojiMonkeyMap.put("coding_emoji_42", "求红包");
        emojiMonkeyMap.put("coding_emoji_43", "新年快乐");

        emojiMonkeyMap.put("festival_emoji_01", "奔月");
        emojiMonkeyMap.put("festival_emoji_02", "吃月饼");
        emojiMonkeyMap.put("festival_emoji_03", "捞月");
        emojiMonkeyMap.put("festival_emoji_04", "打招呼");
        emojiMonkeyMap.put("festival_emoji_05", "中秋快乐");
        emojiMonkeyMap.put("festival_emoji_06", "赏月");
        emojiMonkeyMap.put("festival_emoji_07", "悠闲");
        emojiMonkeyMap.put("festival_emoji_08", "爬爬");

        textToMonkdyMap.put("哈哈", "coding_emoji_01");
        textToMonkdyMap.put("吐", "coding_emoji_02");
        textToMonkdyMap.put("压力山大", "coding_emoji_03");
        textToMonkdyMap.put("忧伤", "coding_emoji_04");
        textToMonkdyMap.put("坏人", "coding_emoji_05");
        textToMonkdyMap.put("酷", "coding_emoji_06");
        textToMonkdyMap.put("哼", "coding_emoji_07");
        textToMonkdyMap.put("你咬我啊", "coding_emoji_08");
        textToMonkdyMap.put("内急", "coding_emoji_09");
        textToMonkdyMap.put("32个赞", "coding_emoji_10");
        textToMonkdyMap.put("加油", "coding_emoji_11");
        textToMonkdyMap.put("闭嘴", "coding_emoji_12");
        textToMonkdyMap.put("wow", "coding_emoji_13");
        textToMonkdyMap.put("泪流成河", "coding_emoji_14");
        textToMonkdyMap.put("NO!", "coding_emoji_15");
        textToMonkdyMap.put("疑问", "coding_emoji_16");
        textToMonkdyMap.put("耶", "coding_emoji_17");
        textToMonkdyMap.put("生日快乐", "coding_emoji_18");
        textToMonkdyMap.put("求包养", "coding_emoji_19");
        textToMonkdyMap.put("吹泡泡", "coding_emoji_20");
        textToMonkdyMap.put("睡觉", "coding_emoji_21");
        textToMonkdyMap.put("惊讶", "coding_emoji_22");
        textToMonkdyMap.put("Hi", "coding_emoji_23");
        textToMonkdyMap.put("打发点咯", "coding_emoji_24");
        textToMonkdyMap.put("呵呵", "coding_emoji_25");
        textToMonkdyMap.put("喷血", "coding_emoji_26");
        textToMonkdyMap.put("Bug", "coding_emoji_27");
        textToMonkdyMap.put("听音乐", "coding_emoji_28");
        textToMonkdyMap.put("垒码", "coding_emoji_29");
        textToMonkdyMap.put("我打你哦", "coding_emoji_30");
        textToMonkdyMap.put("顶足球", "coding_emoji_31");
        textToMonkdyMap.put("放毒气", "coding_emoji_32");
        textToMonkdyMap.put("表白", "coding_emoji_33");
        textToMonkdyMap.put("抓瓢虫", "coding_emoji_34");
        textToMonkdyMap.put("下班", "coding_emoji_35");
        textToMonkdyMap.put("冒泡", "coding_emoji_36");
        textToMonkdyMap.put("2015", "coding_emoji_38");
        textToMonkdyMap.put("拜年", "coding_emoji_39");
        textToMonkdyMap.put("发红包", "coding_emoji_40");
        textToMonkdyMap.put("放鞭炮", "coding_emoji_41");
        textToMonkdyMap.put("求红包", "coding_emoji_42");
        textToMonkdyMap.put("新年快乐", "coding_emoji_43");

        textToMonkdyMap.put("奔月", "festival_emoji_01");
        textToMonkdyMap.put("吃月饼", "festival_emoji_02");
        textToMonkdyMap.put("捞月", "festival_emoji_03");
        textToMonkdyMap.put("打招呼", "festival_emoji_04");
        textToMonkdyMap.put("中秋快乐", "festival_emoji_05");
        textToMonkdyMap.put("赏月", "festival_emoji_06");
        textToMonkdyMap.put("悠闲", "festival_emoji_07");
        textToMonkdyMap.put("爬爬", "festival_emoji_08");
    }

    public static String imageToText(String image) {
        String text = emojiMonkeyMap.get(image);
        if (TextUtils.isEmpty(text)) text = "哈哈";
        return text;
    }

    public EmojiconSpan(Context context, String iconName) {
        super();
        mContext = context;

        String name = textToMonkdyMap.get(iconName);
        if (name == null) {
            name = iconName;
            mIsMonkey = false;
        } else {
            mIsMonkey = true;
        }

        mResourceId = MyImageGetter.getResourceId(name);
    }

    @Override
    public Drawable getDrawable() {
        if (mDrawable == null) {
            try {
                mDrawable = mContext.getResources().getDrawable(mResourceId);
                DrawableTool.zoomDrwable(mDrawable, mIsMonkey);
            } catch (Exception e) {
            }
        }
        return mDrawable;
    }

    public boolean isDefault() {
        return mResourceId == R.drawable.app_icon_emoji;
    }
}
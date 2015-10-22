package net.coding.program.message;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;

import net.coding.program.R;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.enter.EnterLayout;

import java.util.HashMap;

/**
 * Created by chaochen on 14-10-30.
 */
public class EmojiFragment extends Fragment {

    public enum Type {
        Small, Big, Zhongqiu
    }

    private LayoutInflater mInflater;
    private String[] mEmojiData;
    private MyImageGetter myImageGetter;
    private int deletePos;
    private EnterLayout mEnterLayout;

    private Type mType;
    private int mItemLayout = R.layout.gridview_emotion_emoji;
    private int mGridViewColumns;

    public static HashMap<String, String> emojiMonkeyMap = new HashMap<String, String>();
    public static HashMap<String, String> textToMonkdyMap = new HashMap<String, String>();

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

    public EmojiFragment() {
        super();
    }

    public void init(String[] emojis, MyImageGetter imageGetter, EnterLayout enterLayout, Type type) {
        mEmojiData = emojis;
        myImageGetter = imageGetter;
        deletePos = emojis.length - 1;
        mEnterLayout = enterLayout;

        mType = type;
        if (type == Type.Big || type == Type.Zhongqiu) {
            mItemLayout = R.layout.gridview_emotion_big;
            mGridViewColumns = 4;
        } else {
//            if (type == Type.Small) {
                mItemLayout = R.layout.gridview_emotion_emoji;
                mGridViewColumns = 7;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArray("mEmojiData", mEmojiData);
        outState.putInt("deletePos", deletePos);
        outState.putSerializable("mType", mType);
        outState.putInt("mItemLayout", mItemLayout);
        outState.putInt("mGridViewColumns", mGridViewColumns);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mInflater = inflater;
        View v = inflater.inflate(R.layout.emoji_gridview, container, false);

        if (savedInstanceState != null) {
            mEmojiData = savedInstanceState.getStringArray("mEmojiData");
            deletePos = savedInstanceState.getInt("deletePos");
            mType = (EmojiFragment.Type) savedInstanceState.getSerializable("mType");
            mItemLayout = savedInstanceState.getInt("mItemLayout");
            mGridViewColumns = savedInstanceState.getInt("mGridViewColumns");

            myImageGetter = new MyImageGetter(getActivity());
            Activity activity = getActivity();
            if (activity instanceof EnterEmojiLayout) {
                mEnterLayout = ((EnterEmojiLayout) activity).getEnterLayout();
            }
        }

        GridView gridView = (GridView) v.findViewById(R.id.gridView);
        gridView.setNumColumns(mGridViewColumns);
        gridView.setAdapter(adapterIcon);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mType == Type.Small) {
                    int realPos = (int) id;
                    if (realPos == deletePos) {
                        mEnterLayout.deleteOneChar();
                    } else {
                        String name = (String) adapterIcon.getItem((int) id);

                        if (name.equals("my100")) {
                            name = "100";
                        } else if (name.equals("a00001")) {
                            name = "+1";
                        } else if (name.equals("a00002")) {
                            name = "-1";
                        }

                        mEnterLayout.insertEmoji(name);
                    }
                } else {
                    String potoName = (String) adapterIcon.getItem((int) id);
                    String editName = emojiMonkeyMap.get(potoName);
                    mEnterLayout.insertEmoji(editName);
                }
            }
        });
        return v;
    }

    BaseAdapter adapterIcon = new BaseAdapter() {

        @Override
        public int getCount() {
            return mEmojiData.length;
        }

        @Override
        public Object getItem(int position) {
            return mEmojiData[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(mItemLayout, parent, false);
                holder = new ViewHolder();
                holder.icon = convertView.findViewById(R.id.icon);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            String iconName = mEmojiData[position];
            holder.icon.setBackgroundDrawable(myImageGetter.getDrawable(iconName));

            return convertView;
        }

        class ViewHolder {
            public View icon;
        }
    };

    public interface EnterEmojiLayout {
        public EnterLayout getEnterLayout();
    }

}

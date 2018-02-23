package net.coding.program.common.ui.emoji;

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
import net.coding.program.common.widget.input.EmojiKeyboard;
import net.coding.program.common.widget.input.EmojiconSpan;
import net.coding.program.common.widget.input.InputAction;

/**
 * Created by chaochen on 14-10-30.
 * todo 单独的控件
 */
public class EmojiFragment extends Fragment {

    private LayoutInflater mInflater;
    private String[] mEmojiData;
    private MyImageGetter myImageGetter;
    private int deletePos;
    private InputAction mEnterLayout;
    private EmojiKeyboard.Type mType;
    private int mItemLayout = R.layout.gridview_emotion_emoji;
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
            if (!iconName.isEmpty()) {
                holder.icon.setBackgroundDrawable(myImageGetter.getDrawable(iconName));
            }

            return convertView;
        }

        @Override
        public boolean isEnabled(int position) {
            return !mEmojiData[position].isEmpty() && super.isEnabled(position);
        }

        class ViewHolder {
            public View icon;
        }
    };
    private int mGridViewColumns;

    public EmojiFragment() {
        super();
    }

    public void init(String[] emojis, MyImageGetter imageGetter, InputAction enterLayout, EmojiKeyboard.Type type) {
        mEmojiData = emojis;
        myImageGetter = imageGetter;
        deletePos = emojis.length - 1;
        mEnterLayout = enterLayout;

        mType = type;
        if (type == EmojiKeyboard.Type.Big || type == EmojiKeyboard.Type.Zhongqiu) {
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
            mType = (EmojiKeyboard.Type) savedInstanceState.getSerializable("mType");
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
                if (mType == EmojiKeyboard.Type.Small) {
                    int realPos = (int) id;
                    if (realPos == deletePos) {
                        mEnterLayout.deleteOneChar();
                    } else {
                        String name = (String) adapterIcon.getItem((int) id);

                        if (name.isEmpty()) {
                            return;
                        }

                        if (name.equals("leftwards_arrow_with_hook")) {
                            mEnterLayout.enterAction();
                            return;
                        }

                        if (name.equals("one") || name.equals("two")
                                || name.equals("three") || name.equals("four")
                                || name.equals("five") || name.equals("six")
                                || name.equals("seven") || name.equals("eight")
                                || name.equals("nine") || name.equals("zero")) {
                            switch (name) {
                                case "one":
                                    name = "1";
                                    break;
                                case "two":
                                    name = "2";
                                    break;
                                case "three":
                                    name = "3";
                                    break;
                                case "four":
                                    name = "4";
                                    break;
                                case "five":
                                    name = "5";
                                    break;
                                case "six":
                                    name = "6";
                                    break;
                                case "seven":
                                    name = "7";
                                    break;
                                case "eight":
                                    name = "8";
                                    break;
                                case "nine":
                                    name = "9";
                                    break;
                                case "zero":
                                    name = "0";
                                    break;
                            }

                            mEnterLayout.numberAction(name);
                            return;
                        }

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
                    String imageName = (String) adapterIcon.getItem((int) id);
                    String editName = EmojiconSpan.imageToText(imageName);
                    mEnterLayout.insertEmoji(editName);
                }
            }
        });
        return v;
    }

    public interface EnterEmojiLayout {
        EnterLayout getEnterLayout();
    }

}

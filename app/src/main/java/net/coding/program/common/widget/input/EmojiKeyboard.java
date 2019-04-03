package net.coding.program.common.widget.input;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.ui.emoji.EmojiFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by chenchao on 16/1/21.
 * emoji 选择
 */

@EViewGroup(R.layout.input_view_emoji_keyboard)
public class EmojiKeyboard extends FrameLayout {


    private static String emojiIcons[][] = {{
            "smiley",
            "heart_eyes",
            "pensive",
            "flushed",
            "grin",
            "kissing_heart",
            "wink",
            "angry",
            "disappointed",
            "disappointed_relieved",
            "sob",
            "stuck_out_tongue_closed_eyes",
            "rage",
            "persevere",
            "unamused",
            "smile",
            "mask",
            "kissing_face",
            "sweat",
            "joy",
            "ic_keyboard_delete"
    }, {
            "blush",
            "cry",
            "stuck_out_tongue_winking_eye",
            "fearful",
            "cold_sweat",
            "dizzy_face",
            "smirk",
            "scream",
            "sleepy",
            "confounded",
            "relieved",
            "smiling_imp",
            "ghost",
            "santa",
            "dog",
            "pig",
            "cat",
            "a00001",
            "a00002",
            "facepunch",
            "ic_keyboard_delete"
    }, {
            "fist",
            "v",
            "muscle",
            "clap",
            "point_left",
            "point_up_2",
            "point_right",
            "point_down",
            "ok_hand",
            "heart",
            "broken_heart",
            "sunny",
            "moon",
            "star2",
            "zap",
            "cloud",
            "lips",
            "rose",
            "coffee",
            "birthday",

            "ic_keyboard_delete"
    }, {
            "clock10",
            "beer",
            "mag",
            "iphone",
            "house",
            "car",
            "gift",
            "soccer",
            "bomb",
            "gem",
            "alien",
            "my100",
            "money_with_wings",
            "video_game",
            "hankey",
            "sos",
            "zzz",
            "microphone",
            "umbrella",
            "book",
            "ic_keyboard_delete"}
    };
    private static String monkeyIcons[][] = new String[][]{{
            "coding_emoji_01",
            "coding_emoji_02",
            "coding_emoji_03",
            "coding_emoji_04",
            "coding_emoji_05",
            "coding_emoji_06",
            "coding_emoji_07",
            "coding_emoji_08"
    }, {
            "coding_emoji_09",
            "coding_emoji_10",
            "coding_emoji_11",
            "coding_emoji_12",
            "coding_emoji_13",
            "coding_emoji_14",
            "coding_emoji_15",
            "coding_emoji_16"
    }, {
            "coding_emoji_17",
            "coding_emoji_18",
            "coding_emoji_19",
            "coding_emoji_20",
            "coding_emoji_21",
            "coding_emoji_22",
            "coding_emoji_23",
            "coding_emoji_24"
    }, {
            "coding_emoji_25",
            "coding_emoji_26",
            "coding_emoji_27",
            "coding_emoji_28",
            "coding_emoji_29",
            "coding_emoji_30",
            "coding_emoji_31",
            "coding_emoji_32",
    }, {
            "coding_emoji_33",
            "coding_emoji_34",
            "coding_emoji_35",
            "coding_emoji_36",
            "coding_emoji_38",
            "coding_emoji_39",
            "coding_emoji_40",
            "coding_emoji_41",
    }, {
            "coding_emoji_42",
            "coding_emoji_43",
    }};
    private static String zhongqiuIcons[][] = new String[][]{{
            "festival_emoji_01",
            "festival_emoji_02",
            "festival_emoji_03",
            "festival_emoji_04",
            "festival_emoji_05",
            "festival_emoji_06",
            "festival_emoji_07",
            "festival_emoji_08"
    }};
    private static String codeIcons[][] = new String[][]{{
            "one",
            "two",
            "three",
            "four",
            "five",
            "six",
            "seven",
            "eight",
            "nine",
            "zero",
            "point_right",
            "eyes",
            "pencil",
            "monkey",
            "monkey_face",
            "see_no_evil",
            "a00001",
            "a00002",
            "poultry_leg",
            "leftwards_arrow_with_hook",
            "ic_keyboard_delete"
    }, {
            "poop",
            "question",
            "banana",
            "ghost",
            "scream",
            "mailbox_with_no_mail",
            "loudspeaker",
            "leftwards_arrow_with_hook",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "ic_keyboard_delete",
    }};
    @ViewById
    LinearLayout emojiKeyboardIndicator;
    @ViewById
    ViewPager viewPager;
    @ViewById
    View emojiButton, monkeyButton, zhongqiuButton, codeButton;
    PageChangeListener pageChange = new PageChangeListener();
    @ViewById
    View emojiKeyboardLayout, codeButtonLeftLine;
    private MyImageGetter myImageGetter;
    private FragmentActivity mActivity;
    private InputAction inputAction;
    private EmojiPagerAdapter mEmojiPagerAdapter;
    private MonkeyPagerAdapter mMonkeyPagerAdapter;
    private ZhongqiuPagerAdapter mZhongqiuPagerAdapter;
    private CodePagerAdapter mCodePagerAdapter;
    private CheckBox checkBoxEmoji;

    public EmojiKeyboard(Context context, AttributeSet attrs) {
        super(context, attrs);

        mActivity = (FragmentActivity) Global.getActivityFromView(this);
        myImageGetter = new MyImageGetter(mActivity);
        FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
        mEmojiPagerAdapter = new EmojiPagerAdapter(fragmentManager);
        mMonkeyPagerAdapter = new MonkeyPagerAdapter(fragmentManager);
        mZhongqiuPagerAdapter = new ZhongqiuPagerAdapter(fragmentManager);
        mCodePagerAdapter = new CodePagerAdapter(fragmentManager);
    }

    public void showEmojiOnly() {
        monkeyButton.setVisibility(View.INVISIBLE);
        findViewById(R.id.selectMonkeyDivideLine1).setVisibility(View.INVISIBLE);

        zhongqiuButton.setVisibility(View.INVISIBLE);
        findViewById(R.id.selectMonkeyDivideLine2).setVisibility(View.INVISIBLE);

    }

    @AfterViews
    void initEmojiKeyboard() {
        viewPager.setOnPageChangeListener(pageChange);
        emojiButton();

        if (GlobalData.isEnterprise()) {
            codeButton.setVisibility(GONE);
            codeButtonLeftLine.setVisibility(GONE);
        }
    }

    public void setInputAction(InputAction action) {
        inputAction = action;
    }

    @Click
    void emojiButton() {
        setIndicatorCount(emojiIcons.length);

        if (viewPager.getAdapter() != mEmojiPagerAdapter) {
            viewPager.setAdapter(mEmojiPagerAdapter);
            pageChange.resetPos();
        }
        setPressEmojiType(Type.Small);
    }

    @Click
    void monkeyButton() {
        setIndicatorCount(monkeyIcons.length);

        if (viewPager.getAdapter() != mMonkeyPagerAdapter) {
            viewPager.setAdapter(mMonkeyPagerAdapter);
            pageChange.resetPos();
        }
        setPressEmojiType(Type.Big);
    }

    @Click
    void zhongqiuButton() {
        setIndicatorCount(zhongqiuIcons.length);
        if (viewPager.getAdapter() != mZhongqiuPagerAdapter) {
            viewPager.setAdapter(mZhongqiuPagerAdapter);
            pageChange.resetPos();
        }

        setPressEmojiType(Type.Zhongqiu);
    }

    @Click
    void codeButton() {
        setIndicatorCount(codeIcons.length);
        if (viewPager.getAdapter() != mCodePagerAdapter) {
            viewPager.setAdapter(mCodePagerAdapter);
            pageChange.resetPos();
        }

        setPressEmojiType(Type.CODE);
    }

    private void setPressEmojiType(Type type) {

        if (type == Type.Small) {
            setEmojiButtonBackground(emojiButton);
        } else if (type == Type.Big) {
            setEmojiButtonBackground(monkeyButton);
        } else if (type == Type.Zhongqiu) {
            setEmojiButtonBackground(zhongqiuButton);
        } else {
            setEmojiButtonBackground(codeButton);
        }
    }

    private void setEmojiButtonBackground(View view) {
        final int colorNormal = 0x00000000;
        final int colorPress = 0xffe8e8e8;

        View[] views = new View[]{emojiButton, monkeyButton, zhongqiuButton, codeButton};
        for (View item : views) {
            if (view == item) {
                item.setBackgroundColor(colorPress);
            } else {
                item.setBackgroundColor(colorNormal);
            }
        }
    }

    private void setIndicatorCount(int count) {
        if (count <= 1) {
            emojiKeyboardIndicator.setVisibility(INVISIBLE);
            return;
        } else {
            emojiKeyboardIndicator.setVisibility(VISIBLE);
        }

        emojiKeyboardIndicator.removeAllViews();
        int pointWidth = mActivity.getResources().getDimensionPixelSize(R.dimen.point_width);
        int pointHeight = pointWidth;
        int pointMargin = mActivity.getResources().getDimensionPixelSize(R.dimen.point_margin);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(pointWidth, pointHeight);
        lp.leftMargin = pointWidth;
        lp.rightMargin = pointMargin;
        for (int i = 0; i < count; ++i) {
            View pointView = mActivity.getLayoutInflater().inflate(R.layout.common_point, emojiKeyboardIndicator, false);
            emojiKeyboardIndicator.addView(pointView, lp);
        }
        emojiKeyboardIndicator.getChildAt(0).setBackgroundResource(R.drawable.ic_point_select);
    }

    public enum EmojiType {
        Default, SmallOnly
    }

    public enum Type {
        Small, Big, Zhongqiu, CODE
    }

    class PageChangeListener extends ViewPager.SimpleOnPageChangeListener {
        int oldPos = 0;

        public void resetPos() {
            oldPos = 0;
        }

        @Override
        public void onPageSelected(int position) {
            View oldPoint = emojiKeyboardIndicator.getChildAt(oldPos);
            View newPoint = emojiKeyboardIndicator.getChildAt(position);
            oldPoint.setBackgroundResource(R.drawable.ic_point_normal);
            newPoint.setBackgroundResource(R.drawable.ic_point_select);

            oldPos = position;
        }
    }

    class EmojiPagerAdapter extends FragmentStatePagerAdapter {

        EmojiPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            EmojiFragment fragment = new EmojiFragment();
            fragment.init(emojiIcons[i], myImageGetter, inputAction, Type.Small);
            return fragment;
        }

        @Override
        public int getCount() {
            return emojiIcons.length;
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
        }
    }

    class MonkeyPagerAdapter extends FragmentStatePagerAdapter {

        MonkeyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            EmojiFragment fragment = new EmojiFragment();
            fragment.init(monkeyIcons[i], myImageGetter, inputAction, Type.Big);
            return fragment;
        }

        @Override
        public int getCount() {
            return monkeyIcons.length;
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
        }
    }

    class ZhongqiuPagerAdapter extends FragmentStatePagerAdapter {

        ZhongqiuPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            EmojiFragment fragment = new EmojiFragment();
            fragment.init(zhongqiuIcons[position], myImageGetter, inputAction, Type.Zhongqiu);
            return fragment;
        }

        @Override
        public int getCount() {
            return zhongqiuIcons.length;
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
        }
    }

    class CodePagerAdapter extends FragmentStatePagerAdapter {

        CodePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            EmojiFragment fragment = new EmojiFragment();
            fragment.init(codeIcons[position], myImageGetter, inputAction, Type.Small);
            return fragment;
        }

        @Override
        public int getCount() {
            return codeIcons.length;
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
        }
    }
}

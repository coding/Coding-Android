package net.coding.program.common.enter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import net.coding.program.Global;
import net.coding.program.R;
import net.coding.program.common.MyImageGetter;
import net.coding.program.message.EmojiFragment;

/**
 * Created by chaochen on 14-10-31.
 */
public class EnterEmojiLayout extends EnterLayout {

    private CheckBox checkBoxEmoji;
    private View emojiKeyboardLayout;
    private LinearLayout emojiKeyboardIndicator;

    private EmojiPagerAdapter mEmojiPagerAdapter;
    private MonkeyPagerAdapter mMonkeyPagerAdapter;
    private View selectEmoji;
    private View selectMonkey;
    private MyImageGetter myImageGetter;

    private FragmentActivity mActivity;

    private int rootViewHigh = 0;
    private final View rootView;

    public static enum EmojiType {
        Default, SmallOnly
    }

    public EnterEmojiLayout(FragmentActivity activity, View.OnClickListener sendTextOnClick, Type type) {
        this(activity, sendTextOnClick, type, EmojiType.Default);
    }

    public EnterEmojiLayout(FragmentActivity activity, View.OnClickListener sendTextOnClick, Type type, EmojiType emojiType) {
        super(activity, sendTextOnClick, type);

        mActivity = activity;
        myImageGetter = new MyImageGetter(activity);

        checkBoxEmoji = (CheckBox) activity.findViewById(R.id.popEmoji);
        checkBoxEmoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                content.requestFocus();
                if (checkBoxEmoji.isChecked()) {
                    rootViewHigh = rootView.getHeight();

                    final int bottomHigh = Global.dpToPx(100); // 底部虚拟按键高度，nexus5是73dp，以防万一，所以设大一点
                    int rootParentHigh = rootView.getRootView().getHeight();
                    if (rootParentHigh - rootViewHigh > bottomHigh) {
                        // 说明键盘已经弹出来了，等键盘消失后再设置 emoji keyboard 可见
                        Global.popSoftkeyboard(mActivity, content, false);

                        // 魅族手机的 rootView 无论输入法是否弹出高度都是不变的，只好搞个延时做这个事
                        rootView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                rootView.setLayoutParams(rootView.getLayoutParams());
                            }
                        }, 50);

                    } else {
                        emojiKeyboardLayout.setVisibility(View.VISIBLE);
                        rootViewHigh = 0;
                    }
                } else {
                    Global.popSoftkeyboard(mActivity, content, true);
                    emojiKeyboardLayout.setVisibility(View.GONE);
                }
            }
        });

        rootView = mActivity.findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (rootViewHigh == 0) {
                            return;
                        }

                        int high = rootView.getHeight();
                        if (high >= rootViewHigh) {
                            emojiKeyboardLayout.setVisibility(View.VISIBLE);
                            rootViewHigh = 0;
                        }
                    }
                }
        );

        emojiKeyboardLayout = activity.findViewById(R.id.emojiKeyboardLayout);

        final ViewPager viewPager = (ViewPager) activity.findViewById(R.id.viewPager);
        mEmojiPagerAdapter = new EmojiPagerAdapter(activity.getSupportFragmentManager());
        mMonkeyPagerAdapter = new MonkeyPagerAdapter(activity.getSupportFragmentManager());

        emojiKeyboardIndicator = (LinearLayout) mActivity.findViewById(R.id.emojiKeyboardIndicator);

        viewPager.setOnPageChangeListener(pageChange);

        selectEmoji = mActivity.findViewById(R.id.selectEmoji);
        selectEmoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setIndicatorCount(emojiIcons.length);

                if (viewPager.getAdapter() != mEmojiPagerAdapter) {
                    viewPager.setAdapter(mEmojiPagerAdapter);
                    pageChange.resetPos();
                }
                setPressEmojiType(EmojiFragment.Type.Small);
            }
        });

        selectMonkey = mActivity.findViewById(R.id.selectMonkey);
        if (emojiType == EmojiType.SmallOnly) {
            selectMonkey.setVisibility(View.INVISIBLE);
            mActivity.findViewById(R.id.selectMonkeyDivideLine).setVisibility(View.INVISIBLE);
        }

        selectMonkey.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setIndicatorCount(monkeyIcons.length);

                if (viewPager.getAdapter() != mMonkeyPagerAdapter) {
                    viewPager.setAdapter(mMonkeyPagerAdapter);
                    pageChange.resetPos();
                }
                setPressEmojiType(EmojiFragment.Type.Big);
            }
        });

        selectEmoji.performClick();

        content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emojiKeyboardLayout.setVisibility(View.GONE);
                checkBoxEmoji.setChecked(false);
            }
        });

        content.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                emojiKeyboardLayout.setVisibility(View.GONE);
                checkBoxEmoji.setChecked(false);
                return false;
            }
        });
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

    PageChangeListener pageChange = new PageChangeListener();


    public EnterEmojiLayout(FragmentActivity activity, View.OnClickListener sendTextOnClick) {
        this(activity, sendTextOnClick, Type.Default);
    }

    public boolean isEmojiKeyboardShowing() {
        return emojiKeyboardLayout.getVisibility() == View.VISIBLE;
    }

    public void closeEmojiKeyboard() {
        emojiKeyboardLayout.setVisibility(View.GONE);
        checkBoxEmoji.setChecked(false);
    }

    @Override
    public void hide() {
        closeEmojiKeyboard();
        super.hide();
    }

    private void setIndicatorCount(int count) {
        emojiKeyboardIndicator.removeAllViews();
        int pointWidth = mActivity.getResources().getDimensionPixelSize(R.dimen.point_width);
        int pointMargin = mActivity.getResources().getDimensionPixelSize(R.dimen.point_margin);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(pointWidth, pointWidth);
        lp.leftMargin = pointWidth;
        lp.rightMargin = pointMargin;
        for (int i = 0; i < count; ++i) {
            View pointView = mActivity.getLayoutInflater().inflate(R.layout.common_point, null);
            emojiKeyboardIndicator.addView(pointView, lp);
        }
        emojiKeyboardIndicator.getChildAt(0).setBackgroundResource(R.drawable.ic_point_select);
    }

    private void setPressEmojiType(EmojiFragment.Type type) {
        final int colorNormal = 0xffffffff;
        final int colorPress = 0xffe8e8e8;

        if (type == EmojiFragment.Type.Small) {
            selectEmoji.setBackgroundColor(colorPress);
            selectMonkey.setBackgroundColor(colorNormal);
        } else {
            selectEmoji.setBackgroundColor(colorNormal);
            selectMonkey.setBackgroundColor(colorPress);
        }
    }


    static String emojiIcons[][] = {{
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

    static String monkeyIcons[][] = new String[][]{{
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
            "coding_emoji_35",
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
            "coding_emoji_36"
    }};

    class EmojiPagerAdapter extends FragmentStatePagerAdapter {

        EmojiPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            EmojiFragment fragment = new EmojiFragment();
            fragment.init(emojiIcons[i], myImageGetter, EnterEmojiLayout.this, EmojiFragment.Type.Small);
            return fragment;
        }

        @Override
        public int getCount() {
            return emojiIcons.length;
        }
    }

    ;

    class MonkeyPagerAdapter extends FragmentStatePagerAdapter {

        MonkeyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            EmojiFragment fragment = new EmojiFragment();
            fragment.init(monkeyIcons[i], myImageGetter, EnterEmojiLayout.this, EmojiFragment.Type.Big);
            return fragment;
        }

        @Override
        public int getCount() {
            return monkeyIcons.length;
        }
    }

    ;

}

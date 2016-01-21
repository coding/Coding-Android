package net.coding.program.common.widget.input;

/**
 * Created by chaochen on 14-10-31.
 * 输入框
 */
public class EnterEmojiLayout { /* extends EnterLayout {


    protected ViewGroup mInputBox;//文本输入框所在的布局容器
    protected RippleBackground voiceLayout;
    protected InputType mInputType;
    protected boolean isSoftKeyBoard = false;
    PageChangeListener pageChange = new PageChangeListener();
    private CheckBox checkBoxEmoji;
    private View emojiKeyboardLayout;
    private LinearLayout emojiKeyboardIndicator;

    private MyImageGetter myImageGetter;
    private Activity mActivity;
    private boolean firstLayout = true;
    private int contentViewHeight;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowLp;
    private ImageView mWindowView;
    private int statusBarHeight;
    private FrameLayout mPanelLayout;

//    topbar 做这个事
//    private View.OnClickListener checkBoxEmojiOnClicked = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            content.requestFocus();
//            if (checkBoxEmoji.isChecked()) {
//                rootViewHigh = rootView.getHeight();
//
//                final int bottomHigh = Global.dpToPx(100); // 底部虚拟按键高度，nexus5是73dp，以防万一，所以设大一点
//                int rootParentHigh = rootView.getRootView().getHeight();
//                //rootParentHigh - rootViewHigh > bottomHigh
//                if (isSoftKeyBoard) {
//                    // 说明键盘已经弹出来了，等键盘消失后再设置 emoji keyboard 可见
//                    if (commonEnterRoot != null) {
//                        toggleInputTypeWithCloseSoftkeyboard(InputType.Emoji);
//                    } else {
//                        //兼容没有使用common_enter_emoji的输入控件
//                        Global.popSoftkeyboard(mActivity, content, false);
//                        mInputType = InputType.Emoji;
//                    }
//
//                    // 魅族手机的 rootView 无论输入法是否弹出高度都是不变的，只好搞个延时做这个事
//                    rootView.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            rootView.setLayoutParams(rootView.getLayoutParams());
//                        }
//                    }, 50);
//
//                } else {
//                    if (commonEnterRoot != null) {
//                        toggleNoTextInput(InputType.Emoji);
//                    } else {
//                        //兼容没有使用common_enter_emoji的输入控件
//                        emojiKeyboardLayout.setVisibility(View.VISIBLE);
//                    }
//
//                    rootViewHigh = 0;
//                }
//            } else {
//                if (commonEnterRoot != null) {
//                    toggleSoftkeyboardWithCloseNoTextInput(InputType.Emoji);
//                } else {
//                    //兼容没有使用common_enter_emoji的输入控件
//                    Global.popSoftkeyboard(mActivity, content, true);
//                    emojiKeyboardLayout.setVisibility(View.GONE);
//                }
//
//            }
//        }
//    };

    public EnterEmojiLayout(final FragmentActivity activity, View.OnClickListener sendTextOnClick, Type type, EmojiType emojiType) {
        super(activity, sendTextOnClick, type);

        mActivity = activity;
        myImageGetter = new MyImageGetter(activity);
        mWindowManager = (WindowManager) mActivity.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);

        voiceLayout = (RippleBackground) activity.findViewById(R.id.voiceLayout);
        mInputBox = (ViewGroup) activity.findViewById(R.id.mInputBox);
        checkBoxEmoji = (CheckBox) activity.findViewById(R.id.popEmoji);
        checkBoxEmoji.setOnClickListener(checkBoxEmojiOnClicked);
        mPanelLayout = (FrameLayout) activity.findViewById(R.id.mPanelLayout);


        emojiKeyboardLayout = activity.findViewById(R.id.emojiKeyboardLayout);

        final ViewPager viewPager = (ViewPager) activity.findViewById(R.id.viewPager);
        FragmentManager fragmentManager = activity.getSupportFragmentManager();

        emojiKeyboardIndicator = (LinearLayout) mActivity.findViewById(R.id.emojiKeyboardIndicator);

        viewPager.setOnPageChangeListener(pageChange);





        content.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    content.requestFocus();
                    if (commonEnterRoot != null) {
                        toggleSoftkeyboardWithCloseNoTextInput(mInputType);
                    } else {
                        //兼容没有使用common_enter_emoji的输入控件
                        emojiKeyboardLayout.setVisibility(View.GONE);
                        checkBoxEmoji.setChecked(false);
                    }
                }
                return false;
            }
        });
    }

    public EnterEmojiLayout(FragmentActivity activity, View.OnClickListener sendTextOnClick) {
        this(activity, sendTextOnClick, Type.Default, EmojiType.Default);
    }

    private void setInputStyle(int emojiKeyboard, int voiceKeyboard) {
        if (emojiKeyboardLayout != null) {
            emojiKeyboardLayout.setVisibility(emojiKeyboard);
        }
        if (voiceLayout != null) {
            voiceLayout.setVisibility(voiceKeyboard);
        }
    }

    private void dropTempWindow() {
        mInputBox.setVisibility(View.INVISIBLE);
        ValueAnimator va = ValueAnimator.ofInt(mEnterLayoutAnimSupportContainer.softkeyboardOpenY, mEnterLayoutAnimSupportContainer.openY);
        va.setDuration(300);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int y = (int) animation.getAnimatedValue();
                int b = (y - mEnterLayoutAnimSupportContainer.softkeyboardOpenY) * panelHeight / (mEnterLayoutAnimSupportContainer.openY - mEnterLayoutAnimSupportContainer.softkeyboardOpenY) - panelHeight;
                moveTempWindow(y);
                updateEnterLayoutBottom(b);
                if (y == mEnterLayoutAnimSupportContainer.openY) {
                    removeTempWindow();
                    mInputBox.setVisibility(View.VISIBLE);
                }
            }
        });
        va.start();

    }

    private void createTempWindow(View source) {
        if (mWindowView == null) {
            source.destroyDrawingCache();
            source.setDrawingCacheEnabled(true);
            source.buildDrawingCache();
            Bitmap shot = Bitmap.createBitmap(source.getDrawingCache());
            source.setDrawingCacheEnabled(false);
            mWindowView = new ImageView(mActivity);
            mWindowView.setImageBitmap(shot);
            if (mWindowLp == null) {
                mWindowLp = new WindowManager.LayoutParams();
                mWindowLp.height = source.getMeasuredHeight();
                mWindowLp.width = source.getMeasuredWidth();
                mWindowLp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
                mWindowLp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                mWindowLp.format = PixelFormat.RGBA_8888;
                mWindowLp.gravity = Gravity.TOP | Gravity.LEFT;
            }
            mWindowLp.x = source.getLeft();
            mWindowLp.y = mEnterLayoutAnimSupportContainer.softkeyboardOpenY;
            Log.w("softkeyboardOpenY", "" + mWindowLp.y);
            mWindowManager.addView(mWindowView, mWindowLp);
        } else {
            removeTempWindow();
        }
    }

    public void removeTempWindow() {
        if (mWindowView != null) {
            mWindowManager.removeViewImmediate(mWindowView);
            mWindowView = null;
        }
    }

    private void moveTempWindow(int y) {
        mWindowLp.y = y;
        mWindowManager.updateViewLayout(mWindowView, mWindowLp);
    }

    protected int getContentViewHeight(Activity activity) {
        Rect rect = new Rect();
        Window window = activity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rect);
        if (statusBarHeight == 0) {
            statusBarHeight = rect.top;
        }
        return rect.bottom - rect.top;
    }

    private View getCurrentNoTextInput() {
        if (mInputType != null && mInputType != InputType.Text) {
            switch (mInputType) {
                case Voice:
                    return voiceLayout;
                case Emoji:
                    return emojiKeyboardLayout;
            }
        }
        return null;
    }

    protected void toggleInputTypeWithCloseSoftkeyboard(InputType type) {
        mInputType = type;
        mEnterLayoutAnimSupportContainer.setCloseInputMethodBySelf(false);
        Global.popSoftkeyboard(mActivity, content, false);
        //  mPanelLayout.setVisibility(View.GONE);
        //去掉动画 不然太卡了
//        createTempWindow(mInputBox);
//        dropTempWindow();
    }

    protected void toggleSoftkeyboardWithCloseNoTextInput(InputType type) {
        checkBoxEmoji.setChecked(false);
        if (type != null && type != InputType.Text && mEnterLayoutStatus) {
            mInputType = type;
            voiceLayout.setVisibility(View.INVISIBLE);
            emojiKeyboardLayout.setVisibility(View.INVISIBLE);
            Global.popSoftkeyboard(mActivity, content, true);
            ViewHelper.setTranslationY(emojiKeyboardLayout, 0);
            ViewHelper.setTranslationY(voiceLayout, 0);
        } else {
            emojiKeyboardLayout.setVisibility(View.INVISIBLE);
            voiceLayout.setVisibility(View.INVISIBLE);
            ViewHelper.setTranslationY(emojiKeyboardLayout, 0);
            ViewHelper.setTranslationY(voiceLayout, 0);
            updateEnterLayoutBottom(-panelHeight);
            mInputType = InputType.Text;
            Global.popSoftkeyboard(mActivity, content, true);
        }
    }

    protected void toggleNoTextInput(InputType type) {
        mPanelLayout.setVisibility(View.VISIBLE);
        if (type != null && type != InputType.Text) {
            View dropView = getCurrentNoTextInput();
            mInputType = type;
            View popUpView = null;
            switch (type) {
                case Voice:
                    checkBoxEmoji.setChecked(false);
                    popUpView = voiceLayout;
                    if (!mEnterLayoutStatus) {
                        voiceLayout.setVisibility(View.VISIBLE);
                        emojiKeyboardLayout.setVisibility(View.GONE);
                    }
                    break;
                case Emoji:
                    checkBoxEmoji.setChecked(true);
                    popUpView = emojiKeyboardLayout;
                    if (!mEnterLayoutStatus) {
                        emojiKeyboardLayout.setVisibility(View.VISIBLE);
                        voiceLayout.setVisibility(View.GONE);
                    }
                    break;
            }
            mEnterLayoutStatus = mEnterLayoutAnimSupportContainer.isPanelLauoutOpen();
            if (mEnterLayoutStatus) {
                final View dropTarget = dropView;
                final View popUpTarget = popUpView;
                ViewHelper.setTranslationY(popUpView, panelHeight);
                ObjectAnimator drop = ObjectAnimator.ofFloat(dropView, "translationY", 0, panelHeight);
                drop.setDuration(180);
                drop.setInterpolator(new AccelerateInterpolator());
                drop.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        dropTarget.setVisibility(View.GONE);
                        popUpTarget.setVisibility(View.VISIBLE);
                        ObjectAnimator popUp = ObjectAnimator.ofFloat(popUpTarget, "translationY", panelHeight, 0);
                        popUp.setDuration(180);
                        popUp.setInterpolator(new DecelerateInterpolator());
                        popUp.start();
                    }
                });
                drop.start();
            } else {
                popUpView.setVisibility(View.VISIBLE);
                animEnterLayoutStatusChanaged(true);
            }
        }
    }

    @Override
    public void animEnterLayoutStatusChanaged(boolean isOpen) {
        super.animEnterLayoutStatusChanaged(isOpen);
        if (!isOpen) {
            ViewHelper.setTranslationY(emojiKeyboardLayout, 0);
            ViewHelper.setTranslationY(voiceLayout, 0);
        }
    }

    @Override
    protected void onEnterLayoutDropDown(int bottom) {
        super.onEnterLayoutDropDown(bottom);
    }

    @Override
    protected void onEnterLayoutPopUp(int bottom) {
        super.onEnterLayoutPopUp(bottom);
    }

    public boolean isEnterPanelShowing() {
        return mEnterLayoutStatus && (emojiKeyboardLayout.getVisibility() == View.VISIBLE || voiceLayout.getVisibility() == View.VISIBLE);
    }

    public void closeEnterPanel() {
        checkBoxEmoji.setChecked(false);
        if (commonEnterRoot != null) {
            if (mInputType != InputType.Voice) {
                animEnterLayoutStatusChanaged(false);
            }
        }

    }

    public void openEnterPanel() {
        checkBoxEmoji.setChecked(true);
        checkBoxEmojiOnClicked.onClick(checkBoxEmoji);
    }

    @Override
    public void hide() {
        closeEnterPanel();
        super.hide();
    }



    public enum EmojiType {
        Default, SmallOnly
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
*/

}

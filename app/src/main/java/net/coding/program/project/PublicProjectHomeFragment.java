package net.coding.program.project;


import android.content.DialogInterface;
import android.view.View;
import android.widget.TextView;

import com.flyco.roundview.RoundLinearLayout;
import com.loopj.android.http.RequestParams;
import com.readystatesoftware.viewbadger.BadgeView;

import net.coding.program.R;
import net.coding.program.common.CodingColor;
import net.coding.program.common.Global;
import net.coding.program.common.RedPointTip;
import net.coding.program.common.model.DynamicObject;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.param.ProjectJumpParam;
import net.coding.program.project.detail.MembersSelectActivity_;
import net.coding.program.project.detail.ProjectActivity_;
import net.coding.program.project.detail.ProjectFunction;
import net.coding.program.project.git.ForksListActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

@EFragment(R.layout.fragment_public_project_home)
//@OptionsMenu(R.menu.menu_fragment_project_home_public)
public class PublicProjectHomeFragment extends BaseProjectHomeFragment {

    @ViewById
    View buttonStar, buttonWatch, buttonFork;

    ProjectMarkButton mButtonStar;
    ProjectMarkButton mButtonWatch;
    ProjectMarkButton mButtonFork;

    String mUrlStar;
    String mUrlUnstar;
    String mUrlWatch;
    String mUrlUnwatch;
    View.OnClickListener onClickStatCount = v -> {
        String title = String.format("收藏了 %s 的人", mProjectObject.name);
        String url = mProjectObject.getHttptStargazers();
        startUserList(title, url);
    };
    View.OnClickListener onClickFollowCount = v -> {
        String title = String.format("关注了 %s 的人", mProjectObject.name);
        String url = mProjectObject.getHttptwatchers();
        startUserList(title, url);
    };
    View.OnClickListener onClickForkCount = v -> ForksListActivity_
            .intent(PublicProjectHomeFragment.this)
            .mProjectObject(mProjectObject)
            .start();
    private String httpProjectObject;
    private String forkUrl;

    @AfterViews
    final void init() {
        mUrlStar = mProjectObject.getHttpStar(true);
        mUrlUnstar = mProjectObject.getHttpStar(false);
        mUrlWatch = mProjectObject.getHttpWatch(true);
        mUrlUnwatch = mProjectObject.getHttpWatch(false);

        View mRoot = getView();
        initHead2();
        initHead3(mRoot);

        httpProjectObject = mProjectObject.getHttpProjectObject();

        if (needReload) {
            getNetwork(httpProjectObject);
        } else {
            initHead2();
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(httpProjectObject)) {
            if (code == 0) {
                mProjectObject = new ProjectObject(respanse.getJSONObject("data"));
                initHead2();
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(mUrlStar) || tag.equals(mUrlUnstar)) {
            umengEvent(UmengEvent.PROJECT, "收藏项目");
            if (tag.equals(mUrlStar)) {
                umengEvent(UmengEvent.CODE, "收藏");
            } else {
                umengEvent(UmengEvent.CODE, "取消收藏");
            }
            if (code != 0) {
                mButtonStar.changeState();
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(mUrlWatch) || tag.equals(mUrlUnwatch)) {
            umengEvent(UmengEvent.PROJECT, "关注项目");

            if (tag.equals(mUrlWatch)) {
                umengEvent(UmengEvent.CODE, "关注");
            } else {
                umengEvent(UmengEvent.CODE, "取消关注");
            }
            if (code != 0) {
                mButtonWatch.changeState();
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(forkUrl)) {
            showProgressBar(false);
            if (code == 0) {
                umengEvent(UmengEvent.PROJECT, "Fork");

                umengEvent(UmengEvent.CODE, "Fork");
                JSONObject jsonData = respanse.getJSONObject("data");
                String projectName = jsonData.optString("name");
                DynamicObject.Owner owner = new DynamicObject.Owner(jsonData.optJSONObject("owner"));
                ProjectJumpParam param = new ProjectJumpParam(owner.global_key,
                        projectName);
                ProjectHomeActivity_
                        .intent(this)
                        .mJumpParam(param)
                        .start();
                mButtonFork.changeState();
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    private void initHead2() {
        String[][] titles = new String[][]{
                new String[]{"收藏", "已收藏"},
                new String[]{"关注", "已关注"},
                new String[]{"Fork", "Fork"},
        };

        int[][] titlesColors = new int[][]{
                new int[]{CodingColor.font2, CodingColor.fontWhite},
                new int[]{CodingColor.font2, CodingColor.fontWhite},
                new int[]{CodingColor.font2, CodingColor.font2},
        };

        int[][] icons = new int[][]{
                new int[]{R.drawable.project_home_button_public_star, R.drawable.project_home_button_public_ok},
                new int[]{R.drawable.project_home_button_public_watch, R.drawable.project_home_button_public_ok},
                new int[]{R.drawable.project_home_button_public_fork, R.drawable.project_home_button_public_fork},
        };

        int[][] backgroundLeft = new int[][]{
                new int[]{CodingColor.bg, CodingColor.font1},
                new int[]{CodingColor.bg, CodingColor.fontBlue},
                new int[]{CodingColor.divideLine, CodingColor.divideLine},
        };

        mButtonStar = new ProjectMarkButton(buttonStar, titles[0], titlesColors[0], icons[0], backgroundLeft[0], mProjectObject.stared, mProjectObject.star_count, onClickStatCount);
        mButtonWatch = new ProjectMarkButton(buttonWatch, titles[1], titlesColors[1], icons[1], backgroundLeft[1], mProjectObject.watched, mProjectObject.watch_count, onClickFollowCount);
        mButtonFork = new ProjectMarkButton(buttonFork, titles[2], titlesColors[2], icons[2], backgroundLeft[2], mProjectObject.forked, mProjectObject.fork_count, onClickForkCount);
    }

    private void startUserList(String title, String url) {
//        UsersListActivity_.intent(PublicProjectHomeFragment.this)
//                .titleName(title)
//                .statUrl(url)
//                .start();
        MembersSelectActivity_.intent(this)
                .actionBarTitle(title)
                .userListUrl(url)
                .start();
    }

    protected ProjectFunction[] getItems() {
        return new ProjectFunction[]{
                ProjectFunction.dynamic,
                ProjectFunction.topic,
                ProjectFunction.code,
                ProjectFunction.member,
                ProjectFunction.readme,
                ProjectFunction.pullRequest
        };
    }

    private void initHead3(View root) {
        final ProjectFunction[] items = getItems();

        for (ProjectFunction item : items) {
            View view = root.findViewById(item.id);
            view.findViewById(R.id.icon).setBackgroundResource(item.icon);
            ((TextView) view.findViewById(R.id.title)).setText(item.title);
            view.setOnClickListener(v -> {
                switch (v.getId()) {
                    case R.id.itemDynamic:
                        updateDynamic();
                        break;

                    case R.id.itemCode:
                        break;

                    case R.id.itemReadme:
                        break;

                    case R.id.itemMerge:
                        markUsed(RedPointTip.Type.Merge320);
                        break;
                }

                ProjectActivity_.intent(PublicProjectHomeFragment.this)
                        .mProjectObject(mProjectObject)
                        .mJumpType(ProjectFunction.idToEnum(v.getId()))
                        .start();
            });

            if (item == ProjectFunction.dynamic) {
                dynamicBadge = (BadgeView) view.findViewById(R.id.badge);
                Global.setBadgeView(dynamicBadge, mProjectObject.unReadActivitiesCount);
            } else {
                Global.setBadgeView((BadgeView) view.findViewById(R.id.badge), 0);
            }
        }

        updateRedPoinitStyle();
    }

    void updateRedPoinitStyle() {
        final int[] buttons = new int[]{
                R.id.itemMerge
        };

        final RedPointTip.Type[] types = new RedPointTip.Type[]{
                RedPointTip.Type.Merge320
        };

        for (int i = 0; i < buttons.length; ++i) {
            setRedPointStyle(buttons[i], types[i]);
        }
    }

    protected void postNetwork(String url) {
        postNetwork(url, new RequestParams(), url);
    }

    public void getNetwork(String url) {
        getNetwork(url, url);
    }

    @Click
    protected final void buttonStar(View v) {
        mButtonStar.changeState();
        if (mButtonStar.isChecked()) {
            postNetwork(mUrlStar);
        } else {
            postNetwork(mUrlUnstar);
        }
    }

    @Click
    protected final void buttonWatch(View v) {
        mButtonWatch.changeState();
        if (mButtonWatch.isChecked()) {
            postNetwork(mUrlWatch);
        } else {
            postNetwork(mUrlUnwatch);
        }
    }

    @Click
    protected final void buttonFork(View v) {
        if (mProjectObject.isMy()) {
            showButtomToast("不能fork自己的项目");
        } else {
            forkUrl = Global.HOST_API + mProjectObject.getBackendProjectPath() + "/git/fork";
            showDialog("fork", "fork将会将此项目复制到您的个人空间，确定要fork吗?", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    postNetwork(forkUrl);
                    showProgressBar(true, "正在fork项目");
                }
            });
        }
    }

    class ProjectMarkButton {

        // 普通和选中
        String[] title;
        int[] titleColor;
        int[] icon;
        int[] leftBackground;

        boolean mCheck = false;
        int mCount = 0;
        View mButton;

        ProjectMarkButton(View button, String[] title, int[] titleColor, int[] icon, int[] leftBackground,
                          final boolean mCheck, int mCount, View.OnClickListener onClickListener) {
            this.title = title;
            this.titleColor = titleColor;
            this.icon = icon;
            this.leftBackground = leftBackground;
            this.mCheck = mCheck;
            this.mCount = mCount;
            this.mButton = button;

            button.findViewById(R.id.count).setOnClickListener(onClickListener);
            updateControl();
        }

        public void changeState() {
            checkButton(!mCheck);
        }

        public void checkButton(boolean check) {
            if (mCheck == check) {
                return;
            }

            mCheck = check;
            if (mCheck) {
                ++mCount;
            } else {
                --mCount;
            }

            updateControl();
        }

        private void updateControl() {
            mButton.setTag(mCheck);

            TextView countView = (TextView) mButton.findViewById(R.id.count);
            countView.setText(String.valueOf(mCount));
            countView.setTextColor(!mCheck ? titleColor[0] : titleColor[1]);

            mButton.findViewById(R.id.icon).setBackgroundResource(!mCheck ? icon[0] : icon[1]);
            TextView title = (TextView) mButton.findViewById(R.id.title);
            title.setText(!mCheck ? this.title[0] : this.title[1]);
            title.setTextColor(!mCheck ? titleColor[0] : titleColor[1]);

            View line = mButton.findViewById(R.id.divideLine);
            line.setBackgroundColor(!mCheck ? titleColor[0] : titleColor[1]);

            ((RoundLinearLayout) mButton).getDelegate().setBackgroundColor(!mCheck ? leftBackground[0] : leftBackground[1]);
        }

        public boolean isChecked() {
            return mCheck;
        }
    }
}

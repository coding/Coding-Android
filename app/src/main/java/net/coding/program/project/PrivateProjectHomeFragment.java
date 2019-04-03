package net.coding.program.project;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.TextView;

import com.readystatesoftware.viewbadger.BadgeView;
import com.tbruyelle.rxpermissions2.RxPermissions;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.RedPointTip;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.util.PermissionUtil;
import net.coding.program.compatible.CodingCompat;
import net.coding.program.project.detail.ProjectActivity_;
import net.coding.program.project.detail.ProjectFunction;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

@EFragment(R.layout.fragment_project_private)
@OptionsMenu(R.menu.menu_fragment_project_home)
public class PrivateProjectHomeFragment extends BaseProjectHomeFragment {

    public static final String TAG_HOST_PROJECT = "TAG_HOST_PROJECT";
    @ViewById
    View codeLayout1;

    protected ProjectFunction[] getItems() {
        return new ProjectFunction[]{
                ProjectFunction.dynamic,
                ProjectFunction.task,
                ProjectFunction.taskBoard,
                ProjectFunction.wiki,
                ProjectFunction.document,
                ProjectFunction.code,
                ProjectFunction.branchManage,
                ProjectFunction.releaseManage,
                ProjectFunction.merge,
                ProjectFunction.git,
        };
    }

    @SuppressLint("CheckResult")
    @AfterViews
    protected void initPrivateProjectHomeFragment() {
//        final String buttonTitle[] = getItemsTitle();
//        final int buttonIcon[] = getItemsIcon();
//        final int buttonId[] = getItemsId();

        final ProjectFunction[] items = getItems();

        for (ProjectFunction item : items) {
            View view = getView().findViewById(item.id);
            view.findViewById(R.id.icon).setBackgroundResource(item.icon);
            ((TextView) view.findViewById(R.id.title)).setText(item.title);

            view.setOnClickListener(v -> {
                switch (v.getId()) {
                    case R.id.itemDynamic:
                        updateDynamic();
                        break;

                    case R.id.itemTask:
                        break;

                    case R.id.itemCode:
                        break;

                    case R.id.itemReadme:
                        break;

                    case R.id.itemMerge:
                        markUsed(RedPointTip.Type.Merge320);
                        break;

                    case R.id.itemDocment:
                        markUsed(RedPointTip.Type.File320);
                        break;
                }

                ProjectActivity_.intent(PrivateProjectHomeFragment.this)
                        .mProjectObject(mProjectObject)
                        .mJumpType(ProjectFunction.idToEnum(v.getId()))
                        .start();
            });

            if (item.id == R.id.itemDynamic) {
                dynamicBadge = (BadgeView) view.findViewById(R.id.badge);
                Global.setBadgeView(dynamicBadge, mProjectObject.unReadActivitiesCount);
            } else {
                Global.setBadgeView((BadgeView) view.findViewById(R.id.badge), 0);
            }

        }

        updateRedPoinitStyle();

        bindUI();

        if (needReload) {
            getNetwork(mProjectObject.getHttpProjectObject(), TAG_HOST_PROJECT);
        }

    }

    @Click(R.id.itemReadme)
    void clickReadme(View v) {
        ProjectActivity_.intent(PrivateProjectHomeFragment.this)
                .mProjectObject(mProjectObject)
                .mJumpType(ProjectFunction.idToEnum(v.getId()))
                .start();
    }

    protected void bindUI() {
        Global.setBadgeView(dynamicBadge, mProjectObject.unReadActivitiesCount);

        if (mProjectObject.canReadCode()) {
            codeLayout1.setVisibility(View.VISIBLE);
        } else {
            codeLayout1.setVisibility(View.GONE);
        }

    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_HOST_PROJECT)) {
            if (code == 0) {
                mProjectObject = new ProjectObject(respanse.optJSONObject("data"));
                bindUI();
            }
        } else {
            super.parseJson(code, respanse, tag, pos, data);
        }
    }

    void updateRedPoinitStyle() {
        final int[] buttons = new int[]{
                R.id.itemDocment,
                R.id.itemMerge,
        };

        final RedPointTip.Type[] types = new RedPointTip.Type[]{
                RedPointTip.Type.File320,
                RedPointTip.Type.Merge320
        };

        for (int i = 0; i < buttons.length; ++i) {
            setRedPointStyle(buttons[i], types[i]);
        }
    }

    @OptionsItem
    void actionMaopao() {
        CodingCompat.instance().launchProjectMaopao(this, mProjectObject);
    }

//    @OptionsItem
//    void actionSearch() {
//
//    }

    //    private void initHeadHead(View view) {
//        ImageView projectIcon = (ImageView) view.findViewById(R.id.projectIcon);
//        iconfromNetwork(projectIcon, mProjectObject.icon, ImageLoadTool.optionsRounded2);
//
//        ((TextView) view.findViewById(R.id.projectName)).setText(mProjectObject.name);
//        view.findViewById(R.id.iconPrivate).setVisibility(View.VISIBLE);
//        ((TextView) view.findViewById(R.id.projectAuthor)).setText("      " + mProjectObject.owner_user_name);
//    }
}

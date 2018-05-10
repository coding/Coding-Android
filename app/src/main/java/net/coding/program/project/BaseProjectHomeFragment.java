package net.coding.program.project;


import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.readystatesoftware.viewbadger.BadgeView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.RedPointTip;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.project.init.setting.ProjectSetActivity_;
import net.coding.program.project.init.setting.ProjectSettingMainActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

@EFragment(R.layout.fragment_public_project_home)
public abstract class BaseProjectHomeFragment extends BaseFragment {

    public final String HOST_VISTIT = getHostVisit();
    protected boolean isUpdateDynamic = false;

    @FragmentArg
    ProjectObject mProjectObject;

    protected View.OnClickListener clickProjectSetting = v -> {
        if (GlobalData.isEnterprise() || !mProjectObject.isPublic) {
            ProjectSettingMainActivity_.intent(BaseProjectHomeFragment.this)
                    .projectObject(mProjectObject)
                    .start();
        } else {
            ProjectSetActivity_.intent(this).projectObject(mProjectObject).start();
        }
    };

    @FragmentArg
    boolean needReload = true;

    @ViewById
    View recommendIcon, projectHeaderLayout;

    @ViewById
    ImageView projectIcon;

    @ViewById
    TextView projectName, description, projectAuthor;

    BadgeView dynamicBadge;

    public static String getHostVisit() {
        return Global.HOST_API + "/project/%d/update_visit";
    }

    @AfterViews
    protected final void initBaseProjectHomeFragment() {
        iconfromNetwork(projectIcon, mProjectObject.icon, ImageLoadTool.optionsRounded2);

        projectName.setText(mProjectObject.name);
        projectAuthor.setText(mProjectObject.owner_user_name);

        if (mProjectObject.description.isEmpty()) {
            description.setText("未填写");
        } else {
            description.setText(mProjectObject.description);
        }

        initProjectSettingEntrance(projectHeaderLayout);
    }

    protected void initProjectSettingEntrance(View view) {
        view.findViewById(R.id.projectHeaderLayout).setOnClickListener(clickProjectSetting);
    }

    private void initHeadHead() {
        iconfromNetwork(projectIcon, mProjectObject.icon, ImageLoadTool.optionsRounded2);
        projectName.setText(mProjectObject.name);
        projectAuthor.setText(mProjectObject.owner_user_name);

        if (mProjectObject.description.isEmpty()) {
            description.setVisibility(View.GONE);
        } else {
            description.setVisibility(View.VISIBLE);
            description.setText(mProjectObject.description);
        }
    }

    protected void setRedPointStyle(int buttonId, RedPointTip.Type type) {
        View item = getView().findViewById(buttonId);
        View redPoint = item.findViewById(R.id.badge);
        boolean show = RedPointTip.show(getActivity(), type);
        redPoint.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    protected void markUsed(RedPointTip.Type type) {
        RedPointTip.markUsed(getActivity(), type);
        updateRedPoinitStyle();
    }

    abstract void updateRedPoinitStyle();

    protected final void updateDynamic() {
        String s = String.format(BaseProjectHomeFragment.getHostVisit(), mProjectObject.getId());
        getNetwork(s, HOST_VISTIT, 0, mProjectObject.getId());
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_VISTIT)) {
            if (respanse.getInt("code") == 0) {
                EventBus.getDefault().post(new EventProjectModify());
                Global.setBadgeView(dynamicBadge, 0);
                mProjectObject.setReadActivities();
            }
        }
    }
}

package net.coding.program.project;


import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.readystatesoftware.viewbadger.BadgeView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.RedPointTip;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.model.ProjectObject;
import net.coding.program.project.init.InitProUtils;
import net.coding.program.project.init.setting.ProjectSetActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

@EFragment(R.layout.fragment_public_project_home)
public abstract class BaseProjectHomeFragment extends BaseFragment {

    public static final String HOST_VISTIT = Global.HOST_API + "/project/%d/update_visit";
    protected boolean isUpdateDynamic = false;
    @FragmentArg
    ProjectObject mProjectObject;
    @ViewById
    View recommendIcon;
    @ViewById
    ImageView projectIcon;
    @ViewById
    TextView projectName;
    @ViewById
    TextView description;
    @ViewById
    TextView projectAuthor;
    @ViewById
    View projectHeaderLayout;
    BadgeView dynamicBadge;
    private boolean isBackToRefresh = false;

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

        isEnableProjectSet(projectHeaderLayout);
    }

    private void isEnableProjectSet(View view) {
        if (mProjectObject.isMy()) {
            view.findViewById(R.id.projectHeaderLayout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), ProjectSetActivity_.class);
                    intent.putExtra("projectObject", mProjectObject);
                    startActivityForResult(intent, InitProUtils.REQUEST_PRO_UPDATE);
                }
            });

        } else {
            view.findViewById(R.id.iconRight).setVisibility(View.GONE);
        }
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

    public boolean isBackToRefresh() {
        return isBackToRefresh;
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == InitProUtils.REQUEST_PRO_UPDATE) {
            if (resultCode == Activity.RESULT_OK) {
                mProjectObject = (net.coding.program.model.ProjectObject) data.getSerializableExtra("projectObject");
                isBackToRefresh = true;
                initHeadHead();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected final void updateDynamic() {
        String s = String.format(BaseProjectHomeFragment.HOST_VISTIT, mProjectObject.getId());
        getNetwork(s, HOST_VISTIT, 0, mProjectObject.getId());
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_VISTIT)) {
            if (respanse.getInt("code") == 0) {
                int id = (int) data;
                InitProUtils.updateDynamic(getActivity(), id);
                Global.setBadgeView(dynamicBadge, 0);
                mProjectObject.setReadActivities();
            }
        }
    }
}

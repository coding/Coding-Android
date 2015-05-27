package net.coding.program.project;


import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.network.BaseFragment;
import net.coding.program.model.ProjectObject;
import net.coding.program.project.init.InitProUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_public_project_home)
public class BaseProjectHomeFragment extends BaseFragment {

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

    @AfterViews
    protected final void initBaseProjectHomeFragment() {
        iconfromNetwork(projectIcon, mProjectObject.icon, ImageLoadTool.optionsRounded2);

        projectName.setText(mProjectObject.name);
        projectAuthor.setText(mProjectObject.owner_user_name);

        if (mProjectObject.description.isEmpty()) {
            description.setVisibility(View.GONE);
        } else {
            description.setText(mProjectObject.description);
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

    private boolean isBackToRefresh = false;

    public boolean isBackToRefresh() {
        return isBackToRefresh;
    }

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
}

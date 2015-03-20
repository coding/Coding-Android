package net.coding.program.project;

import android.support.v4.app.Fragment;
import android.widget.FrameLayout;

import net.coding.program.BaseActivity;
import net.coding.program.FileUrlActivity;
import net.coding.program.R;
import net.coding.program.model.ProjectObject;
import net.coding.program.project.detail.ProjectActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.json.JSONException;
import org.json.JSONObject;

@EActivity(R.layout.activity_project_home)
//@OptionsMenu(R.menu.menu_project_home)
public class ProjectHomeActivity extends BaseActivity {

    @Extra
    ProjectObject mProjectObject;

    @Extra
    ProjectActivity.ProjectJumpParam mJumpParam;

    @ViewById
    FrameLayout container;

    @StringArrayRes
    String[] dynamic_type_params;

    String mProjectUrl;

    @AfterViews
    protected void init() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (mProjectObject != null) {
            initFragment();
        } else {
            mProjectUrl = String.format(FileUrlActivity.HOST_PROJECT, mJumpParam.mUser, mJumpParam.mProject);
            getNetwork(mProjectUrl, mProjectUrl);
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(mProjectUrl)) {
            if (code == 0) {
                mProjectObject = new ProjectObject(respanse.getJSONObject("data"));
                initFragment();
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    private void initFragment() {
        Fragment fragment;
        if (mProjectObject.isPublic()) {
            fragment = PublicProjectHomeFragment_.builder()
                    .mProjectObject(mProjectObject)
                    .build();
        } else {
            fragment = PrivateProjectHomeFragment_.builder()
                    .mProjectObject(mProjectObject)
                    .mType(dynamic_type_params[0])
                    .build();
        }

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, fragment)
                .commit();
    }

    @OptionsItem(android.R.id.home)
    final protected void clickBack() {
        finish();
    }
}

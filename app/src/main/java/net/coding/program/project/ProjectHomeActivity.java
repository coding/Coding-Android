package net.coding.program.project;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.widget.FrameLayout;

import net.coding.program.R;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.compatible.CodingCompat;
import net.coding.program.param.ProjectJumpParam;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

@EActivity(R.layout.activity_project_home)
public class ProjectHomeActivity extends BackActivity {

    public static final String BROADCAST_CLOSE = ProjectHomeActivity.class.getName() + ".close";

    @Extra
    ProjectObject mProjectObject;

    @Extra
    ProjectJumpParam mJumpParam;

    @Extra
    boolean mNeedUpdateList = false; // 需要更新项目列表的排序

    @ViewById
    FrameLayout container;

    @StringArrayRes
    String[] dynamic_type_params;

    String mProjectUrl;

    private Fragment mCurrentFragment;

    @AfterViews
    protected void initProjectHomeActivity() {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (mProjectObject != null) {
            mProjectUrl = mProjectObject.getHttpProjectObject();
            initFragment(true);
        } else if (mJumpParam != null) {
            mProjectUrl = ProjectObject.getHttpProject(mJumpParam.user, mJumpParam.project);
            onRefrush();
        } else {
            finish();
        }
    }

    public void onRefrush() {
        getNetwork(mProjectUrl, mProjectUrl);
    }

    @Override
    protected boolean userEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventProjectModify(EventProjectModify event) {
        if (event.exitProject) {
            finish();
            return;
        }

        String url = event.projectUrl;
        if (url != null) {
            mProjectUrl = url;
        }

        onRefrush();
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(mProjectUrl)) {
            if (code == 0) {
                mProjectObject = new ProjectObject(respanse.getJSONObject("data"));
                initFragment(false);
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(PrivateProjectHomeFragment.getHostVisit())) {
            if (code == 0) {
                mNeedUpdateList = false;
                EventBus.getDefault().post(new EventProjectModify());
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    private void initFragment(boolean needRelaod) {
        if (mNeedUpdateList) {
            String url = String.format(PrivateProjectHomeFragment.getHostVisit(), mProjectObject.getId());
            getNetwork(url, PrivateProjectHomeFragment.getHostVisit());
        }

        Fragment fragment = CodingCompat.instance().getProjectHome(mProjectObject, needRelaod);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
        mCurrentFragment = fragment;
    }
}

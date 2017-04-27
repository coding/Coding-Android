package net.coding.program.project.detail;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import net.coding.program.FileUrlActivity;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.network.NetworkCallback;
import net.coding.program.common.network.NetworkImpl;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.model.ProjectObject;
import net.coding.program.param.ProjectJumpParam;
import net.coding.program.project.detail.wiki.WikiMainActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_project_parent)
public class ProjectActivity extends BackActivity implements NetworkCallback {

    @Extra
    ProjectObject mProjectObject;

    @Extra
    ProjectJumpParam mJumpParam;

    @Extra
    ProjectFunction mJumpType = ProjectFunction.dynamic;

    List<WeakReference<Fragment>> mFragments = new ArrayList<>();
    String urlProject;

    private NetworkImpl networkImpl;

    @ViewById
    TextView toolbarProjectTitle;

    @ViewById(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @AfterViews
    protected void initProjectActivity() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setActionBarTitle("");
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            AppBarLayout appbar = (AppBarLayout) findViewById(R.id.appbar);
//            appbar.setElevation(GlobalUnit.ACTIONBAR_SHADOW);
//        }

        if (mJumpParam != null) {
            urlProject = String.format(FileUrlActivity.getHostProject(), mJumpParam.user, mJumpParam.project);
            //setActionBarTitle(mJumpParam.mProject);

            networkImpl = new NetworkImpl(this, this);
            networkImpl.initSetting();

            getNetwork(urlProject, urlProject);

        } else if (mProjectObject != null) {
            //setActionBarTitle(mProjectObject.name);
            selectFragment();

        } else {
            finish();
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(urlProject)) {
            if (code == 0) {
                mProjectObject = new ProjectObject(respanse.getJSONObject("data"));
                selectFragment();
            } else {
                Toast.makeText(this, Global.getErrorMsg(respanse), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void getNetwork(String uri, String tag) {
        networkImpl.loadData(uri, null, tag, -1, null, NetworkImpl.Request.Get);
    }

    private void selectFragment() {
        if (mJumpType == ProjectFunction.wiki) {
            WikiMainActivity_.intent(this).project(mProjectObject).start();
            finish();
            overridePendingTransition(0, 0);
            return;
        }

        Fragment fragment;
        Bundle bundle = new Bundle();

        try {
            if (mJumpType == ProjectFunction.task) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    AppBarLayout appbar = (AppBarLayout) findViewById(R.id.appbar);
//                    appbar.setElevation(0);
//                }
            } else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                toolbarProjectTitle.setBackgroundResource(0);
            }

            Class fragmentClass = mJumpType.fragment;
            fragment = (Fragment) fragmentClass.newInstance();

            bundle.putSerializable("mProjectObject", mProjectObject);
            bundle.putSerializable("mProjectPath", ProjectObject.translatePath(mProjectObject.backend_project_path));
            fragment.setArguments(bundle);

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container, fragment, fragmentClass.getName());
            ft.commit();

            mFragments.add(new WeakReference(fragment));

            toolbarProjectTitle.setText(mJumpType.title);

        } catch (Exception e) {
            Global.errorLog(e);
        }
    }

    @OnActivityResult(ProjectAttachmentFragment.RESULT_REQUEST_FILES)
    void onFileResult(int resultCode, Intent data) {
        for (WeakReference<Fragment> item : mFragments) {
            Fragment f = item.get();
            if (f instanceof ProjectAttachmentFragment_) {
                ((ProjectAttachmentFragment_) f).onFileResult(resultCode, data);
            }
        }
    }

}

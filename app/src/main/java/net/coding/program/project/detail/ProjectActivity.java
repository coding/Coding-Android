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
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.network.NetworkCallback;
import net.coding.program.common.network.NetworkImpl;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.model.ProjectObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        if (mJumpParam != null) {
            urlProject = String.format(FileUrlActivity.getHostProject(), mJumpParam.mUser, mJumpParam.mProject);
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
        Fragment fragment;
        Bundle bundle = new Bundle();

        try {
            if (mJumpType == ProjectFunction.task) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            } else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
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
            if (mJumpType != ProjectFunction.task) {
                toolbarProjectTitle.setBackgroundResource(0);
            }

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

    public static class ProjectJumpParam implements Serializable {
        public String mProject = "";
        public String mUser = "";

        public ProjectJumpParam(String mUser, String mProject) {
            this.mUser = mUser;
            this.mProject = mProject;
        }

        public ProjectJumpParam(String path) {
            path = MyApp.transformEnterpriseUri(path);
            String[] regexs = new String[]{
                    "^/u/(.*?)/p/(.*?)(?:/git)?$",
                    "^/user/(.*)/project/(.*)$",
                    "^/t/(.*?)/p/(.*?)(?:/git)?$",
                    "^/team/(.*)/p/(.*)$"
            };
            for (String item : regexs) {
                if (isMatch(path, item)) {
                    return;
                }
            }
        }

        private boolean isMatch(String path, String regex) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(path);
            if (matcher.find()) {
                this.mUser = matcher.group(1);
                this.mProject = matcher.group(2);
                return true;
            }

            return false;
        }
    }
}

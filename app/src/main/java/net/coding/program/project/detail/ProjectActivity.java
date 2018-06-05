package net.coding.program.project.detail;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.network.NetworkCallback;
import net.coding.program.common.network.NetworkImpl;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.param.ProjectJumpParam;
import net.coding.program.project.detail.file.v2.ProjectFileMainActivity_;
import net.coding.program.project.detail.wiki.WikiMainActivity_;
import net.coding.program.project.git.local.GitMainActivity_;
import net.coding.program.task.board.TaskBoardActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
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
    @ViewById
    TextView toolbarProjectTitle;
    @ViewById(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    private NetworkImpl networkImpl;

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
            urlProject = ProjectObject.getHttpProject(mJumpParam.user, mJumpParam.project);
            networkImpl = new NetworkImpl(this, this);
            networkImpl.initSetting();

            getNetwork(urlProject, urlProject);

        } else if (mProjectObject != null) {
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
        if (mJumpType == ProjectFunction.member) {
            ProjectMembersActivity_.intent(this).projectObject(mProjectObject).start();
            finish();
            return;
        } else if (mJumpType == ProjectFunction.taskBoard) {
            TaskBoardActivity_.intent(this).projectObject(mProjectObject)
                    .param(new ProjectJumpParam(mProjectObject)).start();
            finish();
            return;
        } else if (mJumpType == ProjectFunction.git) {
            GitMainActivity_.intent(this).project(mProjectObject).start();
            finish();
            return;
        } else if (mJumpType == ProjectFunction.wiki) {
            WikiMainActivity_.intent(this).project(mProjectObject).start();
            finish();
            return;
        } else if (mJumpType == ProjectFunction.document) {
//            if (!GlobalData.isPrivateEnterprise()) {
            ProjectFileMainActivity_.intent(this).project(mProjectObject).start();
//            } else {
//                AttachmentsActivity_.intent(this)
//                        .mAttachmentFolderObject(AttachmentFolderObject.create(AttachmentFolderObject.ROOT_FOLDER_ID))
//                        .mProjectObjectId(mProjectObject.id)
//                        .mProject(mProjectObject)
//                        .start();
//            }
            finish();
            return;
        }

        Fragment fragment;
        Bundle bundle = new Bundle();

        try {
            if (mJumpType == ProjectFunction.task) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    AppBarLayout appbar = (AppBarLayout) findViewById(R.id.appbar);
                    appbar.setElevation(0);
                }
                hideActionbarShade();
            } else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                toolbarProjectTitle.setBackgroundResource(0);
            }

            Class fragmentClass = mJumpType.fragment;
            fragment = (Fragment) fragmentClass.newInstance();

            bundle.putSerializable("mProjectObject", mProjectObject);
            bundle.putSerializable("mProjectPath", ProjectObject.translatePath(mProjectObject.getBackendProjectPath()));
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
}

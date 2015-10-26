package net.coding.program.project.detail;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.widget.Toast;

import net.coding.program.FileUrlActivity;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.network.NetworkCallback;
import net.coding.program.common.network.NetworkImpl;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.model.ProjectObject;
import net.coding.program.project.detail.merge.ProjectMergeFragment_;
import net.coding.program.project.detail.merge.ProjectPullFragment_;
import net.coding.program.project.detail.readme.ReadmeFragment_;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EActivity(R.layout.activity_project)
public class ProjectActivity extends BackActivity implements NetworkCallback {

    public static final ProjectJumpParam.JumpType[] PRIVATE_JUMP_TYPES = new ProjectJumpParam.JumpType[]{
            ProjectJumpParam.JumpType.typeDynamic,
            ProjectJumpParam.JumpType.typeTask,
            ProjectJumpParam.JumpType.typeTopic,
            ProjectJumpParam.JumpType.typeDocument,
            ProjectJumpParam.JumpType.typeCode,
            ProjectJumpParam.JumpType.typeMember,
            ProjectJumpParam.JumpType.typeReadme,
            ProjectJumpParam.JumpType.typeMerge
    };
    public static final ProjectJumpParam.JumpType[] PUBLIC_JUMP_TYPES = new ProjectJumpParam.JumpType[]{
            ProjectJumpParam.JumpType.typeDynamic,
            ProjectJumpParam.JumpType.typeTopic,
            ProjectJumpParam.JumpType.typeCode,
            ProjectJumpParam.JumpType.typeMember,
            ProjectJumpParam.JumpType.typeReadme,
            ProjectJumpParam.JumpType.typeMerge
    };

    @Extra
    ProjectObject mProjectObject;

    @Extra
    ProjectJumpParam mJumpParam;

    @Extra
    ProjectJumpParam.JumpType mJumpType = ProjectJumpParam.JumpType.typeDynamic;

    List<WeakReference<Fragment>> mFragments = new ArrayList<>();
    ArrayList<String> project_activity_action_list = new ArrayList<>(Arrays.asList(
            "项目动态",
            "项目讨论",
            "项目代码",
            "项目成员",
            "readme",
            "mergerequest"
    ));
    String urlProject;

    //    MySpinnerAdapter mSpinnerAdapter;
    ArrayList<Integer> spinnerIcons = new ArrayList<>(Arrays.asList(
            R.drawable.ic_spinner_dynamic,
            R.drawable.ic_spinner_topic,
            R.drawable.ic_spinner_git,
            R.drawable.ic_spinner_user,
            R.drawable.ic_spinner_user,
            R.drawable.ic_spinner_user
    ));

    ArrayList<Class> spinnerFragments = new ArrayList<Class>(Arrays.asList(
            ProjectDynamicParentFragment_.class,
            TopicFragment_.class,
            ProjectGitFragmentMain_.class,
            MembersListFragment_.class,
            ReadmeFragment_.class,
            ProjectPullFragment_.class
    ));
    private NetworkImpl networkImpl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();

        if (mJumpParam != null) {
            urlProject = String.format(FileUrlActivity.HOST_PROJECT, mJumpParam.mUser, mJumpParam.mProject);
            actionBar.setTitle(mJumpParam.mProject);

            networkImpl = new NetworkImpl(this, this);
            networkImpl.initSetting();

            getNetwork(urlProject, urlProject);

        } else if (mProjectObject != null) {
            actionBar.setTitle(mProjectObject.name);
            initData();

        } else {
            finish();
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(urlProject)) {
            if (code == 0) {
                mProjectObject = new ProjectObject(respanse.getJSONObject("data"));
                initData();
            } else {
                Toast.makeText(this, Global.getErrorMsg(respanse), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void getNetwork(String uri, String tag) {
        networkImpl.loadData(uri, null, tag, -1, null, NetworkImpl.Request.Get);
    }

    private void initData() {
        // 私有项目才有任务
        if (!mProjectObject.isPublic()) {
            final int insertPos = 1;
            spinnerIcons.add(insertPos, R.drawable.ic_spinner_task);
            spinnerFragments.add(insertPos, ProjectTaskFragment_.class);
            project_activity_action_list.add(insertPos, "项目任务");

            // 私有项目添加项目文档
            final int insertAttPos = 3;
            spinnerIcons.add(insertAttPos, R.drawable.ic_spinner_attachment);
            spinnerFragments.add(insertAttPos, ProjectAttachmentFragment_.class);
            project_activity_action_list.add(insertAttPos, "项目文件");
        }
        selectFragment(getJumpPos());
    }

    private int getJumpPos() {
        if (mProjectObject.isPublic()) {
            for (int i = 0; i < PUBLIC_JUMP_TYPES.length; ++i) {
                if (PUBLIC_JUMP_TYPES[i] == mJumpType) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < PRIVATE_JUMP_TYPES.length; ++i) {
                if (PRIVATE_JUMP_TYPES[i] == mJumpType) {
                    return i;
                }
            }
        }

        return 0;
    }

    private void selectFragment(int position) {
        Fragment fragment;
        Bundle bundle = new Bundle();

        try {
            Class fragmentClass = spinnerFragments.get(position);
            if (fragmentClass == ProjectPullFragment_.class && !mProjectObject.isPublic()) {
                fragmentClass = ProjectMergeFragment_.class;
            }

            fragment = (Fragment) fragmentClass.newInstance();

            bundle.putSerializable("mProjectObject", mProjectObject);
            bundle.putSerializable("mProjectPath", ProjectObject.translatePath(mProjectObject.project_path));
            fragment.setArguments(bundle);

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container, fragment, project_activity_action_list.get(position));
            ft.commit();

            mFragments.add(new WeakReference(fragment));

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
            Pattern pattern = Pattern.compile("^/u/(\\w*)/p/(\\w*)$");
            Matcher matcher = pattern.matcher(path);
            if (matcher.find()) {
                this.mUser = matcher.group(1);
                this.mProject = matcher.group(2);
            }
        }

        public enum JumpType {
            typeDynamic,
            typeTask,
            typeTopic,
            typeDocument,
            typeCode,
            typeMember,
            typeReadme,
            typeMerge,
            typeHome
        }
    }
}

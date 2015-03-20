package net.coding.program.project.detail;

import android.support.v7.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import net.coding.program.BaseActivity;
import net.coding.program.FileUrlActivity;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.network.NetworkCallback;
import net.coding.program.common.network.NetworkImpl;
import net.coding.program.model.ProjectObject;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@EActivity(R.layout.activity_project)
public class ProjectActivity extends BaseActivity implements NetworkCallback {

    public static final ProjectJumpParam.JumpType[] PRIVATE_JUMP_TYPES = new ProjectJumpParam.JumpType[] {
            ProjectJumpParam.JumpType.typeDynamic,
            ProjectJumpParam.JumpType.typeTask,
            ProjectJumpParam.JumpType.typeTopic,
            ProjectJumpParam.JumpType.typeDocument,
            ProjectJumpParam.JumpType.typeCode,
            ProjectJumpParam.JumpType.typeMember,
    };
    public static final ProjectJumpParam.JumpType[] PUBLIC_JUMP_TYPES = new ProjectJumpParam.JumpType[] {
            ProjectJumpParam.JumpType.typeDynamic,
            ProjectJumpParam.JumpType.typeTopic,
            ProjectJumpParam.JumpType.typeCode,
            ProjectJumpParam.JumpType.typeMember,
    };
    @Extra
    ProjectObject mProjectObject;

    @Extra
    ProjectJumpParam mJumpParam;

//    @Extra
//    int mPos = 0;
    @Extra
    ProjectJumpParam.JumpType mJumpType = ProjectJumpParam.JumpType.typeDynamic;

    List<WeakReference<Fragment>> mFragments = new ArrayList<>();


    public static class ProjectJumpParam implements Serializable {
        public String mProject;
        public String mUser;

        public enum JumpType {
            typeDynamic,
            typeTask,
            typeTopic,
            typeDocument,
            typeCode,
            typeMember,
            typeHome
        }

        public ProjectJumpParam(String mUser, String mProject) {
            this.mUser = mUser;
            this.mProject = mProject;
//            this.mJumpType = mJumpType;
        }
    }

    ArrayList<String> project_activity_action_list = new ArrayList(Arrays.asList(
            "项目动态",
            "项目讨论",
            "项目代码",
            "项目成员"
    ));

//    MySpinnerAdapter mSpinnerAdapter;

    String urlProject;

    private NetworkImpl networkImpl;

    ArrayList<Integer> spinnerIcons = new ArrayList(Arrays.asList(
            R.drawable.ic_spinner_dynamic,
            R.drawable.ic_spinner_topic,
            R.drawable.ic_spinner_git,
            R.drawable.ic_spinner_user
    ));

    ArrayList<Class> spinnerFragments = new ArrayList<Class>(Arrays.asList(
            ProjectDynamicParentFragment_.class,
            TopicFragment_.class,
            ProjectGitFragmentMain_.class,
            MembersListFragment_.class
    ));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (mProjectObject == null) {
            urlProject = String.format(FileUrlActivity.HOST_PROJECT, mJumpParam.mUser, mJumpParam.mProject);
            actionBar.setTitle(mJumpParam.mProject);

            networkImpl = new NetworkImpl(this, this);
            networkImpl.initSetting();

            getNetwork(urlProject, urlProject);

        } else {
            actionBar.setTitle(mProjectObject.name);
            initData();
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
            project_activity_action_list.add(insertAttPos, "项目文档");
        }
        selectFragment(getJumpPos());

//        mSpinnerAdapter = new MySpinnerAdapter(getLayoutInflater());

//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayShowTitleEnabled(true);
//        actionBar.setDisplayShowCustomEnabled(true);
//        actionBar.setCustomView(R.layout.actionbar_custom_spinner);
//        Spinner spinner = (Spinner) actionBar.getCustomView().findViewById(R.id.spinner);
//        spinner.setAdapter(mSpinnerAdapter);
//
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                selectFragment(position);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//
//        if (mJumpParam != null && !mJumpParam.mDefault.isEmpty()) {
//            spinner.setSelection(1);
//        }
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
//        mSpinnerAdapter.setCheckPos(position);

        Fragment fragment;
        Bundle bundle = new Bundle();

        try {
            fragment = (Fragment) spinnerFragments.get(position).newInstance();

            bundle.putSerializable("mProjectObject", mProjectObject);
            fragment.setArguments(bundle);

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container, fragment, project_activity_action_list.get(position));
            ft.commit();

            mFragments.add(new WeakReference(fragment));

        } catch (Exception e) {
            Global.errorLog(e);
        }
    }


    @OptionsItem(android.R.id.home)
    void close() {
        onBackPressed();
    }

//    class MySpinnerAdapter extends BaseAdapter {
//
//        private LayoutInflater inflater;
//
//        public MySpinnerAdapter(LayoutInflater inflater) {
//            this.inflater = inflater;
//        }
//
//        int checkPos = 0;
//
//        public void setCheckPos(int pos) {
//            checkPos = pos;
//        }
//
//
//        @Override
//        public int getCount() {
//            return spinnerIcons.size();
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return position;
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return position;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            if (convertView == null) {
//                convertView = inflater.inflate(R.layout.spinner_layout_head, parent, false);
//            }
//
//            ((TextView) convertView).setText(project_activity_action_list.get(position));
//
//            return convertView;
//        }
//
//        @Override
//        public View getDropDownView(int position, View convertView, ViewGroup parent) {
//            if (convertView == null) {
//                convertView = inflater.inflate(R.layout.spinner_layout_item, parent, false);
//            }
//
//            TextView title = (TextView) convertView.findViewById(R.id.title);
//            title.setText(project_activity_action_list.get(position));
//
//            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
//            icon.setImageResource(spinnerIcons.get(position));
//
//            if (checkPos == position) {
//                convertView.setBackgroundColor(getResources().getColor(R.color.green));
//            } else {
//                convertView.setBackgroundColor(getResources().getColor(R.color.spinner_black));
//            }
//
//
//            return convertView;
//        }
//    }

    @OnActivityResult(ProjectAttachmentFragment.RESULT_REQUEST_FILES)
    void onFileResult(int resultCode, Intent data) {

        for (WeakReference<Fragment> item : mFragments) {
            Fragment f = item.get();
            if (f != null && f instanceof ProjectAttachmentFragment_) {
                ((ProjectAttachmentFragment_) f).onFileResult(resultCode, data);
            }
        }
    }
}

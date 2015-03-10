package net.coding.program.project.detail;

import android.support.v7.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
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

    @Extra
    ProjectObject mProjectObject;

    @Extra
    ProjectJumpParam mJumpParam;

    List<WeakReference<Fragment>> mFragments = new ArrayList<>();

    public static class ProjectJumpParam implements Serializable {
        public String mProject;
        public String mUser;
        public String mDefault = ""; // 如果为""就说明要跳转到


        public ProjectJumpParam(String mUser, String mProject, String defaultSelect) {
            this.mUser = mUser;
            this.mProject = mProject;

            if (defaultSelect != null) {
                this.mDefault = defaultSelect;
            }
        }
    }

    ArrayList<String> project_activity_action_list = new ArrayList(Arrays.asList(
            "项目动态",
            "项目讨论",
            "项目成员"
    ));

    MySpinnerAdapter mSpinnerAdapter;

    String urlProject;

    private NetworkImpl networkImpl;

    ArrayList<Integer> spinnerIcons = new ArrayList(Arrays.asList(
            R.drawable.ic_spinner_dynamic,
            R.drawable.ic_spinner_topic,
            R.drawable.ic_spinner_user
    ));

    ArrayList<Class> spinnerFragments = new ArrayList<Class>(Arrays.asList(
            ProjectDynamicParentFragment_.class,
            TopicFragment_.class,
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
        if (mProjectObject.type == 2) {
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

        final int insertGitPos = Math.min(spinnerIcons.size(), 4);
        spinnerIcons.add(insertGitPos, R.drawable.ic_spinner_git);
        spinnerFragments.add(insertGitPos, ProjectGitFragmentMain_.class);
        project_activity_action_list.add(insertGitPos, "项目代码");

        mSpinnerAdapter = new MySpinnerAdapter(getLayoutInflater());

//        final BaseAdapter mOnNavigationListener = new  {
//
//            @Override
//            public boolean onNavigationItemSelected(int position, long itemId) {
//
//                return true;
//            }
//        };

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.actionbar_custom_spinner);
        Spinner spinner = (Spinner) actionBar.getCustomView().findViewById(R.id.spinner);
        spinner.setAdapter(mSpinnerAdapter);
//        spinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            }
//        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                mSpinnerAdapter.setCheckPos(position);

                Fragment fragment;
                Bundle bundle = new Bundle();

                try {
                    fragment = (Fragment) spinnerFragments.get(position).newInstance();

                    bundle.putSerializable("mProjectObject", mProjectObject);
                    fragment.setArguments(bundle);

                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.container, fragment, project_activity_action_list.get(position));
                    ft.commit();

                    mFragments.add(new WeakReference<Fragment>(fragment));

                } catch (Exception e) {
                    Global.errorLog(e);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
//        actionBar.setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);

        if (mJumpParam != null && !mJumpParam.mDefault.isEmpty()) {
//            actionBar.setSelectedNavigationItem(1);
            spinner.setSelection(1);
        }
    }


    @OptionsItem(android.R.id.home)
    void close() {
        onBackPressed();
    }

    class MySpinnerAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        public MySpinnerAdapter(LayoutInflater inflater) {
            this.inflater = inflater;
        }

        int checkPos = 0;

        public void setCheckPos(int pos) {
            checkPos = pos;
        }


        @Override
        public int getCount() {
            return spinnerIcons.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.spinner_layout_head, parent, false);
            }

            ((TextView) convertView).setText(project_activity_action_list.get(position));

            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.spinner_layout_item, parent, false);
            }

            TextView title = (TextView) convertView.findViewById(R.id.title);
            title.setText(project_activity_action_list.get(position));

            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
            icon.setImageResource(spinnerIcons.get(position));

            if (checkPos == position) {
                convertView.setBackgroundColor(getResources().getColor(R.color.green));
            } else {
                convertView.setBackgroundColor(getResources().getColor(R.color.spinner_black));
            }


            return convertView;
        }
    }

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

package net.coding.program.project.detail;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.model.TaskObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

@EActivity(R.layout.activity_member_authority)
@OptionsMenu(R.menu.member_authority)
public class MemberAuthorityActivity extends BackActivity {

    @Extra
    TaskObject.Members member;

    @Extra
    TaskObject.Members me;

    @Extra
    int projectId;

    @ViewById
    DropdownListItemView projectManager, projectMember, projectMemberLimited;

    @ViewById
    View managerDivide;

    @AfterViews
    void initMemberAuthorityActivity() {
        if (me.getType() == TaskObject.Members.Type.manager) {
            projectManager.setVisibility(View.GONE);
            managerDivide.setVisibility(View.GONE);
        }

        projectManager.setText("项目管理员");
        projectMember.setText("项目成员");
        projectMemberLimited.setText("受限成员");

        projectManager.setChecked(false);
        projectMember.setChecked(false);
        projectMemberLimited.setChecked(false);

        switch (member.getType()) {
            case manager:
                projectManager.setChecked(true);
                break;
            case member:
                projectMember.setChecked(true);
                break;
            case limited:
                projectMemberLimited.setChecked(true);
                break;
        }
    }

    @Click
    void projectManager() {
        modifyAuthority(TaskObject.Members.Type.manager.getType());
    }

    @Click
    void projectMember() {
        modifyAuthority(TaskObject.Members.Type.member.getType());
    }

    @Click
    void projectMemberLimited() {
        modifyAuthority(TaskObject.Members.Type.limited.getType());
    }

    private void modifyAuthority(int id) {
        String url = String.format(Global.HOST_API + "/project/%d/member/%s/%d", projectId, member.user.global_key, id);
        MyAsyncHttpClient.post(this, url, new MyJsonResponse(MemberAuthorityActivity.this) {
            @Override
            public void onMySuccess(JSONObject response) {
                super.onMySuccess(response);
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFinish() {
                super.onFinish();
                showProgressBar(false);
            }
        });
        showProgressBar(true);
    }

    @OptionsItem
    void action_about() {
        showPop("项目所有者：拥有对项目的所有权限。\n" +
                "项目管理员：拥有对项目的部分权限。不能删除，转让项目，不能对其他管理员进行操作。\n" +
                "普通成员：可以阅读和推送代码。\n" +
                "受限成员：不能进入与代码相关的页面。");
    }


    private void showPop(String s) {
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        android.support.v4.app.Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        DialogFragment newFragment = MyDialogFragment.newInstance(s);
        newFragment.show(ft, "dialog");
    }

    public static class MyDialogFragment extends DialogFragment {
        String mStringData;

        static MyDialogFragment newInstance(String data) {
            MyDialogFragment f = new MyDialogFragment();

            // Supply data input as an argument.
            Bundle args = new Bundle();
            args.putString("data", data);
            f.setArguments(args);

            return f;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mStringData = getArguments().getString("data", "");
            setStyle(DialogFragment.STYLE_NORMAL, R.style.MyDialog);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.skill_workexp_tip, container, false);
            TextView textView = (TextView) v.findViewById(R.id.tipText);
            textView.setText(mStringData);
            v.setOnClickListener(v1 -> getActivity().getSupportFragmentManager().popBackStack());

            return v;
        }
    }
}

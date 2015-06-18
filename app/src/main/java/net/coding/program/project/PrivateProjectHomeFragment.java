package net.coding.program.project;

import android.view.View;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.project.detail.ProjectActivity;
import net.coding.program.project.detail.ProjectActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

@EFragment(R.layout.fragment_project_private)
public class PrivateProjectHomeFragment extends BaseProjectHomeFragment {

    @AfterViews
    protected void init2() {
        final String buttonTitle[] = new String[]{
                "动态",
                "任务",
                "讨论",
                "文档",
                "代码",
                "成员",
                "Readme",
                "Merge Request"
        };

        final int buttonIcon[] = new int[]{
                R.drawable.project_button_icon_dynamic,
                R.drawable.project_button_icon_task,
                R.drawable.project_button_icon_topic,
                R.drawable.project_button_icon_docment,
                R.drawable.project_button_icon_code,
                R.drawable.project_button_icon_member,
                R.drawable.project_button_icon_readme,
                R.drawable.project_button_icon_merge,
        };

        final int buttonId[] = new int[]{
                R.id.itemDynamic,
                R.id.itemTask,
                R.id.itemTopic,
                R.id.itemDocment,
                R.id.itemCode,
                R.id.itemMember,
                R.id.itemReadme,
                R.id.itemMerge
        };

        for (int i = 0; i < buttonId.length; ++i) {
            View button = getView().findViewById(buttonId[i]);
            button.findViewById(R.id.icon).setBackgroundResource(buttonIcon[i]);
            ((TextView) button.findViewById(R.id.title)).setText(buttonTitle[i]);
            final int pos = i;
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ProjectActivity_.intent(PrivateProjectHomeFragment.this)
                            .mProjectObject(mProjectObject)
                            .mJumpType(ProjectActivity.PRIVATE_JUMP_TYPES[pos])
                            .start();
                }
            });
        }
    }

//    private void initHeadHead(View view) {
//        ImageView projectIcon = (ImageView) view.findViewById(R.id.projectIcon);
//        iconfromNetwork(projectIcon, mProjectObject.icon, ImageLoadTool.optionsRounded2);
//
//        ((TextView) view.findViewById(R.id.projectName)).setText(mProjectObject.name);
//        view.findViewById(R.id.iconPrivate).setVisibility(View.VISIBLE);
//        ((TextView) view.findViewById(R.id.projectAuthor)).setText("      " + mProjectObject.owner_user_name);
//    }
}

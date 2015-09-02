package net.coding.program.project;

import android.view.View;
import android.widget.TextView;

import com.readystatesoftware.viewbadger.BadgeView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.RedPointTip;
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
                "文件",
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
            View item = getView().findViewById(buttonId[i]);
            item.findViewById(R.id.icon).setBackgroundResource(buttonIcon[i]);
            ((TextView) item.findViewById(R.id.title)).setText(buttonTitle[i]);
            final int pos = i;
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (buttonId[pos]) {
                        case R.id.itemDynamic:
                            updateDynamic();
                            break;

                        case R.id.itemTask:
                            break;

                        case R.id.itemCode:
                            break;

                        case R.id.itemReadme:
                            break;

                        case R.id.itemMerge:
                            markUsed(RedPointTip.Type.Merge320);
                            break;

                        case R.id.itemDocment:
                            markUsed(RedPointTip.Type.File320);
                            break;
                    }

                    ProjectActivity_.intent(PrivateProjectHomeFragment.this)
                            .mProjectObject(mProjectObject)
                            .mJumpType(ProjectActivity.PRIVATE_JUMP_TYPES[pos])
                            .start();
                }

            });

            if (buttonId[i] == R.id.itemDynamic) {
                dynamicBadge = (BadgeView) item.findViewById(R.id.badge);
                Global.setBadgeView(dynamicBadge, mProjectObject.un_read_activities_count);
            } else {
                Global.setBadgeView((BadgeView) item.findViewById(R.id.badge), 0);
            }
        }

        updateRedPoinitStyle();
    }


    void updateRedPoinitStyle() {
        final int[] buttons = new int[]{
                R.id.itemDocment,
                R.id.itemMerge,
        };

        final RedPointTip.Type[] types = new RedPointTip.Type[]{
                RedPointTip.Type.File320,
                RedPointTip.Type.Merge320
        };

        for (int i = 0; i < buttons.length; ++i) {
            setRedPointStyle(buttons[i], types[i]);
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

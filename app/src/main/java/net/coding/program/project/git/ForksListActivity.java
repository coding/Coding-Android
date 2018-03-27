package net.coding.program.project.git;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalCommon;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.ui.ActivityParamBuilder;
import net.coding.program.common.ui.BaseListActivity;
import net.coding.program.param.ProjectJumpParam;
import net.coding.program.project.ProjectHomeActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

@EActivity(R.layout.activity_notify_list)
public class ForksListActivity extends BaseListActivity {

    @AfterViews
    public void initForksListActivity() {
        initBaseListActivity();
    }

    @Extra
    ProjectObject mProjectObject;
    AdapterView.OnItemClickListener mItemClick = (parent, view, position, id) -> {
        ProjectObject projectObject = (ProjectObject) parent.getItemAtPosition(position);
        ProjectJumpParam param = new ProjectJumpParam(projectObject.depot_path);
        ProjectHomeActivity_.intent(ForksListActivity.this)
                .mJumpParam(param)
                .start();
    };

    @Override
    public ActivityParam getActivityParam() {
        String title = String.format("Fork 了 %s 的人", mProjectObject.name);
        return new ActivityParamBuilder().setTitle(title)
                .setUrl(Global.HOST_API + mProjectObject.getProjectPath() + "/git/forks/paging?pageSize=20")
                .setViewHold(ViewHold.class)
                .setItemClick(mItemClick)
                .createActivityParam();
    }

    public static class ViewHold implements BaseViewHold {
        ImageView icon;
        TextView title;
        TextView content;
        ImageLoadTool mImageTool;

        public int getLayout() {
            return R.layout.project_fork_list_item;
        }

        public void init(View v, ImageLoadTool imageLoadTool) {
            icon = (ImageView) v.findViewById(R.id.icon);
            title = (TextView) v.findViewById(R.id.name);
            content = (TextView) v.findViewById(R.id.comment);
            mImageTool = imageLoadTool;
            icon.setOnClickListener(GlobalCommon.mOnClickUser);
        }

        public void setData(Object item) {
            ProjectObject data = (ProjectObject) item;
            title.setText(data.getForkPath());
            content.setText(String.format("%s Fork于 %s", data.owner_user_name, Global.mDateFormat.format(data.created_at)));
            mImageTool.loadImage(icon, data.getOwner().avatar);
            icon.setTag(data.getOwner().global_key);
        }
    }

}

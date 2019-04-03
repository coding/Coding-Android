package net.coding.program.common.ui;

import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.TextView;

import com.cpiz.android.bubbleview.BubblePopupWindow;
import com.cpiz.android.bubbleview.BubbleStyle;
import com.cpiz.android.bubbleview.BubbleTextView;

import net.coding.program.R;
import net.coding.program.common.RedPointTip;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.param.ProjectJumpParam;
import net.coding.program.project.ProjectHomeActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;

/*
 * 使用 common_actionbar 的 activity
 */

@EActivity
public abstract class CodingToolbarBackActivity extends BaseActivity {

    private Toolbar toolbar;
    private TextView toolbarTitle;

    private BubblePopupWindow bubbleTitle = null;

    private boolean isResume = false;

    protected abstract
    @Nullable
    ProjectObject getProject();

    @Extra
    protected boolean showClickTitleTip = false;

    protected String getProjectPath() {
        return "";
    }

    private void onClickTollbarTitle() {
        ProjectObject projectObject = getProject();
        if (projectObject != null) {
            ProjectHomeActivity_.intent(this).mProjectObject(projectObject).start();
            hideGuide();
            return;
        }

        String path = getProjectPath();
        if (!TextUtils.isEmpty(path)) {
            hideGuide();
            ProjectJumpParam param = new ProjectJumpParam(path);
            ProjectHomeActivity_.intent(this).mJumpParam(param).start();
        }
    }

    private void hideGuide() {
        if (bubbleTitle != null && bubbleTitle.isShowing()) {
            bubbleTitle.dismiss();
        }

        RedPointTip.markUsed(this, RedPointTip.Type.TitleJump_C445);
        bubbleTitle = null;
    }

    @AfterViews
    protected final void annotationDispayHomeAsUp() {
        toolbar = (Toolbar) findViewById(R.id.codingToolbar);
        if (toolbar == null) {
            throw new RuntimeException("not include codingToolbar");
        }

        toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbarTitle);
        toolbarTitle.setOnClickListener(v -> onClickTollbarTitle());

        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

    }

    @Override
    public void setActionBarTitle(String title) {
        toolbarTitle.setText(title);

        popGuide();
    }

    @Override
    public void onResume() {
        super.onResume();
        isResume = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isResume = false;
    }

    @UiThread(delay = 2000)
    protected void popGuide() {

        if (RedPointTip.show(this, RedPointTip.Type.TitleJump_C445) && isResume && showClickTitleTip) {
            BubbleTextView bbView = (BubbleTextView) getLayoutInflater().inflate(R.layout.guide_bubble_view_up, null);
            bbView.setText("点击标题可跳转到项目首页哦");
            bbView.setOnClickListener(v -> hideGuide());

            bubbleTitle = new BubblePopupWindow(bbView, bbView);
            bubbleTitle.setCancelOnTouchOutside(false);
            bubbleTitle.showArrowTo(toolbarTitle, BubbleStyle.ArrowDirection.Up);
        }
    }

}

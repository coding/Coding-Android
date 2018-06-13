package net.coding.program.project.detail;

import android.support.annotation.NonNull;
import android.webkit.WebView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.CodingGlobal;
import net.coding.program.R;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.model.TopicLabelObject;
import net.coding.program.common.model.topic.TopicData;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.BaseFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

import java.util.List;

@EFragment(R.layout.fragment_topic_preview)
@OptionsMenu(R.menu.topic_detail_edit_preview)
public class TopicPreviewFragment extends BaseFragment {

    @ViewById
    protected TextView title;
    @ViewById
    protected TopicLabelBar labelBar;
    @ViewById
    protected WebView content;
    protected MyJsonResponse myJsonResponse;
    private EditPreviewMarkdown editPreviewMarkdown;

    @AfterViews
    protected void initTopicPreviewFragment() {
        editPreviewMarkdown = ((EditPreviewMarkdown) getActivity());
        updatePreview();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            updatePreview();
        }
    }

    private void updatePreview() {
        TopicData data = editPreviewMarkdown.loadData();
        title.setText(data.title);
        updateLabels(data.labels);

        myJsonResponse = new MyJsonResponse(getActivity()) {
            @Override
            public void onMySuccess(JSONObject response) {
                super.onMySuccess(response);
                if (!isResumed()) {
                    return;
                }

                String html = response.optString("data", "");
                CodingGlobal.setWebViewContent(content, getWebViewTempate(), html);
            }
        };

        mdToHtml(data.content);
    }

    @NonNull
    protected String getWebViewTempate() {
        return "markdown.html";
    }

    public void updateLabels(List<TopicLabelObject> labels) {
        if (labelBar != null && (getActivity() instanceof TopicLabelBar.Controller)) {
            labelBar.bind(labels, (TopicLabelBar.Controller) getActivity());
        }
    }

    @Override
    public void onDestroy() {
        editPreviewMarkdown = null;
        super.onDestroy();
    }

    public void switchEdit() {
        action_edit();
    }

    @OptionsItem
    protected void action_edit() {
        editPreviewMarkdown.switchEdit();
    }

    @OptionsItem
    protected void action_save() {
        editPreviewMarkdown.exit();
    }

    // 重载此函数，修改预览方法
    protected void mdToHtml(String contentMd) {
        String uri = ProjectObject.getMdPreview(editPreviewMarkdown.getProjectPath());
        RequestParams params = new RequestParams();
        params.put("content", contentMd);
        MyAsyncHttpClient.post(getActivity(), uri, params, myJsonResponse);
    }
}

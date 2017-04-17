package net.coding.program.project.detail;

import android.webkit.WebView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.model.ProjectObject;
import net.coding.program.model.TopicLabelObject;
import net.coding.program.param.TopicData;

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
    private EditPreviewMarkdown editPreviewMarkdown;

    protected MyJsonResponse myJsonResponse;

    @AfterViews
    protected void init() {
        editPreviewMarkdown = ((EditPreviewMarkdown) getActivity());

        TopicData data = editPreviewMarkdown.loadData();
        title.setText(data.title);
        updateLabels(data.labels);

        myJsonResponse = new MyJsonResponse(getActivity()) {
            @Override
            public void onMySuccess(JSONObject response) {
                super.onMySuccess(response);
                String html = response.optString("data", "");
                Global.setWebViewContent(content, "markdown.html", html);
            }
        };

        mdToHtml(data.content);

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

package net.coding.program.project.detail;

import android.webkit.WebView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.model.ProjectObject;
import net.coding.program.model.TopicLabelObject;
import net.coding.program.project.detail.TopicEditFragment.SaveData;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@EFragment(R.layout.fragment_topic_preview)
@OptionsMenu(R.menu.topic_detail_edit_preview)
public class TopicPreviewFragment extends BaseFragment {

    private static final String TAG_HTTP_MD_PREVIEW = "TAG_HTTP_MD_PREVIEW";
    @ViewById
    protected TextView title;
    @ViewById
    protected TopicLabelBar labelBar;
    @ViewById
    protected WebView content;
    private SaveData saveData;

    @AfterViews
    protected void init() {
        saveData = ((SaveData) getActivity());

        TopicAddActivity.TopicData data = saveData.loadData();
        title.setText(data.title);
        updateLabels(data.labels);
        mdToHtml(data.content);
    }

    public void updateLabels(List<TopicLabelObject> labels) {
        if (labelBar != null && getActivity() != null)
            labelBar.bind(labels, (TopicLabelBar.Controller) getActivity());
    }

    @Override
    public void onDestroy() {
        saveData = null;
        super.onDestroy();
    }

    @OptionsItem
    protected void action_edit() {
        saveData.switchEdit();
    }

    @OptionsItem
    protected void action_save() {
        saveData.exit();
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_HTTP_MD_PREVIEW)) {
            if (code == 0) {
                String html = respanse.optString("data", "");
                Global.setWebViewContent(content, "topic-android", html);

            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    private void mdToHtml(String contentMd) {
        RequestParams params = new RequestParams();
        params.put("content", contentMd);
        String uri = ProjectObject.getMdPreview(saveData.getProjectPath());
        postNetwork(uri, params, TAG_HTTP_MD_PREVIEW);
    }
}

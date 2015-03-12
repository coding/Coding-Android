package net.coding.program.project.detail;

import android.webkit.WebView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.network.BaseFragment;
import net.coding.program.project.detail.TopicEditFragment.SaveData;
import net.coding.program.task.TaskDescripHtmlFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

@EFragment(R.layout.fragment_topic_preview)
@OptionsMenu(R.menu.topic_detail_edit_preview)
public class TopicPreviewFragment extends BaseFragment {

    @ViewById
    protected TextView title;

    @ViewById
    protected WebView content;

    private SaveData saveData;

    @AfterViews
    protected void init() {
        saveData = ((SaveData) getActivity());

        TopicAddActivity.TopicData data = saveData.loadData();
        title.setText(data.title);
        mdToHtml(data.content);
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
        if (tag.equals(TaskDescripHtmlFragment.HOST_PREVIEW)) {
            if (code == 0) {
                String html = respanse.optString("data", "");
                String bubble = "";
                try {
                    bubble = Global.readTextFile(getActivity().getAssets().open("topic-android"));
                } catch (Exception e) {
                    Global.errorLog(e);
                }
                TopicListDetailActivity.setTopicWebView(getActivity(), content, bubble, html);

            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    private void mdToHtml(String contentMd) {
        RequestParams params = new RequestParams();
        params.put("content", contentMd);
        postNetwork(TaskDescripHtmlFragment.HOST_PREVIEW, params, TaskDescripHtmlFragment.HOST_PREVIEW);
    }
}

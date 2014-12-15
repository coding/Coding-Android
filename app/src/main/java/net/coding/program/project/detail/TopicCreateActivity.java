package net.coding.program.project.detail;

import android.content.Intent;
import android.widget.EditText;

import com.loopj.android.http.RequestParams;

import net.coding.program.BaseActivity;
import net.coding.program.Global;
import net.coding.program.R;
import net.coding.program.model.ProjectObject;
import net.coding.program.model.TopicObject;
import net.coding.program.third.EmojiFilter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

@EActivity(R.layout.activity_topic_create)
@OptionsMenu(R.menu.topic_create)
public class TopicCreateActivity extends BaseActivity {

    @Extra
    ProjectObject projectObject;

    @ViewById
    EditText title;

    @ViewById
    EditText content;

    String url = Global.HOST + "/api/project/%s/topic?parent=0";

    @AfterViews
    void init() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        url = String.format(url, projectObject.id);
    }

    @OptionsItem(android.R.id.home)
    void back() {
        onBackPressed();
    }

    @OptionsItem
    void action_finish() {
        String titleString = title.getText().toString();
        if (titleString.isEmpty()) {
            showMiddleToast("标题不能为空");
            return;
        }

        String contentString = content.getText().toString();
        if (contentString.isEmpty()) {
            showMiddleToast("内容不能为空");
            return;
        }

        if (EmojiFilter.containsEmoji(titleString) || EmojiFilter.containsEmoji(contentString)) {
            showMiddleToast("暂不支持发表情");
            return;
        }

        RequestParams params = new RequestParams();
        params.put("title", titleString);
        params.put("content", contentString);

        postNetwork(url, params, url);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(url)) {
            if (code == 0) {
                Intent intent = new Intent();
                TopicObject topic = new TopicObject(respanse.getJSONObject("data"));
                intent.putExtra("topic", topic);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }
}

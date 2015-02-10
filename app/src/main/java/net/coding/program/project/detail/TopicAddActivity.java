package net.coding.program.project.detail;

import android.content.Intent;
import android.widget.EditText;

import com.loopj.android.http.RequestParams;

import net.coding.program.BaseActivity;
import net.coding.program.R;
import net.coding.program.common.Global;
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

@EActivity(R.layout.activity_topic_add)
@OptionsMenu(R.menu.topic_add)
public class TopicAddActivity extends BaseActivity {

    @Extra
    protected ProjectObject projectObject;

    @ViewById
    protected EditText title;

    @ViewById
    protected EditText content;

    String url = Global.HOST + "/api/project/%s/topic?parent=0";

    @AfterViews
    protected void init() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        url = String.format(url, getTopicId());
    }

    @OptionsItem(android.R.id.home)
    protected void back() {
        onBackPressed();
    }

    @OptionsItem
    protected void action_finish() {
        String titleString = title.getText().toString();
        if (EmojiFilter.containsEmptyEmoji(this, titleString, "标题不能为空", "标题不能包含表情")) {
            return;
        }

        String contentString = content.getText().toString();
        if (EmojiFilter.containsEmptyEmoji(this, contentString, "内容不能为空", "内容不能包含表情")) {
            return;
        }

        RequestParams params = new RequestParams();
        params.put("title", titleString);
        params.put("content", contentString);

        postNetwork(url, params, url);
        showProgressBar(true, getSendingTip());
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(url)) {
            showProgressBar(false);
            if (code == 0) {
                Intent intent = new Intent();
                TopicObject topic = new TopicObject(respanse.getJSONObject("data"));
                intent.putExtra("topic", topic);
                setResult(RESULT_OK, intent);
                finish();
                showSuccess();
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    protected int getTopicId() {
        return projectObject.getId();
    }

    protected void showSuccess() {
    }

    protected String getSendingTip() {
        return "正在发表讨论...";
    }
}

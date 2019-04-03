package net.coding.program.user;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.HtmlContent;
import net.coding.program.common.model.UserObject;
import net.coding.program.common.param.MessageParse;
import net.coding.program.message.MessageListActivity;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chenchao on 2017/1/9.
 * 选择成员转发私信
 */
@EActivity(R.layout.activity_pick_user)
public class PickUserRelayMessageActivity extends PickUserActivity {

    private static final String TAG_RELAY_MESSAGE = "TAG_RELAY_MESSAGE";
    @Extra
    String relayString = "";

    @Override
    protected void initListItemClick() {
        listView.setOnItemClickListener((parent, view, position, id) -> {
            UserObject user = (UserObject) parent.getItemAtPosition(position);
            showDialog("转发给" + user.name, (dialog, which) -> {
                MessageParse messageParse = HtmlContent.parseMessage(relayString);
                RequestParams params = new RequestParams();
                String text = messageParse.text;
                for (String url : messageParse.uris) {
                    String photoTemplate = "\n![图片](%s)";
                    text += String.format(photoTemplate, url);

                }
                params.put("content", text);
                params.put("receiver_global_key", user.global_key);
                postNetwork(MessageListActivity.getSendMessage(), params, TAG_RELAY_MESSAGE);
                showProgressBar(true, "发送中...");
            });
        });
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_RELAY_MESSAGE)) {
            showProgressBar(false);
            if (code == 0) {
                showMiddleToast("发送成功");
                finish();
            } else {
                showErrorMsg(code, respanse);
            }
        } else {
            super.parseJson(code, respanse, tag, pos, data);
        }
    }
}

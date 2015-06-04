package net.coding.program.project.detail.merge;

import android.widget.EditText;

import net.coding.program.BackActivity;
import net.coding.program.R;
import net.coding.program.model.Merge;
import net.coding.program.third.EmojiFilter;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

@EActivity(R.layout.activity_merge_accept)
//@OptionsMenu(R.menu.menu_merge_accept)
public class MergeAcceptActivity extends BackActivity {

    private static final String HOST_ACCEPT_MEREGE = "HOST_ACCEPT_MEREGE";
    @Extra
    Merge mMerge;
    @ViewById
    EditText message;
    boolean isCheck;

    @Click
    protected final void send() {
        String text = message.getText().toString();
        if (EmojiFilter.containsEmptyEmoji(this, text)) {
            return;
        }

        Merge.PostRequest request = mMerge.getHttpMerge(text, isCheck);
        postNetwork(request, HOST_ACCEPT_MEREGE);

    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_ACCEPT_MEREGE)) {
            if (code == 0) {
                setResult(RESULT_OK);
                finish();
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }
}

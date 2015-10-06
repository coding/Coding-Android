package net.coding.program.project.detail.merge;

import android.view.View;
import android.widget.EditText;

import net.coding.program.common.ui.BackActivity;
import net.coding.program.R;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.model.MergeDetail;
import net.coding.program.model.PostRequest;
import net.coding.program.third.EmojiFilter;

import org.androidannotations.annotations.AfterViews;
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
    MergeDetail mMergeDetail;
    @ViewById
    EditText message;

    @ViewById
    View delSrc;

    @AfterViews
    protected final void initMergeAcceptActivity() {
        message.setText(mMergeDetail.generalMergeMessage());
        boolean canDelSrc = mMergeDetail.isCan_edit_src_branch();
        if (canDelSrc) {
            delSrc.setVisibility(View.VISIBLE);
        } else {
            delSrc.setVisibility(View.GONE);
        }
    }

    @Click
    protected final void send() {
        String text = message.getText().toString();
        if (EmojiFilter.containsEmptyEmoji(this, text)) {
            return;
        }

        PostRequest request = mMergeDetail.getHttpMerge(text, delSrc.getVisibility() == View.VISIBLE);
        postNetwork(request, HOST_ACCEPT_MEREGE);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_ACCEPT_MEREGE)) {
            umengEvent(UmengEvent.CODE, "合并mrpr");
            if (code == 0) {
                setResult(RESULT_OK);
                finish();
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    @Click
    public final void listItemDelSrc() {
        int srcStyle = delSrc.getVisibility();
        switch (srcStyle) {
            case View.VISIBLE:
                delSrc.setVisibility(View.INVISIBLE);
                break;

            case View.INVISIBLE:
                delSrc.setVisibility(View.VISIBLE);
                break;

            case View.GONE:
                showMiddleToast("不能删除源分支");
                break;
        }
    }
}

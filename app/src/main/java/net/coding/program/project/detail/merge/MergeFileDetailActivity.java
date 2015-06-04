package net.coding.program.project.detail.merge;

import net.coding.program.BackActivity;
import net.coding.program.R;
import net.coding.program.model.DiffFile;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsMenu;
import org.json.JSONException;
import org.json.JSONObject;

@EActivity(R.layout.activity_merge_file_detail)
@OptionsMenu(R.menu.menu_merge_file_detail)
public class MergeFileDetailActivity extends BackActivity {

    public static final String HOST_COMMIT_FILE_DETAIL = "HOST_COMMIT_FILE_DETAIL";
    @Extra
    String mProjectPath;
    @Extra
    DiffFile.DiffSingleFile mSingleFile;

    @AfterViews
    protected final void initMergeFileDetailActivity() {
        String url = mSingleFile.getHttpFileDiffDetail(mProjectPath);
        getNetwork(url, HOST_COMMIT_FILE_DETAIL);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_COMMIT_FILE_DETAIL)) {
            if (code == 0) {

            } else {
                showErrorMsg(code, respanse);
            }
        }
    }
}

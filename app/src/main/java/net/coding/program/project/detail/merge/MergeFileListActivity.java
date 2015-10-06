package net.coding.program.project.detail.merge;

import android.widget.ListView;

import net.coding.program.common.ui.BackActivity;
import net.coding.program.R;
import net.coding.program.model.DiffFile;
import net.coding.program.model.Merge;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

//@EActivity(R.layout.activity_merge_file_list)
@EActivity(R.layout.activity_commit_list)
//@OptionsMenu(R.menu.menu_merge_file_list)
public class MergeFileListActivity extends BackActivity {

    private static final String HOST_MERGE_FILES = "HOST_MERGE_FILES";
    @Extra
    Merge mMerge;
    @ViewById
    ListView listView;
    MergeFileAdapter mAdapter;

    @AfterViews
    protected final void initCommitListActivity() {
        mAdapter = new MergeFileAdapter();
        listView.setAdapter(mAdapter);

        getNetwork(mMerge.getHttpFiles(), HOST_MERGE_FILES);
    }

    @ItemClick
    public final void listView(DiffFile.DiffSingleFile data) {
        MergeFileDetailActivity_.intent(this)
                .mProjectPath(mMerge.getProjectPath())
                .mSingleFile(data)
                .mergeIid(mMerge.getIid())
                .mMerge(mMerge)
                .start();
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_MERGE_FILES)) {
            if (code == 0) {
                DiffFile diffFile = new DiffFile(respanse.getJSONObject("data"));
                mAdapter.appendDataUpdate((ArrayList) diffFile.getFiles());
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }
}

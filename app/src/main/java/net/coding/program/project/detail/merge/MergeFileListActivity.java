package net.coding.program.project.detail.merge;

import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.model.DiffFile;
import net.coding.program.common.model.Merge;
import net.coding.program.common.ui.BackActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@EActivity(R.layout.activity_commit_list)
public class MergeFileListActivity extends BackActivity {

    private static final String HOST_MERGE_FILES = "HOST_MERGE_FILES";
    @Extra
    Merge mMerge;
    @ViewById
    ListView listView;
    MergeFileAdapter mAdapter;

    View header;

    @AfterViews
    protected final void initCommitListActivity() {
        header = getLayoutInflater().inflate(R.layout.commit_file_list_simple, listView, false);
        listView.addHeaderView(header, null, false);
        header.setVisibility(View.INVISIBLE);
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
                header.setVisibility(View.VISIBLE);
                ((TextView) header.findViewById(R.id.fileCount)).setText(String.format("%s 个文件", diffFile.getFileCount()));
                ((TextView) header.findViewById(R.id.fileChange))
                        .setText(String.format("共 %s 新增和 %s 删除", diffFile.getInsertions(), diffFile.getDeletions()));


                mAdapter.appendDataUpdate((ArrayList) diffFile.getFiles());
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }
}

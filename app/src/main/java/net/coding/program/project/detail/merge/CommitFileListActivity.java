package net.coding.program.project.detail.merge;

import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.coding.program.BackActivity;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.model.Commit;
import net.coding.program.model.DiffFile;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

@EActivity(R.layout.common_refresh_listview)
@OptionsMenu(R.menu.menu_commit_file_list)
public class CommitFileListActivity extends BackActivity {

    private static final String HOST_COMMIT_FILES = "HOST_COMMIT_FILES";
    @Extra
    Commit mCommit;
    @Extra
    String mProjectPath = "";
    @ViewById
    ListView listView;
    MergeFileAdapter mAdapter;
    private View mListHead;

    @AfterViews
    protected final void initCommitFileListActivity() {
        getSupportActionBar().setTitle(mCommit.getTitle());
        mAdapter = new MergeFileAdapter();

        initListhead();

        listView.setAdapter(mAdapter);

        getNetwork(mCommit.getHttpFiles(mProjectPath), HOST_COMMIT_FILES);
    }

    private void initListhead() {
        mListHead = mInflater.inflate(R.layout.commit_file_list_head, listView, false);
        listView.addHeaderView(mListHead);

        bindData(mListHead, R.id.title, mCommit.getTitle());
        bindData(mListHead, R.id.icon, mCommit.getIcon());
        bindData(mListHead, R.id.name, mCommit.getName());
        bindData(mListHead, R.id.time, Global.dayToNow(mCommit.getCommitTime(), "创建%s"));
        bindData(mListHead, R.id.mergeId, mCommit.getCommitIdPrefix());

        String preString = "";
        bindData(mListHead, R.id.preView, preString);
    }

    private void bindData(View view, int textViewId, String text) {
        View v = view.findViewById(textViewId);
        if (v instanceof TextView) {
            TextView textview = (TextView) v;
            textview.setText(text);
        } else if (v instanceof ImageView) {
            ImageView icon = (ImageView) v;
            imagefromNetwork(icon, text);
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_COMMIT_FILES)) {
            if (code == 0) {
                DiffFile diffFile = new DiffFile(respanse.getJSONObject("data"));

                findViewById(R.id.preView);
                String s = String.format("%d 个文件，共 %d 新增和 %d 删除", diffFile.getFileCount(),
                        diffFile.getInsertions(), diffFile.getDeletions());
                bindData(mListHead, R.id.preView, s);

                mAdapter.appendDataUpdate(diffFile.getFiles());
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }
}

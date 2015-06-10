package net.coding.program.project.detail.merge;

import android.webkit.WebView;

import net.coding.program.BackActivity;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.model.DiffFile;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_merge_file_detail)
//@OptionsMenu(R.menu.menu_merge_file_detail)
public class MergeFileDetailActivity extends BackActivity {

    public static final String HOST_COMMIT_FILE_DETAIL = "HOST_COMMIT_FILE_DETAIL";
    @Extra
    String mProjectPath;
    @Extra
    DiffFile.DiffSingleFile mSingleFile;

    @ViewById
    WebView webView;

    @AfterViews
    protected final void initMergeFileDetailActivity() {
        String url = mSingleFile.getHttpFileDiffDetail(mProjectPath);

        Global.setWebViewContent(webView, "diff", "${diff-content}", url);
    }
}

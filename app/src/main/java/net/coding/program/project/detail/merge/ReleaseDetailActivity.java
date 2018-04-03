package net.coding.program.project.detail.merge;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import net.coding.program.CodingGlobal;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.network.model.code.Attachment;
import net.coding.program.network.model.code.Release;
import net.coding.program.network.model.code.ResourceReference;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_release_detail)
@OptionsMenu(R.menu.common_edit)
public class ReleaseDetailActivity extends BackActivity {

    @Extra
    ProjectObject projectObject;

    @Extra
    Release release;

    @ViewById
    TextView titleText, timeText;

    @ViewById
    WebView webView;

    @ViewById
    ViewGroup releaseFile, releaseRef;

    @AfterViews
    void initReleaseDetailActivity() {
        String title = release.title;
        if (TextUtils.isEmpty(title)) title = release.tagName;
        titleText.setText(title);

        timeText.setText(String.format("%s 发布于 %s", release.lastCommit.committer.name, Global.simpleDayByNow(release.createdAt)));

        CodingGlobal.setWebViewContent(webView, CodingGlobal.WebviewType.markdown, release.markdownBody);

        if (release.resourceReferences != null && release.resourceReferences.size() > 0) {
            releaseRef.setVisibility(View.VISIBLE);
            ViewGroup fileLayout = releaseRef.findViewById(R.id.fileLayout);
            for (ResourceReference item : release.resourceReferences) {
                View fileView = getLayoutInflater().inflate(R.layout.release_file_item, fileLayout, false);
                ((TextView) fileView.findViewById(R.id.fileName)).setText(item.title);
                fileLayout.addView(fileView);
            }
        } else {
            releaseRef.setVisibility(View.GONE);
        }

        if (release.attachments != null && release.attachments.size() > 0) {
            releaseFile.setVisibility(View.VISIBLE);
            ViewGroup fileLayout = releaseFile.findViewById(R.id.fileLayout);
            for (Attachment item : release.attachments) {
                View fileView = getLayoutInflater().inflate(R.layout.release_file_item, fileLayout, false);
                ((TextView) fileView.findViewById(R.id.fileName)).setText(item.name);
                fileLayout.addView(fileView);
            }
        } else {
            releaseFile.setVisibility(View.GONE);
        }
    }

    @OptionsItem
    void actionEdit() {

    }
}

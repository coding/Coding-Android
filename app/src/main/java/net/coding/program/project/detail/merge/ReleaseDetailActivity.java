package net.coding.program.project.detail.merge;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import net.coding.program.CodingGlobal;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalCommon;
import net.coding.program.common.GlobalData;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.network.BaseHttpObserver;
import net.coding.program.network.HttpObserver;
import net.coding.program.network.Network;
import net.coding.program.network.model.code.Attachment;
import net.coding.program.network.model.code.Release;
import net.coding.program.network.model.code.ResourceReference;
import net.coding.program.param.ProjectJumpParam;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EActivity(R.layout.activity_release_detail)
public class ReleaseDetailActivity extends BackActivity {

    private static final int RESULT_MODIFY = 1;

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
        bindData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.release_edit, menu);
        if (!release.author.global_key.equals(GlobalData.sUserObject.global_key)) {
            menu.findItem(R.id.actionDelete).setVisible(false);
        }

        return true;
    }

    @OptionsItem
    void actionDelete() {
        Release branch = release;
        showDialog(String.format("请确认是否要删除版本 %s ？", branch.tagName), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Network.getRetrofit(ReleaseDetailActivity.this)
                        .deleteRelease(projectObject.owner_user_name, projectObject.name, branch.tagName)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new BaseHttpObserver(ReleaseDetailActivity.this) {
                            @Override
                            public void onSuccess() {
                                super.onSuccess();

                                showButtomToast(String.format("版本 %s 已删除", branch.tagName));

                                setResult(RESULT_OK);
                                finish();
                            }
                        });
            }
        });

    }

    @OptionsItem
    void actionEdit() {
        EditReleaseDetailActivity_.intent(this)
                .projectParam(new ProjectJumpParam(projectObject.getProjectPath()))
                .release(release)
                .startForResult(RESULT_MODIFY);
    }

    @OnActivityResult(RESULT_MODIFY)
    void onResultModify(int result) {
        if (result == RESULT_OK) {
            setResult(RESULT_OK);
            onRefresh();
        }
    }

    private void onRefresh() {
        Network.getRetrofit(this)
                .getRelease(projectObject.owner_user_name, projectObject.name, release.tagName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserver<Release>(this) {
                    @Override
                    public void onSuccess(Release data) {
                        super.onSuccess(data);
                        release = data;
                        bindData();
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                    }
                });
    }

    private void bindData() {
        String title = release.title;
        if (TextUtils.isEmpty(title)) title = release.tagName;
        titleText.setText(title);

        timeText.setText(String.format("%s %s 发布于 %s", release.tagName,
                release.lastCommit.committer.name, Global.simpleDayByNow(release.createdAt)));

        CodingGlobal.setWebViewContent(webView, CodingGlobal.WebviewType.markdown, release.markdownBody);

        if (release.attachments != null && release.attachments.size() > 0) {
            ViewGroup fileLayout = releaseFile.findViewById(R.id.fileLayout);
            ((TextView) releaseFile.findViewById(R.id.title)).setText("下载");
            fileLayout.removeAllViews();
            for (Attachment item : release.attachments) {
                View fileView = getLayoutInflater().inflate(R.layout.release_file_item, fileLayout, false);
                TextView leftText = fileView.findViewById(R.id.fileName);
                leftText.setText(item.name);
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) leftText.getLayoutParams();
                lp.rightMargin = GlobalCommon.dpToPx(100);
                leftText.setLayoutParams(lp);

                ((TextView) fileView.findViewById(R.id.fileSize)).setText(item.getSizeString());

                fileLayout.addView(fileView);
            }

            String[] items = new String[]{"源代码 (zip)", "源代码 (tar.gz)"};

            for (String item : items) {
                View fileView = getLayoutInflater().inflate(R.layout.release_file_item, fileLayout, false);
                TextView leftText = fileView.findViewById(R.id.fileName);
                leftText.setText(item);
                fileLayout.addView(fileView);
            }
        } else {
            releaseFile.setVisibility(View.GONE);
        }

        if (release.resourceReferences != null && release.resourceReferences.size() > 0) {
            releaseRef.setVisibility(View.VISIBLE);
            ViewGroup fileLayout = releaseRef.findViewById(R.id.fileLayout);
            ((TextView) releaseRef.findViewById(R.id.title)).setText("引用");
            fileLayout.removeAllViews();
            for (ResourceReference item : release.resourceReferences) {
                View fileView = getLayoutInflater().inflate(R.layout.release_file_item, fileLayout, false);
                TextView leftText = fileView.findViewById(R.id.fileName);
                leftText.setCompoundDrawablesRelativeWithIntrinsicBounds(item.getTypeIcon(), 0, 0, 0);
                leftText.setText(String.format("#%s %s", item.code, item.title));

                fileLayout.addView(fileView);
            }


        } else {
            releaseRef.setVisibility(View.GONE);
        }
    }


}

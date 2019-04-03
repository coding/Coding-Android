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
import net.coding.program.common.util.BlankViewHelp;
import net.coding.program.network.BaseHttpObserver;
import net.coding.program.network.HttpObserver;
import net.coding.program.network.Network;
import net.coding.program.network.model.HttpResult;
import net.coding.program.network.model.code.Attachment;
import net.coding.program.network.model.code.Release;
import net.coding.program.network.model.code.ResourceReference;
import net.coding.program.param.ProjectJumpParam;
import net.coding.program.project.git.BranchMainActivity_;
import net.coding.program.route.URLSpanNoUnderline;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EActivity(R.layout.activity_release_detail)
public class ReleaseDetailActivity extends BackActivity {

    private static final int RESULT_MODIFY = 1;

    @Extra
    ProjectObject projectObject;

    @Extra
    JumpParam param;

    @Extra
    Release release;

    @ViewById
    TextView titleText, timeText;

    @ViewById
    WebView webView;

    @ViewById
    ViewGroup releaseFile, releaseRef;

    @ViewById
    View blankLayout;

    @AfterViews
    void initReleaseDetailActivity() {
        if (param != null) {
            BlankViewHelp.setBlankLoading(blankLayout, true);
            onRefrush();
        } else {
            bindData();
        }
    }

    public void onRefrush() {
        Observable<HttpResult<ProjectObject>> r1 = Network.getRetrofit(this)
                .getProject(param.user, param.project)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        Observable<HttpResult<Release>> r2 = Network.getRetrofit(this)
                .getRelease(param.user, param.project, param.tag)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        Observable.zip(r1, r2, (p, r) -> {
            projectObject = p.data;
            release = r.data;

            if (release == null) {
                if (r.code == 3900) {
                    return true;
                } else {
                    return false;
                }
            }

            return true;
        }).subscribe(new Subscriber<Boolean>() {
            @Override
            public void onCompleted() {
                if (release == null) {
                    return;
                }

                bindData();
                invalidateOptionsMenu();
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Boolean aBoolean) {
                if (release == null) {
                    if (aBoolean) {
                        BlankViewHelp.setBlank(0, ReleaseDetailActivity.this, true, blankLayout, null);
                    }
                } else {
                    BlankViewHelp.setBlank(1, ReleaseDetailActivity.this, true, blankLayout, null);
                }
            }
        });
    }

    public static class JumpParam extends ProjectJumpParam {
        String tag;

        public JumpParam(String user, String project, String tag) {
            super(user, project);
            this.tag = tag;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (release == null) {
            return true;
        }

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
        if (release == null) return;

        findViewById(R.id.contentLayout).setVisibility(View.VISIBLE);

        String title = release.title;
        if (TextUtils.isEmpty(title)) title = release.tagName;
        titleText.setText(title);

        timeText.setText(String.format("%s %s 发布于 %s", release.tagName,
                release.author.name, Global.simpleDayByNow(release.createdAt)));
        timeText.setOnClickListener(v -> {
            BranchMainActivity_.intent(this)
                    .mProjectPath(projectObject.getProjectPath())
                    .mVersion(release.tagName)
                    .start();
        });

        CodingGlobal.setWebViewContent(webView, CodingGlobal.WebviewType.markdown, release.markdownBody);

        bindUIDownload();
        bindUIRefs();
    }

    private void bindUIDownload() {
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

            fileView.setOnClickListener(v -> showMiddleToast("暂不支持下载"));

            fileLayout.addView(fileView);
        }

        String[] items = new String[]{"源代码 (zip)", "源代码 (tar.gz)"};

        for (String item : items) {
            View fileView = getLayoutInflater().inflate(R.layout.release_file_item, fileLayout, false);
            TextView leftText = fileView.findViewById(R.id.fileName);
            leftText.setText(item);
            fileLayout.addView(fileView);
            fileView.setOnClickListener(v -> showMiddleToast("暂不支持下载"));
        }
    }

    private void bindUIRefs() {
        if (release.resourceReferences != null && release.resourceReferences.size() > 0) {
            releaseRef.setVisibility(View.VISIBLE);
            ViewGroup fileLayout = releaseRef.findViewById(R.id.fileLayout);
            ((TextView) releaseRef.findViewById(R.id.title)).setText("关联资源");
            fileLayout.removeAllViews();
            for (ResourceReference item : release.resourceReferences) {
                View fileView = getLayoutInflater().inflate(R.layout.release_file_item, fileLayout, false);
                TextView leftText = fileView.findViewById(R.id.fileName);
                leftText.setCompoundDrawablesRelativeWithIntrinsicBounds(item.getTypeIcon(), 0, 0, 0);
                leftText.setText(String.format("#%s %s", item.code, item.title));
                fileLayout.addView(fileView);
                fileView.setOnClickListener(v -> {
                    URLSpanNoUnderline.openActivityByUri(this, item.link, false);
                });
            }


        } else {
            releaseRef.setVisibility(View.GONE);
        }
    }

}

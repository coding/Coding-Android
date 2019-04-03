package net.coding.program.project.detail.merge;

import android.support.annotation.NonNull;

import net.coding.program.R;
import net.coding.program.common.CodingColor;
import net.coding.program.common.Global;
import net.coding.program.common.base.MDEditPreviewActivity;
import net.coding.program.common.model.topic.TopicData;
import net.coding.program.network.BaseHttpObserver;
import net.coding.program.network.Network;
import net.coding.program.network.model.code.Release;
import net.coding.program.network.model.code.ResourceReference;
import net.coding.program.network.model.wiki.WikiDraft;
import net.coding.program.param.ProjectJumpParam;
import net.coding.program.project.detail.wiki.WikiEditFragment_;
import net.coding.program.project.detail.wiki.WikiPreviewFragment_;
import net.coding.program.third.EmojiFilter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import info.hoang8f.android.segmented.SegmentedGroup;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EActivity(R.layout.activity_edit_release_detail)
public class EditReleaseDetailActivity extends MDEditPreviewActivity {

    @Extra
    ProjectJumpParam projectParam;

    @Extra
    Release release;

    private TopicData modifyData = new TopicData();

    @AfterViews
    void initWikiEditActivity() {
        useToolbar();

        SegmentedGroup segmented = findViewById(R.id.segmented);
        segmented.setTintColor(CodingColor.font2);

        modifyData.title = release.title;
        modifyData.content = release.body;

        editFragment = WikiEditFragment_.builder().build();
        previewFragment = WikiPreviewFragment_.builder().build();
        initEditPreviewFragment();

        switchEdit();
    }

    @Click(R.id.editButton)
    void editWiki() {
        previewFragment.switchEdit();
    }

    @Click(R.id.previewButton)
    void previewWiki() {
        editFragment.switchPreview();
    }

    @Override
    public void saveData(TopicData data) {
        modifyData = data;
    }

    @Override
    public TopicData loadData() {
        return modifyData;
    }

    @Override
    public void onBackPressed() {
        if (editFragment.isContentModify()) {
            TopicData topicData = editFragment.generalDraft();
            WikiDraft draft = new WikiDraft(topicData);

            showDialog("", "保存修改?",
                    (dialog, which) -> {
                        save(true);
                    },
                    (dialog, which) -> {
                        finish();
                    },
                    "保存", "不保存");
            return;
        }

        finish();
    }

    @Override
    public void exit() {
//        new AlertDialog.Builder(this)
//                .setItems(new String[]{"更新 Release", "保存草稿"}, (dialogInterface, i) -> {
//                    if (i == 0) {
//                        save(false);
//                    } else {
//                        save(true);
//                    }
//                }).show();

        save(false);
    }

    private void save(boolean draft) {
        String titleString = modifyData.title;
        if (EmojiFilter.containsEmptyEmoji(this, titleString, "标题不能为空", "标题不能包含表情")) {
            return;
        }

        String contentString = modifyData.content;
        if (EmojiFilter.containsEmptyEmoji(this, contentString, "内容不能为空", "内容不能包含表情")) {
            return;
        }

        Global.hideSoftKeyboard(this);

        Map<String, String> map = new HashMap<>();
        map.put("tag_name", release.tagName);
        map.put("commit_sha", release.commitSha);
        map.put("target_commitish", release.targetCommitish);
        map.put("title", modifyData.title);
        map.put("body", modifyData.content);
        map.put("draft", String.valueOf(draft));
        map.put("pre", String.valueOf(false));

        ArrayList<Integer> refs = new ArrayList<>();
        for (ResourceReference item : release.resourceReferences) {
            refs.add(item.code);
        }

        Network.getRetrofit(this)
                .modifyRelease(projectParam.user, projectParam.project, release.tagName, refs, map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseHttpObserver(this) {
                    @Override
                    public void onSuccess() {
                        super.onSuccess();

                        setResult(RESULT_OK);

                        finish();
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                        showProgressBar(false);
                    }
                });

        showProgressBar(true);
    }

    @Override
    public String getProjectPath() {
        return projectParam.toPath();
    }

    @Override
    public boolean isProjectPublic() {
        return false;
    }
}

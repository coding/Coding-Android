package net.coding.program.project.detail.wiki;

import android.support.annotation.NonNull;

import net.coding.program.R;
import net.coding.program.common.CodingColor;
import net.coding.program.common.Global;
import net.coding.program.common.base.MDEditPreviewActivity;
import net.coding.program.common.event.EventRefresh;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.model.topic.TopicData;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.network.HttpObserver;
import net.coding.program.network.Network;
import net.coding.program.network.model.wiki.Wiki;
import net.coding.program.network.model.wiki.WikiDraft;
import net.coding.program.param.ProjectJumpParam;
import net.coding.program.third.EmojiFilter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;

import info.hoang8f.android.segmented.SegmentedGroup;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EActivity(R.layout.activity_wiki_edit)
public class WikiEditActivity extends MDEditPreviewActivity {

    @Extra
    ProjectJumpParam projectParam;

    @Extra
    boolean useDraft = false;

    @Extra
    Wiki wiki;

    private boolean saveDraft = true;

    private TopicData modifyData = new TopicData();

    @AfterViews
    void initWikiEditActivity() {
        useToolbar();

        SegmentedGroup segmented = (SegmentedGroup) findViewById(R.id.segmented);
        segmented.setTintColor(CodingColor.font2);

        ArrayList<WikiDraft> drafts = AccountInfo.loadWikiDraft(this, projectParam.toPath(), wiki.id);
        if (useDraft && !drafts.isEmpty()) {
            WikiDraft draft = drafts.get(0);
            modifyData.title = draft.title;
            modifyData.content = draft.content;

            wiki.updatedAt = draft.updateAt;
        } else {
            modifyData.title = wiki.title;
            modifyData.content = wiki.content;
        }

        editFragment = WikiEditFragment_.builder().build();
        previewFragment = WikiPreviewFragment_.builder().build();
        initEditPreviewFragment();
        switchEdit();
    }

    @Click(R.id.editWiki)
    void editWiki() {
        previewFragment.switchEdit();
    }

    @Click(R.id.previewWiki)
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
            draft.updateAt = wiki.updatedAt;

            showDialog("", " 是否需要保存草稿？",
                    (dialog, which) -> {
                        saveDraft = true;
                        finish();
                    },
                    (dialog, which) -> {
                        saveDraft = false;
                        finish();
                    },
                    "保存", "不保存");
            return;
        }

        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (saveDraft && editFragment.isContentModify()) {
            TopicData topicData = editFragment.generalDraft();
            WikiDraft draft = new WikiDraft(topicData);
            draft.updateAt = wiki.updatedAt;
            AccountInfo.saveWikiDraft(this, draft, projectParam.toPath(), wiki.id);
        }
    }

    @Override
    public void exit() {
        String titleString = modifyData.title;
        if (EmojiFilter.containsEmptyEmoji(this, titleString, "标题不能为空", "标题不能包含表情")) {
            return;
        }

        String contentString = modifyData.content;
        if (EmojiFilter.containsEmptyEmoji(this, contentString, "内容不能为空", "内容不能包含表情")) {
            return;
        }

        Global.hideSoftKeyboard(this);

        HashMap<String, String> map = new HashMap<>();
        map.put("iid", String.valueOf(wiki.iid));
        map.put("parentIid", String.valueOf(wiki.parentIid));
        map.put("title", titleString);
        map.put("content", contentString);
        map.put("msg", "");
        map.put("order", String.valueOf(wiki.order));

        Network.getRetrofit(this)
                .postWiki(projectParam.user, projectParam.project, map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserver<Wiki>(this) {
                    @Override
                    public void onSuccess(Wiki data) {
                        super.onSuccess(data);
                        umengEvent(UmengEvent.E_WIKI, "编辑_提交");

                        EventBus.getDefault().post(new EventRefresh(true));
                        showProgressBar(false);

                        showButtomToast("修改 Wiki 成功");

                        saveDraft = false;
                        AccountInfo.deleteWikiDraft(WikiEditActivity.this, projectParam.toPath(), wiki.id);

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

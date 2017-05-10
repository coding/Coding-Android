package net.coding.program.project.detail.wiki;

import android.support.annotation.NonNull;

import net.coding.program.R;
import net.coding.program.common.CodingColor;
import net.coding.program.common.Global;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.event.EventRefresh;
import net.coding.program.network.HttpObserver;
import net.coding.program.network.Network;
import net.coding.program.network.model.wiki.Wiki;
import net.coding.program.param.ProjectJumpParam;
import net.coding.program.param.TopicData;
import net.coding.program.project.detail.EditPreviewMarkdown;
import net.coding.program.project.detail.TopicEditFragment;
import net.coding.program.project.detail.TopicPreviewFragment;
import net.coding.program.third.EmojiFilter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

import info.hoang8f.android.segmented.SegmentedGroup;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EActivity(R.layout.activity_wiki_edit)
public class WikiEditActivity extends BackActivity implements EditPreviewMarkdown {

    @Extra
    ProjectJumpParam projectParam;

    @Extra
    Wiki wiki;
    TopicEditFragment editFragment;
    TopicPreviewFragment previewFragment;
    private TopicData modifyData = new TopicData();

    @AfterViews
    void initWikiEditActivity() {
        useToolbar();

        SegmentedGroup segmented = (SegmentedGroup) findViewById(R.id.segmented);
        segmented.setTintColor(CodingColor.font2);

        modifyData.title = wiki.title;
        modifyData.content = wiki.content;

        editFragment = WikiEditFragment_.builder().build();
        previewFragment = WikiPreviewFragment_.builder().build();

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
            showDialog("", "确定放弃此次编辑？", (dialog, which) -> finish());
            return;
        }

        super.onBackPressed();
    }

    @Override
    public void switchPreview() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, previewFragment).commit();
    }

    @Override
    public void switchEdit() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, editFragment).commit();
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

                        EventBus.getDefault().post(new EventRefresh(true));
                        showProgressBar(false);

                        showButtomToast("修改 Wiki 成功");

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

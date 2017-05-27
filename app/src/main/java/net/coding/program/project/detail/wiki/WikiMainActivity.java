package net.coding.program.project.detail.wiki;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.cpiz.android.bubbleview.BubblePopupWindow;
import com.cpiz.android.bubbleview.BubbleStyle;
import com.cpiz.android.bubbleview.BubbleTextView;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import net.coding.program.R;
import net.coding.program.common.BlankViewDisplay;
import net.coding.program.common.Global;
import net.coding.program.common.RedPointTip;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.databinding.ActivityWikiDetailHeaderBinding;
import net.coding.program.event.EventRefresh;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.ProjectObject;
import net.coding.program.network.HttpObserver;
import net.coding.program.network.Network;
import net.coding.program.network.model.wiki.Wiki;
import net.coding.program.param.ProjectJumpParam;
import net.coding.program.param.WikiDraft;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EActivity(R.layout.activity_wiki_main)
public class WikiMainActivity extends BackActivity {

    @Extra
    ProjectObject project;

    @ViewById
    DrawerLayout drawerLayoutRoot;

    @ViewById
    NestedScrollView contentFrame;

    @ViewById
    View blankLayout;

    @ViewById
    ViewGroup drawerLayout;

    @ViewById
    WebView webView;

    @ViewById
    View bottomBarLayout;

    @ViewById
    Toolbar toolbar;

    MenuItem actionMore;

    ActivityWikiDetailHeaderBinding headerBinding;

    List<Wiki> dataList = new ArrayList<>();

    AndroidTreeView treeViewBuilder;
    View treeView = null;

    TreeNode firstTreeNode = null;
    TreeNode treeRoot;

    NodeHolder selectNode = null;
    Wiki selectWiki;

    BubblePopupWindow bubbleWindowTree = null;
    BubblePopupWindow bubbleWindowHistory = null;
    int oldToolbarFlags = 0;

    @AfterViews
    void initWikiMainActivity() {
        useToolbar();
        setActionBarTitle(project.name);

        oldToolbarFlags = ((AppBarLayout.LayoutParams) toolbar.getLayoutParams()).getScrollFlags();

        headerBinding = ActivityWikiDetailHeaderBinding.bind(findViewById(R.id.wikiHeader));

        onRefrush();

        Global.initWebView(webView);

        popGuide0();
    }

    @UiThread(delay = 3000)
    void popGuide0() {
        if (dataList.isEmpty()) {
            return;
        }

        if (RedPointTip.show(this, RedPointTip.Type.WikiTree200)) {
            bubbleWindowTree = popGuide(RedPointTip.Type.WikiTree200, "这里可查看页面目录", R.id.clickPopDrawer, v -> popGuide1());
        } else {
            popGuide1();
        }
    }

    void popGuide1() {
        if (dataList.isEmpty()) {
            return;
        }

        if (RedPointTip.show(this, RedPointTip.Type.WikiHistory200)) {
            bubbleWindowHistory = popGuide(RedPointTip.Type.WikiHistory200, "这里可查看历史版本", R.id.clickHistory, null);
        }
    }

    private BubblePopupWindow popGuide(RedPointTip.Type type, String text, int target, View.OnClickListener click) {
        View rootView = getLayoutInflater().inflate(R.layout.guide_bubble_view, null);
        BubbleTextView bubbleView = (BubbleTextView) rootView;
        bubbleView.setText(text);

        BubblePopupWindow bubbleWindow = new BubblePopupWindow(rootView, bubbleView);
        bubbleWindow.setCancelOnTouchOutside(false);
        bubbleWindow.showArrowTo(findViewById(target), BubbleStyle.ArrowDirection.Down);

        bubbleView.setOnClickListener(v -> {
            hideGuide(type, bubbleWindow, v.getContext());
            if (click != null) click.onClick(v);
        });

        disableToolbarScroll(true);

        return bubbleWindow;
    }

    private void hideGuide(RedPointTip.Type type, BubblePopupWindow bubbleWindow, Context context) {
        if (bubbleWindow != null) {
            if (bubbleWindow.isShowing()) {
                bubbleWindow.dismiss();
            }
        }

        RedPointTip.markUsed(context, type);

        disableToolbarScroll(!(selectWiki != null && !TextUtils.isEmpty(selectWiki.content)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_wiki, menu);
        actionMore = menu.findItem(R.id.action_more);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventUpdate(EventRefresh event) {
        onRefrush();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(NodeHolder node) {
        selectNode(node);
    }

    private void buildTree() {
        if (dataList.size() > 0) {
            firstTreeNode = null;

            treeRoot = TreeNode.root();
            addTreeNode(treeRoot, dataList);

            treeViewBuilder = new AndroidTreeView(this, treeRoot);
            treeViewBuilder.setDefaultViewHolder(NodeHolder.class);

            treeView = treeViewBuilder.getView();
            drawerLayout.addView(treeView);

            selectNode((NodeHolder) firstTreeNode.getViewHolder());

            treeViewBuilder.expandAll();
            bottomBarLayout.setVisibility(View.VISIBLE);
            disableToolbarScroll(false);

            if (actionMore != null) {
                actionMore.setVisible(true);
            }
        } else {
            BlankViewDisplay.setBlank(0, WikiMainActivity.this, true, blankLayout, v -> onRefrush());
            bottomBarLayout.setVisibility(View.GONE);
            disableToolbarScroll(true);

            if (actionMore != null) {
                actionMore.setVisible(false);
            }
        }
    }

    private void disableToolbarScroll(boolean disable) {
        AppBarLayout.LayoutParams lp = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        lp.setScrollFlags(disable ? 0 : oldToolbarFlags);
        toolbar.setLayoutParams(lp);
    }

    @Click(R.id.clickEdit)
    void onClickEdit() {
        ProjectJumpParam projectParam = project.generateJumpParam();
        ArrayList<WikiDraft> drafts = AccountInfo.loadWikiDraft(this, projectParam.toPath(), selectWiki.id);
        boolean useDraft = false;
        if (!drafts.isEmpty()) {
            WikiDraft draft = drafts.get(0);
            if (draft.updateAt < selectWiki.updatedAt) {
                showDialog("", "有最新版本更新，您是否继续编辑上一次的草稿？",
                        (dialog, which) -> jumpToEdit(true),
                        (dialog, which) -> jumpToEdit(false),
                        "编辑草稿", "编辑新版本");
                return;
            } else {
                useDraft = true;
            }
        }

        jumpToEdit(useDraft);
    }

    private void jumpToEdit(boolean useDraft) {
        ProjectJumpParam projectParam = project.generateJumpParam();
        WikiEditActivity_.intent(this)
                .projectParam(projectParam)
                .useDraft(useDraft)
                .wiki(selectWiki)
                .start();
    }

    @Click(R.id.clickPopDrawer)
    void onClickPopDrawer() {
        hideGuide(RedPointTip.Type.WikiTree200, bubbleWindowTree, this);
        bubbleWindowTree = null;
        popGuide1();

        drawerLayoutRoot.openDrawer(GravityCompat.START);
    }

    @Click(R.id.clickHistory)
    void onClickHistory() {
        hideGuide(RedPointTip.Type.WikiHistory200, bubbleWindowHistory, this);
        bubbleWindowHistory = null;

        WikiHistoryActivity_.intent(this)
                .jumpParam(project.generateJumpParam())
                .wiki(selectWiki)
                .start();
    }

    private void onRefrush() {
        Network.getRetrofit(this)
                .getWikis(project.owner_user_name, project.name)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserver<List<Wiki>>(this) {
                    @Override
                    public void onSuccess(List<Wiki> data) {
                        super.onSuccess(data);

                        dataList.clear();
                        dataList.addAll(data);

                        if (treeView != null) {
                            drawerLayout.removeView(treeView);
                        }

                        buildTree();
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                    }
                });
    }

    @OptionsItem(R.id.action_delete)
    void onActionDelete() {
        showDialog("", "删除的同时也会删除历史版本，请确认删除该 Wiki ?", (dialog, which) -> deleteSelectWiki(), null);
    }

    private void deleteSelectWiki() {
        Wiki wiki = selectNode.getNodeValue();
        Network.getRetrofit(this)
                .deleteWiki(project.owner_user_name, project.name, wiki.iid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserver<Boolean>(this) {
                    @Override
                    public void onSuccess(Boolean data) {
                        super.onSuccess(data);
                        showProgressBar(false);

                        selectWiki = null;
                        selectNode = null;
                        onRefrush();
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                        showProgressBar(false);
                    }
                });
        showProgressBar(true);
    }

    // 主动选择 item
    private void selectNode(NodeHolder node) {
        if (selectNode != null) {
            selectNode.select(false);
        }

        selectNode = node;

        if (selectNode != null) {
            selectNode.select(true);

            drawerLayoutRoot.closeDrawer(GravityCompat.START);

            loadContent();
        }
    }

    private void loadContent() {
        Wiki wiki = selectNode.getNodeValue();
        Network.getRetrofit(this)
                .getWikiDetail(project.owner_user_name, project.name, wiki.iid, -1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserver<Wiki>(this) {
                    @Override
                    public void onSuccess(Wiki data) {
                        super.onSuccess(data);

                        selectWiki = data;
                        displayWebviewContent(selectWiki.html);

                        disableToolbarScroll(TextUtils.isEmpty(data.content));
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                        BlankViewDisplay.setBlank(0, WikiMainActivity.this, false, blankLayout, v -> onRefrush());
                    }
                });
    }

//    String wikiContent = "";

//    @Background(serial = "content")
//    void getContent(String html) {
//        wikiContent = "";
//        try {
//            String bubble = Global.readTextFile(getAssets().open("markdown.html"));
//            wikiContent =  bubble.replace("${webview_content}", html);
//        } catch (Exception e) {
//            Global.errorLog(e);
//        }
//    }

    void displayWebviewContent(String html) {
//        getContent(html);
//        Global.setWebViewContent(webView, wikiContent);
        Global.setWebViewContent(webView, "wiki.html", html);
        BlankViewDisplay.setBlank(html.length(), WikiMainActivity.this, true, blankLayout, v -> onRefrush());
        headerBinding.setWiki(selectWiki);

        contentFrame.scrollTo(0, 0);
    }

    private void addTreeNode(TreeNode node, List<Wiki> wikis) {
        for (Wiki item : wikis) {
            TreeNode childNode = new TreeNode(item);
            node.addChild(childNode);
            if (firstTreeNode == null) {
                firstTreeNode = childNode;
            }

            if (selectWiki != null && selectWiki.id == item.id) {
                firstTreeNode = childNode;
            }

            addTreeNode(childNode, item.children);
        }
    }

}

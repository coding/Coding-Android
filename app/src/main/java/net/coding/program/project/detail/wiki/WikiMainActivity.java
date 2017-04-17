package net.coding.program.project.detail.wiki;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.orhanobut.logger.Logger;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import net.coding.program.R;
import net.coding.program.common.BlankViewDisplay;
import net.coding.program.common.Global;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.event.EventAction;
import net.coding.program.event.EventRefresh;
import net.coding.program.event.WikiEvent;
import net.coding.program.model.ProjectObject;
import net.coding.program.network.HttpObserver;
import net.coding.program.network.Network;
import net.coding.program.network.model.wiki.Wiki;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EActivity(R.layout.activity_wiki_main)
@OptionsMenu(R.menu.main_wiki)
public class WikiMainActivity extends BackActivity {

    @Extra
    ProjectObject project;

    @ViewById
    DrawerLayout drawerLayoutRoot;

    @ViewById
    View blankLayout;

    @ViewById
    ViewGroup drawerLayout;

    @ViewById
    WebView webView;

    List<Wiki> dataList = new ArrayList<>();
    AndroidTreeView treeViewBuilder;
    View treeView = null;

    NodeHolder selectNode = null;
    TreeNode firstTreeNode = null;

    private TreeNode treeRoot;

    Wiki selectWiki;

    @AfterViews
    void initWikiMainActivity() {
        useToolbar();

        setActionBarTitle(project.name);

        onRefrush();

        Global.initWebView(webView);
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


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventUpdateSingle(WikiEvent event) {
        if (event.action == EventAction.modify) {
            updateTreeData(treeRoot, event.wiki);
        }
    }

    private boolean updateTreeData(TreeNode node, Wiki data) {
        Wiki value = (Wiki) node.getValue();
        if (value != null && value.id == data.id) {
            value.update(data);
            NodeHolder h = (NodeHolder) node.getViewHolder();
            h.notifyDataSetChanged();
            selectNode(h);
            return true;
        } else {
            for (TreeNode t : node.getChildren()) {
                if (updateTreeData(t, data)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void buildTree() {
        treeRoot = TreeNode.root();
        addTreeNode(treeRoot, dataList);

        treeViewBuilder = new AndroidTreeView(this, treeRoot);
        treeViewBuilder.setDefaultViewHolder(NodeHolder.class);

        treeView = treeViewBuilder.getView();
        drawerLayout.addView(treeView);

        selectNode((NodeHolder) firstTreeNode.getViewHolder());

        treeViewBuilder.expandAll();
    }

    @Click(R.id.clickEdit)
    void onClickEdit() {
//        EventBus.getDefault().post(new EventRefresh(true));
        WikiEditActivity_.intent(this)
                .projectParam(project.generateJumpParam())
                .wiki(selectNode.getNodeValue())
                .start();
    }

    @Click(R.id.clickPopDrawer)
    void onClickPopDrawer() {
        drawerLayoutRoot.openDrawer(GravityCompat.START);
    }

    @Click(R.id.clickHistory)
    void onClickHistory() {
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
                .getWikiDetail(project.owner_user_name, project.name, wiki.iid, wiki.lastVersion)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserver<Wiki>(this) {
                    @Override
                    public void onSuccess(Wiki data) {
                        super.onSuccess(data);

                        selectWiki = data;
                        displayWebviewContent(selectWiki.html);
                        Logger.d(data.html);
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                        BlankViewDisplay.setBlank(0, WikiMainActivity.this, false, blankLayout, v -> onRefrush());
                    }
                });
    }

    String wikiContent = "";

    @Background(serial = "content")
    void getContent(String html) {
        wikiContent = "";
        try {
            String bubble = Global.readTextFile(getAssets().open("markdown.html"));
            wikiContent =  bubble.replace("${webview_content}", html);
        } catch (Exception e) {
            Global.errorLog(e);
        }
    }

    void displayWebviewContent(String html) {
//        getContent(html);
//        Global.setWebViewContent(webView, wikiContent);
        Global.setWebViewContent(webView, "markdown.html", html);
        BlankViewDisplay.setBlank(html.length(), WikiMainActivity.this, true, blankLayout, v -> onRefrush());

    }

    private void addTreeNode(TreeNode node, List<Wiki> wikis) {
        for (Wiki item : wikis) {
            TreeNode childNode = new TreeNode(item);
            node.addChild(childNode);
            if (firstTreeNode == null) {
                firstTreeNode = childNode;
            }

            addTreeNode(childNode, item.children);
        }
    }

}

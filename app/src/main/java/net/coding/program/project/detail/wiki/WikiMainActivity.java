package net.coding.program.project.detail.wiki;

import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import net.coding.program.R;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.model.ProjectObject;
import net.coding.program.network.HttpObserver;
import net.coding.program.network.Network;
import net.coding.program.network.model.wiki.Wiki;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
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
    ViewGroup drawerLayout;

    List<Wiki> dataList = new ArrayList<>();
    AndroidTreeView treeView;

    NodeHolder selectNode = null;
    TreeNode firstTreeNode = null;

    @AfterViews
    void initWikiMainActivity() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setActionBarTitle(project.name);

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

                        buildTree();
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(NodeHolder node) {
        selectNode(node);
    }

    private void buildTree() {
        TreeNode root = TreeNode.root();
        addTreeNode(root, dataList);

        treeView = new AndroidTreeView(this, root);
        treeView.setDefaultViewHolder(NodeHolder.class);
        drawerLayout.addView(treeView.getView());

        selectNode((NodeHolder) firstTreeNode.getViewHolder());
    }

    private void selectNode(NodeHolder node) {
        if (selectNode == node) {
            return;
        }

        if (selectNode != null) {
            selectNode.select(false);
        }

        selectNode = node;

        if (selectNode != null) {
            selectNode.select(true);
        }
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

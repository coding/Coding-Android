package net.coding.program.project.detail.wiki;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.quickAdapter.easyRegularAdapter;

import net.coding.program.R;
import net.coding.program.common.CodingColor;
import net.coding.program.common.event.EventRefresh;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.network.HttpObserver;
import net.coding.program.network.Network;
import net.coding.program.network.model.wiki.Wiki;
import net.coding.program.network.model.wiki.WikiHistory;
import net.coding.program.param.ProjectJumpParam;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EActivity(R.layout.activity_wiki_history)
public class WikiHistoryActivity extends BackActivity {

    @Extra
    ProjectJumpParam jumpParam;

    @Extra
    Wiki wiki;

    @ViewById
    UltimateRecyclerView listView;

//    @ViewById
//    View blankView;

    MyAdapter adapter;

    List<WikiHistory> listData = new ArrayList<>();

    @AfterViews
    void initOrderListFragment() {
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        listView.setLayoutManager(manager);

        adapter = new MyAdapter(listData);
        listView.setAdapter(adapter);

//        blankView.setVisibility(View.VISIBLE);

        onRefrush();
    }

    void onRefrush() {
        Network.getRetrofit(this)
                .getWikiHistory(jumpParam.user, jumpParam.project, wiki.iid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserver<List<WikiHistory>>(this) {
                    @Override
                    public void onSuccess(List<WikiHistory> data) {
                        super.onSuccess(data);

                        listData.addAll(data);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);

                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventRefresh(EventRefresh event) {
        if (event.refresh) {
            adapter.notifyDataSetChanged();
        }
    }

    protected class MyAdapter extends easyRegularAdapter<WikiHistory, ViewHolder> {

        private View.OnClickListener clickItem = v -> {
            umengEvent(UmengEvent.E_WIKI, "点击历史版本");

            WikiHistory history = (WikiHistory) v.getTag();
            WikiHistoryDetailActivity_.intent(WikiHistoryActivity.this)
                    .wiki(wiki)
                    .version(history.version)
                    .project(jumpParam)
                    .start();
        };

        public MyAdapter(List<WikiHistory> list) {
            super(list);
        }

        @Override
        protected int getNormalLayoutResId() {
            return R.layout.wiki_history_item;
        }

        @Override
        protected ViewHolder newViewHolder(View view) {
            return new ViewHolder(view, clickItem);
        }

        @Override
        protected void withBindHolder(ViewHolder holder, WikiHistory data, int position) {
            holder.title.setText(data.getVersion());
            holder.style.setText(data.getTime());
            holder.id.setText(data.getEditorName());
            String msg = data.getMsg();
            if (TextUtils.isEmpty(msg)) {
                msg = "未填写";
                holder.user.setTextColor(CodingColor.font3);
            } else {
                holder.user.setTextColor(CodingColor.font1);
            }
            holder.user.setText(msg);

            if (position == source.size() - 1) {
                holder.nextTopDivide.setVisibility(View.GONE);
            } else {
                holder.nextTopDivide.setVisibility(View.VISIBLE);
            }

            holder.rootLayout.setTag(data);
        }
    }

    class ViewHolder extends UltimateRecyclerviewViewHolder {

        private View rootLayout;
        private TextView title;
        private TextView style;
        private View divideLine;
        private TextView orderId;
        private TextView orderUser;
        private TextView orderTime;
        private TextView id;
        private TextView user;

        private View nextTopDivide;

        public ViewHolder(View view, View.OnClickListener click) {
            super(view);
            rootLayout = view;
            title = (TextView) view.findViewById(R.id.title);
            style = (TextView) view.findViewById(R.id.style);
            divideLine = (View) view.findViewById(R.id.divideLine);
            orderId = (TextView) view.findViewById(R.id.orderId);
            orderUser = (TextView) view.findViewById(R.id.orderUser);
            orderTime = (TextView) view.findViewById(R.id.orderTime);
            id = (TextView) view.findViewById(R.id.id);
            user = (TextView) view.findViewById(R.id.submitMessage);
            nextTopDivide = view.findViewById(R.id.nextTopDivide);
            rootLayout.setOnClickListener(click);
        }
    }

}

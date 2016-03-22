package net.coding.program.project.detail.merge;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.DensityUtil;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.ClickSmallImage;
import net.coding.program.common.Global;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.RedPointTip;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.comment.BaseCommentParam;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.common.widget.ListItem1;
import net.coding.program.model.BaseComment;
import net.coding.program.model.Merge;
import net.coding.program.model.MergeDetail;
import net.coding.program.model.ProjectObject;
import net.coding.program.model.RefResourceObject;
import net.coding.program.model.RequestData;
import net.coding.program.project.detail.MembersSelectActivity_;
import net.coding.program.project.git.CommitListActivity_;
import net.coding.program.task.add.RefResourceActivity;
import net.coding.program.task.add.RefResourceActivity_;
import net.coding.program.user.UserDetailActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import se.emilsjolander.stickylistheaders.ExpandableStickyListHeadersListView;

@EActivity(R.layout.activity_merge_detail)
//@OptionsMenu(R.menu.menu_merge_detail)
public class MergeDetailActivity extends BackActivity {

    public static final int RESULT_COMMENT = 1;
    public static final int RESULT_MERGE = 2;
    public static final int RESULT_RESUSE_REFRESOURCE = 3;

    private static final String HOST_MERGE_COMMENTS = "HOST_MERGE_COMMENTS";
    private static final String HOST_MERGE_REFUSE = "HOST_MERGE_REFUSE";
    private static final String HOST_MERGE_CANNEL = "HOST_MERGE_CANNEL";
    private static final String HOST_MERGE_DETAIL = "HOST_MERGE_DETAIL";
    private static final String HOST_DELETE_COMMENT = "HOST_DELETE_COMMENT";

    private static final String TAG_REVIEW_GOOD = "TAG_REVIEW_GOOD";

    @Extra
    Merge mMerge;

    @Extra
    String mMergeUrl;

    @ViewById
    View actionLayout;
    @ViewById
    View actionAccept;
    @ViewById
    View actionRefuse;
    @ViewById
    View actionCancel;
    @ViewById
    ExpandableStickyListHeadersListView listView;

    MergeCommentAdaper mAdapter;
    MyImageGetter myImageGetter = new MyImageGetter(this);

    private ArrayList<RefResourceObject> refResourceList = new ArrayList<>();

    View.OnClickListener mOnClickItem = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final BaseComment item = (BaseComment) v.getTag();
            if (item.isMy()) {
                showDialog("merge", "删除评论?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String url = mMerge.getHttpDeleteComment(item);
                        deleteNetwork(url, HOST_DELETE_COMMENT, item);
                    }
                });
            } else {
                String name = item.owner.name;
                CommentActivity.CommentParam param = createParam(name);
                CommentActivity_.intent(MergeDetailActivity.this).mParam(param).startForResult(RESULT_COMMENT);
            }
        }
    };

    private MergeDetail mMergeDetail;
    private TextView mergeContent;
    private View mergeContentDivide;

    @AfterViews
    protected final void initMergeDetailActivity() {
        String httpReviewers;
        if (mMerge != null) {
            initByMereData();
            getNetwork(mMerge.getHttpDetail(), HOST_MERGE_DETAIL);
            httpReviewers = mMerge.getHttpReviewers();
        } else {
            showDialogLoading();
            String s = mMergeUrl.replace("/u/", "/api/user/")
                    .replace("/p/", "/project/");
//            s += "/base";
            getNetwork(s + "/base", HOST_MERGE_DETAIL);
            httpReviewers = s + "/reviewers";
        }
    }

    private void initByMereData() {
        getSupportActionBar().setTitle(mMerge.getTitleIId());

        String uri = mMerge.getHttpComments();
        getNetwork(uri, HOST_MERGE_COMMENTS);

        View mListHead = mInflater.inflate(R.layout.activity_merge_detail_head, null);
        initHead(mListHead);
        listView.addHeaderView(mListHead, null, false);
        View footer = mInflater.inflate(R.layout.activity_merge_detail_footer, null);
        listView.addFooterView(footer);
        initFooter(footer);

        BaseCommentParam param = new BaseCommentParam(new ClickSmallImage(this), mOnClickItem, myImageGetter, getImageLoad(), mOnClickUser);
        mAdapter = new MergeCommentAdaper(param);
        listView.setAdapter(mAdapter);
        listView.setAreHeadersSticky(false);

        updateReviewer();
        setActionStyle(false, false, false);
    }

    private void updateBottomBarStyle() {
        if (mMergeDetail == null) {
            return;
        }

        boolean canEdit = mMergeDetail.isCanEdit();
        boolean canEditSrc = mMergeDetail.isCanEditSrcBranch();
        if (mMerge.isStyleCanMerge()) {
            setActionStyle(canEdit, canEdit, canEditSrc);
        } else if (mMerge.isStyleCannotMerge()) {
            setActionStyle(canEdit, false, canEditSrc);
        } else {
            setActionStyle(false, false, false);
        }
    }

    private void setActionStyle(boolean refuse, boolean accept, boolean cancel) {
        if (!refuse && !accept && !cancel) {
            actionLayout.setVisibility(View.GONE);
        } else {
            actionLayout.setVisibility(View.VISIBLE);
            actionAccept.setVisibility(accept ? View.VISIBLE : View.GONE);
            actionRefuse.setVisibility(refuse ? View.VISIBLE : View.GONE);
            actionCancel.setVisibility(cancel ? View.VISIBLE : View.GONE);
        }
    }

    @Click
    protected final void actionAccept() {
        MergeAcceptActivity_.intent(this).mMergeDetail(mMergeDetail).startForResult(RESULT_MERGE);
    }

    @Click
    protected final void actionRefuse() {
        showDialog(mMerge.getTitle(), "确定要拒绝吗？", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String host = mMerge.getHttpRefuse();
                postNetwork(host, HOST_MERGE_REFUSE);
            }
        });
    }

    @Click
    protected final void actionCancel() {
        showDialog(mMerge.getTitle(), "确定要取消吗？", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String host = mMerge.getHttpCancel();
                postNetwork(host, HOST_MERGE_CANNEL);
            }
        });
    }

    private void initHead(View head) {
        head.findViewById(R.id.itemCommit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommitListActivity_.intent(MergeDetailActivity.this).mMerge(mMerge).start();
            }
        });

        ListItem1 itemFiles = (ListItem1) head.findViewById(R.id.itemFile);
        itemFiles.showBadge(RedPointTip.show(this, RedPointTip.Type.MergeFile320));
        itemFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RedPointTip.markUsed(MergeDetailActivity.this, RedPointTip.Type.MergeFile320);
                ((ListItem1) v).showBadge(false);
                MergeFileListActivity_.intent(MergeDetailActivity.this).mMerge(mMerge).start();
            }
        });

        ((TextView) head.findViewById(R.id.title)).setText(mMerge.getTitleSpannable());

        ImageView imageView = (ImageView) head.findViewById(R.id.icon);
        iconfromNetwork(imageView, mMerge.getAuthor().avatar);

        ((TextView) head.findViewById(R.id.time)).setText(Global.dayToNowCreate(mMerge.getCreatedAt()));

        TextView styleView = (TextView) head.findViewById(R.id.mergeStyle);

        String[] styles = Merge.STYLES;
        final String[] styleStrings = new String[]{
                "已合并",
                "已拒绝",
                "可合并",
                "不可合并",
                "已取消"
        };

        final String[] actionStrings = new String[]{
                "已合并",
                "已拒绝",
                "已拒绝",
                "已拒绝",
                "已取消"
        };
        String action = "";
        final int[] styleColors = new int[]{
                0xff3bbd79, 0xfffb3b30, 0xff3bbd79, 0xffac8cd3, 0xff666666
        };

        for (int i = 0; i < styles.length; ++i) {
            if (mMerge.getMergeStatus().equals(styles[i])) {
                styleView.setText(styleStrings[i]);
                styleView.setTextColor(styleColors[i]);
                action = actionStrings[i];
            }
        }

        String src = mMerge.getSrcBranch();
        String desc = mMerge.getDescBranch();

        ((TextView) head.findViewById(R.id.branchSrc)).setText(src);
        ((TextView) head.findViewById(R.id.branchDesc)).setText(desc);


        ((TextView) head.findViewById(R.id.mergeActionUser)).setText(mMerge.getActionAuthor().name);
        String mergeActionMessage = String.format(" %s %s这个%s", Global.dayToNow(mMerge.getAction_at()),
                action, ProjectObject.getTitle(mMerge.isPull()));
        ((TextView) head.findViewById(R.id.mergeLog)).setText(mergeActionMessage);


        View mergeTreate = head.findViewById(R.id.mergeTreate);
        if (mMerge.isMergeTreate()) {
            mergeTreate.setVisibility(View.VISIBLE);
            int color;
            int iconRes;
            if (mMerge.isMergeAccept()) {
                color = R.color.merge_green;
                iconRes = R.drawable.ic_listitem_merge_accept;
            } else {
                color = R.color.merge_red;
                iconRes = R.drawable.ic_listitem_merge_refuse;
            }
            head.findViewById(R.id.mergeIcon0).setBackgroundResource(color);
            head.findViewById(R.id.mergeIcon1).setBackgroundResource(iconRes);
        } else {
            mergeTreate.setVisibility(View.GONE);
        }

        // 取到 detail 后再显示
        mergeContent = (TextView) head.findViewById(R.id.mergeContent);
        mergeContent.setVisibility(View.GONE);
        mergeContentDivide = head.findViewById(R.id.mergeContentDivide);
        mergeContentDivide.setVisibility(View.GONE);

        head.findViewById(R.id.itemRefResource).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RefResourceActivity.Param param = new RefResourceActivity.Param(mMerge.getProjectPath(),
                        mMerge.getIid());

                RefResourceActivity_.intent(MergeDetailActivity.this)
                        .mData(refResourceList)
                        .mParam(param)
                        .startForResult(RESULT_RESUSE_REFRESOURCE);
            }
        });
    }

    private void initFooter(View footer) {
        footer.findViewById(R.id.itemAddComment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommentActivity.CommentParam param = createParam("");
                CommentActivity_.intent(MergeDetailActivity.this).mParam(param).startForResult(RESULT_COMMENT);
            }
        });
    }

    public CommentActivity.CommentParam createParam(final String atSomeOne) {
        return new MergeCommentParam(mMerge, atSomeOne);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_COMMENT) {
            if (resultCode == RESULT_OK) {
                String commentString = (String) data.getSerializableExtra("data");
                try {
                    JSONObject json = new JSONObject(commentString);
                    BaseComment comment = new BaseComment(json);
                    mAdapter.appendData(comment);
                    mAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    showButtomToast("" + e.toString());
                }
            }

        } else if (requestCode == RESULT_MERGE) {
            if (resultCode == RESULT_OK) {
                finishAndUpdateList();
            }
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_MERGE_COMMENTS)) {
            if (code == 0) {
                JSONArray json = respanse.getJSONArray("data");
                ArrayList<BaseComment> arrayData = new ArrayList<>();
                for (int i = 0; i < json.length(); ++i) {
                    BaseComment comment = new BaseComment(json.getJSONArray(i).getJSONObject(0));
                    arrayData.add(comment);
                }
                mAdapter.appendDataUpdate(arrayData);
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(HOST_DELETE_COMMENT)) {
            if (code == 0) {
                mAdapter.removeDataUpdate(data);
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(HOST_MERGE_REFUSE)) {
            umengEvent(UmengEvent.CODE, "拒绝mrpr");
            if (code == 0) {
                finishAndUpdateList();
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(HOST_MERGE_CANNEL)) {
            umengEvent(UmengEvent.CODE, "取消mrpr");
            if (code == 0) {
                finishAndUpdateList();
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(HOST_MERGE_DETAIL)) {
            hideProgressDialog();
            if (code == 0) {
                mMergeDetail = new MergeDetail(respanse.optJSONObject("data"));
                if (mMerge == null) {
                    mMerge = mMergeDetail.getMerge();
                    initByMereData();
                }
                updateBottomBarStyle();
                Spannable spanContent = Global.changeHyperlinkColor(mMergeDetail.getContent());
                if (spanContent.length() == 0) {
                    mergeContent.setVisibility(View.GONE);
                    mergeContentDivide.setVisibility(View.GONE);
                } else {
                    mergeContent.setVisibility(View.VISIBLE);
                    mergeContentDivide.setVisibility(View.VISIBLE);
                    mergeContent.setText(spanContent);
                }

                refreshReviewers();
                refreshRefResource();
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(TAG_REVIEW_GOOD)) {
            if (code == 0) {
                refreshReviewers();
            }
        }
    }


    private void refreshRefResource() {
        String url = Global.HOST_API + mMerge.getProjectPath() + "/resource_reference/" + mMerge.getIid();
        MyAsyncHttpClient.get(this, url, new MyJsonResponse(this) {
            @Override
            public void onMySuccess(JSONObject response) {
                super.onMySuccess(response);

                refResourceList.clear();
                JSONObject jsonData = response.optJSONObject("data");
                Iterator<String> iter = jsonData.keys();
                while (iter.hasNext()) {
                    String key = iter.next();
                    JSONArray array = jsonData.optJSONArray(key);
                    for (int i = 0; i < array.length(); ++i) {
                        JSONObject item = array.optJSONObject(i);
                        try {
                            refResourceList.add(new RefResourceObject(item));
                        } catch (Exception e) {
                            Global.errorLog(e);
                        }
                    }
                }

                updateRefResourceUI();
            }
        });
    }

    private void updateRefResourceUI() {
        View item = findViewById(R.id.itemRefResource);
        if (refResourceList.isEmpty()) {
            item.setVisibility(View.GONE);
        } else {
            item.setVisibility(View.VISIBLE);

            ((TextView) item.findViewById(R.id.text2)).setText(refResourceList.size() + "个资源");
            item.findViewById(R.id.text2).setVisibility(View.VISIBLE);
        }
    }

    private void refreshReviewers() {
        MyAsyncHttpClient.get(this, mMerge.getHttpReviewers(), new MyJsonResponse(this) {
            @Override
            public void onMySuccess(JSONObject response) {
                Log.d("reviewers", response.toString());
                JSONArray json = null;
                try {
                    json = response.optJSONObject("data").optJSONArray("reviewers");
                    List<Merge.Reviewer> arrayData = new ArrayList<>();
                    for (int i = 0; i < json.length(); ++i) {
                        Merge.Reviewer user = new Merge.Reviewer(json.getJSONObject(i));
                        arrayData.add(user);
                    }
                    json = response.optJSONObject("data").optJSONArray("volunteer_reviewers");
                    for (int i = 0; i < json.length(); ++i) {
                        Merge.Reviewer user = new Merge.Reviewer(json.getJSONObject(i));
                        arrayData.add(user);
                    }
                    mMerge.setReviewers(arrayData);
                    updateReviewer();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void finishAndUpdateList() {
        setResult(RESULT_OK);
        finish();
    }

    private static class MergeCommentParam extends CommentActivity.CommentParam implements Serializable {
        private Merge mMerge;
        private String atSomeOne;

        public MergeCommentParam(Merge mMerge, String atSomeOne) {
            this.mMerge = mMerge;
            this.atSomeOne = atSomeOne;
        }

        @Override
        public RequestData getSendCommentParam(String input) {
            RequestData request = mMerge.getHttpSendComment();
            request.setContent(input);
            return request;
        }

        @Override
        public String getAtSome() {
            return atSomeOne;
        }

        @Override
        public String getAtSomeUrl() {
            return mMerge.getMergeAtMemberUrl();
        }

        @Override
        public String getProjectPath() {
            return mMerge.getProjectPath();
        }

        @Override
        public boolean isPublicProject() {
            return mMerge.isPull();
        }
    }

    private void updateReviewer() {
        ListItem1 reviewers = (ListItem1) findViewById(R.id.itemReviewer);

        int role = 0;
        if (mMerge.authorIsMe()) {
            role = 1;
        } else {
            role = 2;
            if (mMerge.getReviewers() != null) {
                for (Merge.Reviewer reviewer : mMerge.getReviewers()) {
                    if (MyApp.sUserObject.id == reviewer.user.id) {
                        if (reviewer.value > 0)
                            role = 3;
                        break;
                    }
                }
            }
        }
        if (role > 0) {
            TextView tv = (TextView) reviewers.findViewById(R.id.text2);
            tv.setVisibility(View.VISIBLE);
            View arrow = reviewers.findViewById(R.id.arrow);
            if (role == 1) {
                tv.setText("添加");
                tv.setTextColor(getResources().getColor(R.color.font_black_9));
                tv.setCompoundDrawables(null, null, null, null);
                arrow.setVisibility(View.VISIBLE);
            } else if (role == 3) {
                tv.setText("撤消 +1 ");
                tv.setTextColor(getResources().getColor(R.color.green));
                tv.setCompoundDrawables(null, null, null, null);
                arrow.setVisibility(View.GONE);
            } else if (role == 2) {
                tv.setText("+1  ");
                Drawable up = getResources().getDrawable(R.drawable.thumb_up);
                up.setBounds(0, 0, up.getMinimumWidth(), up.getMinimumHeight());
                tv.setCompoundDrawables(up, null, null, null);
                arrow.setVisibility(View.GONE);
                tv.setTextColor(getResources().getColor(R.color.green));
            }
            tv.setGravity(Gravity.CENTER);
        }
        final int roleFinal = role;
        reviewers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (roleFinal == 0) {
                    Intent intent = new Intent(MergeDetailActivity.this, MembersSelectActivity_.class);
                    intent.putExtra("mMerge", mMerge);
                    startActivityForResult(intent, MergeReviewerListFragment.RESULT_ADD_USER);
                } else if (roleFinal == 1) {
                    Intent intent = new Intent(MergeDetailActivity.this, MembersSelectActivity_.class);
                    intent.putExtra("mMerge", mMerge);
                    intent.putExtra("mSelect", true);
                    startActivityForResult(intent, MergeReviewerListFragment.RESULT_ADD_USER);
                } else if (roleFinal == 2) {
                    postNetwork(mMerge.getHttpReviewGood(), new RequestParams(), TAG_REVIEW_GOOD);
                } else if (roleFinal == 3) {
                    deleteNetwork(mMerge.getHttpReviewGood(), TAG_REVIEW_GOOD);
                }
            }
        });


        LinearLayout reviewersLayout = (LinearLayout) findViewById(R.id.reviewers);
        reviewersLayout.removeAllViews();
        if (mMerge.getReviewers() != null && mMerge.getReviewers().size() > 0) {
            int imageSize = DensityUtil.dip2px(this, 33);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(imageSize, imageSize);
            lp.rightMargin = DensityUtil.dip2px(this, 10);
            for (Merge.Reviewer reviewer : mMerge.getReviewers()) {
                CircleImageView circleImageView = new CircleImageView(this);
                reviewersLayout.addView(circleImageView, lp);
                iconfromNetwork(circleImageView, reviewer.user.avatar);
                circleImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UserDetailActivity_.intent(MergeDetailActivity.this)
                                .globalKey(reviewer.user.global_key).start();
                    }
                });
            }
            reviewersLayout.setVisibility(View.VISIBLE);
        } else {
            reviewersLayout.setVisibility(View.GONE);
        }
    }

    @OnActivityResult(RESULT_RESUSE_REFRESOURCE)
    void resultRefResource(int resultCode, @OnActivityResult.Extra ArrayList<RefResourceObject> resultData) {
        if (resultCode == RESULT_OK) {
            refResourceList = resultData;
            updateRefResourceUI();
        }
    }

    @OnActivityResult(MergeReviewerListFragment.RESULT_ADD_USER)
    public void onAddReviewer(int result) {
        if (result == Activity.RESULT_OK) {
            refreshReviewers();
        }
    }
}

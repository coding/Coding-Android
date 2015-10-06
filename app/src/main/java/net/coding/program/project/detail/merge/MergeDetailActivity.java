package net.coding.program.project.detail.merge;

import android.content.DialogInterface;
import android.content.Intent;
import android.text.Spannable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.coding.program.common.ui.BackActivity;
import net.coding.program.R;
import net.coding.program.common.ClickSmallImage;
import net.coding.program.common.Global;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.RedPointTip;
import net.coding.program.common.comment.BaseCommentParam;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.common.widget.ListItem1;
import net.coding.program.model.BaseComment;
import net.coding.program.model.Merge;
import net.coding.program.model.MergeDetail;
import net.coding.program.model.PostRequest;
import net.coding.program.model.ProjectObject;
import net.coding.program.project.git.CommitListActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.ExpandableStickyListHeadersListView;

@EActivity(R.layout.activity_merge_detail)
//@OptionsMenu(R.menu.menu_merge_detail)
public class MergeDetailActivity extends BackActivity {

    public static final int RESULT_COMMENT = 1;
    public static final int RESULT_MERGE = 2;

    private static final String HOST_MERGE_COMMENTS = "HOST_MERGE_COMMENTS";
    private static final String HOST_MERGE_REFUSE = "HOST_MERGE_REFUSE";
    private static final String HOST_MERGE_CANNEL = "HOST_MERGE_CANNEL";
    private static final String HOST_MERGE_DETAIL = "HOST_MERGE_DETAIL";
    private static final String HOST_DELETE_COMMENT = "HOST_DELETE_COMMENT";

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
        if (mMerge != null) {
            initByMereData();
            getNetwork(mMerge.getHttpDetail(), HOST_MERGE_DETAIL);
        } else {
            showDialogLoading();
            String s = mMergeUrl.replace("/u/", "/api/user/")
                    .replace("/p/", "/project/");
            s += "/base";
            getNetwork(s, HOST_MERGE_DETAIL);
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
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    private void finishAndUpdateList() {
        setResult(RESULT_OK);
        finish();
    }

    private static class MergeCommentParam implements CommentActivity.CommentParam, Serializable {
        private Merge mMerge;
        private String atSomeOne;

        public MergeCommentParam(Merge mMerge, String atSomeOne) {
            this.mMerge = mMerge;
            this.atSomeOne = atSomeOne;
        }

        @Override
        public PostRequest getSendCommentParam(String input) {
            PostRequest request = mMerge.getHttpSendComment();
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
}

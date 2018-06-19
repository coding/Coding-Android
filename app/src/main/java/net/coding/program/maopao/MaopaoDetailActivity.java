package net.coding.program.maopao;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.CodingGlobal;
import net.coding.program.CustomWebViewClientOpenNew;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalCommon;
import net.coding.program.common.GlobalData;
import net.coding.program.common.ListModify;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.StartActivity;
import net.coding.program.common.model.Maopao;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.common.widget.input.MainInputView;
import net.coding.program.maopao.item.HtmlCommentHolder;
import net.coding.program.maopao.item.MaopaoLikeAnimation;
import net.coding.program.maopao.share.CustomShareBoard;
import net.coding.program.project.maopao.ProjectMaopaoAddActivity_;
import net.coding.program.third.EmojiFilter;
import net.coding.program.util.TextWatcherAt;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

@EActivity(R.layout.activity_maopao_detail)
public class MaopaoDetailActivity extends BackActivity implements StartActivity, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG_MAOPAO = "TAG_MAOPAO";
    private static final String TAG_PROJECT = "TAG_PROJECT";
    final String HOST_GOOD = Global.HOST_API + "/tweet/%s/%s";
    final int RESULT_REQUEST_AT = 1;
    private static final int RESULT_EDIT = 2;

    private final String TAG_LIKE_USERS = "TAG_LIKE_USERS";
    String URI_COMMENT_DELETE = Global.HOST_API + "/tweet/%s/comment/%s";
    String URI_COMMENT = Global.HOST_API + "/tweet/%s/comments?pageSize=500";
    String ADD_COMMENT = Global.HOST_API + "/tweet/%s/comment";
    String TAG_DELETE_MAOPAO = "TAG_DELETE_MAOPAO";
    @Extra
    Maopao.MaopaoObject mMaopaoObject;
    Maopao.MaopaoObject mMaopaoObjectOld;

    ProjectObject mProjectObject;

    @Extra
    ClickParam mClickParam;
    @ViewById
    ListView listView;
    @ViewById
    SwipeRefreshLayout swipeRefreshLayout;
    @ViewById
    MainInputView mEnterLayout;

    ArrayList<Maopao.Comment> mData = new ArrayList<>();
    MyImageGetter myImageGetter = new MyImageGetter(this);

    View.OnClickListener onClickSend = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mMaopaoObject == null) {
                showButtomToast(R.string.maopao_load_fail_comment);
                return;
            }

            EditText content = mEnterLayout.getEditText();
            String input = content.getText().toString();

            if (EmojiFilter.containsEmptyEmoji(v.getContext(), input)) {
                return;
            }

            Maopao.Comment comment = (Maopao.Comment) content.getTag();
            String uri = String.format(ADD_COMMENT, comment.tweet_id);

            RequestParams params = new RequestParams();

            String contentString;
            if (comment.id == 0) {
                contentString = Global.encodeInput("", input);
            } else {
                contentString = Global.encodeInput(comment.owner.name, input);
            }
            params.put("content", contentString);
            postNetwork(uri, params, ADD_COMMENT, 0, comment);

            showProgressBar(R.string.sending_comment);
        }
    };
    CheckBox likeBtn;
    LikeUsersArea likeUsersArea;
    View mListHead;
    TextView commentBtn;
    TextView reward;

    View.OnClickListener onClickDeleteMaopao = v -> actionDeleteMaopao();
    View.OnClickListener onClickEditMaopao = v -> actionEditMaopao();

    boolean mModifyComment = false;
    View.OnClickListener onClickComment = v -> {
        final Maopao.Comment comment = (Maopao.Comment) v.getTag();
        if (comment.isMy()) {
            showDialog("删除评论？", (dialog, which) -> {
                String url = String.format(URI_COMMENT_DELETE, comment.tweet_id, comment.id);
                deleteNetwork(url, URI_COMMENT_DELETE);
            });

        } else {
            prepareAddComment(comment, true);
        }
    };
    BaseAdapter adapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HtmlCommentHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.activity_maopao_detail_item, parent, false);
                holder = new HtmlCommentHolder(convertView, onClickComment, myImageGetter, getImageLoad(), GlobalCommon.mOnClickUser);
                convertView.setTag(R.id.layout, holder);

            } else {
                holder = (HtmlCommentHolder) convertView.getTag(R.id.layout);
            }

            Maopao.Comment data = mData.get(position);
            holder.setContent(data);

            return convertView;
        }
    };

    private void actionEditMaopao() {
        if (mClickParam == null || !mClickParam.isProjectMaopao()) {
            return;
        }

        ProjectMaopaoAddActivity_.intent(this)
                .projectObject(mProjectObject)
                .maopao(mMaopaoObject)
                .startForResult(RESULT_EDIT);
    }

    private void actionDeleteMaopao() {
        final int maopaoId = mMaopaoObject.id;
        showDialog("确定删除？", (dialog, which) -> {
            final String url;
            if (mClickParam != null && mClickParam.isProjectMaopao()) {
                if (mProjectObject != null) {
                    url = String.format(Global.HOST_API + "/project/%s/tweet/%s", mProjectObject.getId(), maopaoId);
                } else {
                    return;
                }
            } else {
                url = String.format(Global.HOST_API + "/tweet/%s", maopaoId);
            }
            deleteNetwork(url, TAG_DELETE_MAOPAO);
        });
    }

    @AfterViews
    protected final void initMaopaoDetailActivity() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mEnterLayout.setClickSend(onClickSend);
        mEnterLayout.addTextWatcher(new TextWatcherAt(this, this, RESULT_REQUEST_AT));

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.font_green);
        loadDataFromNetwork();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mClickParam != null && mClickParam.isProjectMaopao()) {
            // doing nothing
        } else if (mMaopaoObject != null) {
            int menuId = R.menu.activity_maopao_detail;
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(menuId, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case net.coding.program.R.id.action_copy:
                action_copy();
                return true;
            case android.R.id.home:
                close();
                return true;
            case R.id.action_del_maopao:
                actionDeleteMaopao();
                return true;

//            case R.id.action_inform:
//                InformMaopaoActivity_.intent(this)
//                        .maopaoId(mMaopaoObject.id)
//                        .start();
//                return true;

            default:
                return false;
        }
    }

    @OptionsItem
    protected final void action_share() {
        action_share_third();
    }

    protected final void action_copy() {
        Global.tipCopyLink(this, getLink());
    }

    @Override
    public void onRefresh() {
        if (mMaopaoObject != null) {
            if (mClickParam == null) {
                mClickParam = new ClickParam(mMaopaoObject.owner.global_key, String.valueOf(mMaopaoObject.id));
            }
            mMaopaoObjectOld = mMaopaoObject;
            mMaopaoObject = null;
        }

        loadDataFromNetwork();
    }

    private void loadDataFromNetwork() {
        if (mMaopaoObject == null) {
            if (!mClickParam.isProjectMaopao()) {
                String maopaoUrl = String.format(Global.HOST_API + "/tweet/%s/%s", mClickParam.name, mClickParam.maopaoId);
                getNetwork(maopaoUrl, TAG_MAOPAO);
            } else {
                String projectUrl = mClickParam.getHttpProject();
                setActionBarTitle(mClickParam.projectName);
                getNetwork(projectUrl, TAG_PROJECT);
                mEnterLayout.setVisibility(View.GONE);
            }
        } else {
            initData();
        }
    }

    private void initData() {
        URI_COMMENT = String.format(URI_COMMENT, mMaopaoObject.id);

        initHead();
        listView.setAdapter(adapter);

        // 项目公告不显示评论
        if (mProjectObject != null) {
            return;
        }

        getNetwork(URI_COMMENT, URI_COMMENT);
        prepareAddComment(mMaopaoObject, false);
    }

    void initHead() {
        if (mListHead == null) {
            int maopaoHeaderId = R.layout.activity_maopao_detail_head;
            if (mClickParam != null && mClickParam.isProjectMaopao()) {
                maopaoHeaderId = R.layout.activity_project_maopao_detail_head;
            }

            mListHead = mInflater.inflate(maopaoHeaderId, listView, false);
            listView.addHeaderView(mListHead, null, false);
        }

        ImageView icon = (ImageView) mListHead.findViewById(R.id.icon);
        icon.setOnClickListener(GlobalCommon.mOnClickUser);

        TextView name = (TextView) mListHead.findViewById(R.id.name);
        name.setOnClickListener(GlobalCommon.mOnClickUser);

        TextView time = (TextView) mListHead.findViewById(R.id.time);
        time.setText(Global.dayToNow(mMaopaoObject.created_at));

        iconfromNetwork(icon, mMaopaoObject.owner.avatar);
        icon.setTag(mMaopaoObject.owner);

        name.setText(mMaopaoObject.owner.name);
        name.setTag(mMaopaoObject.owner.global_key);

        WebView webView = mListHead.findViewById(R.id.comment);
        webView.setWebViewClient(new CustomWebViewClientOpenNew(this, mMaopaoObject.content));
        CodingGlobal.setWebViewContent(webView, CodingGlobal.WebviewType.markdown, mMaopaoObject.content);

        mListHead.setOnClickListener(v -> prepareAddComment(mMaopaoObject, true));

        mListHead.findViewById(R.id.shareBtn).setOnClickListener(v -> action_share_third());

        reward = (TextView) mListHead.findViewById(R.id.rewardCount);

        reward.setOnClickListener(v -> MaopaoListBaseFragment.popReward(MaopaoDetailActivity.this, v, null));

        commentBtn = (TextView) mListHead.findViewById(R.id.commentBtn);
        commentBtn.setOnClickListener(v -> prepareAddComment(mMaopaoObject, true));

        commentBtn.setText(String.valueOf(mMaopaoObject.comments));
        reward.setText(String.valueOf(mMaopaoObject.rewards));
        Drawable rewardIcon = getResources().getDrawable(mMaopaoObject.rewarded ?
                R.drawable.maopao_extra_rewarded : R.drawable.maopao_extra_reward);
        rewardIcon.setBounds(0, 0, rewardIcon.getIntrinsicWidth(), rewardIcon.getIntrinsicHeight());
        reward.setCompoundDrawables(rewardIcon,
                null, null, null);
        reward.setTag(mMaopaoObject);

        likeBtn = (CheckBox) mListHead.findViewById(R.id.likeBtn);
        likeBtn.setChecked(mMaopaoObject.liked);
        likeBtn.setText(String.valueOf(mMaopaoObject.likes));
        likeBtn.setOnClickListener(v -> {
            if (mMaopaoObject == null) {
                showMiddleToast(R.string.maopao_load_fail_like);
                return;
            }

            boolean like = ((CheckBox) v).isChecked();
            String type = like ? "like" : "unlike";
            if (like) {
                View good = mListHead.findViewById(R.id.maopaoGood);
                MaopaoLikeAnimation.playAnimation(good, v);
            }
            String uri = String.format(HOST_GOOD, mMaopaoObject.id, type);

            postNetwork(uri, new RequestParams(), HOST_GOOD, 0, mMaopaoObject);
        });


        likeUsersArea = new LikeUsersArea(mListHead, this, getImageLoad(), GlobalCommon.mOnClickUser);

        likeUsersArea.likeUsersLayout.setTag(MaopaoListBaseFragment.TAG_MAOPAO, mMaopaoObject);
        if (mMaopaoObject.like_users.isEmpty() && mMaopaoObject.likes > 0) {
            String hostLikes = String.format(LikeUsersListActivity.getHostLikesUser(), mMaopaoObject.id);
            getNetwork(hostLikes, TAG_LIKE_USERS);
        }
        likeUsersArea.displayLikeUser();

        TextView locationView = (TextView) mListHead.findViewById(R.id.location);
        MaopaoLocationArea.bind(locationView, mMaopaoObject);

        TextView photoType = (TextView) mListHead.findViewById(R.id.photoType);
        String device = mMaopaoObject.device;
        if (!device.isEmpty()) {
            final String format = "来自 %s";
            photoType.setText(String.format(format, device));
            photoType.setVisibility(View.VISIBLE);
        } else {
            photoType.setText("");
            photoType.setVisibility(View.GONE);
        }

        View deleteButton = mListHead.findViewById(R.id.deleteButton);
        if (mMaopaoObject.owner.isMe()) {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(onClickDeleteMaopao);
        } else {
            deleteButton.setVisibility(View.INVISIBLE);
        }

        View editButton = mListHead.findViewById(R.id.editButton);
        if (editButton != null) {
            if (mMaopaoObject.owner.isMe()) {
                editButton.setVisibility(View.VISIBLE);
                editButton.setOnClickListener(onClickEditMaopao);
            } else {
                editButton.setVisibility(View.INVISIBLE);
            }

            // 项目管理员可以编辑删除冒泡
            if (mProjectObject != null && mProjectObject.isManagerLevel()) {
                deleteButton.setVisibility(View.VISIBLE);
                deleteButton.setOnClickListener(onClickDeleteMaopao);

                editButton.setVisibility(View.VISIBLE);
                editButton.setOnClickListener(onClickEditMaopao);
            }
        }
    }

    @OnActivityResult(RESULT_REQUEST_AT)
    void onResultAt(int requestCode, Intent data) {
        if (requestCode == Activity.RESULT_OK) {
            String name = data.getStringExtra("name");
            mEnterLayout.insertText(name);
        }
    }

    @OnActivityResult(RESULT_EDIT)
    void onResult(int requestCode) {
        if (requestCode == RESULT_OK) {
            onRefresh();
            setResult(RESULT_OK);
        }
    }

    @OptionsItem(android.R.id.home)
    void close() {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (mEnterLayout.isPopCustomKeyboard()) {
            mEnterLayout.closeCustomKeyboard();
            return;
        }

        super.onBackPressed();
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(URI_COMMENT)) {
            swipeRefreshLayout.setRefreshing(false);
            if (code == 0) {
                mData.clear();
                JSONObject jsonData = respanse.optJSONObject("data");
                JSONArray jsonArray;
                if (jsonData != null) {
                    jsonArray = jsonData.optJSONArray("list");
                } else {
                    jsonArray = respanse.optJSONArray("data");
                }
                for (int i = 0; i < jsonArray.length(); ++i) {
                    Maopao.Comment comment = new Maopao.Comment(jsonArray.getJSONObject(i));
                    mData.add(comment);
                }

                if (mModifyComment) {
                    mMaopaoObject.comments = mData.size();
                    mMaopaoObject.comment_list = mData;
                    Intent intent = new Intent();
                    intent.putExtra(ListModify.DATA, mMaopaoObject);
                    intent.putExtra(ListModify.TYPE, ListModify.Edit);
                    setResult(Activity.RESULT_OK, intent);
                }

                adapter.notifyDataSetChanged();

            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(ADD_COMMENT)) {
            showProgressBar(false);
            if (code == 0) {
                umengEvent(UmengEvent.MAOPAO, "添加冒泡评论");
                getNetwork(URI_COMMENT, URI_COMMENT);

                mEnterLayout.restoreDelete(data);

                mEnterLayout.clearContent();
                mEnterLayout.hideKeyboard();

                mModifyComment = true;


                Intent intent = new Intent();
                intent.putExtra(ListModify.DATA, mMaopaoObject);
                intent.putExtra(ListModify.TYPE, ListModify.ModifyComment);
                setResult(Activity.RESULT_OK, intent);

            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(HOST_GOOD)) {
            if (code == 0) {
                Maopao.MaopaoObject maopao = mMaopaoObject;
                maopao.liked = !maopao.liked;
                if (maopao.liked) {
                    umengEvent(UmengEvent.MAOPAO, "冒泡点赞");
                    Maopao.Like_user like_user = new Maopao.Like_user(GlobalData.sUserObject);
                    maopao.like_users.add(0, like_user);
                    ++maopao.likes;
                } else {
                    umengEvent(UmengEvent.MAOPAO, "冒泡取消点赞");
                    for (int j = 0; j < maopao.like_users.size(); ++j) {
                        if (maopao.like_users.get(j).global_key.equals(GlobalData.sUserObject.global_key)) {
                            maopao.like_users.remove(j);
                            --maopao.likes;
                            break;
                        }
                    }
                }

                likeUsersArea.displayLikeUser();
                likeBtn.setText(String.valueOf(mMaopaoObject.likes));

                Intent intent = new Intent();
                intent.putExtra(ListModify.DATA, mMaopaoObject);
                intent.putExtra(ListModify.TYPE, ListModify.Edit);
                setResult(Activity.RESULT_OK, intent);

            } else {
                showErrorMsg(code, respanse);
            }
            likeBtn.setChecked(mMaopaoObject.liked);
        } else if (tag.equals(TAG_PROJECT)) {
            if (code == 0) {
                mProjectObject = new ProjectObject(respanse.optJSONObject("data"));
                String maopaoUrl = Maopao.getHttpProjectMaopao(mProjectObject.getId(), Integer.valueOf(mClickParam.maopaoId));

                String projectPath = "/project/" + mProjectObject.getId();
                URI_COMMENT_DELETE = Global.HOST_API + projectPath + "/tweet/%s/comment/%s";
                URI_COMMENT = Global.HOST_API + projectPath + "/tweet/%s/comments?pageSize=500";
                ADD_COMMENT = Global.HOST_API + projectPath + "/tweet/%s/comment";

                getNetwork(maopaoUrl, TAG_MAOPAO);
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(TAG_MAOPAO)) {
            if (code == 0) {
                mMaopaoObject = new Maopao.MaopaoObject(respanse.getJSONObject("data"));
                invalidateOptionsMenu();
                initData();
            } else {
                mMaopaoObject = mMaopaoObjectOld;
                swipeRefreshLayout.setRefreshing(false);
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(URI_COMMENT_DELETE)) {
            if (code == 0) {
                mModifyComment = true;
                getNetwork(URI_COMMENT, URI_COMMENT);


                Intent intent = new Intent();
                intent.putExtra(ListModify.DATA, mMaopaoObject);
                intent.putExtra(ListModify.TYPE, ListModify.ModifyComment);
                setResult(Activity.RESULT_OK, intent);

            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(TAG_DELETE_MAOPAO)) {
            if (code == 0) {

                Intent intent = new Intent();
                intent.putExtra(ListModify.TYPE, ListModify.Delete);
                intent.putExtra(ListModify.ID, mMaopaoObject.id);
                setResult(Activity.RESULT_OK, intent);
                finish();
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(TAG_LIKE_USERS)) {
            if (code == 0) {
                JSONObject jsonData = respanse.getJSONObject("data");
                JSONArray jsonArray = jsonData.getJSONArray("list");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    Maopao.Like_user user = new Maopao.Like_user(jsonArray.getJSONObject(i));
                    mMaopaoObject.like_users.add(user);
                }

                likeUsersArea.displayLikeUser();
            }
        }
    }

    void prepareAddComment(Object data, boolean popKeyboard) {
        Maopao.Comment comment = null;
        EditText content = mEnterLayout.getEditText();
        if (data instanceof Maopao.Comment) {
            comment = (Maopao.Comment) data;
            content.setHint("回复 " + comment.owner.name);
            content.setTag(comment);
        } else if (data instanceof Maopao.MaopaoObject) {
            comment = new Maopao.Comment((Maopao.MaopaoObject) data);
            if (mClickParam != null && mClickParam.isProjectMaopao()) {
                content.setHint("评论公告");
            } else {
                content.setHint("评论冒泡");
            }
            content.setTag(comment);
        }

        mEnterLayout.restoreLoad(comment);

        if (popKeyboard) {
            content.requestFocus();
            Global.popSoftkeyboard(MaopaoDetailActivity.this, content, true);
        }
    }

    protected String getLink() {
        if (mMaopaoObject == null) {
            return "";
        } else {
            return mMaopaoObject.getLink();
        }
    }

    void action_share_third() {
        mEnterLayout.hideKeyboard();
        CustomShareBoard.ShareData shareData = new CustomShareBoard.ShareData(mMaopaoObject);
        CustomShareBoard shareBoard = new CustomShareBoard(this, shareData);
        Rect rect = new Rect();
        View decorView = getWindow().getDecorView();
        decorView.getWindowVisibleDisplayFrame(rect);
        int winHeight = getWindow().getDecorView().getHeight();
        // 在 5.0 的android手机上，如果是 noactionbar，显示会有问题
        shareBoard.showAtLocation(decorView, Gravity.BOTTOM, 0, winHeight - rect.bottom);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CustomShareBoard.onActivityResult(requestCode, resultCode, data, this);
    }

    public static class ClickParam implements Serializable {
        String name = "";
        String maopaoId = "";
        String projectName = "";

        public ClickParam(String name, String maopaoId) {
            this.name = name;
            this.maopaoId = maopaoId;
        }

        public ClickParam(String userGK, String projectName, String maopaoId) {
            this.name = userGK;
            this.projectName = projectName;
            this.maopaoId = maopaoId;
        }

        public boolean isProjectMaopao() {
            return !projectName.isEmpty();
        }

        public String getHttpProject() {
            return ProjectObject.getHttpProject(name, projectName);
        }
    }

    @Override
    protected void onDestroy() {
        CustomShareBoard.onDestory(this);

        super.onDestroy();
    }

    //    private UMSocialService mController = UMServiceFactory.getUMSocialService("net.coding.program");

}

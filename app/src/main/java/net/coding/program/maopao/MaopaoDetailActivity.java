package net.coding.program.maopao;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.umeng.socialize.sso.UMSsoHandler;

import net.coding.program.common.ui.BackActivity;
import net.coding.program.ImagePagerActivity_;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.HtmlContent;
import net.coding.program.common.ListModify;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.StartActivity;
import net.coding.program.common.TextWatcherAt;
import net.coding.program.common.comment.HtmlCommentHolder;
import net.coding.program.common.enter.EnterEmojiLayout;
import net.coding.program.common.enter.EnterLayout;
import net.coding.program.common.htmltext.URLSpanNoUnderline;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.maopao.item.MaopaoLikeAnimation;
import net.coding.program.maopao.share.CustomShareBoard;
import net.coding.program.model.Maopao;
import net.coding.program.third.EmojiFilter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;

@EActivity(R.layout.activity_maopao_detail)
public class MaopaoDetailActivity extends BackActivity implements StartActivity, SwipeRefreshLayout.OnRefreshListener {

    final String HOST_GOOD = Global.HOST_API + "/tweet/%s/%s";
    final int RESULT_REQUEST_AT = 1;
    final String URI_COMMENT_DELETE = Global.HOST_API + "/tweet/%s/comment/%s";
    private final String TAG_LIKE_USERS = "TAG_LIKE_USERS";
    @Extra
    Maopao.MaopaoObject mMaopaoObject;
    Maopao.MaopaoObject mMaopaoObjectOld;

    @Extra
    ClickParam mClickParam;
    @ViewById
    ListView listView;
    @ViewById
    SwipeRefreshLayout swipeRefreshLayout;
    String maopaoUrl;
    String maopaoOwnerGlobal = "";
    String maopaoId = "";
    ArrayList<Maopao.Comment> mData = new ArrayList<>();
    MyImageGetter myImageGetter = new MyImageGetter(this);
    String URI_COMMENT = Global.HOST_API + "/tweet/%s/comments?pageSize=500";
    String ADD_COMMENT = Global.HOST_API + "/tweet/%s/comment";
    String TAG_DELETE_MAOPAO = "TAG_DELETE_MAOPAO";
    EnterEmojiLayout mEnterLayout;
    String bubble;
    View.OnClickListener onClickSend = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mMaopaoObject == null) {
                showButtomToast(R.string.maopao_load_fail_comment);
                return;
            }

            EditText content = mEnterLayout.content;
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
    View.OnClickListener onClickDeleteMaopao = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            action_delete_maopao();
        }
    };

    private void action_delete_maopao() {
        final int maopaoId = mMaopaoObject.id;
        showDialog("冒泡", "删除冒泡？", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String HOST_MAOPAO_DELETE = Global.HOST_API + "/tweet/%d";
                deleteNetwork(String.format(HOST_MAOPAO_DELETE, maopaoId), TAG_DELETE_MAOPAO);
            }
        });
    }

    boolean mModifyComment = false;
    View.OnClickListener onClickComment = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final Maopao.Comment comment = (Maopao.Comment) v.getTag();
            if (comment.isMy()) {
                showDialog("冒泡", "删除评论？", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String url = String.format(URI_COMMENT_DELETE, comment.tweet_id, comment.id);
                        deleteNetwork(url, URI_COMMENT_DELETE);
                    }
                });

            } else {
                prepareAddComment(comment, true);
            }
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
                holder = new HtmlCommentHolder(convertView, onClickComment, myImageGetter, getImageLoad(), mOnClickUser);
                convertView.setTag(R.id.layout, holder);

            } else {
                holder = (HtmlCommentHolder) convertView.getTag(R.id.layout);
            }

            Maopao.Comment data = mData.get(position);
            holder.setContent(data);

            return convertView;
        }
    };

    @AfterViews
    protected final void initMaopaoDetailActivity() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mEnterLayout = new EnterEmojiLayout(this, onClickSend, EnterLayout.Type.TextOnly, EnterEmojiLayout.EmojiType.SmallOnly);
        mEnterLayout.content.addTextChangedListener(new TextWatcherAt(this, this, RESULT_REQUEST_AT));

        try {
            bubble = readTextFile(getAssets().open("bubble"));
        } catch (Exception e) {
            Global.errorLog(e);
        }

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.green);
        loadData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mMaopaoObject != null) {
            int menuId = R.menu.activity_maopao_detail;
//            if (mMaopaoObject.owner.isMe()) {
//                menuId = R.menu.activity_maopao_detail_my;
//            }
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
                action_delete_maopao();
                return true;

            case R.id.action_inform:
                InformMaopaoActivity_.intent(this)
                        .maopaoId(mMaopaoObject.id)
                        .start();
                return true;
            default:
                return false;
        }
    }

    protected final void action_copy() {
        String link = getLink();
        if (link.isEmpty()) {
            showButtomToast("复制链接失败");
        } else {
            Global.copy(this, link);
            showButtomToast("已复制链接 " + link);
        }
    }

    @Override
    public void onRefresh() {
        if (mMaopaoObject != null) {
            mClickParam = new ClickParam(mMaopaoObject.owner.global_key, String.valueOf(mMaopaoObject.id));
            mMaopaoObjectOld = mMaopaoObject;
            mMaopaoObject = null;
        }

        loadData();
    }

    private void loadData() {
        if (mMaopaoObject == null) {
            maopaoOwnerGlobal = mClickParam.name;
            maopaoId = mClickParam.maopaoId;

            final String url = Global.HOST_API + "/tweet/%s/%s";
            maopaoUrl = String.format(url, maopaoOwnerGlobal, maopaoId);

            getNetwork(maopaoUrl, maopaoUrl);

        } else {
            maopaoOwnerGlobal = mMaopaoObject.owner.global_key;
            initData();
        }
    }

    private void initData() {
        URI_COMMENT = String.format(URI_COMMENT, mMaopaoObject.id);

        initHead();
        listView.setAdapter(adapter);


        getNetwork(URI_COMMENT, URI_COMMENT);

        prepareAddComment(mMaopaoObject, false);
    }

    private String readTextFile(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();

        } catch (IOException e) {
            Global.errorLog(e);
        }
        return outputStream.toString();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//            if (maopaoOwnerGlobal.equals(MyApp.sUserObject.global_key)) {
//                getMenuInflater().inflate(R.menu.maopao_detail, menu);
//            }
//
//        return super.onCreateOptionsMenu(menu);
//    }

    void initHead() {
        if (mListHead == null) {
            mListHead = mInflater.inflate(R.layout.activity_maopao_detail_head, null, false);
            listView.addHeaderView(mListHead, null, false);
        }

        ImageView icon = (ImageView) mListHead.findViewById(R.id.icon);
        icon.setOnClickListener(mOnClickUser);

        TextView name = (TextView) mListHead.findViewById(R.id.name);
        name.setOnClickListener(mOnClickUser);

        TextView time = (TextView) mListHead.findViewById(R.id.time);
        time.setText(Global.dayToNow(mMaopaoObject.created_at));

        iconfromNetwork(icon, mMaopaoObject.owner.avatar);
        icon.setTag(mMaopaoObject.owner.global_key);

        name.setText(mMaopaoObject.owner.name);
        name.setTag(mMaopaoObject.owner.global_key);

        WebView webView = (WebView) mListHead.findViewById(R.id.comment);
        Global.initWebView(webView);
        String replaceContent = bubble.replace("${webview_content}", mMaopaoObject.content);
        webView.loadDataWithBaseURL(null, replaceContent, "text/html", "UTF-8", null);
        webView.setWebViewClient(new CustomWebViewClient(this, mMaopaoObject.content));

        mListHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareAddComment(mMaopaoObject, true);
            }
        });

        mListHead.findViewById(R.id.shareBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action_share_third();
            }
        });

        likeBtn = (CheckBox) mListHead.findViewById(R.id.likeBtn);
        mListHead.findViewById(R.id.commentBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareAddComment(mMaopaoObject, true);
            }
        });

        likeBtn.setChecked(mMaopaoObject.liked);
        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });


        likeUsersArea = new LikeUsersArea(mListHead, this, getImageLoad(), mOnClickUser);

        likeUsersArea.likeUsersLayout.setTag(MaopaoListBaseFragment.TAG_MAOPAO, mMaopaoObject);
        if (mMaopaoObject.like_users.isEmpty() && mMaopaoObject.likes > 0) {
            String hostLikes = String.format(LikeUsersListActivity.HOST_LIKES_USER, mMaopaoObject.id);
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
    }

    @OnActivityResult(RESULT_REQUEST_AT)
    void onResultAt(int requestCode, Intent data) {
        if (requestCode == Activity.RESULT_OK) {
            String name = data.getStringExtra("name");
            mEnterLayout.insertText(name);
        }
    }

    @OptionsItem(android.R.id.home)
    void close() {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (mEnterLayout.isEnterPanelShowing()) {
            mEnterLayout.closeEnterPanel();
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

                JSONArray jsonArray = respanse.getJSONObject("data").getJSONArray("list");
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


            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(HOST_GOOD)) {
            if (code == 0) {
                Maopao.MaopaoObject maopao = mMaopaoObject;
                maopao.liked = !maopao.liked;
                if (maopao.liked) {
                    umengEvent(UmengEvent.MAOPAO, "冒泡点赞");
                    Maopao.Like_user like_user = new Maopao.Like_user(MyApp.sUserObject);
                    maopao.like_users.add(0, like_user);
                    ++maopao.likes;
                } else {
                    umengEvent(UmengEvent.MAOPAO, "冒泡取消点赞");
                    for (int j = 0; j < maopao.like_users.size(); ++j) {
                        if (maopao.like_users.get(j).global_key.equals(MyApp.sUserObject.global_key)) {
                            maopao.like_users.remove(j);
                            --maopao.likes;
                            break;
                        }
                    }
                }

                likeUsersArea.displayLikeUser();

                Intent intent = new Intent();
                intent.putExtra(ListModify.DATA, mMaopaoObject);
                intent.putExtra(ListModify.TYPE, ListModify.Edit);
                setResult(Activity.RESULT_OK, intent);

            } else {
                showErrorMsg(code, respanse);
            }
            likeBtn.setChecked(mMaopaoObject.liked);
        } else if (tag.equals(maopaoUrl)) {
            if (code == 0) {
                mMaopaoObject = new Maopao.MaopaoObject(respanse.getJSONObject("data"));
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
        EditText content = mEnterLayout.content;
        if (data instanceof Maopao.Comment) {
            comment = (Maopao.Comment) data;
            content.setHint("回复 " + comment.owner.name);
            content.setTag(comment);
        } else if (data instanceof Maopao.MaopaoObject) {
            comment = new Maopao.Comment((Maopao.MaopaoObject) data);
            content.setHint("评论冒泡");
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
        shareBoard.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
    }

    public static class ClickParam implements Serializable {
        String name;
        String maopaoId;

        public ClickParam(String name, String maopaoId) {
            this.name = name;
            this.maopaoId = maopaoId;
        }
    }

    public static class CustomWebViewClient extends WebViewClient {

        private final Context mContext;
        private final ArrayList<String> mUris;

        public CustomWebViewClient(Context context, String content) {
            mContext = context;
            mUris = HtmlContent.parseMaopao(content).uris;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            for (int i = 0; i < mUris.size(); ++i) {
                if (mUris.get(i).equals(url)) {
                    ImagePagerActivity_.intent(mContext)
                            .mArrayUri(mUris)
                            .mPagerPosition(i)
                            .start();
                    return true;
                }
            }

            URLSpanNoUnderline.openActivityByUri(mContext, url, false, true);
            return true;
        }
    }

//    private UMSocialService mController = UMServiceFactory.getUMSocialService("net.coding.program");

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMSsoHandler ssoHandler = CustomShareBoard.getShareController().getConfig().getSsoHandler(
                requestCode);
        if (ssoHandler != null) {
            ssoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }
}

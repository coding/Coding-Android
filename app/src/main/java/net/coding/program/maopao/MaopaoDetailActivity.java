package net.coding.program.maopao;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
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

import net.coding.program.BaseFragmentActivity;
import net.coding.program.Global;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.ListModify;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.StartActivity;
import net.coding.program.common.TextWatcherAt;
import net.coding.program.common.comment.HtmlCommentHolder;
import net.coding.program.common.enter.EnterEmojiLayout;
import net.coding.program.common.enter.EnterLayout;
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
public class MaopaoDetailActivity extends BaseFragmentActivity implements StartActivity {

    @Extra
    Maopao.MaopaoObject mMaopaoObject;

    @Extra
    ClickParam mClickParam;

    public static class ClickParam implements Serializable {
        String name;
        String maopaoId;

        public ClickParam(String name, String maopaoId) {
            this.name = name;
            this.maopaoId = maopaoId;
        }
    }

    @ViewById
    ListView listView;

    String maopaoUrl;

    String maopaoOwnerGlobal = "";
    String maopaoId = "";

    ArrayList<Maopao.Comment> mData = new ArrayList<Maopao.Comment>();

    MyImageGetter myImageGetter = new MyImageGetter(this);

    String UrlComment = Global.HOST + "/api/tweet/%s/comments?pageSize=500";

    final String URI_COMMENT = Global.HOST + "/api/tweet/%s/comment";

    final String TAG_DELETE_MAOPAO = "TAG_DELETE_MAOPAO";

    EnterEmojiLayout mEnterLayout;

    String bubble;

    @AfterViews
    void init() {
        getActionBar().setDisplayHomeAsUpEnabled(true);

        mEnterLayout = new EnterEmojiLayout(this, onClickSend, EnterLayout.Type.TextOnly, EnterEmojiLayout.EmojiType.SmallOnly);
        mEnterLayout.content.addTextChangedListener(new TextWatcherAt(this, this, RESULT_REQUEST_AT));

        try {
            bubble = readTextFile(getAssets().open("bubble"));
        } catch (Exception e) {
            Global.errorLog(e);
        }

        if (mMaopaoObject == null) {
            maopaoOwnerGlobal = mClickParam.name;
            maopaoId = mClickParam.maopaoId;

            final String url = Global.HOST + "/api/tweet/%s/%s";
            maopaoUrl = String.format(url, maopaoOwnerGlobal, maopaoId);

            getNetwork(maopaoUrl, maopaoUrl);

        } else {
            maopaoOwnerGlobal = mMaopaoObject.owner.global_key;
            initData();
        }
    }

    private void initData() {
        UrlComment = String.format(UrlComment, mMaopaoObject.id);

        initHead();
        listView.setAdapter(adapter);

        getNetwork(UrlComment, UrlComment);

        prepareAddComment(mMaopaoObject, false);
    }

    View.OnClickListener onClickSend = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            EditText content = mEnterLayout.content;
            String input = content.getText().toString();
            if (input.isEmpty()) {
                return;
            }

            if (EmojiFilter.containsEmoji(input)) {
                showMiddleToast("暂不支持发表情");
                return;
            }

            Maopao.Comment comment = (Maopao.Comment) content.getTag();
            String uri = String.format(URI_COMMENT, comment.tweet_id);

            RequestParams params = new RequestParams();

            String contentString;
            if (comment.id.isEmpty()) {
                contentString = input;
            } else {
                contentString = String.format("@%s : %s", comment.owner.name, input);
            }
            params.put("content", contentString);
            postNetwork(uri, params, URI_COMMENT, 0, comment);
        }
    };

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
        }
        return outputStream.toString();
    }

    CheckBox likeBtn;

    LikeUsersArea likeUsersArea;

    final String HOST_GOOD = Global.HOST + "/api/tweet/%s/%s";

    void initHead() {
        View head = mInflater.inflate(R.layout.activity_maopao_detail_head, null, false);

        ImageView icon = (ImageView) head.findViewById(R.id.icon);
        icon.setOnClickListener(mOnClickUser);

        TextView name = (TextView) head.findViewById(R.id.name);

        TextView time = (TextView) head.findViewById(R.id.time);
        time.setText(Global.dayToNow(mMaopaoObject.created_at));

        iconfromNetwork(icon, mMaopaoObject.owner.avatar);
        icon.setTag(mMaopaoObject.owner.global_key);

        name.setText(mMaopaoObject.owner.name);
        name.setTag(mMaopaoObject.owner.global_key);

        WebView webView = (WebView) head.findViewById(R.id.comment);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setBackgroundColor(0);
        webView.getBackground().setAlpha(0);

        webView.getSettings().setDefaultTextEncodingName("UTF-8");
        webView.loadDataWithBaseURL(null, bubble.replace("${webview_content}", mMaopaoObject.content), "text/html", "UTF-8", null);

        head.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareAddComment(mMaopaoObject, true);
            }
        });

        likeBtn = (CheckBox) head.findViewById(R.id.likeBtn);
        CheckBox commentBtn = (CheckBox) head.findViewById(R.id.commentBtn);
        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareAddComment(mMaopaoObject, true);
            }
        });


        likeBtn.setChecked(mMaopaoObject.liked);
        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String type = ((CheckBox) v).isChecked() ? "like" : "unlike";
                String uri = String.format(HOST_GOOD, mMaopaoObject.id, type);

                postNetwork(uri, new RequestParams(), HOST_GOOD, 0, mMaopaoObject);
            }
        });


        likeUsersArea = new LikeUsersArea(head, this, getImageLoad(), mOnClickUser);

        likeUsersArea.likeUsersLayout.setTag(MaopaoListFragment.TAG_MAOPAO, mMaopaoObject);
        likeUsersArea.displayLikeUser();


        TextView photoType = (TextView) head.findViewById(R.id.photoType);
        String device = mMaopaoObject.device;
        if (!device.isEmpty()) {
            final String format = "来自 %s";
            photoType.setText(String.format(format, device));
        } else {
            photoType.setText("");
        }

        View maopaoDelete = head.findViewById(R.id.maopaoDelete);
        if (mMaopaoObject.owner.global_key.equals(MyApp.sUserObject.global_key)) {
            maopaoDelete.setVisibility(View.VISIBLE);
            maopaoDelete.setOnClickListener(onClickDeleteMaopao);
        } else {
            maopaoDelete.setVisibility(View.INVISIBLE);
        }

        listView.addHeaderView(head);
    }

    View.OnClickListener onClickDeleteMaopao = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String maopaoId = mMaopaoObject.id;
            showDialog("删除冒泡", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String HOST_MAOPAO_DELETE = Global.HOST + "/api/tweet/%s";
                    deleteNetwork(String.format(HOST_MAOPAO_DELETE, maopaoId), TAG_DELETE_MAOPAO);

                }
            });
        }
    };


    final int RESULT_REQUEST_AT = 1;

    @OnActivityResult(RESULT_REQUEST_AT)
    void onResultAt(int requestCode, Intent data) {
        if (requestCode == Activity.RESULT_OK) {
            String name = data.getStringExtra("name");
            mEnterLayout.insertText(name);
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//            if (maopaoOwnerGlobal.equals(MyApp.sUserObject.global_key)) {
//                getMenuInflater().inflate(R.menu.maopao_detail, menu);
//            }
//
//        return super.onCreateOptionsMenu(menu);
//    }

    @OptionsItem(android.R.id.home)
    void close() {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (mEnterLayout.isEmojiKeyboardShowing()) {
            mEnterLayout.closeEmojiKeyboard();
            return;
        }

        super.onBackPressed();
    }

    boolean mModifyComment = false;

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(UrlComment)) {
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
                mEnterLayout.clearContent();
                mEnterLayout.hideKeyboard();

            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(URI_COMMENT)) {
            if (code == 0) {
                getNetwork(UrlComment, UrlComment);
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
                    Maopao.Like_user like_user = new Maopao.Like_user(MyApp.sUserObject);
                    maopao.like_users.add(0, like_user);
                    ++maopao.likes;
                } else {
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
            }
        } else if (tag.equals(URI_COMMENT_DELETE)) {
            if (code == 0) {
                mModifyComment = true;
                getNetwork(UrlComment, UrlComment);
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
        }
    }

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

    final String URI_COMMENT_DELETE = Global.HOST + "/api/tweet/%s/comment/%s";

    View.OnClickListener onClickComment = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final Maopao.Comment comment = (Maopao.Comment) v.getTag();
            if (comment.isMy()) {
                showDialog("删除评论", new DialogInterface.OnClickListener() {
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

    void prepareAddComment(Object data, boolean popKeyboard) {
        Maopao.Comment comment;
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

        if (popKeyboard) {
            content.requestFocus();
            Global.popSoftkeyboard(MaopaoDetailActivity.this, content, true);
        }
    }

    View.OnClickListener onClickLikeUsrs = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MaopaoDetailActivity.this, LikeUsersListActivity_.class);
            intent.putExtra("id", (String) v.getTag());
            startActivity(intent);
        }
    };
}

package net.coding.program.project.detail.merge;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.loopj.android.http.RequestParams;

import net.coding.program.common.ui.BackActivity;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.model.DiffFile;
import net.coding.program.model.Merge;
import net.coding.program.model.PostRequest;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_merge_file_detail)
@OptionsMenu(R.menu.menu_merge_file_detail)
public class MergeFileDetailActivity extends BackActivity {

    public static final String HOST_COMMIT_FILE_DETAIL = "HOST_COMMIT_FILE_DETAIL";
    private static final String TAG_HTTP_ALL_COMMENTS = "TAG_HTTP_ALL_COMMENTS";
    private static final String TAG_HTTP_DELETE_COMMENT = "TAG_HTTP_DELETE_COMMENT";
    private static final String TAG_LINE_NOTE_CREATE = "TAG_LINE_NOTE_CREATE";
    private final int RESULT_COMMENT = 1;
    @Extra
    String mProjectPath;
    @Extra
    Merge mMerge;
    @Extra
    DiffFile.DiffSingleFile mSingleFile;
    @Extra
    int mergeIid = 0; // 如果没有传，则是 commit 那边的
    @ViewById
    WebView webView;
    String mRef = "";
    WebViewClient webViewClient = new WebViewClient() {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            int index = url.indexOf('?');
            if (index == -1) {
                url = url.replaceFirst("coding://", "coding://note?");
            }
            Uri uri = Uri.parse(url);
            String host = uri.getHost();
            switch (host) {
                case "line_note": {
                    final LineNoteBase lineNote = new LineNoteBase(uri, mMerge);
                    showDialog("line " + lineNote.line, "添加评论", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            LineNoteParam param = createParam(lineNote, "");
                            CommentActivity_.intent(MergeFileDetailActivity.this).mParam(param).startForResult(RESULT_COMMENT);
                        }
                    });
                    break;
                }

                case "note": {
                    final LineNote lineNote = new LineNote(uri, mMerge);
                    if (lineNote.isMe()) {
                        showDialog("line note", "删除评论", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String deleteComment = lineNote.getHttpDeleteComment(mProjectPath);
                                deleteNetwork(deleteComment, TAG_HTTP_DELETE_COMMENT, lineNote.getId());
                                showProgressBar(true, "正在删除...");
                            }
                        });
                    } else {
                        LineNoteParam param = createParam(lineNote, lineNote.getGlobalKey());
                        CommentActivity_.intent(MergeFileDetailActivity.this).mParam(param).startForResult(RESULT_COMMENT);
                    }

                    break;
                }
            }
            Log.d("", url);

            return true;
        }

    };
    private String mContent = "";
    private JSONObject mCommentsData;

    // JsonArray 要 api19 才支持删除
    public static JSONArray remove(final int idx, final JSONArray from) {
        final List<JSONObject> objs = asList(from);
        objs.remove(idx);

        final JSONArray ja = new JSONArray();
        for (final JSONObject obj : objs) {
            ja.put(obj);
        }

        return ja;
    }

    public static List<JSONObject> asList(final JSONArray ja) {
        final int len = ja.length();
        final ArrayList<JSONObject> result = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            final JSONObject obj = ja.optJSONObject(i);
            if (obj != null) {
                result.add(obj);
            }
        }
        return result;
    }

    @AfterViews
    protected final void initMergeFileDetailActivity() {
        showProgressBar(true, "正在载入");

        getSupportActionBar().setTitle(mSingleFile.getName());

        String url;
        if (mergeIid != 0) {
            url = mSingleFile.getHttpFileDiffComment(mProjectPath, mergeIid, mMerge);
        } else {
            url = mSingleFile.getHttpFileDiffComment(mProjectPath);
        }
        getNetwork(url, TAG_HTTP_ALL_COMMENTS);
    }

    @OptionsItem
    protected final void action_source() {
        if (mRef.isEmpty()) {
            showButtomToast("稍等，正在载入数据");
        } else {
            String url = mSingleFile.getHttpSourceFile(mProjectPath, mRef);
            Log.d("", url);
            SourceActivity_.intent(this).url(url).start();
        }
    }

    @OnActivityResult(RESULT_COMMENT)
    void onResultCreateComment(int result, Intent data) {
        if (result == RESULT_OK) {
            String s = data.getStringExtra("data");
            try {
                JSONObject commentItem = new JSONObject(s);
                mCommentsData.optJSONArray("data").put(commentItem);
                updateWebViewDisplay();
                showButtomToast("添加评论成功");

            } catch (Exception e) {
                Global.errorLog(e);
            }
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_COMMIT_FILE_DETAIL)) {
            showProgressBar(false);
            if (code == 0) {
                try {
                    respanse.remove("code");
                    mRef = respanse.optJSONObject("data").optString("linkRef", "");
                    Global.initWebView(webView);
                    mContent = respanse.toString();

                    updateWebViewDisplay();
                } catch (Exception e) {
                    Global.errorLog(e);
                }

            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(TAG_HTTP_ALL_COMMENTS)) {
            if (code == 0) {
                String url;
                if (mergeIid != 0) {
                    url = mSingleFile.getHttpFileDiffDetail(mProjectPath, mergeIid, mMerge);
                } else {
                    url = mSingleFile.getHttpFileDiffDetail(mProjectPath);
                }
                getNetwork(url, HOST_COMMIT_FILE_DETAIL);
                mCommentsData = respanse;
            } else {
                showProgressBar(false);
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(TAG_LINE_NOTE_CREATE)) {
            showProgressBar(false);
            if (code == 0) {
                umengEvent(UmengEvent.CODE, "linenote添加评论");
                mCommentsData.optJSONArray("data").put(respanse.optJSONObject("data"));
                updateWebViewDisplay();
                showButtomToast("添加评论成功");
            } else {
                showErrorMsg(code, respanse);
            }

        } else if (tag.equals(TAG_HTTP_DELETE_COMMENT)) {
            showProgressBar(false);
            if (code == 0) {
                umengEvent(UmengEvent.CODE, "linenote删除评论");
                int deleteItemId = (int) data;
                JSONArray comments = mCommentsData.optJSONArray("data");
                for (int i = 0; i < comments.length(); ++i) {
                    if (comments.optJSONObject(i).optInt("id") == deleteItemId) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            comments.remove(i);
                        } else {
                            JSONObject json = new JSONObject();
                            json.put("code", 0);
                            json.put("data", remove(i, comments));
                            mCommentsData = json;
                        }
                        showButtomToast("删除评论成功");
                        updateWebViewDisplay();
                        break;
                    }
                }

            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    private void updateWebViewDisplay() {
        Global.setWebViewContent(webView, "diff", "${diff-content}",
                mContent, "${comments}", mCommentsData.toString());
        webView.setWebViewClient(webViewClient);
    }

    public LineNoteParam createParam(LineNoteBase lineNote, final String atSomeOne) {
        return new LineNoteParam(mProjectPath, lineNote, atSomeOne);
    }

    public static class LineNoteBase implements Serializable {
        public String commitId;
        public String noteable_type;
        public String path;
        public String position;
        public String line;
        public String anchor;
        public String noteable_id;// = "24212";

        public LineNoteBase(Uri uri, Merge merge) {
            commitId = uri.getQueryParameter("commitId");
            noteable_type = uri.getQueryParameter("noteable_type");
            path = uri.getQueryParameter("path");
            position = uri.getQueryParameter("position");
            line = uri.getQueryParameter("line");
            anchor = uri.getQueryParameter("anchor");

            if (merge != null) {
                int noteableId = merge.getId();
                noteable_id = String.valueOf(noteableId);
            }
        }

        public RequestParams getPostParam(String content) {
            RequestParams params = new RequestParams();
            params.put("commitId", commitId);
            params.put("noteable_type", noteable_type);
            params.put("path", path);
            params.put("position", position);
            params.put("line", line);
            params.put("anchor", anchor);
            params.put("noteable_id", noteable_id);
            params.put("content", content);

            return params;
        }
    }

    public static class LineNote extends LineNoteBase implements Serializable {
        public String line_note_commentclicked_line_note_id;
        public String clicked_user_name; // 其实是 global key

        public LineNote(Uri uri, Merge merge) {
            super(uri, merge);
            line_note_commentclicked_line_note_id = uri.getQueryParameter("line_note_commentclicked_line_note_id");
            clicked_user_name = uri.getQueryParameter("clicked_user_name");
        }

        public boolean isMe() {
            return MyApp.sUserObject.global_key.equals(clicked_user_name);
        }

        public String getGlobalKey() {
            return clicked_user_name;
        }

        public int getId() {
            return Integer.valueOf(line_note_commentclicked_line_note_id);
        }

        public String getHttpDeleteComment(String projectPath) {
            return Global.HOST_API +
                    projectPath +
                    "/git/line_notes/" +
                    line_note_commentclicked_line_note_id;
        }

    }

    static class LineNoteParam implements CommentActivity.CommentParam, Serializable {

        String mProjectPath;
        LineNoteBase mLineNote;
        String mAtGlobalKey;

        public LineNoteParam(String mProjectPath, LineNoteBase mLineNote, String mAtGlobalKey) {
            this.mProjectPath = mProjectPath;
            this.mLineNote = mLineNote;
            this.mAtGlobalKey = mAtGlobalKey;
        }

        @Override
        public PostRequest getSendCommentParam(String input) {
            String url = Global.HOST_API + mProjectPath + "/git/line_notes";
            RequestParams params = mLineNote.getPostParam(input);
            return new PostRequest(url, params);
        }

        @Override
        public String getAtSome() {
            return mAtGlobalKey;
        }

        @Override
        public String getAtSomeUrl() {
            return "";
        }

        @Override
        public String getProjectPath() {
            return mProjectPath;
        }

        @Override
        public boolean isPublicProject() {
            return false;
        }
    }


}

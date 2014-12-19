package net.coding.program.maopao;


import android.app.ActionBar;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.FootUpdate;
import net.coding.program.Global;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.BlankViewDisplay;
import net.coding.program.common.ClickSmallImage;
import net.coding.program.common.ListModify;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.StartActivity;
import net.coding.program.common.TextWatcherAt;
import net.coding.program.common.enter.EnterEmojiLayout;
import net.coding.program.common.enter.EnterLayout;
import net.coding.program.common.network.RefreshBaseFragment;
import net.coding.program.maopao.item.CommentArea;
import net.coding.program.maopao.item.ContentArea;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.DynamicObject;
import net.coding.program.model.Maopao;
import net.coding.program.model.UserObject;
import net.coding.program.third.EmojiFilter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

@EFragment(R.layout.fragment_maopao_list)
public class MaopaoListFragment extends RefreshBaseFragment implements FootUpdate.LoadMore, StartActivity {

    ArrayList<Maopao.MaopaoObject> mData = new ArrayList<Maopao.MaopaoObject>();

    final String maopaoUrlFormat = Global.HOST + "/api/tweet/public_tweets?last_id=%s&sort=%s";
    final String friendUrl = Global.HOST + "/api/activities/user_tweet?last_id=%s";
    final String myUrl = Global.HOST + "/api/tweet/user_public?user_id=%s&last_id=%s";

    final String URI_COMMENT = Global.HOST + "/api/tweet/%s/comment";

    String id = UPDATE_ALL;

    boolean mNoMore = false;

    @FragmentArg
    String mType;

    @FragmentArg
    String userId;

    @ViewById
    ListView listView;

    @ViewById
    View blankLayout;

    @ViewById
    View commonEnterRoot;

    EnterEmojiLayout mEnterLayout;

    int needScrollY = 0;
    int oldListHigh = 0;
    int cal1 = 0;

    private MyImageGetter myImageGetter;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @AfterViews
    protected void init() {
        super.init();

        mData = AccountInfo.loadMaopao(getActivity(), mType ,userId);
        if (mData.isEmpty()) {
            showDialogLoading();
        } else {
            setRefreshing(true);
        }

        myImageGetter = new MyImageGetter(getActivity());

        if (mType.equals("friends")) {
            id = "";
        }

        if (mType.equals("hot")) {
            mNoMore = true;
        }

        addDoubleClickActionbar();

        mFootUpdate.init(listView, mInflater, this);
        listView.setAdapter(mAdapter);
        getNetwork(createUrl(), maopaoUrlFormat);

        ViewTreeObserver vto = listView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int listHeight = listView.getHeight();

                if (oldListHigh > listHeight) {
                    if (cal1 == 0) {
                        cal1 = 1;
                        needScrollY = needScrollY + oldListHigh - listHeight;

                    } else if (cal1 == 1) {
                        int scrollResult = needScrollY + oldListHigh - listHeight;
                        listView.smoothScrollBy(scrollResult, 1);
                        needScrollY = 0;
                    }

                    oldListHigh = listHeight;
                }
            }
        });

        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                    hideSoftkeyboard();
                }

                return false;
            }
        });

        mEnterLayout = new EnterEmojiLayout(getActivity(), onClickSendText, EnterLayout.Type.TextOnly, EnterEmojiLayout.EmojiType.SmallOnly);
        mEnterLayout.content.addTextChangedListener(new TextWatcherAt(getActivity(), this, RESULT_AT));
        mEnterLayout.hide();
    }

    private void addDoubleClickActionbar() {
        ActionBar actionBar = getActivity().getActionBar();
        View v = mInflater.inflate(R.layout.action_bar_custom, null);
        v.setOnClickListener(new View.OnClickListener() {

            long mLastTime = 0;
            final long DOUBLE_CLICK_TIME = 300;

            @Override
            public void onClick(View v) {
                long lastTime = mLastTime;
                long nowTime = Calendar.getInstance().getTimeInMillis();
                mLastTime = nowTime;

                if (nowTime - lastTime < DOUBLE_CLICK_TIME) {
                    if (!isRefreshing()) {
                        listView.smoothScrollToPosition(0);
                        setRefreshing(true);
                        onRefresh();
                    }
                }
            }
        });
        actionBar.setCustomView(v);
    }

    @Override
    public void loadMore() {
        getNetwork(createUrl(), maopaoUrlFormat);
    }

    @Override
    public void onRefresh() {
        initSetting();
        getNetwork(createUrl(), maopaoUrlFormat);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!mType.equals("user")) {
            inflater.inflate(R.menu.maopao_add, menu);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @OptionsItem
    void action_add_maopao() {
        Intent intent = new Intent(getActivity(), AddMaopaoActivity_.class);
        startActivityForResult(intent, RESULT_EDIT_MAOPAO);
    }

    @Override
    protected void initSetting() {
        super.initSetting();
        id = UPDATE_ALL;
    }

    View.OnClickListener onClickSendText = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String input = mEnterLayout.getContent();
            if (input.isEmpty()) {
                return;
            }

            if (EmojiFilter.containsEmoji(input)) {
                showMiddleToast("暂不支持发表情");
                return;
            }

            Maopao.Comment commentObject = (Maopao.Comment) mEnterLayout.content.getTag();
            String uri = String.format(URI_COMMENT, commentObject.tweet_id);

            RequestParams params = new RequestParams();

            String commentString;
            if (commentObject.id.isEmpty()) {
                commentString = input;
            } else {
                commentString = String.format("@%s : %s", commentObject.owner.name, input);
            }
            params.put("content", commentString);
            postNetwork(uri, params, URI_COMMENT, 0, commentObject);
        }
    };

    static final int RESULT_EDIT_MAOPAO = 100;
    static final int RESULT_AT = 101;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_EDIT_MAOPAO) {
            if (resultCode == Activity.RESULT_OK || data != null) {
                int type = data.getIntExtra(ListModify.TYPE, 0);
                if (type == ListModify.Edit) {
                    Maopao.MaopaoObject maopao = (Maopao.MaopaoObject) data.getSerializableExtra(ListModify.DATA);
                    for (int i = 0; i < mData.size(); ++i) {
                        Maopao.MaopaoObject item = mData.get(i);
                        if (item.id.equals(maopao.id)) {
                            mData.remove(i);
                            mData.add(i, maopao);
                            mAdapter.notifyDataSetChanged();

                            break;
                        }
                    }


                } else if (type == ListModify.Delete) {
                    String maopaoId = data.getStringExtra(ListModify.ID);
                    for (int i = 0; i < mData.size(); ++i) {
                        Maopao.MaopaoObject item = mData.get(i);
                        if (item.id.equals(maopaoId)) {
                            mData.remove(i);
                            mAdapter.notifyDataSetChanged();
                            break;
                        }
                    }

                } else if (type == ListModify.Add) {
                    Maopao.MaopaoObject addItem = (Maopao.MaopaoObject) data.getSerializableExtra(ListModify.DATA);
                    mData.add(0, addItem);
                    mAdapter.notifyDataSetChanged();
                }
            }
        } else if (requestCode == RESULT_AT) {
            if (resultCode == Activity.RESULT_OK) {
                String name = data.getStringExtra("name");
                mEnterLayout.insertText(name);
            }
        }
    }

    String createUrl() {
        if (mType.equals("friends")) {
            return String.format(friendUrl, id);
        } else if (mType.equals("my")) {
            UserObject my = AccountInfo.loadAccount(getActivity());
            return String.format(myUrl, my.id, id);
        } else if (mType.equals("user")) {
            return String.format(myUrl, userId, id);
        } else {
            return String.format(maopaoUrlFormat, id, mType);
        }
    }

    final String HOST_GOOD = Global.HOST + "/api/tweet/%s/%s";

    private void hideSoftkeyboard() {
        mEnterLayout.clearContent();
        mEnterLayout.hideKeyboard();
        mEnterLayout.hide();
    }

    View.OnClickListener onClickRetry = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onRefresh();
        }
    };

    final String TAG_DELETE_MAOPAO = "TAG_DELETE_MAOPAO";
    final String TAG_DELETE_MAOPAO_COMMENT = "TAG_DELETE_MAOPAO_COMMENT";

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(maopaoUrlFormat)) {
            hideProgressDialog();
            setRefreshing(false);
            if (code == 0) {
                if (id.equals(UPDATE_ALL)) {
                    mData.clear();
                }

                JSONArray jsonArray = respanse.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    Maopao.MaopaoObject item = new Maopao.MaopaoObject(jsonArray.getJSONObject(i));
                    mData.add(item);
                }

                AccountInfo.saveMaopao(getActivity(), mData, mType, userId);

                if (jsonArray.length() == 0) {
                    mNoMore = true;
                } else {
                    id = mData.get(mData.size() - 1).id;
                    mAdapter.notifyDataSetChanged();
                }

                if (mNoMore) {
                    mFootUpdate.dismiss();
                } else {
                    mFootUpdate.showLoading();
                }

                BlankViewDisplay.setBlank(mData.size(), this, true, blankLayout, onClickRetry);

            } else {
                if (mData.size() > 0) {
                    mFootUpdate.showFail();
                } else {
                    mFootUpdate.dismiss();
                }

                showErrorMsg(code, respanse);
                BlankViewDisplay.setBlank(mData.size(), this, false, blankLayout, onClickRetry);
            }
        } else if (tag.equals(URI_COMMENT)) {
            if (code == 0) {
                mEnterLayout.clearContent();
                Maopao.Comment myComment = new Maopao.Comment(respanse.getJSONObject("data"));
                myComment.owner = new DynamicObject.Owner(MyApp.sUserObject);
                Maopao.Comment otherComment = (Maopao.Comment) data;

                for (int i = 0; i < mData.size(); ++i) {
                    Maopao.MaopaoObject item = mData.get(i);
                    if (otherComment.tweet_id.equals(item.id)) {
                        item.comment_list.add(0, myComment);
                        ++item.comments;

                        mAdapter.notifyDataSetChanged();
                        hideSoftkeyboard();

                        return;
                    }
                }
            } else {
                showErrorMsg(code, respanse);
            }

        } else if (tag.equals(HOST_GOOD)) {
            if (code == 0) {
                Maopao.MaopaoObject item = (Maopao.MaopaoObject) data;
                for (int i = 0; i < mData.size(); ++i) {
                    Maopao.MaopaoObject maopao = mData.get(i);
                    if (maopao.id.equals(((Maopao.MaopaoObject) data).id)) {
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

                        break;
                    }
                }
            } else {
                showErrorMsg(code, respanse);
            }

            mAdapter.notifyDataSetChanged();

        } else if (tag.equals(TAG_DELETE_MAOPAO)) {
            String maopaoId = (String) data;
            if (code == 0) {
                for (int i = 0; i < mData.size(); ++i) {
                    Maopao.MaopaoObject item = mData.get(i);
                    if (item.id.equals(maopaoId)) {
                        mData.remove(i);
                        mAdapter.notifyDataSetChanged();
                    }
                }

            } else {
                showButtomToast("删除失败");
            }
        } else if (tag.equals(TAG_DELETE_MAOPAO_COMMENT)) {
            Maopao.Comment comment = (Maopao.Comment) data;
            if (code == 0) {
                for (int i = 0; i < mData.size(); ++i) {
                    Maopao.MaopaoObject item = mData.get(i);
                    if (item.id.equals(comment.tweet_id)) {
                        for (int j = 0; j < item.comment_list.size(); ++j) {
                            if (item.comment_list.get(j).id.equals(comment.id)) {
                                item.comment_list.remove(j);
                                --item.comments;
                                mAdapter.notifyDataSetChanged();
                                return;
                            }
                        }
                    }
                }

            } else {
                showButtomToast("删除失败");
            }
        }
    }

    BaseAdapter mAdapter = new BaseAdapter() {
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
            final ViewHolder holder;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.fragment_maopao_list_item, parent, false);

                holder.maopaoItem = convertView.findViewById(R.id.MaopaoItem);
                holder.maopaoItem.setOnClickListener(mOnClickMaopaoItem);

                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.icon.setOnClickListener(mOnClickUser);

                holder.name = (TextView) convertView.findViewById(R.id.name);

                holder.time = (TextView) convertView.findViewById(R.id.time);

                holder.contentArea = new ContentArea(convertView, mOnClickMaopaoItem, onClickImage, myImageGetter, getImageLoad());

                holder.commentLikeArea = convertView.findViewById(R.id.commentLikeArea);
                holder.likeUsersArea = new LikeUsersArea(convertView, MaopaoListFragment.this, getImageLoad(), mOnClickUser);

                holder.photoType = (TextView) convertView.findViewById(R.id.photoType);
                holder.likeBtn = (CheckBox) convertView.findViewById(R.id.likeBtn);
                holder.commentBtn = (CheckBox) convertView.findViewById(R.id.commentBtn);
                holder.likeBtn.setTag(R.id.likeBtn, holder);
                holder.commentBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popComment(v);
                    }
                });

                holder.maopaoDelete = convertView.findViewById(R.id.maopaoDelete);
                holder.maopaoDelete.setOnClickListener(onClickDeleteMaopao);

                holder.commentArea = new CommentArea(convertView, onClickComment, myImageGetter);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Maopao.MaopaoObject data = (Maopao.MaopaoObject) getItem(position);

            holder.likeUsersArea.likeUsersLayout.setTag(TAG_MAOPAO, data);
            holder.likeUsersArea.displayLikeUser();

            if (data.likes > 0 || data.comments > 0) {
                holder.commentLikeArea.setVisibility(View.VISIBLE);
            } else {
                holder.commentLikeArea.setVisibility(View.GONE);
            }

            String device = data.device;
            if (!device.isEmpty()) {
                final String format = "来自 %s";
                device = String.format(format, device);
            }
            holder.photoType.setText(device);

            iconfromNetwork(holder.icon, data.owner.avatar);
            holder.icon.setTag(data.owner.global_key);

            holder.name.setText(data.owner.name);
            holder.name.setTag(data.owner.global_key);

            holder.maopaoItem.setTag(data);

            holder.contentArea.setData(data, ContentArea.Type.Maopao);

            holder.time.setText(Global.dayToNow(data.created_at));

            holder.likeBtn.setOnCheckedChangeListener(null);
            holder.likeBtn.setChecked(data.liked);
            holder.likeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String type = ((CheckBox) v).isChecked() ? "like" : "unlike";
                    String uri = String.format(HOST_GOOD, data.id, type);
                    v.setTag(data);

                    postNetwork(uri, new RequestParams(), HOST_GOOD, 0, data);
                }
            });

            holder.commentBtn.setTag(data);

            if (data.owner_id.equals(MyApp.sUserObject.id)) {
                holder.maopaoDelete.setVisibility(View.VISIBLE);
                holder.maopaoDelete.setTag(TAG_MAOPAO_ID, data.id);
            } else {
                holder.maopaoDelete.setVisibility(View.INVISIBLE);
            }


            holder.commentArea.displayContentData(data);

            if (mData.size() - position <= 1) {
                if (!mNoMore) {
                    getNetwork(createUrl(), maopaoUrlFormat);
                }
            }

            return convertView;
        }

        ClickSmallImage onClickImage = new ClickSmallImage(MaopaoListFragment.this);

        View.OnClickListener onClickDeleteMaopao = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String maopaoId = (String) v.getTag(TAG_MAOPAO_ID);
                showDialog("删除冒泡", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String HOST_MAOPAO_DELETE = Global.HOST + "/api/tweet/%s";
                        deleteNetwork(String.format(HOST_MAOPAO_DELETE, maopaoId), TAG_DELETE_MAOPAO,
                                -1, maopaoId);
                    }
                });
            }
        };

        protected View.OnClickListener mOnClickMaopaoItem = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Maopao.MaopaoObject data = (Maopao.MaopaoObject) v.getTag();
                MaopaoDetailActivity_
                        .intent(getActivity())
                        .mMaopaoObject(data)
                        .startForResult(RESULT_EDIT_MAOPAO);
            }
        };

        View.OnClickListener onClickComment = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Maopao.Comment comment = (Maopao.Comment) v.getTag(TAG_COMMENT);
                if (MyApp.sUserObject.id.equals(comment.owner_id)) {
                    showDialog("删除评论", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final String URI_COMMENT_DELETE = Global.HOST + "/api/tweet/6597/comment/%s";
                            deleteNetwork(String.format(URI_COMMENT_DELETE, comment.id), TAG_DELETE_MAOPAO_COMMENT, -1, comment);
                        }
                    });
                } else {
                    popComment(v);
                }
            }
        };

        void popComment(View v) {
            EditText comment = mEnterLayout.content;
            Object data = v.getTag();
            Maopao.Comment commentObject;
            if (data instanceof Maopao.Comment) {
                commentObject = (Maopao.Comment) v.getTag();
                comment.setHint("回复 " + commentObject.owner.name);
                comment.setTag(commentObject);
            } else if (data instanceof Maopao.MaopaoObject) {
                commentObject = new Maopao.Comment((Maopao.MaopaoObject) data);
                comment.setHint("评论冒泡");
                comment.setTag(commentObject);
            } else {
                data = v.getTag(MaopaoListFragment.TAG_COMMENT);
                if (data instanceof Maopao.Comment) {
                    commentObject = (Maopao.Comment) data;
                    comment.setHint("回复 " + commentObject.owner.name);
                    comment.setTag(commentObject);
                }
            }

            mEnterLayout.show();

            Object tag1 = v.getTag(R.id.likeBtn);

            int itemLocation[] = new int[2];
            v.getLocationOnScreen(itemLocation);
            int itemHeight = v.getHeight();

            int listLocation[] = new int[2];
            listView.getLocationOnScreen(listLocation);
            int listHeight = listView.getHeight();

            oldListHigh = listHeight;
            if (tag1 == null) {
                needScrollY = (itemLocation[1] + itemHeight) - (listLocation[1] + listHeight);
            } else {
                needScrollY = (itemLocation[1] + itemHeight + commonEnterRoot.getHeight()) - (listLocation[1] + listHeight);
            }

            cal1 = 0;

            comment.requestFocus();
            Global.popSoftkeyboard(MaopaoListFragment.this.getActivity(), comment, true);
        }

    };

    static class ViewHolder {

        View maopaoItem;

        ImageView icon;
        TextView name;
        TextView time;
        ContentArea contentArea;

        View maopaoDelete;

        TextView photoType;
        CheckBox likeBtn;
        CheckBox commentBtn;

        LikeUsersArea likeUsersArea;
        View commentLikeArea;

        CommentArea commentArea;
    }

    //    public final static int TAG_USER_GLOBAL_KEY = R.id.name;
    public final static int TAG_MAOPAO_ID = R.id.maopaoDelete;
    public final static int TAG_MAOPAO = R.id.maopao;
    public final static int TAG_COMMENT = R.id.comment;

    public static class ClickImageParam {
        public ArrayList<String> urls;
        public int pos;
        public boolean needEdit;

        public ClickImageParam(ArrayList<String> urlsParam, int posParam, boolean needEditParam) {
            urls = urlsParam;
            pos = posParam;
            needEdit = needEditParam;
        }

        public ClickImageParam(String url) {
            urls = new ArrayList<String>();
            urls.add(url);
            pos = 0;
            needEdit = false;
        }
    }
}

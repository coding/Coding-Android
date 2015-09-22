package net.coding.program.maopao;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.twotoasters.jazzylistview.JazzyListView;
import com.umeng.socialize.sso.UMSsoHandler;

import net.coding.program.FootUpdate;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.BlankViewDisplay;
import net.coding.program.common.ClickSmallImage;
import net.coding.program.common.Global;
import net.coding.program.common.ListModify;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.StartActivity;
import net.coding.program.common.TextWatcherAt;
import net.coding.program.common.enter.EnterEmojiLayout;
import net.coding.program.common.enter.EnterLayout;
import net.coding.program.common.network.RefreshBaseFragment;
import net.coding.program.maopao.item.CommentArea;
import net.coding.program.maopao.item.MaopaoLikeAnimation;
import net.coding.program.maopao.share.CustomShareBoard;
import net.coding.program.model.DynamicObject;
import net.coding.program.model.Maopao;
import net.coding.program.third.EmojiFilter;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by chenchao on 15/9/22.
 */
@EFragment
public abstract class MaopaoListBaseFragment extends RefreshBaseFragment implements FootUpdate.LoadMore, StartActivity {

    abstract protected String createUrl();
    abstract protected String getMaopaoUrlFormat();
    abstract protected void setActionTitle();
    abstract protected void initMaopaoType();
    abstract protected void initData();

    @ViewById
    protected JazzyListView listView;
    @ViewById
    protected View blankLayout;
    @ViewById
    protected View commonEnterRoot;


    protected boolean mIsToMaopaoTopic = false;

    protected int id = UPDATE_ALL_INT;

    int needScrollY = 0;
    int oldListHigh = 0;
    int cal1 = 0;

    //    public final static int TAG_USER_GLOBAL_KEY = R.id.name;
    public final static int TAG_MAOPAO_ID = R.id.maopaoMore;
    public final static int TAG_MAOPAO = R.id.clickMaopao;
    public final static int TAG_COMMENT = R.id.comment;
    public final static int TAG_COMMENT_TEXT = R.id.commentArea;

    public static final String HOST_GOOD = Global.HOST_API + "/tweet/%s/%s";
    public static final String URI_COMMENT = Global.HOST_API + "/tweet/%s/comment";
    public static final String TAG_DELETE_MAOPAO = "TAG_DELETE_MAOPAO";
    public static final String TAG_DELETE_MAOPAO_COMMENT = "TAG_DELETE_MAOPAO_COMMENT";

    static final int RESULT_EDIT_MAOPAO = 100;
    static final int RESULT_AT = 101;

    private MyImageGetter myImageGetter;
    private int mPxImageWidth;

    boolean mNoMore = false;
    EnterEmojiLayout mEnterLayout;

    protected ArrayList<Maopao.MaopaoObject> mData = new ArrayList<>();

    View.OnClickListener onClickRetry = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onRefresh();
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    protected void initImageWidth() {
        // 图片显示，单位为 dp
        // 62 photo 3 photo 3 photo 34
        final int divide = 3;
        mPxImageWidth = Global.dpToPx(MyApp.sWidthDp - 62 - 34 - divide * 2) / 3;
        int pxPadding = getResources().getDimensionPixelSize(R.dimen.padding_12);
        mPxImageWidth = (MyApp.sWidthPix - pxPadding * 2 - Global.dpToPx(divide) * 2) / 3;

        myImageGetter = new MyImageGetter(getActivity());
    }

    protected void initMaopaoListBaseFragmen() {
        initRefreshLayout();
        initImageWidth();

        // 图片显示，单位为 dp
        // 62 photo 3 photo 3 photo 34
        final int divide = 3;
        mPxImageWidth = Global.dpToPx(MyApp.sWidthDp - 62 - 34 - divide * 2) / 3;

//        mData = AccountInfo.loadMaopao(getActivity(), mType.toString(), userId);

        setActionTitle();
        if (mData.isEmpty()) {
            showDialogLoading();
        } else {
            setRefreshing(true);
        }

        initMaopaoType();
        myImageGetter = new MyImageGetter(getActivity());


        mFootUpdate.init(listView, mInflater, this);
        listView.setAdapter(mAdapter);

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

        initData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_EDIT_MAOPAO) {
            if (resultCode == Activity.RESULT_OK || data != null) {
                int type = data.getIntExtra(ListModify.TYPE, 0);
                if (type == ListModify.Edit) {
                    Maopao.MaopaoObject maopao = (Maopao.MaopaoObject) data.getSerializableExtra(ListModify.DATA);
                    for (int i = 0; i < mData.size(); ++i) {
                        Maopao.MaopaoObject item = mData.get(i);
                        if (item.id == maopao.id) {
                            mData.remove(i);
                            mData.add(i, maopao);
                            mAdapter.notifyDataSetChanged();

                            break;
                        }
                    }

                } else if (type == ListModify.Delete) {
                    int maopaoId = data.getIntExtra(ListModify.ID, 0);
                    for (int i = 0; i < mData.size(); ++i) {
                        Maopao.MaopaoObject item = mData.get(i);
                        if (item.id == (maopaoId)) {
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


        super.onActivityResult(requestCode, resultCode, data);
        UMSsoHandler ssoHandler = CustomShareBoard.getShareController().getConfig().getSsoHandler(
                requestCode);
        if (ssoHandler != null) {
            ssoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }

    protected void hideSoftkeyboard() {
        if (!mEnterLayout.isShow()) {
            return;
        }

        mEnterLayout.restoreSaveStop();
        mEnterLayout.clearContent();
        mEnterLayout.hideKeyboard();
        mEnterLayout.hide();
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(getMaopaoUrlFormat())) {
            hideProgressDialog();
            setRefreshing(false);
            if (code == 0) {
                if (id == UPDATE_ALL_INT) {
                    mData.clear();
                }

                JSONArray jsonArray = respanse.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    Maopao.MaopaoObject item = new Maopao.MaopaoObject(jsonArray.getJSONObject(i));
                    mData.add(item);
                }

                ArrayList<Maopao.MaopaoObject> mSaveData = new ArrayList<>();
                int minSize = Math.min(mData.size(), 5);
                for (int i = 0; i < minSize; ++i) {
                    mSaveData.add(mData.get(i));
                }
//                AccountInfo.saveMaopao(getActivity(), mSaveData, mType.toString(), userId);

                if (jsonArray.length() == 0) {
                    mNoMore = true;
                } else {
                    int oldId = id;
                    id = mData.get(mData.size() - 1).id;
                    mAdapter.notifyDataSetChanged();

                    if (oldId == UPDATE_ALL_INT) {
                        // 当单个的冒泡item大于一屏时，smoothScrollToPosition(0)不会滚动到listview的顶端
                        listView.setSelectionAfterHeaderView();
                    }
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
            showProgressBar(false);
            if (code == 0) {
                mEnterLayout.clearContent();
                Maopao.Comment myComment = new Maopao.Comment(respanse.getJSONObject("data"));
                myComment.owner = new DynamicObject.Owner(MyApp.sUserObject);
                Maopao.Comment otherComment = (Maopao.Comment) data;
                mEnterLayout.restoreDelete(myComment);

                for (int i = 0; i < mData.size(); ++i) {
                    Maopao.MaopaoObject item = mData.get(i);
                    if (otherComment.tweet_id == item.id) {
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
//                int headCount = listView.getHeaderViewsCount();
                for (int i = 0; i < mData.size(); ++i) {
                    Maopao.MaopaoObject maopao = mData.get(i);
                    if (maopao.id == ((Maopao.MaopaoObject) data).id) {
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

                        mAdapter.notifyDataSetChanged();

                        break;
                    }
                }
            } else {
                showErrorMsg(code, respanse);
            }

        } else if (tag.equals(TAG_DELETE_MAOPAO)) {
            int maopaoId = (int) data;
            if (code == 0) {
                for (int i = 0; i < mData.size(); ++i) {
                    Maopao.MaopaoObject item = mData.get(i);
                    if (item.id == maopaoId) {
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
                    if (item.id == (comment.tweet_id)) {
                        for (int j = 0; j < item.comment_list.size(); ++j) {
                            if (item.comment_list.get(j).id == (comment.id)) {
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


    @Override
    protected void initSetting() {
        super.initSetting();
        id = UPDATE_ALL_INT;
    }

    @Override
    public void loadMore() {
        getNetwork(createUrl(), getMaopaoUrlFormat());
    }

    @Override
    public void onRefresh() {
        initSetting();
        getNetwork(createUrl(), getMaopaoUrlFormat());
    }

    View.OnClickListener onClickSendText = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String input = mEnterLayout.getContent();

            if (EmojiFilter.containsEmptyEmoji(v.getContext(), input)) {
                return;
            }

            Maopao.Comment commentObject = (Maopao.Comment) mEnterLayout.content.getTag();
            String uri = String.format(URI_COMMENT, commentObject.tweet_id);

            RequestParams params = new RequestParams();

            String commentString;
            if (commentObject.id == 0) {
                commentString = input;
            } else {
                commentString = Global.encodeInput(commentObject.owner.name, input);
            }
            params.put("content", commentString);
            postNetwork(uri, params, URI_COMMENT, 0, commentObject);

            showProgressBar(R.string.sending_comment);
        }
    };

    View.OnClickListener onClickDeleteMaopao = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final int maopaoId = (int) v.getTag(TAG_MAOPAO_ID);
            showDialog("冒泡", "删除冒泡？", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String HOST_MAOPAO_DELETE = Global.HOST_API + "/tweet/%s";
                    deleteNetwork(String.format(HOST_MAOPAO_DELETE, maopaoId), TAG_DELETE_MAOPAO,
                            -1, maopaoId);
                }
            });
        }
    };

    protected void popComment(View v) {
        EditText comment = mEnterLayout.content;

        Object data = v.getTag();
        Maopao.Comment commentObject = null;
        if (data instanceof Maopao.Comment) {
            commentObject = (Maopao.Comment) v.getTag();
            comment.setHint("回复 " + commentObject.owner.name);
            comment.setTag(commentObject);
        } else if (data instanceof Maopao.MaopaoObject) {
            commentObject = new Maopao.Comment((Maopao.MaopaoObject) data);
            comment.setHint("评论冒泡");
            comment.setTag(commentObject);
        } else {
            data = v.getTag(TAG_COMMENT);
            if (data instanceof Maopao.Comment) {
                commentObject = (Maopao.Comment) data;
                comment.setHint("回复 " + commentObject.owner.name);
                comment.setTag(commentObject);
            }
        }

        mEnterLayout.closeEnterPanel();
        mEnterLayout.show();

        mEnterLayout.restoreLoad(commentObject);

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
        Global.popSoftkeyboard(getActivity(), comment, true);
    }

    protected BaseAdapter mAdapter = new BaseAdapter() {
        final int[] commentsId = new int[]{
                R.id.comment0,
                R.id.comment1,
                R.id.comment2,
                R.id.comment3,
                R.id.comment4,
        };
        protected View.OnClickListener mOnClickMaopaoItem = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Maopao.MaopaoObject data = (Maopao.MaopaoObject) v.getTag();
                Fragment parent = getParentFragment();
                MaopaoDetailActivity_
                        .intent(parent != null ? parent : MaopaoListBaseFragment.this)
                        .mMaopaoObject(data)
                        .startForResult(RESULT_EDIT_MAOPAO);
            }
        };
        ClickSmallImage onClickImage = new ClickSmallImage(MaopaoListBaseFragment.this);

        View.OnClickListener onClickMaopaoMore = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int maopaoId = (int) v.getTag(TAG_MAOPAO_ID);
                showDialog(new String[]{"删除冒泡"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            showDialog("冒泡", "删除冒泡？", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String HOST_MAOPAO_DELETE = Global.HOST_API + "/tweet/%s";
                                    deleteNetwork(String.format(HOST_MAOPAO_DELETE, maopaoId), TAG_DELETE_MAOPAO,
                                            -1, maopaoId);
                                }
                            });
                        }
                    }
                });

            }
        };

        View.OnClickListener onClickComment = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Maopao.Comment comment = (Maopao.Comment) v.getTag(TAG_COMMENT);
                if (MyApp.sUserObject.id == (comment.owner_id)) {
                    showDialog("冒泡", "删除评论？", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final String URI_COMMENT_DELETE = Global.HOST_API + "/tweet/%d/comment/%d";
                            deleteNetwork(String.format(URI_COMMENT_DELETE, comment.tweet_id, comment.id), TAG_DELETE_MAOPAO_COMMENT, -1, comment);
                        }
                    });
                } else {
                    popComment(v);
                }
            }
        };

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

        void action_share_third(Maopao.MaopaoObject mMaopaoObject) {
            mEnterLayout.hideKeyboard();
            CustomShareBoard.ShareData shareData = new CustomShareBoard.ShareData(mMaopaoObject);
            CustomShareBoard shareBoard = new CustomShareBoard(getActivity(), shareData);
            shareBoard.showAtLocation(getActivity().getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.fragment_maopao_list_item, parent, false);

                holder.maopaoItemTop = convertView.findViewById(R.id.maopao_item_top);

                holder.maopaoItem = convertView.findViewById(R.id.MaopaoItem);
                holder.maopaoItem.setOnClickListener(mOnClickMaopaoItem);

                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.icon.setOnClickListener(mOnClickUser);

                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.name.setOnClickListener(mOnClickUser);
                holder.time = (TextView) convertView.findViewById(R.id.time);

                holder.contentArea = new ContentArea(convertView, mOnClickMaopaoItem, onClickImage, myImageGetter, getImageLoad(), mPxImageWidth);

                holder.commentLikeArea = convertView.findViewById(R.id.commentLikeArea);
                holder.likeUsersArea = new LikeUsersArea(convertView, MaopaoListBaseFragment.this, getImageLoad(), mOnClickUser);

                holder.location = (TextView) convertView.findViewById(R.id.location);
                holder.photoType = (TextView) convertView.findViewById(R.id.photoType);
                holder.likeBtn = (CheckBox) convertView.findViewById(R.id.likeBtn);
                holder.commentBtn = convertView.findViewById(R.id.commentBtn);
                holder.likeBtn.setTag(R.id.likeBtn, holder);
                holder.maopaoGoodView = convertView.findViewById(R.id.maopaoGood);
                holder.likeAreaDivide = convertView.findViewById(R.id.likeAreaDivide);
                holder.commentBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popComment(v);
                    }
                });

                holder.shareBtn = convertView.findViewById(R.id.shareBtn);
                holder.shareBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Object item = v.getTag();
                        if (item instanceof Maopao.MaopaoObject) {
                            action_share_third((Maopao.MaopaoObject) item);
                        }
                    }
                });

                holder.maopaoDelete = convertView.findViewById(R.id.deleteButton);
                holder.maopaoDelete.setOnClickListener(onClickDeleteMaopao);

                holder.commentArea = new CommentArea(convertView, onClickComment, myImageGetter);

                View[] divides = new View[commentsId.length];
                for (int i = 0; i < commentsId.length; ++i) {
                    divides[i] = convertView.findViewById(commentsId[i]).findViewById(R.id.commentTopDivider);
                }
                holder.divides = divides;

                convertView.setTag(R.id.MaopaoItem, holder);
            } else {
                holder = (ViewHolder) convertView.getTag(R.id.MaopaoItem);
            }

            final Maopao.MaopaoObject data = (Maopao.MaopaoObject) getItem(position);

            holder.likeUsersArea.likeUsersLayout.setTag(TAG_MAOPAO, data);
            holder.likeUsersArea.displayLikeUser();

            if (data.likes > 0 || data.comments > 0) {
                holder.commentLikeArea.setVisibility(View.VISIBLE);
            } else {
                holder.commentLikeArea.setVisibility(View.GONE);
            }

            if (position == 0 && mIsToMaopaoTopic) {
                holder.maopaoItemTop.setVisibility(View.VISIBLE);
            } else {
                holder.maopaoItemTop.setVisibility(View.GONE);
            }

            MaopaoLocationArea.bind(holder.location, data);

            if (!data.device.isEmpty()) {
                String device = String.format("来自 %s", data.device);
                holder.photoType.setVisibility(View.VISIBLE);
                holder.photoType.setText(device);
            } else {
                holder.photoType.setVisibility(View.GONE);
            }

            iconfromNetwork(holder.icon, data.owner.avatar);
            holder.icon.setTag(data.owner.global_key);

            holder.name.setText(data.owner.name);
            holder.name.setTag(data.owner.global_key);

            holder.maopaoItem.setTag(data);

            holder.contentArea.setData(data);

            holder.time.setText(Global.dayToNow(data.created_at));


            if (data.owner_id == (MyApp.sUserObject.id)) {
                holder.maopaoDelete.setVisibility(View.VISIBLE);
                holder.maopaoDelete.setTag(TAG_MAOPAO_ID, data.id);
            } else {
                holder.maopaoDelete.setVisibility(View.INVISIBLE);
            }

            holder.likeBtn.setOnCheckedChangeListener(null);
            holder.likeBtn.setChecked(data.liked);
            holder.likeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean like = ((CheckBox) v).isChecked();
                    if (like) {
                        MaopaoLikeAnimation.playAnimation(holder.maopaoGoodView, v);
                    }
                    String type = like ? "like" : "unlike";
                    String uri = String.format(HOST_GOOD, data.id, type);
                    v.setTag(data);

                    postNetwork(uri, new RequestParams(), HOST_GOOD, 0, data);
                }
            });
            holder.shareBtn.setTag(data);

            if (data.likes > 0) {
                holder.likeAreaDivide.setVisibility(data.comments > 0 ? View.VISIBLE : View.INVISIBLE);
            }

            holder.commentBtn.setTag(data);

            holder.commentArea.displayContentData(data);

            int commentCount = data.comment_list.size();
            int needShow = commentCount - 1;
            for (int i = 0; i < commentsId.length; ++i) {
                if (i < needShow) {
                    holder.divides[i].setVisibility(View.VISIBLE);
                } else {
                    holder.divides[i].setVisibility(View.INVISIBLE);
                }
            }
            if (commentsId.length < data.comments) { // 评论数超过5时
                holder.divides[commentsId.length - 1].setVisibility(View.VISIBLE);
            }

            if (mData.size() - position <= 1) {
                if (!mNoMore) {
                    getNetwork(createUrl(), getMaopaoUrlFormat());
                }
            }

            return convertView;
        }

    };

    static class ViewHolder {
        View maopaoItemTop;

        View maopaoItem;

        ImageView icon;
        TextView name;
        TextView time;
        ContentArea contentArea;

//        View maopaoDelete;

        TextView photoType;
        CheckBox likeBtn;
        View commentBtn;
        View shareBtn;

        LikeUsersArea likeUsersArea;
        View commentLikeArea;

        CommentArea commentArea;

        View[] divides;

        View likeAreaDivide;
        TextView location;

        View maopaoGoodView;
        View maopaoDelete;
    }
}

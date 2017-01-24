package net.coding.program.project.detail;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.FootUpdate;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.network.RefreshBaseFragment;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.model.AttachmentFolderObject;
import net.coding.program.model.ProjectObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Created by yangzhen on 2014/10/25.
 * 文件列表的一级目录
 */
@EFragment(R.layout.folder_main_refresh_listview)
public class ProjectAttachmentFragment extends RefreshBaseFragment implements FootUpdate.LoadMore {
    public static final int RESULT_REQUEST_FILES = 1;
    final public static int RESULT_MOVE_FOLDER = 13;

    @FragmentArg
    ProjectObject mProjectObject;

    @ViewById
    ListView listView;
    
    @ViewById
    View blankLayout;

    ArrayList<AttachmentFolderObject> selectFolder;
    private ArrayList<AttachmentFolderObject> mData = new ArrayList<>();
    private String HOST_FOLDER = Global.HOST_API + "/project/%d/all_folders?pageSize=9999";
    private String HOST_FILECOUNT = Global.HOST_API + "/project/%d/folders/all-file-count-with-share";
    private String HOST_FOLDER_NAME = Global.HOST_API + "/project/%d/dir/%s/name/%s";
    private String HOST_FOLDER_NEW = Global.HOST_API + "/project/%d/mkdir";

    private String HOST_FOLDER_DELETE_FORMAT = Global.HOST_API + "/project/%d/rmdir/%s";
    private String HOST_FOLDER_DELETE;
    private HashMap<String, Integer> fileCountMap = new HashMap<>();
    private boolean isEditMode = false;

    /**
     * 弹出框
     */
    private int selectedPosition;
    BaseAdapter adapter = new BaseAdapter() {
        private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AttachmentFolderObject data = mData.get((Integer) buttonView.getTag());
                data.isSelected = isChecked;
            }
        };
        private View.OnClickListener onMoreClickListener = view -> showPop((Integer) view.getTag());

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
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.project_attachment_list_item, parent, false);
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
                holder.more = (RelativeLayout) convertView.findViewById(R.id.more);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            AttachmentFolderObject data = mData.get(position);
            holder.name.setText(data.getNameCount());
            if (data.file_id.equals("0")) {
                holder.icon.setImageResource(R.drawable.ic_project_git_folder);
                holder.more.setVisibility(View.GONE);
            } else if (data.file_id.equals("-1")) {
                holder.icon.setImageResource(R.drawable.icon_file_folder_share);
                holder.more.setVisibility(View.GONE);
            } else {
                holder.icon.setImageResource(R.drawable.ic_project_git_folder2);
                holder.more.setVisibility(View.VISIBLE);
            }
            //iconfromNetwork(holder.icon, data.user.avatar);
            if (isEditMode && data.isDeleteable()) {
                holder.checkBox.setVisibility(View.VISIBLE);
                if (data.isSelected) {
                    holder.checkBox.setChecked(true);
                } else {
                    holder.checkBox.setChecked(false);
                }
            } else {
                holder.checkBox.setVisibility(View.GONE);
            }

            holder.checkBox.setOnCheckedChangeListener(onCheckedChangeListener);
            holder.checkBox.setTag(new Integer(position));

            holder.more.setTag(new Integer(position));
            holder.more.setOnClickListener(onMoreClickListener);
            holder.more.setVisibility(View.INVISIBLE);

            if (position == mData.size() - 1) {
                loadMore();
            }

            return convertView;
        }
    };

    @AfterViews
    protected void initProjectAttachmentFragment() {
        // 根目录下不能上传文件
        View rootLayout = getView();
        if (rootLayout != null) {
            rootLayout.findViewById(R.id.common_folder_bottom_upload).setEnabled(false);
            Drawable drawable = getResources().getDrawable(R.drawable.project_file_action_upload_disable);

            if (drawable != null) {
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                ((TextView) rootLayout.findViewById(R.id.textUploadFile)).setCompoundDrawables(
                        drawable,
                        null,
                        null,
                        null
                );
            }
        }

        listViewAddHeaderSection(listView);
        listView.setVisibility(View.INVISIBLE);

        initRefreshLayout();

        showDialogLoading();
        HOST_FOLDER = String.format(HOST_FOLDER, mProjectObject.getId());
        HOST_FILECOUNT = String.format(HOST_FILECOUNT, mProjectObject.getId());
        //mFootUpdate.init(listView, mInflater, this);
        listView.setAdapter(adapter);

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            listViewItemLongClicked((int) id);
            return true;
        });

        getNetwork(HOST_FILECOUNT, HOST_FILECOUNT);
    }

    @ItemClick
    public void listViewItemClicked(AttachmentFolderObject folderObject) {
        AttachmentsActivity_.intent(getActivity())
                .mAttachmentFolderObject(folderObject)
                .mProjectObjectId(mProjectObject.getId())
                .mProject(mProjectObject)
                .startForResult(RESULT_REQUEST_FILES);

    }

    public void listViewItemLongClicked(int position) {
        showPop(position);
    }

    @OnActivityResult(RESULT_REQUEST_FILES)
    public void onFileResult(int resultCode, Intent data) {
        initSetting();
        setRefreshing(true);
        getNetwork(HOST_FILECOUNT, HOST_FILECOUNT);
    }

    @Override
    public void onRefresh() {
        initSetting();
        //getNetwork(HOST_FOLDER, HOST_FOLDER);
        getNetwork(HOST_FILECOUNT, HOST_FILECOUNT);
    }

    @Click
    void common_folder_bottom_add() {
        doNowFolder();
    }

    @Override
    public void loadMore() {
        getNextPageNetwork(HOST_FOLDER, HOST_FOLDER);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_FOLDER)) {
            hideDialogLoading();
            setRefreshing(false);
            if (code == 0) {
                if (isLoadingFirstPage(tag)) {
                    mData.clear();
                }
                JSONArray folders = respanse.getJSONObject("data").getJSONArray("list");

                AttachmentFolderObject shareFolder = new AttachmentFolderObject();
                shareFolder.file_id = AttachmentFolderObject.SHARE_FOLDER_ID;
                shareFolder.setCount(fileCountMap.get(shareFolder.file_id));
                shareFolder.name = "分享中";
                if (TextUtils.isEmpty(MyApp.getEnterpriseGK())) {
                    mData.add(shareFolder);
                }

                AttachmentFolderObject defaultFolder = new AttachmentFolderObject();
                defaultFolder.setCount(fileCountMap.get(defaultFolder.file_id));
                mData.add(defaultFolder);

                for (int i = 0; i < folders.length(); ++i) {
                    AttachmentFolderObject folder = new AttachmentFolderObject(folders.getJSONObject(i));
                    folder.setCount(fileCountMap.get(folder.file_id));
                    ArrayList<AttachmentFolderObject> subFolders = folder.sub_folders;
                    for (AttachmentFolderObject subFolder : subFolders) {
                        subFolder.setCount(fileCountMap.get(subFolder.file_id));
                    }
                    mData.add(folder);
                }

                adapter.notifyDataSetChanged();
                listView.setVisibility(View.VISIBLE);
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(HOST_FILECOUNT)) {
            if (code == 0) {
                JSONObject dataRoot = respanse.optJSONObject("data");

                fileCountMap.put(AttachmentFolderObject.SHARE_FOLDER_ID, dataRoot.optInt("shareCount"));
                JSONArray counts = dataRoot.optJSONArray("folders");
                for (int i = 0; i < counts.length(); ++i) {
                    JSONObject countItem = counts.optJSONObject(i);
                    fileCountMap.put(countItem.optString("folder"), countItem.optInt("count"));
                }
                loadMore();
            } else {
                hideDialogLoading();
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(HOST_FOLDER_NAME)) {
            if (code == 0) {
                umengEvent(UmengEvent.FILE, "重命名文件夹");

                showButtomToast("重命名成功");
                AttachmentFolderObject folderObject = mData.get(pos);
                folderObject.name = (String) data;
                adapter.notifyDataSetChanged();
                //mData.clear();
                //AttachmentFolderObject folderObject = (AttachmentFolderObject)data;
                //loadMore();
            } else {
                showButtomToast("重命名失败");
            }
        } else if (tag.equals(HOST_FOLDER_NEW)) {
            if (code == 0) {
                umengEvent(UmengEvent.FILE, "新建文件夹");
                AttachmentFolderObject folder = new AttachmentFolderObject(respanse.getJSONObject("data"));
                mData.add(1, folder);
                adapter.notifyDataSetChanged();
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(HOST_FOLDER_DELETE)) {
            if (code == 0) {
                umengEvent(UmengEvent.FILE, "删除文件夹");
                setRefreshing(false);
                mData.remove(selectFolder.get(0));
                selectFolder.remove(0);
                if (selectFolder.size() > 0) {
                    deleteFolders();
                } else {
                    showButtomToast("删除完成");
                    adapter.notifyDataSetChanged();
                }
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(TAG_MOVE_FOLDER)) {
            if (code == 0) {
                umengEvent(UmengEvent.FILE, "移动文件夹");

                showButtomToast("移动成功");
                mData.remove(pickedFolderObject);
                pickedFolderObject = null;
                adapter.notifyDataSetChanged();

                if (data instanceof AttachmentFolderObject) {
                    AttachmentFolderObject folder = (AttachmentFolderObject) data;
                    EventBus.getDefault().post(folder.name);
                }

                onRefresh();
            } else {
                showErrorMsg(code, respanse);
            }
        }

    }

    /**
     * 重命名文件
     * @param position
     * @param folderObject
     */
    private void doRename(final int position, final AttachmentFolderObject folderObject) {
        if (folderObject.file_id.equals("0")) {
            showButtomToast("默认文件夹无法重命名");
            return;
        }
        //final EditText input = new EditText(getActivity());
        LayoutInflater li = LayoutInflater.from(getActivity());
        View v1 = li.inflate(R.layout.dialog_input, null);
        final EditText input = (EditText) v1.findViewById(R.id.value);
        input.setText(folderObject.name);
        new AlertDialog.Builder(getActivity())
                .setTitle("重命名")
                .setView(v1)
                .setPositiveButton("确定", (dialog, which) -> {
                    String newName = input.getText().toString();
                    //从网页版扒来的正则
                    String namePatternStr = "[,`~!@#$%^&*:;()''\"\"><|.\\ /=]";
                    Pattern namePattern = Pattern.compile(namePatternStr);
                    if (newName.equals("")) {
                        showButtomToast("名字不能为空");
                    } else if (namePattern.matcher(newName).find()) {
                        showButtomToast("文件夹名：" + newName + " 不能采用");
                        // if(folder.name.match(/[,`~!@#$%^&*:;()''""><|.\ /=]/g))
                    } else {
                        if (!newName.equals(folderObject.name)) {
                            HOST_FOLDER_NAME = String.format(HOST_FOLDER_NAME, mProjectObject.getId(), folderObject.file_id, newName);
                            putNetwork(HOST_FOLDER_NAME, HOST_FOLDER_NAME, position, newName);
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();

        input.requestFocus();
    }

    /**
     * 新建文件夹
     */
    private void doNowFolder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //final EditText input = new EditText(getActivity());
        LayoutInflater li = LayoutInflater.from(getActivity());
        View v1 = li.inflate(R.layout.dialog_input, null);
        final EditText input = (EditText) v1.findViewById(R.id.value);
        input.setHint("请输入文件夹名称");
        builder.setTitle("新建文件夹")
                .setView(v1).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = input.getText().toString();
                String namePatternStr = "[,`~!@#$%^&*:;()''\"\"><|.\\ /=]";
                Pattern namePattern = Pattern.compile(namePatternStr);
                if (newName.equals("")) {
                    showButtomToast("名字不能为空");
                } else if (namePattern.matcher(newName).find()) {
                    showButtomToast("文件夹名：" + newName + " 不能采用");
                    // if(folder.name.match(/[,`~!@#$%^&*:;()''""><|.\ /=]/g))
                } else {
                    HOST_FOLDER_NEW = String.format(HOST_FOLDER_NEW, mProjectObject.getId());
                    RequestParams params = new RequestParams();
                    params.put("name", newName);
                    postNetwork(HOST_FOLDER_NEW, params, HOST_FOLDER_NEW);
                }
            }
        }).setNegativeButton("取消", null)
                .show();
        input.requestFocus();
    }

    private void setListEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
        adapter.notifyDataSetChanged();
    }

    /**
     * 删除文件夹
     */
    void action_delete() {
        selectFolder = new ArrayList<>();
        for (AttachmentFolderObject folderObject : mData) {
            if (folderObject.isSelected)
                selectFolder.add(folderObject);
        }
        //showButtomToast("selected count:" + selectFolder.size());
        if (selectFolder.size() == 0) {
            return;
        }
        String messageFormat = "确定要删除%s个文件夹么？";
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("删除文件夹").setMessage(String.format(messageFormat, selectFolder.size()))
                .setPositiveButton("确定", (dialog, which) -> deleteFolders())
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 删除单个文件
     * @param selectedFolderObject
     */
    void action_delete_single(AttachmentFolderObject selectedFolderObject) {
        if (selectedFolderObject == null)
            return;

        selectFolder = new ArrayList<>();
        selectFolder.add(selectedFolderObject);
        String messageFormat = "确定删除文件夹\"%s\"？";
        showDialog("删除文件夹", String.format(messageFormat, selectedFolderObject.name), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteFolders();
            }
        });
    }

    void deleteFolders() {
        if (selectFolder.size() > 0) {
            setRefreshing(true);
            HOST_FOLDER_DELETE = String.format(HOST_FOLDER_DELETE_FORMAT, mProjectObject.getId(), selectFolder.get(0).file_id);
            deleteNetwork(HOST_FOLDER_DELETE, HOST_FOLDER_DELETE);
        }
    }

    void action_all() {
        for (AttachmentFolderObject folderObject : mData) {
            if (folderObject.isDeleteable())
                folderObject.isSelected = true;
        }
        adapter.notifyDataSetChanged();
    }

    void action_inverse() {
        for (AttachmentFolderObject folderObject : mData) {
            if (folderObject.isDeleteable())
                folderObject.isSelected = !folderObject.isSelected;
        }
        adapter.notifyDataSetChanged();
    }

    public void showPop(final int position) {
        if (position == 0) {
            return;
        }
//        if (mAttachmentPopupWindow == null) {
//            initBottomPop();
//        }
        selectedPosition = position;
        AttachmentFolderObject selectedFolderObject = mData.get(selectedPosition);


        final int deletePos;

        String[] itemTitles;
        if (selectedFolderObject.file_id.equals("0")) {
            return;
        } else if (selectedFolderObject.count != 0) {
            if (selectedFolderObject.sub_folders.isEmpty()) {
                itemTitles = new String[]{"重命名", "移动到"};
                deletePos = 3;
            } else {
                itemTitles = new String[]{"重命名"};
                deletePos = 2;
            }
        } else {
            if (selectedFolderObject.sub_folders.isEmpty()) {
                itemTitles = new String[]{"重命名", "移动到", "删除"};
                deletePos = 3;
            } else {
                itemTitles = new String[]{"重命名", "删除"};
                deletePos = 2;
            }
        }

        new AlertDialog.Builder(getActivity())
                .setItems(itemTitles, (dialog, which) -> {
                    AttachmentFolderObject itemData = mData.get(position);
                    AttachmentFolderObject folderObject = mData.get(selectedPosition);
                    if (which == 0) {
                        doRename(selectedPosition, folderObject);
                    } else if (which == deletePos) {
                        AttachmentFolderObject selectedFolderObject1 = folderObject;
                        if (selectedFolderObject1.isDeleteable()) {
                            action_delete_single(selectedFolderObject1);
                        } else {
                            showButtomToast("请先清空文件夹");
                        }
                    } else {
                        actionMove(folderObject);
                    }
                })
                .show();
    }

    AttachmentFolderObject pickedFolderObject;

    private void actionMove(AttachmentFolderObject folderObject) {
        pickedFolderObject = folderObject;
        AttachmentsFolderSelectorActivity_.intent(this)
                .mProjectObjectId(mProjectObject.getId())
                .sourceRootFolder(pickedFolderObject)
                .startForResult(RESULT_MOVE_FOLDER);
    }

    @OnActivityResult(RESULT_MOVE_FOLDER)
    void onResultFolderMove(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            AttachmentFolderObject selectedFolder = (AttachmentFolderObject) data.getSerializableExtra("mAttachmentFolderObject");
            if (selectedFolder.file_id.equals("0")) {
                return;
            }

            if (pickedFolderObject == null) {
                return;
            }

//            AttachmentFileObject source = selectFile.get(selectFile.size() - 1);

            String host = String.format("%s/%s/folder/%s/move-to/%s", Global.HOST_API, mProjectObject.getProjectPath(), pickedFolderObject.file_id, selectedFolder.file_id);
            putNetwork(host, null, TAG_MOVE_FOLDER, selectedFolder);
        }
    }

    private final String TAG_MOVE_FOLDER = "TAG_MOVE_FOLDER";

    static class ViewHolder {
        ImageView icon;
        TextView name;
        CheckBox checkBox;
        RelativeLayout more;
    }
}

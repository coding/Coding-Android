package net.coding.program.project.detail;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.CodingColor;
import net.coding.program.common.Global;
import net.coding.program.common.LoadMore;
import net.coding.program.common.model.AttachmentFileObject;
import net.coding.program.common.model.AttachmentFolderObject;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.ui.holder.FolderHolder;
import net.coding.program.common.umeng.UmengEvent;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * 在文件做移动操作时，选择文件目录的Activity
 * Created by yangzhen
 */
@EActivity(R.layout.activity_attachments_folder_selector)
public class AttachmentsFolderSelectorActivity extends BackActivity implements LoadMore {
    private static String TAG = AttachmentsFolderSelectorActivity.class.getSimpleName();
    private final String STRING_OUT_FOLDER = "移出目录";

    @Extra
    int mProjectObjectId;
    @Extra
    AttachmentFileObject sourceFileObject;
    @Extra
    AttachmentFolderObject sourceRootFolder;

    AttachmentFolderObject mAttachmentFolderObject;

    @ViewById
    View btnLeft, btnRight;

    @ViewById
    ListView listView;

    private boolean inChildFolder = false;
    private String HOST_FOLDER = Global.HOST_API + "/project/%s/all_folders?pageSize=9999";
    private String HOST_FOLDER_NEW = Global.HOST_API + "/project/%s/mkdir";
    private ArrayList<AttachmentFolderObject> mData = new ArrayList<>();

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
            FolderHolder holder = FolderHolder.instance(convertView, parent);

            AttachmentFolderObject data = mData.get(position);
            //holder.name.setText(data.getNameCount());
            holder.name.setText(data.name);
            if (data.name.equals(STRING_OUT_FOLDER)) {
                holder.name.setTextColor(0xFFAAB1B9);
            } else {
                holder.name.setTextColor(CodingColor.font1);
            }

            if (data.file_id.equals("0")) {
                if (isChildFolder()) {
                    holder.icon.setImageResource(R.drawable.icon_file_folder_out);
                } else {
                    holder.icon.setImageResource(R.drawable.ic_project_git_folder);
                }
            } else {
                holder.icon.setImageResource(R.drawable.ic_project_git_folder2);
            }

            if (position == (getCount() - 1)) {
                holder.bottomLine.setVisibility(View.GONE);
            } else {
                holder.bottomLine.setVisibility(View.VISIBLE);
            }

            return holder.getRootView();
        }

    };
    private ArrayList<AttachmentFolderObject> mDefaultData = new ArrayList<>();

    private boolean isRootFolder() {
        return sourceRootFolder != null;
    }

    private boolean isChildFolder() {
        return sourceFileObject != null && sourceFileObject.isFolder;
    }

    private boolean isFile() {
        return sourceFileObject == null || !sourceFileObject.isFolder;
    }

    @OptionsItem(android.R.id.home)
    void close() {
        if (mAttachmentFolderObject != null) {
            if (mAttachmentFolderObject.parent != null) {
                mAttachmentFolderObject = mAttachmentFolderObject.parent;
                mData.clear();
                mData.addAll(mAttachmentFolderObject.sub_folders);
                adapter.notifyDataSetChanged();
                //isTopFolder = false;
                setActionBarTitle(mAttachmentFolderObject.name);
            } else {
                mAttachmentFolderObject = null;
                mData.clear();
                mData.addAll(mDefaultData);
                adapter.notifyDataSetChanged();
                setActionBarTitle(R.string.title_activity_attachment_folder_selector);
                ///isTopFolder = true;
            }

            setBottomBtn();
        } else {
            onBackPressed();
        }
    }

    @AfterViews
    protected final void initAttachmentsFolderSelectorActivity() {
        setActionBarTitle(R.string.title_activity_attachment_folder_selector);

        setBottomBtn();

        HOST_FOLDER = String.format(HOST_FOLDER, mProjectObjectId);

        listViewAddFootSection(listView);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (!isFile() && inChildFolder) {
                return;
            }

            inChildFolder = true;
            mAttachmentFolderObject = mData.get(position);
            mData.clear();
            mData.addAll(mAttachmentFolderObject.sub_folders);
            adapter.notifyDataSetChanged();
            //isTopFolder = false;
            getSupportActionBar().setTitle(mAttachmentFolderObject.name);
            setBottomBtn();
        });

        showDialogLoading();
        loadMore();
    }

    private void setBottomBtn() {
        if (mAttachmentFolderObject == null) {
            btnRight.setVisibility(View.GONE);
            btnLeft.setVisibility(View.VISIBLE);
        } else {
            btnRight.setVisibility(View.VISIBLE);
            if (mAttachmentFolderObject.file_id.equals("0") || mAttachmentFolderObject.parent != null) {
                btnLeft.setVisibility(View.GONE);
            } else {
                btnLeft.setVisibility(View.VISIBLE);
            }

        }
    }

    @Override
    public void loadMore() {
        getNextPageNetwork(HOST_FOLDER, HOST_FOLDER);
    }

    @Click(R.id.btnRight)
    void action_move() {
        if (mAttachmentFolderObject == null) {
            showButtomToast("请选择文件夹");
        } else {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("mAttachmentFolderObject", mAttachmentFolderObject);
            setResult(RESULT_OK, resultIntent);
            finish();
        }

    }

    @Click(R.id.btnLeft)
    void action_new_folder() {
        if (mAttachmentFolderObject != null && mAttachmentFolderObject.file_id.equals("0")) {
            showButtomToast("默认文件夹不能创建子文件夹");
            return;
        }

        //final EditText input = new EditText(getActivity());
        LayoutInflater li = LayoutInflater.from(AttachmentsFolderSelectorActivity.this);
        View v1 = li.inflate(R.layout.dialog_input, null);
        final EditText input = (EditText) v1.findViewById(R.id.value);
        input.setHint("请输入文件夹名称");
        new AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                .setTitle("新建文件夹")
                .setView(v1)
                .setPositiveButton("确定", (dialog, which) -> {
                    String newName = input.getText().toString();
                    String namePatternStr = "[,`~!@#$%^&*:;()''\"\"><|.\\ /=]";
                    Pattern namePattern = Pattern.compile(namePatternStr);
                    if (newName.equals("")) {
                        showButtomToast("名字不能为空");
                    } else if (namePattern.matcher(newName).find()) {
                        showButtomToast("文件夹名：" + newName + " 不能采用");
                        // if(folder.name.match(/[,`~!@#$%^&*:;()''""><|.\ /=]/g))
                    } else {
                        HOST_FOLDER_NEW = String.format(HOST_FOLDER_NEW, mProjectObjectId);
                        RequestParams params = new RequestParams();
                        params.put("name", newName);
                        if (mAttachmentFolderObject != null) {
                            params.put("parentId", mAttachmentFolderObject.file_id);
                        }
                        postNetwork(HOST_FOLDER_NEW, params, HOST_FOLDER_NEW);
                    }
                })
                .setNegativeButton("取消", null)
                .show();

        input.requestFocus();
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_FOLDER)) {
            hideProgressDialog();
            if (code == 0) {
                JSONArray folders = respanse.getJSONObject("data").getJSONArray("list");

                AttachmentFolderObject defaultFolder = new AttachmentFolderObject();
                //defaultFolder.setCount(fileCountMap.get(defaultFolder.file_id));

                if (isChildFolder()) {
                    defaultFolder.name = STRING_OUT_FOLDER;
                    mData.add(defaultFolder);
                } else if (isRootFolder()) {
                    // do nothing
                } else { // 移动文件显示默认文件夹
                    mData.add(defaultFolder);
                }

                for (int i = 0; i < folders.length(); ++i) {
                    AttachmentFolderObject folder = new AttachmentFolderObject(folders.getJSONObject(i));
                    mData.add(folder);
                }

                mDefaultData.addAll(mData);
                adapter.notifyDataSetChanged();
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(HOST_FOLDER_NEW)) {
            if (code == 0) {
                umengEvent(UmengEvent.FILE, "新建文件夹");

                AttachmentFolderObject folder = new AttachmentFolderObject(respanse.getJSONObject("data"));
                if (mAttachmentFolderObject == null) {
                    mData.add(1, folder);
                    mDefaultData.clear();
                    mDefaultData.addAll(mData);
                    adapter.notifyDataSetChanged();
                } else {
                    mAttachmentFolderObject.sub_folders.add(0, folder);
                    folder.parent = mAttachmentFolderObject;
                    mData.add(0, folder);
                    adapter.notifyDataSetChanged();
                }
                //setResult(Activity.RESULT_OK);
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

}

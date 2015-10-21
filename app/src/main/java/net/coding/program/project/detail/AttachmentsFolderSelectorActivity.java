package net.coding.program.project.detail;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.FootUpdate;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ui.BaseActivity;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.model.AttachmentFolderObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * 在文档做移动操作时，选择文档目录的Activity
 * Created by yangzhen
 */
@EActivity(R.layout.activity_attachments_folder_selector)
@OptionsMenu(R.menu.project_attachment_folder_selector)
public class AttachmentsFolderSelectorActivity extends BaseActivity implements FootUpdate.LoadMore {
    private static String TAG = AttachmentsFolderSelectorActivity.class.getSimpleName();
    @Extra
    int mProjectObjectId;

    AttachmentFolderObject mAttachmentFolderObject;

    @ViewById
    Button btnLeft;

    @ViewById
    Button btnRight;

    //Boolean isTopFolder = true;
    @ViewById
    ListView listView;
    private String HOST_FOLDER = Global.HOST_API + "/project/%s/all_folders?pageSize=9999";
    private String HOST_FOLDER_NEW = Global.HOST_API + "/project/%s/mkdir";
    private ArrayList<AttachmentFolderObject> mData = new ArrayList<>();
    BaseAdapter adapter = new BaseAdapter() {
        private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AttachmentFolderObject data = mData.get((Integer) buttonView.getTag());
                data.isSelected = isChecked;
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

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = ViewHolder.instance(convertView, parent);

            AttachmentFolderObject data = mData.get(position);
            //holder.name.setText(data.getNameCount());
            holder.name.setText(data.name);
            if (data.file_id.equals("0")) {
                holder.icon.setImageResource(R.drawable.ic_project_git_folder);
                holder.more.setVisibility(View.GONE);
            } else {
                holder.icon.setImageResource(R.drawable.ic_project_git_folder2);
                holder.more.setVisibility(View.VISIBLE);
            }
            //iconfromNetwork(holder.icon, data.user.avatar);

            holder.checkBox.setVisibility(View.GONE);

            holder.more.setVisibility(View.GONE);

            /*if (position == mData.size() - 1) {
                loadMore();
            }*/

            return holder.getRootView();
        }

    };
    private ArrayList<AttachmentFolderObject> mDefaultData = new ArrayList<>();

    @OptionsItem(android.R.id.home)
    void close() {
        if (mAttachmentFolderObject != null) {
            if (mAttachmentFolderObject.parent != null) {
                mAttachmentFolderObject = mAttachmentFolderObject.parent;
                mData.clear();
                mData.addAll(mAttachmentFolderObject.sub_folders);
                adapter.notifyDataSetChanged();
                //isTopFolder = false;
                getSupportActionBar().setTitle(mAttachmentFolderObject.name);
            } else {
                mAttachmentFolderObject = null;
                mData.clear();
                mData.addAll(mDefaultData);
                adapter.notifyDataSetChanged();
                getSupportActionBar().setTitle(R.string.title_activity_attachment_folder_selector);
                ///isTopFolder = true;
            }

            setBottomBtn();
        } else {
            onBackPressed();
        }
    }

    @OptionsItem
    void action_cancel() {
        finish();
    }

    @AfterViews
    protected final void initAttachmentsFolderSelectorActivity() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_activity_attachment_folder_selector);

        setBottomBtn();

        HOST_FOLDER = String.format(HOST_FOLDER, mProjectObjectId);

        mFootUpdate.init(listView, mInflater, this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mAttachmentFolderObject = mData.get(position);
                mData.clear();
                mData.addAll(mAttachmentFolderObject.sub_folders);
                adapter.notifyDataSetChanged();
                //isTopFolder = false;
                getSupportActionBar().setTitle(mAttachmentFolderObject.name);
                setBottomBtn();
            }
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

        AlertDialog.Builder builder = new AlertDialog.Builder(AttachmentsFolderSelectorActivity.this);
        //final EditText input = new EditText(getActivity());
        LayoutInflater li = LayoutInflater.from(AttachmentsFolderSelectorActivity.this);
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
                    HOST_FOLDER_NEW = String.format(HOST_FOLDER_NEW, mProjectObjectId);
                    RequestParams params = new RequestParams();
                    params.put("name", newName);
                    if (mAttachmentFolderObject != null) {
                        params.put("parentId", mAttachmentFolderObject.file_id);
                    }
                    postNetwork(HOST_FOLDER_NEW, params, HOST_FOLDER_NEW);
                }
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        //builder.create().show();
        AlertDialog dialog = builder.create();
        dialog.show();
        dialogTitleLineColor(dialog);
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
                mData.add(defaultFolder);

                for (int i = 0; i < folders.length(); ++i) {
                    AttachmentFolderObject folder = new AttachmentFolderObject(folders.getJSONObject(i));
                    /*folder.setCount(fileCountMap.get(folder.file_id));
                    ArrayList<AttachmentFolderObject> subFolders = folder.sub_folders;
                    for (AttachmentFolderObject subFolder:subFolders){
                        subFolder.setCount(fileCountMap.get(subFolder.file_id));
                    }*/
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

    public static class ViewHolder {
        public ImageView icon;
        public TextView name;
        public CheckBox checkBox;
        public RelativeLayout more;
        View rootView;

        public View getRootView() {
            return rootView;
        }

        public static ViewHolder instance(View convertView, ViewGroup parent) {
            return instance(convertView, parent, null);
        }

        public static ViewHolder instance(View convertView, ViewGroup parent, CompoundButton.OnCheckedChangeListener onCheckedChange) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_attachment_list_item, parent, false);
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
                holder.checkBox.setOnCheckedChangeListener(onCheckedChange);
                holder.more = (RelativeLayout) convertView.findViewById(R.id.more);
                holder.rootView = convertView;
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            return holder;

        }
    }

}

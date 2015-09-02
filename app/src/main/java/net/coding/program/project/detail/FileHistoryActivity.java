package net.coding.program.project.detail;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.coding.program.R;
import net.coding.program.common.CustomDialog;
import net.coding.program.common.FileUtil;
import net.coding.program.model.AttachmentFileHistoryObject;
import net.coding.program.model.AttachmentFileObject;
import net.coding.program.model.PostRequest;
import net.coding.program.model.util.FileRequestHelp;
import net.coding.program.project.detail.file.FileDownloadBaseActivity;
import net.coding.program.project.detail.file.FileDynamicActivity;
import net.coding.program.project.detail.file.ViewHolderFile;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/*
    网络请求和list其实是绑定关系
    1.所有页面加下拉刷新
    2.按页号分页的（大部分）（上拉更多）
    3.按id号分页的（冒泡）（上拉更多）
    4.无分页的
    5.带 section 的

    fragment 和 activity

 */
//@EActivity(R.layout.activity_attachments)
@EActivity(R.layout.activity_file_history)
//@OptionsMenu(R.menu.menu_file_history)
public class FileHistoryActivity extends FileDownloadBaseActivity {

    final public static int FILE_DELETE_CODE = 11;
    private static final String TAG_FILE_HISTORY = "TAG_FILE_HISTORY";
    private static final String TAG_FILE_HISTORY_REMARK = "TAG_FILE_HISTORY_REMARK";
    private static final String TAG_FILE_HISTORY_DELETE = "TAG_FILE_HISTORY_DELETE";
    @Extra
    FileDynamicActivity.ProjectFileParam mProjectFileParam;
    @ViewById
    ListView listView;
    FileHistoryAdapter mAdapter;
    ArrayList<AttachmentFileHistoryObject> mData = new ArrayList<>();
    protected View.OnClickListener onMoreClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Integer position = (Integer) view.getTag();
            AttachmentFileObject file = mData.get(position);

            int selectedPosition = position;
            if (file.isDownload) {
                listViewItemClicked(position);
            } else {
                action_download_single(mData.get(selectedPosition));
            }
        }
    };
    protected View.OnClickListener cancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AttachmentFileObject data = mData.get((Integer) v.getTag());

            long downloadId = data.downloadId;
            removeDownloadFile(downloadId);
            data.downloadId = 0L;
            mAdapter.notifyDataSetChanged();
        }
    };
    FileRequestHelp mFileRequest;

    @AfterViews
    protected final void initFileHistoryActivity() {
        mFileRequest = new FileRequestHelp(mProjectFileParam.getProjectPath(),
                mProjectFileParam.getFileId());
        getNetwork(mFileRequest.getHttpRequest(), TAG_FILE_HISTORY);

        mAdapter = new FileHistoryAdapter(this, mData);
        listView.setAdapter(mAdapter);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_FILE_HISTORY)) {
            if (code == 0) {
                mData.addAll(mFileRequest.parseJson(respanse, getFileDownloadPath(), getProjectId()));
                mAdapter.notifyDataSetChanged();
            }
        } else if (tag.equals(TAG_FILE_HISTORY_REMARK)) {
            if (code == 0) {
                ((FileHistoryRemarkRequest) data).remarkItem();
                mAdapter.notifyDataSetChanged();
            }
        } else if (tag.equals(TAG_FILE_HISTORY_DELETE)) {
            if (code == 0) {
                mData.remove(data);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @ItemLongClick
    protected void listViewItemLongClicked(final int pos) {
        if (pos == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setItems(new String[]{"修改备注"}, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            showRemarkHistoryDialog(mData.get(pos));
                        }
                    });
            builder.show();
        } else {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            String[] itemTitles = {"修改备注"};
            if (mData.get(pos).owner.isMe()) {
                itemTitles = new String[]{"修改备注", "删除"};
            }
            builder
                    .setItems(itemTitles, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            AttachmentFileHistoryObject itemData = mData.get(pos);
                            if (which == 0) {
                                showRemarkHistoryDialog(itemData);
                            } else {
                                showDeleteHistoryDialog(itemData);
                            }
                        }
                    });
            builder.show();
        }
    }

    @Override
    public int getProjectId() {
        return mProjectFileParam.getProjectId();
    }

    @ItemClick
    protected void listViewItemClicked(int position) {
        AttachmentFileObject data = mData.get(position);

        if (data.isFolder) {
            AttachmentsActivity_.intent(this)
                    .mAttachmentFolderObject(data.folderObject)
                    .mProjectObjectId(mProjectFileParam.getProjectId())
                    .mProject(mProjectFileParam.getProject())
                    .startForResult(ProjectAttachmentFragment.RESULT_REQUEST_FILES);
        } else if (data.isImage()) {
            AttachmentsPicDetailActivity_.intent(this)
                    .mProjectObjectId(mProjectFileParam.getProjectId())
                    .mAttachmentFileObject(data)
                    .startForResult(FILE_DELETE_CODE);
        } else {
            AttachmentsDownloadDetailActivity_.intent(this)
                    .mProjectObjectId(mProjectFileParam.getProjectId())
                    .mHideHistoryLayout(true)
                    .mAttachmentFileObject(data)
                    .mProject(mProjectFileParam.getProject())
                    .startForResult(FILE_DELETE_CODE);
        }
    }

    @OnActivityResult(FILE_DELETE_CODE)
    void onFileResult(int resultCode, Intent data) {
        Log.d("onFileResult", resultCode + "");

        if (resultCode == Activity.RESULT_OK) {
            AttachmentFileObject deletedFileObject = (AttachmentFileObject) data.getSerializableExtra("mAttachmentFileObject");
            Log.d("onFileResult", resultCode + " " + deletedFileObject.getName());
            for (AttachmentFileObject file : mData) {
                if (file.file_id.equals(deletedFileObject.file_id)) {
                    mData.remove(file);
                    mAdapter.notifyDataSetChanged();
                    break;
                }
            }
            setResult(Activity.RESULT_OK);
        }
    }

    private void showDeleteHistoryDialog(final AttachmentFileHistoryObject item) {
        showDialog("删除", "确定删除该版本？删除后将不能恢复", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String url = mFileRequest.getHttpHistoryDelete(item.getHistory_id());
                deleteNetwork(url, TAG_FILE_HISTORY_DELETE, item);
            }
        });
    }

    private void showRemarkHistoryDialog(final AttachmentFileHistoryObject item) {
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.dialog_remark_file_history, null);
        final EditText edit2fa = (EditText) textEntryView.findViewById(R.id.edit);
        edit2fa.setText(item.getRemark());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog = builder
                .setTitle("修改备注")
                .setView(textEntryView)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String editStr1 = edit2fa.getText().toString().trim();
                        remarkFileHistory(editStr1, item);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).show();

        CustomDialog.dialogTitleLineColor(this, dialog);
    }

    private void remarkFileHistory(String editStr1, AttachmentFileHistoryObject item) {
        if (TextUtils.isEmpty(editStr1)) {
            Toast.makeText(FileHistoryActivity.this, "不能为空", Toast.LENGTH_LONG).show();
            return;
        }

        PostRequest request = mFileRequest.getHttpHistoryRemark(item.getHistory_id(), editStr1);
        postNetwork(request, TAG_FILE_HISTORY_REMARK, new FileHistoryRemarkRequest(item, editStr1));
    }

    private void deleteHistory(int pos) {

    }

    @Override
    public void checkFileDownloadStatus() {
        for (AttachmentFileObject fileObject : mData) {
            if (!fileObject.isFolder) {
                setDownloadStatus(fileObject);
                //Log.d("onResume", "update status:" + fileObject.name + " " + fileObject.isDownload);
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    private void setDownloadStatus(AttachmentFileObject mFileObject) {
        Long downloadId = getDownloadId(mFileObject);
        if (downloadId != 0L) {
            mFileObject.downloadId = downloadId;
            updateFileDownloadStatus(mFileObject);
            mFileObject.isDownload = false;
        } else {
            File mFile = FileUtil.getDestinationInExternalPublicDir(getFileDownloadPath(), mFileObject.getSaveName(mProjectFileParam.getProjectId()));
            if (mFile.exists() && mFile.isFile()) {
                mFileObject.isDownload = true;
            } else {
                mFileObject.downloadId = 0L;
                mFileObject.isDownload = false;
            }
        }
    }

    @Override
    public String getProjectPath() {
        return mProjectFileParam.getProjectPath();
    }

    static class ViewHolder {
        ImageView icon;
        TextView name;
        CheckBox checkBox;
        RelativeLayout more;
    }

    class FileHistoryAdapter extends ArrayAdapter<AttachmentFileHistoryObject> {
        public FileHistoryAdapter(Context context, List<AttachmentFileHistoryObject> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolderFile holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.project_attachment_file_history_list_item, parent, false);
                holder = new ViewHolderFile(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolderFile) convertView.getTag();
            }

            AttachmentFileHistoryObject data = mData.get(position);

            holder.icon_txt.setText(data.getVersionString());
            holder.name.setText(data.getRemark());

            holder.username.setText(data.owner.name);
            holder.desc.setText(data.getActionMsg());

            holder.downloadFlag.setText(data.isDownload ? "查看" : "下载");
            convertView.setBackgroundResource(data.isDownload
                    ? R.drawable.list_item_selector_project_file
                    : R.drawable.list_item_selector);

            holder.more.setTag(position);
            holder.more.setOnClickListener(onMoreClickListener);

            AttachmentFileObject mFileObject = data;
            if (mFileObject.bytesAndStatus != null) {
                Log.v("updateFileDownload", mFileObject.getName() + ":" + mFileObject.bytesAndStatus[0] + " " + mFileObject.bytesAndStatus[1] + " " + mFileObject.bytesAndStatus[2]);
            }

            if (data.downloadId != 0L) {
                holder.cancel.setTag(position);
                int status = data.bytesAndStatus[2];
                if (AttachmentsDownloadDetailActivity.isDownloading(status)) {
                    if (data.bytesAndStatus[1] < 0) {
                        holder.progressBar.setProgress(0);
                    } else {
                        holder.progressBar.setProgress(data.bytesAndStatus[0] * 100 / data.bytesAndStatus[1]);
                    }
                    data.isDownload = false;
                    holder.desc_layout.setVisibility(View.GONE);
                    holder.progress_layout.setVisibility(View.VISIBLE);
                } else {
                    if (status == DownloadManager.STATUS_FAILED) {
                        data.isDownload = false;
                    } else if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        data.isDownload = true;
                        downloadFileSuccess(data.file_id);
                    } else {
                        data.isDownload = false;
                    }

                    data.downloadId = 0L;

                    holder.desc_layout.setVisibility(View.VISIBLE);
                    holder.progress_layout.setVisibility(View.GONE);
                }
            } else {
                holder.desc_layout.setVisibility(View.VISIBLE);
                holder.progress_layout.setVisibility(View.GONE);
            }

            if (position == 0) {
                holder.icon_txt.setTextColor(0xffffffff);
                holder.icon_txt.setBackgroundResource(R.drawable.round_rect_file_history_orange);
            } else {
                holder.icon_txt.setTextColor(0xff808080);
                holder.icon_txt.setBackgroundResource(R.drawable.round_rect_file_history_gray);
            }

            holder.cancel.setOnClickListener(cancelClickListener);

            holder.more.setTag(position);
            holder.more.setOnClickListener(onMoreClickListener);
            holder.downloadFlag.setText(data.isDownload ? "查看" : "下载");
            return convertView;
        }
    }

    private class FileHistoryRemarkRequest {
        private AttachmentFileHistoryObject item;
        private String remark;

        public FileHistoryRemarkRequest(AttachmentFileHistoryObject item, String remark) {
            this.item = item;
            this.remark = remark;
        }

        public void remarkItem() {
            item.setRemark(remark);
        }
    }

}

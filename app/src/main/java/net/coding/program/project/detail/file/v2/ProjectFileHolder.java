package net.coding.program.project.detail.file.v2;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.network.model.file.CodingFile;

import java.util.Set;

/**
 * Created by chenchao on 2017/5/15.
 */
public class ProjectFileHolder extends UltimateRecyclerviewViewHolder {

    public ImageView icon;
    public TextView icon_txt;
    public TextView name;
    public TextView content;
    public TextView desc;
    public View shareMark;

    public LinearLayout file_info_layout;
    public TextView folder_name;

    public CheckBox checkBox;

    public RelativeLayout more;

    public TextView username;
    public View bottomLine;

    public RelativeLayout icon_layout;

    public LinearLayout desc_layout, progress_layout;
    public ProgressBar progressBar;
    public TextView cancel;
    public TextView downloadFlag;
    public View item_layout_root;

    public ProjectFileHolder(View convertView) {
        super(convertView);
        item_layout_root = convertView.findViewById(R.id.item_layout_root);
        name = (TextView) convertView.findViewById(R.id.name);
        icon = (ImageView) convertView.findViewById(R.id.icon);
        icon_txt = (TextView) convertView.findViewById(R.id.icon_txt);
        content = (TextView) convertView.findViewById(R.id.comment);
        desc = (TextView) convertView.findViewById(R.id.desc);
        checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);

        file_info_layout = (LinearLayout) convertView.findViewById(R.id.file_info_layout);
        folder_name = (TextView) convertView.findViewById(R.id.folder_name);

        more = (RelativeLayout) convertView.findViewById(R.id.more);
        downloadFlag = (TextView) convertView.findViewById(R.id.downloadFlag);

        username = (TextView) convertView.findViewById(R.id.username);
        bottomLine = convertView.findViewById(R.id.bottomLine);

        icon_layout = (RelativeLayout) convertView.findViewById(R.id.icon_layout);

        desc_layout = (LinearLayout) convertView.findViewById(R.id.desc_layout);
        progress_layout = (LinearLayout) convertView.findViewById(R.id.progress_layout);
        progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
        cancel = (TextView) convertView.findViewById(R.id.cancel);
        shareMark = convertView.findViewById(R.id.shareMark);
    }

    public void bind(CodingFile data, boolean isEditMode, Set<CodingFile> selectFiles) {
        name.setText(data.getName());

        if (data.isFolder()) {
            icon.setImageResource(R.drawable.ic_project_git_folder2);
            icon.setVisibility(View.VISIBLE);
            icon.setBackgroundResource(android.R.color.transparent);
            icon_txt.setVisibility(View.GONE);
            file_info_layout.setVisibility(View.GONE);
            folder_name.setText(data.getName());
            folder_name.setVisibility(View.VISIBLE);
        } else if (data.isImage()) {
            //Log.d("imagePattern", "data.preview:" + data.preview);
            ImageLoadTool.loadFileImage(icon, data.preview, ImageLoadTool.optionsRounded2);
            icon.setVisibility(View.VISIBLE);
            icon.setBackgroundResource(R.drawable.shape_image_icon_bg);
            icon_txt.setVisibility(View.GONE);
            file_info_layout.setVisibility(View.VISIBLE);
            folder_name.setVisibility(View.GONE);
        } else {
            ImageLoadTool.loadFileImage(icon, "drawable://" + data.getIconResourceId(), ImageLoadTool.optionsRounded2);
            icon.setVisibility(View.VISIBLE);
            icon.setBackgroundResource(android.R.color.transparent);
            icon_txt.setVisibility(View.GONE);
            file_info_layout.setVisibility(View.VISIBLE);
            folder_name.setVisibility(View.GONE);
        }

        content.setText(Global.HumanReadableFilesize(data.getSize()));
        desc.setText(String.format("发布于%s", Global.dayToNow(data.createdAt)));
        username.setText(data.owner.name);

        if (data.isShared()) {
            shareMark.setVisibility(View.VISIBLE);
        } else {
            shareMark.setVisibility(View.INVISIBLE);
        }

        checkBox.setTag(data);
        if (isEditMode) {
            checkBox.setVisibility(View.VISIBLE);

            if (selectFiles.contains(data)) {
                checkBox.setChecked(true);
            } else {
                checkBox.setChecked(false);
            }
            //((RelativeLayout.LayoutParams) bottomLine.getLayoutParams()).addRule(RelativeLayout.LEFT_OF, R.id.icon);
            ((RelativeLayout.LayoutParams) bottomLine.getLayoutParams()).leftMargin = Global.dpToPx(62);
        } else {
            checkBox.setVisibility(View.GONE);
            //((RelativeLayout.LayoutParams) bottomLine.getLayoutParams()).removeRule(RelativeLayout.LEFT_OF);
            ((RelativeLayout.LayoutParams) bottomLine.getLayoutParams()).leftMargin = Global.dpToPx(15);
        }

//        if (data.downloadId != 0L) {
//            cancel.setTag(position);
//            int status = data.bytesAndStatus[2];
//            if (AttachmentsDownloadDetailActivity.isDownloading(status)) {
//                if (data.bytesAndStatus[1] < 0) {
//                    progressBar.setProgress(0);
//                } else {
//                    progressBar.setProgress(data.bytesAndStatus[0] * 100 / data.bytesAndStatus[1]);
//                }
//                data.isDownload = false;
//                desc_layout.setVisibility(View.GONE);
//                content.setVisibility(View.GONE);
//                more.setVisibility(View.GONE);
//                progress_layout.setVisibility(View.VISIBLE);
//            } else {
//                if (status == DownloadManager.STATUS_FAILED) {
//                    data.isDownload = false;
//                } else if (status == DownloadManager.STATUS_SUCCESSFUL) {
//                    data.isDownload = true;
//                    downloadFileSuccess(data.file_id);
//                } else {
//                    data.isDownload = false;
//                }
//
//                data.downloadId = 0L;
//
//                desc_layout.setVisibility(View.VISIBLE);
//                content.setVisibility(View.VISIBLE);
//                more.setVisibility(View.VISIBLE);
//                progress_layout.setVisibility(View.GONE);
//            }
//        } else {
//            desc_layout.setVisibility(View.VISIBLE);
//            content.setVisibility(View.VISIBLE);
//            more.setVisibility(View.VISIBLE);
//            progress_layout.setVisibility(View.GONE);
//        }

//        cancel.setOnClickListener(cancelClickListener);

//        more.setTag(position);
//        more.setOnClickListener(onMoreClickListener);
//        downloadFlag.setText(data.isDownload ? "查看" : "下载");
//        item_layout_root.setBackgroundResource(data.isDownload
//                ? R.drawable.list_item_selector_project_file
//                : R.drawable.list_item_selector);

        if (data.isFolder()) {
            more.setVisibility(View.INVISIBLE);
        } else {
            more.setVisibility(View.VISIBLE);
        }
    }
}

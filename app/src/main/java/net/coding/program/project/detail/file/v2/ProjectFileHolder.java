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
    public TextView name;
    public TextView content;
    public TextView desc;
    public View shareMark;

    public LinearLayout file_info_layout;
    public TextView folder_name;

    public CheckBox checkBox;

    public RelativeLayout more;

    public TextView username;
//    public View bottomLine;

    public RelativeLayout icon_layout;

    public LinearLayout desc_layout;
    public View progressLayout;
    public ProgressBar progressBar;
    public TextView downloadFlag;
    public View item_layout_root;

    public ProjectFileHolder(View convertView) {
        super(convertView);
        item_layout_root = convertView.findViewById(R.id.item_layout_root);
        name = (TextView) convertView.findViewById(R.id.name);
        icon = (ImageView) convertView.findViewById(R.id.icon);
        content = (TextView) convertView.findViewById(R.id.comment);
        desc = (TextView) convertView.findViewById(R.id.desc);
        checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);

        file_info_layout = (LinearLayout) convertView.findViewById(R.id.file_info_layout);
        folder_name = (TextView) convertView.findViewById(R.id.folder_name);

        more = (RelativeLayout) convertView.findViewById(R.id.more);
        downloadFlag = (TextView) convertView.findViewById(R.id.downloadFlag);

        username = (TextView) convertView.findViewById(R.id.username);
//        bottomLine = convertView.findViewById(R.id.bottomLine);

        icon_layout = (RelativeLayout) convertView.findViewById(R.id.icon_layout);

        desc_layout = convertView.findViewById(R.id.desc_layout);
        progressLayout = convertView.findViewById(R.id.progress_layout);
        progressBar = convertView.findViewById(R.id.progressBar);
        shareMark = convertView.findViewById(R.id.shareMark);
    }

    public void bind(CodingFile data, boolean isEditMode, Set<CodingFile> selectFiles) {
        item_layout_root.setTag(data);
        name.setText(data.getName());

        if (data.isFolder()) {
            if (data.isShareFolder()) {
                icon.setImageResource(R.drawable.icon_file_folder_share);
                folder_name.setText(data.getName());
            } else {
                icon.setImageResource(R.drawable.ic_project_git_folder2);
                folder_name.setText(String.format("%s", data.getName()));
            }
            icon.setVisibility(View.VISIBLE);
            icon.setBackgroundResource(android.R.color.transparent);
            file_info_layout.setVisibility(View.GONE);
            folder_name.setVisibility(View.VISIBLE);
        } else if (data.isImage()) {
            //Log.d("imagePattern", "data.preview:" + data.preview);
            ImageLoadTool.loadFileImage(icon, data.preview, ImageLoadTool.optionsRounded2);
            icon.setVisibility(View.VISIBLE);
            icon.setBackgroundResource(R.drawable.shape_image_icon_bg);
            file_info_layout.setVisibility(View.VISIBLE);
            folder_name.setVisibility(View.GONE);
        } else {
            ImageLoadTool.loadFileImage(icon, "drawable://" + data.getIconResourceId(), ImageLoadTool.optionsRounded2);
            icon.setVisibility(View.VISIBLE);
            icon.setBackgroundResource(android.R.color.transparent);
            file_info_layout.setVisibility(View.VISIBLE);
            folder_name.setVisibility(View.GONE);
        }

        content.setText(Global.HumanReadableFilesize(data.getSize()));
        desc.setText(String.format("发布于%s", Global.dayToNow(data.createdAt)));

        if (data.owner != null && data.owner.name != null) {
            username.setText(data.owner.name);
        } else {
            username.setText("");
        }

        if (data.isShared()) {
            shareMark.setVisibility(View.VISIBLE);
        } else {
            shareMark.setVisibility(View.INVISIBLE);
        }

        checkBox.setTag(data);
        if (isEditMode) {
            if (data.isShareFolder()) {
                checkBox.setVisibility(View.GONE);
            } else {
                checkBox.setVisibility(View.VISIBLE);

                if (selectFiles.contains(data)) {
                    checkBox.setChecked(true);
                } else {
                    checkBox.setChecked(false);
                }
            }
        } else {
            checkBox.setVisibility(View.GONE);
        }

        if (data.isDownloading()) {
            progressBar.setProgress(data.downloadProgress);
        }

        more.setTag(data);

        if (data.isDownloaded()) {
            progressLayout.setVisibility(View.GONE);
            desc_layout.setVisibility(View.VISIBLE);
            downloadFlag.setText("查看");
        } else if (data.isDownloading()) {
            progressLayout.setVisibility(View.VISIBLE);
            desc_layout.setVisibility(View.GONE);
            downloadFlag.setText("取消");
        } else {
            progressLayout.setVisibility(View.GONE);
            desc_layout.setVisibility(View.VISIBLE);
            downloadFlag.setText("下载");
        }

        item_layout_root.setBackgroundResource(data.isDownloaded()
                ? R.drawable.list_item_selector_project_file
                : R.drawable.list_item_selector);

        if (data.isFolder()) {
            more.setVisibility(View.INVISIBLE);
        } else {
            more.setVisibility(View.VISIBLE);
        }
    }
}

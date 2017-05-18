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

    public LinearLayout desc_layout;
    public ProgressBar progressBar;
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
        progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
        shareMark = convertView.findViewById(R.id.shareMark);
    }

    public void bind(CodingFile data, boolean isEditMode, Set<CodingFile> selectFiles) {
        item_layout_root.setTag(data);
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
        } else {
            checkBox.setVisibility(View.GONE);
        }

        if (data.isDownloading()) {
            progressBar.setProgress(data.downloadProgress);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }

        more.setTag(data);

        if (data.isDownloaded()) {
            downloadFlag.setText("查看");
        } else if (data.isDownloading()) {
            downloadFlag.setText("取消");
        } else {
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

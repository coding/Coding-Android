package net.coding.program.common.ui.holder;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.coding.program.R;

/**
 * Created by chenchao on 15/8/21.
 * 项目文件 list item
 */
public class FileHistoryHolder {
    public ImageView icon;
    public TextView name;
    public TextView content;
    public TextView desc;
    public View shareMark;
    public TextView iconText;

    public LinearLayout file_info_layout;
    public TextView folder_name;

    public CheckBox checkBox;

    public RelativeLayout more;

    public TextView username;
    public View bottomLine;

    public RelativeLayout icon_layout;

    public LinearLayout desc_layout, progress_layout;
    public ProgressBar progressBar;
    public TextView downloadFlag;
    public View item_layout_root;

    public FileHistoryHolder(View convertView) {
        item_layout_root = convertView.findViewById(R.id.item_layout_root);
        name = (TextView) convertView.findViewById(R.id.name);
        icon = (ImageView) convertView.findViewById(R.id.icon);
        content = (TextView) convertView.findViewById(R.id.comment);
        desc = (TextView) convertView.findViewById(R.id.desc);
        checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
        iconText = (TextView) convertView.findViewById(R.id.icon_txt);

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
        shareMark = convertView.findViewById(R.id.shareMark);
    }

}

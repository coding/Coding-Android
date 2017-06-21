package net.coding.program.common.ui.holder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import net.coding.program.R;

/**
 * Created by chenchao on 2017/6/21.
 * 文件夹 holder
 */
public class FolderHolder {
    public ImageView icon;
    public TextView name;
    public CheckBox checkBox;
    public View bottomLine;
    private View rootView;

    public static FolderHolder instance(View convertView, ViewGroup parent) {
        return instance(convertView, parent, null);
    }

    public static FolderHolder instance(View convertView, ViewGroup parent, CompoundButton.OnCheckedChangeListener onCheckedChange) {
        FolderHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_file_list_item_folder, parent, false);
            holder = new FolderHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
            holder.checkBox.setOnCheckedChangeListener(onCheckedChange);
            holder.bottomLine = convertView.findViewById(R.id.bottomLine);
            holder.rootView = convertView;
            convertView.setTag(holder);
        } else {
            holder = (FolderHolder) convertView.getTag();
        }

        return holder;
    }

    public View getRootView() {
        return rootView;
    }
}

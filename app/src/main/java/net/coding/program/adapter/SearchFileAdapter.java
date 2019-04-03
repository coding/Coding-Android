package net.coding.program.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import net.coding.program.R;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.ViewHolder;
import net.coding.program.common.model.AttachmentFileObject;
import net.coding.program.search.HoloUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Vernon on 15/11/30.
 */
public class SearchFileAdapter extends BaseAdapter {
    private List<AttachmentFileObject> mData;
    private Context context;
    private String key;

    public SearchFileAdapter(List<AttachmentFileObject> mData, String key, Context context) {
        this.mData = mData;
        this.key = key;
        this.context = context;
    }

    /**
     * 返回byte的数据大小对应的文本
     */
    public static String getDataSize(long size) {
        DecimalFormat formater = new DecimalFormat("####.00");
        if (size < 1024) {
            return size + "bytes";
        } else if (size < 1024 * 1024) {
            float kbsize = size / 1024f;
            return formater.format(kbsize) + "KB";
        } else if (size < 1024 * 1024 * 1024) {
            float mbsize = size / 1024f / 1024f;
            return formater.format(mbsize) + "MB";
        } else if (size < 1024 * 1024 * 1024 * 1024) {
            float gbsize = size / 1024f / 1024f / 1024f;
            return formater.format(gbsize) + "GB";
        } else {
            return "size: error";
        }
    }

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
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.search_file_list, null);
        }
        TextView txtTitle = ViewHolder.get(convertView, R.id.txtTitle);
        ImageView fileImg = ViewHolder.get(convertView, R.id.fileImg);
        TextView txtFileSize = ViewHolder.get(convertView, R.id.txtFileSize);
        TextView txtContent = ViewHolder.get(convertView, R.id.txtContent);

        AttachmentFileObject bean = mData.get(position);

        HoloUtils.setHoloText(txtTitle, bean.getName());
        txtFileSize.setText(getDataSize(bean.getSize()));
        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
        txtContent.setText(bean.owner.name + " 创建于 " + format.format(bean.created_at));
        ImageLoader.getInstance().displayImage(bean.owner.avatar, fileImg, ImageLoadTool.optionsRounded2);

        return convertView;
    }

}

package net.coding.program.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import net.coding.program.R;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.ViewHolder;
import net.coding.program.common.model.MergeObject;
import net.coding.program.databinding.SearchMergeListBinding;
import net.coding.program.search.HoloUtils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchMergeAdapter extends BaseAdapter {
    private List<MergeObject> mData;
    private Context context;

    public SearchMergeAdapter(List<MergeObject> mData, String key, Context context) {
        this.mData = mData;
        this.context = context;
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
        SearchMergeListBinding binding;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            binding = SearchMergeListBinding.inflate(inflater, parent, false);
            convertView = binding.layoutRoot;
            convertView.setTag(binding);
        } else {
            binding = (SearchMergeListBinding) convertView.getTag();
        }
        binding.setData(mData.get(position));
        return convertView;
    }
}


package net.coding.program.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import net.coding.program.common.model.Merge;
import net.coding.program.databinding.SearchMergeListBinding;

import java.util.List;

public class SearchMergeAdapter extends BaseAdapter {
    private List<Merge> mData;
    private Context context;

    public SearchMergeAdapter(List<Merge> mData, String key, Context context) {
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


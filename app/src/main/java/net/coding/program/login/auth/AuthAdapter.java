package net.coding.program.login.auth;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.model.AccountInfo;

import java.util.ArrayList;

/**
 * Created by chenchao on 15/7/1.
 */
public class AuthAdapter extends ArrayAdapter<AuthInfo> {
    public AuthAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HolderAuthinfo holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.auth_list_item, parent, false);
            holder = new HolderAuthinfo();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.code = (TextView) convertView.findViewById(R.id.code);
            holder.time = (TextView) convertView.findViewById(R.id.time);
            holder.indicator = (CountdownIndicator) convertView.findViewById(R.id.indicator);

            convertView.setTag(holder);
        } else {
            holder = (HolderAuthinfo) convertView.getTag();
        }

        AuthInfo data = getItem(position);
        holder.name.setText(data.getUriString());
        holder.code.setText(data.getCode());

        return convertView;
    }

    public void saveData() {
        ArrayList<String> uris = new ArrayList<>();
        for (int i = 0; i < getCount(); ++i) {
            String uri = getItem(i).getUriString();
            uris.add(uri);
        }

        AccountInfo.saveAuthDatas(getContext(), uris);
    }

    static final class HolderAuthinfo {
        public TextView name;
        public TextView code;
        public TextView time;
        public CountdownIndicator indicator;
    }


}

package net.coding.program.task.add;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.htmltext.URLSpanNoUnderline;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.model.RefResourceObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

@EActivity(R.layout.activity_ref_resource)
public class RefResourceActivity extends BackActivity {

    @Extra
    ArrayList<RefResourceObject> mData;

    @Extra
    Param mParam;

    @ViewById
    ListView listView;

    @AfterViews
    final void initRefResourceActivity() {
        listView.setAdapter(adapter);
    }

    @ItemClick(R.id.listView)
    void itemClick(RefResourceObject item) {
        URLSpanNoUnderline.openActivityByUri(this, item.link, false);
    }

    @ItemLongClick(R.id.listView)
    void itemLongClick(RefResourceObject item) {
        new AlertDialog.Builder(this)
                .setItems(new String[]{"取消关联"},
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    deleteRef(item);
                                }
                            }
                        })
                .show();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("resultData", mData);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void deleteRef(RefResourceObject item) {
        String url = String.format("%s%s/resource_reference/%d?iid=%d", Global.HOST_API,
                mParam.projectPath, mParam.taskId, item.code);
        MyAsyncHttpClient.delete(this, url, new MyJsonResponse(this) {
            @Override
            public void onMySuccess(JSONObject response) {
                super.onMySuccess(response);
                mData.remove(item);
                adapter.notifyDataSetChanged();
            }
        });
    }

    BaseAdapter adapter = new BaseAdapter() {
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
            Holder holder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_ref_resource, parent, false);
                holder = new Holder();
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }

            RefResourceObject data = mData.get(position);
            int iconId;
            switch (data.target_type) {
                case "Task":
                    iconId = R.drawable.ic_task_ref_task;
                    break;
                case "MergeRequestBean":
                    iconId = R.drawable.ic_task_ref_mr;
                    break;
                case "ProjectTopic":
                    iconId = R.drawable.ic_task_ref_topic;
                    break;
                default: // ProjectFile
                    iconId = R.drawable.ic_task_ref_file;
                    break;
            }
            holder.icon.setImageResource(iconId);

            String format = "<font color=\"#3bbd79\">#%d</font>  %s";
            holder.title.setText(Html.fromHtml(String.format(format, data.code, data.title)));

            return convertView;
        }


        class Holder {
            ImageView icon;
            TextView title;
        }
    };

    public static class Param implements Serializable {
        String projectPath;
        int taskId;

        public Param(String projectPath, int taskId) {
            this.projectPath = projectPath;
            this.taskId = taskId;
        }
    }
}

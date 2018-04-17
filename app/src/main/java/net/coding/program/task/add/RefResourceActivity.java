package net.coding.program.task.add;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.model.RefResourceObject;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.route.URLSpanNoUnderline;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

import java.util.ArrayList;

@EActivity(R.layout.activity_ref_resource)
public class RefResourceActivity extends BackActivity {

    @Extra
    ArrayList<RefResourceObject> mData;

    @Extra
    TaskParam mParam;

    @ViewById
    ListView listView;

    @ViewById
    View blankLayout;

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

            holder.title.setText(Global.createGreenHtml("", "#" + data.code, data.title));

            return convertView;
        }


        class Holder {
            ImageView icon;
            TextView title;
        }
    };

    @AfterViews
    final void initRefResourceActivity() {
        listView.setAdapter(adapter);
        listView.setEmptyView(blankLayout);
    }

    @ItemClick(R.id.listView)
    void itemClick(RefResourceObject item) {
        URLSpanNoUnderline.openActivityByUri(this, item.link, false);
    }

    @ItemLongClick(R.id.listView)
    void itemLongClick(RefResourceObject item) {
        new AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                .setItems(new String[]{"取消关联"},
                        (dialog, which) -> {
                            if (which == 0) {
                                deleteRef(item);
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
        String url = String.format("%s%s/resource_reference/%s?iid=%s", Global.HOST_API,
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

}

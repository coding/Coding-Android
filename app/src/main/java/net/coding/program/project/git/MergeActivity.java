package net.coding.program.project.git;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import net.coding.program.common.ui.BackActivity;
import net.coding.program.R;
import net.coding.program.model.Merge;
import net.coding.program.model.ProjectObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsMenu;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@EActivity(R.layout.activity_merge)
@OptionsMenu(R.menu.menu_merge)
public class MergeActivity extends BackActivity {

    final String HOST_MERGE_OPEN = "HOST_MERGE_OPEN";
    final String HOST_MERGE_CLOSED = "HOST_MERGE_CLOSED";
    ArrayList<Merge> mData;
    @Extra
    ProjectObject projectObject;
    BaseAdapter baseAdapter = new BaseAdapter() {
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
//            ImageCommentHolder holder;
//            if (convertView == null) {
//                convertView = mInflater.inflate(R.layout.activity_task_comment_much_image, parent, false);
//                holder = new ImageCommentHolder(convertView, onClickComment, myImageGetter, getImageLoad(), mOnClickUser, onClickImage);
//                convertView.setTag(R.id.layout, holder);
//            } else {
//                holder = (ImageCommentHolder) convertView.getTag(R.id.layout);
//            }
//
//            TopicObject data = (TopicObject) getItem(position);
//            holder.setTaskCommentContent(data);

            return convertView;
        }
    };

    @AfterViews
    protected final void init() {
        String url = projectObject.getHttpMerge(true);
        getNetwork(url, HOST_MERGE_OPEN);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_MERGE_OPEN)) {
            if (code == 0) {
                if (isLoadingFirstPage(tag)) {
                    mData.clear();
                }

                JSONArray jsonArray = respanse.getJSONObject("data").getJSONArray("list");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    Merge merge = new Merge(jsonArray.getJSONObject(i));
                    mData.add(merge);
                }

            } else {
                showErrorMsg(code, respanse);
            }
            baseAdapter.notifyDataSetChanged();
            mFootUpdate.updateState(code, isLoadingLastPage(tag), mData.size());

        } else if (tag.equals(HOST_MERGE_CLOSED)) {

        }
    }

    public void updateFragment() {

    }
}

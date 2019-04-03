package net.coding.program.user;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.model.UserObject;
import net.coding.program.third.sidebar.IndexableListView;
import net.coding.program.third.sidebar.StringMatcher;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

@EActivity(R.layout.activity_pick_user)
public class PickUserActivity extends BaseEnterpriseUserListActivity {

    @ViewById
    IndexableListView listView;

    UserAdapter adapter = new UserAdapter();

    @AfterViews
    void initPickUserActivity() {
        adapter.initSection();
        listView.setAdapter(adapter);
        listView.setFastScrollEnabled(true);
        listView.setFastScrollAlwaysVisible(true);

        initListItemClick();
    }

    protected void initListItemClick() {
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent();
            UserObject user = (UserObject) parent.getItemAtPosition(position);
            intent.putExtra(UsersListActivity.RESULT_EXTRA_NAME, user.name);
            intent.putExtra(UsersListActivity.RESULT_EXTRA_USESR, user);
            setResult(Activity.RESULT_OK, intent);
            finish();
        });
    }

    @Override
    protected void searchItem(String s) {
        super.searchItem(s);

        adapter.notifyDataSetChanged();
    }

    @Override
    protected void parseUserJson(JSONObject respanse) {
        super.parseUserJson(respanse);
        adapter.notifyDataSetChanged();
    }

    class UserAdapter extends BaseAdapter implements SectionIndexer, StickyListHeadersAdapter {

        private String mSections = "ABCDEFGHIJKLMNOPQRSTUVWXYZ#";
        private ArrayList<String> mSectionTitle = new ArrayList<>();
        private ArrayList<Integer> mSectionId = new ArrayList<>();

        public void initSection() {
            mSectionTitle.clear();
            mSectionId.clear();

            if (allListData.size() > 0) {
                String lastLetter = "";

                for (int i = 0; i < allListData.size(); ++i) {
                    UserObject item = allListData.get(i);
                    if (!item.getFirstLetter().equals(lastLetter)) {
                        lastLetter = item.getFirstLetter();
                        mSectionTitle.add(item.getFirstLetter());
                        mSectionId.add(i);
                    }
                }
            }
        }

        @Override
        public int getCount() {
            return listData.size();
        }

        @Override
        public Object getItem(int position) {
            return listData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.activity_users_list_item, parent, false);
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.divideTitle = (TextView) convertView.findViewById(R.id.divideTitle);
                holder.divideLine = convertView.findViewById(R.id.divide_line);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final UserObject data = listData.get(position);

            if (isSection(position)) {
                holder.divideTitle.setVisibility(View.VISIBLE);
                holder.divideTitle.setText(data.getFirstLetter());
                holder.divideLine.setVisibility(View.GONE);
            } else {
                holder.divideTitle.setVisibility(View.GONE);
                holder.divideLine.setVisibility(View.VISIBLE);
            }

            holder.name.setText(data.name);
            iconfromNetwork(holder.icon, data.avatar);

            return convertView;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            initSection();
        }

        private boolean isSection(int pos) {
            if (getCount() == 0) {
                return true;
            }

            if (pos == 0) {
                return true;
            }

            String currentItem = allListData.get(pos).getFirstLetter();
            String preItem = allListData.get(pos - 1).getFirstLetter();
            return !currentItem.equals(preItem);
        }

        @Override
        public int getPositionForSection(int section) {
            // If there is no item for current section, previous section will be selected
            for (int i = section; i >= 0; i--) {
                for (int j = 0; j < getCount(); j++) {
                    if (i == 0) {
                        // For numeric section
                        for (int k = 0; k <= 9; k++) {
                            if (StringMatcher.match(((UserObject) getItem(j)).getFirstLetter().toUpperCase(), String.valueOf(k)))
                                return j;
                        }
                    } else {
                        if (StringMatcher.match(((UserObject) getItem(j)).getFirstLetter().toUpperCase(), String.valueOf(mSections.charAt(i))))
                            return j;
                    }
                }
            }
            return 0;
        }

        @Override
        public int getSectionForPosition(int position) {
            return 0;
        }

        @Override
        public Object[] getSections() {
            String[] sections = new String[mSections.length()];
            for (int i = 0; i < mSections.length(); i++)
                sections[i] = String.valueOf(mSections.charAt(i));
            return sections;
        }

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {
            HeaderViewHolder holder;
            if (convertView == null) {
                holder = new HeaderViewHolder();
                convertView = getLayoutInflater().inflate(R.layout.fragment_project_dynamic_list_head, parent, false);
                holder.mHead = (TextView) convertView.findViewById(R.id.head);
                convertView.setTag(holder);
            } else {
                holder = (HeaderViewHolder) convertView.getTag();
            }

            holder.mHead.setText(mSectionTitle.get(getSectionForPosition(position)));
            return convertView;
        }

        @Override
        public long getHeaderId(int i) {
            return getSectionForPosition(i);
        }

        class HeaderViewHolder {
            TextView mHead;
        }

    }


    static class ViewHolder {
        ImageView icon;
        TextView name;
        TextView divideTitle;
        View divideLine;
    }

}

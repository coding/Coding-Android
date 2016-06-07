package net.coding.program.login.phone;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.model.PhoneCountry;
import net.coding.program.third.country.sidebar.IndexableListView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;

import java.io.Serializable;
import java.util.ArrayList;

@EActivity(R.layout.activity_country_pick)
public class CountryPickActivity extends BackActivity {

    @ViewById
    IndexableListView listView;

    CountryAdapter countryAdapter;

    private ArrayList<PhoneCountry> countryDataSrc = new ArrayList<>();
    private ArrayList<PhoneCountry> countryDataTargetFull = new ArrayList<>();
    private ArrayList<PhoneCountry> countryDataTargetSearch = new ArrayList<>();
//    private Tess adapter;

    private final String[] topCountryCode = new String[]{
            "86",
            "852",
            "853",
            "886"
    };

//    private RecyclerView.LayoutManager manager;

    @AfterViews
    void initCountryPickActivity() {
        countryDataSrc = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(Global.readTextFile(this, "country"));
            for (int i = 0; i < jsonArray.length(); ++i) {
                countryDataSrc.add(new PhoneCountry(jsonArray.optJSONObject(i)));
            }
        } catch (Exception e) {
        }

//        initAllCountryRecyclerView();
        initListview();

    }

    private void initListview() {
        countryDataTargetFull = new ArrayList<>(countryDataSrc.size() + topCountryCode.length);
        for (String item : topCountryCode) {
            insertTop(countryDataTargetFull, item);
        }
        countryDataTargetFull.addAll(countryDataSrc);
        countryAdapter = new CountryAdapter(countryDataTargetFull, getLayoutInflater(), topCountryCode.length);
        listView.setAdapter(countryAdapter);
        listView.setFastScrollEnabled(true);
        listView.setFastScrollAlwaysVisible(true);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent();
            intent.putExtra("resultData", (Serializable) countryAdapter.getItem(position));
            setResult(RESULT_OK, intent);
            finish();
        });
    }

    private void insertTop(ArrayList<PhoneCountry> list, String countryCode) {
        for (PhoneCountry item : countryDataSrc) {
            if (item.country_code.equals(countryCode)) {
                list.add(item);
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_search, menu);


        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default


//        MenuItem menuItem = menu.findItem(R.id.action_search);
////        menuItem.expandActionView();
//        SearchView searchView = (SearchView) menuItem.getActionView();
//        searchView.onActionViewExpanded();
//        searchView.setIconified(false);
//        searchView.setQueryHint("");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                search(s);
                return true;
            }
        });

//        MenuItemCompat.setOnActionExpandListener(menuItem, new MenuItemCompat.OnActionExpandListener() {
//            @Override
//            public boolean onMenuItemActionExpand(MenuItem item) {
//                return true;
//            }
//
//            @Override
//            public boolean onMenuItemActionCollapse(MenuItem item) {
//                onBackPressed();
//                return false;
//            }
//        });

        return true;
    }

    private void search(String input) {
        if (input == null || input.replaceAll(" ", "").replaceAll("　", "").isEmpty()) {
            if (!countryAdapter.isUseData(countryDataTargetFull)) {
                countryAdapter.setData(countryDataTargetFull, topCountryCode.length);
            }

            return;
        }

        input = input.toLowerCase();
        countryDataTargetSearch.clear();
        for (PhoneCountry item : countryDataSrc) {
            if (item.country.toLowerCase().contains(input)) {
                countryDataTargetSearch.add(item);
            }
        }
        countryAdapter.setData(countryDataTargetSearch, 0);
    }

    private static class ViewHolder {
        TextView countryName;
        TextView countryPhone;
        View topLine;
        TextView title;
        View topSection;
    }

    class CountryAdapter extends BaseAdapter implements SectionIndexer {

        private final String mSections = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        private final String TOP_SECTION = "常用";
//        private ArrayList<String> mSectionTitle = new ArrayList<>();
//        private ArrayList<Integer> mSectionId = new ArrayList<>();

        private ArrayList<PhoneCountry> mData;
        private LayoutInflater mInflater;

        ArrayList<String> headData = new ArrayList<>();
        ArrayList<Integer> posData = new ArrayList<>();

        public CountryAdapter(ArrayList<PhoneCountry> data , LayoutInflater mInflater, int topItemCount) {
            this.mData = data;
            this.mInflater = mInflater;
            updateSection(topItemCount);
        }

//        public void initSection() {
//            mSectionTitle.clear();
////            mSectionId.clear();
//
//            if (mData.size() > 0) {
//                String lastLetter = "";
//
//                for (int i = 0; i < mData.size(); ++i) {
//                    PhoneCountry item = mData.get(i);
//                    if (!item.getFirstLetter().equals(lastLetter)) {
//                        lastLetter = item.getFirstLetter();
//                        mSectionTitle.add(item.getFirstLetter());
//                        mSectionId.add(i);
//                    }
//                }
//            }
//        }

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
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.country_list_item, parent, false);
                holder = new ViewHolder();
                holder.countryName = (TextView) convertView.findViewById(R.id.countryName);
                holder.countryPhone = (TextView) convertView.findViewById(R.id.countryPhone);
                holder.topLine = convertView.findViewById(R.id.topLine);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.topSection = convertView.findViewById(R.id.topSection);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            PhoneCountry data = mData.get(position);
            holder.countryName.setText(data.country);
            holder.countryPhone.setText("+" + data.country_code);
            if (isSection(position)) {
                holder.topLine.setVisibility(View.GONE);
                holder.topSection.setVisibility(View.VISIBLE);
                String title = postionToSectionTitle(position);
                holder.title.setText(title);
            } else {
                holder.topLine.setVisibility(View.VISIBLE);
                holder.topSection.setVisibility(View.GONE);
            }

            return convertView;
        }

        private boolean isSection(int pos) {
            if (getCount() == 0) {
                return true;
            }

            if (pos <= 0) {
                return true;
            }

            for (int sectionPos : posData) {
                if (sectionPos == pos) {
                    return true;
                } else if (sectionPos > pos) {
                    break;
                }
            }
            return false;
        }

        @Override
        public int getPositionForSection(int section) {
//            // If there is no item for current section, previous section will be selected
//            for (int i = section; i >= 0; i--) {
//                for (int j = 0; j < getCount(); j++) {
//                    if (i == 0) {
//                        // For numeric section
//                        for (int k = 0; k <= 9; k++) {
//                            if (StringMatcher.match(((PhoneCountry) getItem(j)).getFirstLetter().toUpperCase(), String.valueOf(k)))
//                                return j;
//                        }
//                    } else {
//                        if (StringMatcher.match(((PhoneCountry) getItem(j)).getFirstLetter().toUpperCase(), String.valueOf(mSections.charAt(i))))
//                            return j;
//                    }
//                }
//            }
//            return 0;
//            if (1 == 1) {
//                return 1;
//            }

            Log.d("", "dddddd " + section);
            String tagetLetter;
            if (section == 0) {
                tagetLetter = TOP_SECTION;
            } else if (section < mSections.length()) {
                tagetLetter = mSections.substring(section, section + 1);
            } else {
                tagetLetter = mSections.substring(mSections.length() - 1, mSections.length());
            }

            int pos = 0;
            for (int i = 0; i < headData.size(); ++i) {
                String sectionString = headData.get(i);
                if (sectionString.equals(tagetLetter)) {
                    pos = i;
                    return posData.get(pos);
                }
            }

//            return posData.get(pos);

            return -1;
//            return posData.get(section);
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

        public boolean isUseData(ArrayList<PhoneCountry> src) {
            return src == mData;
        }

        public void setData(ArrayList<PhoneCountry> src, int topCount) {
            mData = src;
            updateSection(topCount);
            notifyDataSetChanged();
        }

        public void updateSection(int topCount) {
            headData.clear();
            posData.clear();
            if (topCount > 0) {
                headData.add(TOP_SECTION);
                posData.add(0);
            }

            char last = ' ';
            for (int i = topCount; i < mData.size(); ++i) {
                PhoneCountry item = mData.get(i);
                char firstLetter = item.country.charAt(0);
                if (last != firstLetter) {
                    last = firstLetter;
                    headData.add(item.country.substring(0, 1));
                    posData.add(i);
                }
            }
        }

        private String postionToSectionTitle(int position) {
            if (posData.size() == 0) {
                return "";
            }

            for (int i = 0; i < posData.size(); ++i) {
                if (position < posData.get(i)) {
                    return headData.get(i - 1);
                }
            }
            return headData.get(headData.size() - 1);
        }
    }
}

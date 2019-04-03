package net.coding.program.maopao;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.event.EventRefrushMaopao;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.subject.SubjectWallActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.greenrobot.eventbus.EventBus;

@EFragment(R.layout.fragment_main_maopao)
public class MainMaopaoFragment extends BaseFragment {

    @ViewById
    Spinner toolbarMaopaoTitle;

    @ViewById
    Toolbar mainMaopaoToolbar;

    @StringArrayRes(R.array.maopao_action_types)
    String[] maopaoActionTypes;

    MaopaoTypeAdapter mSpinnerAdapter;
    long lastClick = 0;

    @AfterViews
    void initMainMaopaoFragment() {
//        mainMaopaoToolbar.inflateMenu(R.menu.menu_fragment_maopao);
//        mainMaopaoToolbar.setOnMenuItemClickListener(item -> {
//            if (item.getItemId() == R.id.action_search) {
//                action_search();
//            }
//
//            return true;
//        });

        mSpinnerAdapter = new MaopaoTypeAdapter(getActivity().getLayoutInflater(), maopaoActionTypes);
        toolbarMaopaoTitle.setAdapter(mSpinnerAdapter);
        toolbarMaopaoTitle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            String[] strings = getResources().getStringArray(R.array.maopao_action_types);

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Fragment fragment;
                Bundle bundle = new Bundle();
                mSpinnerAdapter.setCheckPos(position);

                switch (position) {
                    case 1:
                        fragment = new MaopaoListFragment_();
                        bundle.putSerializable("mType", MaopaoListFragment.Type.friends);
                        break;

                    case 2:
                        fragment = new MaopaoListFragment_();
                        bundle.putSerializable("mType", MaopaoListFragment.Type.hot);
                        break;

                    default: // case 0
                        fragment = new MaopaoListFragment_();
                        bundle.putSerializable("mType", MaopaoListFragment.Type.time);

                        break;
                }

                fragment.setArguments(bundle);

                getChildFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment, strings[position])
                        .commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Click
    void mainMaopaoToolbar() {
        long now = System.currentTimeMillis();
        if (now - lastClick < 500) {
            EventBus.getDefault().post(new EventRefrushMaopao());
            lastClick = 0;
        } else {
            lastClick = now;
        }
    }

    void action_search() {
        SubjectWallActivity_.intent(this).start();
    }

    private class MaopaoTypeAdapter extends BaseAdapter {

        final int spinnerIcons[] = new int[]{
                R.drawable.ic_spinner_maopao_time,
                R.drawable.ic_spinner_maopao_friend,
                R.drawable.ic_spinner_maopao_hot,
        };

        int checkPos = 0;
        private LayoutInflater inflater;
        private String[] project_activity_action_list;

        private MaopaoTypeAdapter(LayoutInflater inflater, String[] titles) {
            this.inflater = inflater;
            this.project_activity_action_list = titles;
        }

        private void setCheckPos(int pos) {
            checkPos = pos;
        }

        @Override
        public int getCount() {
            return spinnerIcons.length;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.spinner_layout_head, parent, false);
            }

            ((TextView) convertView).setText(project_activity_action_list[position]);

            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.spinner_layout_item, parent, false);
            }

            TextView title = (TextView) convertView.findViewById(R.id.title);
            title.setText(project_activity_action_list[position]);

            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
            icon.setImageResource(spinnerIcons[position]);

            if (checkPos == position) {
                convertView.setBackgroundColor(getResources().getColor(R.color.divide));
            } else {
                convertView.setBackgroundColor(getResources().getColor(R.color.transparent));
            }
            return convertView;
        }
    }

}

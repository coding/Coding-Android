package net.coding.program;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.android.tpush.service.XGPushService;

import net.coding.program.maopao.MaopaoListFragment_;
import net.coding.program.message.UsersListFragment_;
import net.coding.program.model.AccountInfo;
import net.coding.program.project.ProjectFragment_;
import net.coding.program.setting.SettingFragment_;
import net.coding.program.task.TaskFragment_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;

@EActivity(R.layout.activity_main)
public class MainActivity extends BaseFragmentActivity
        implements NavigationDrawerFragment_.NavigationDrawerCallbacks {

    NavigationDrawerFragment_ mNavigationDrawerFragment;
    CharSequence mTitle;

    @Extra
    String mPushUrl;

    @StringArrayRes
    String drawer_title[];

    @StringArrayRes
    String maopao_action_types[];

    public static final String BroadcastPushStyle = "BroadcastPushStyle";

    @ViewById
    ViewGroup drawer_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApp.setMainActivityState(true);

        XGPushConfig config = new XGPushConfig();
//        config.enableDebug(this, true);

        IntentFilter intentFilter = new IntentFilter(BroadcastPushStyle);
        registerReceiver(mUpdatePushReceiver, intentFilter);

        // qq push
        Context context = getApplicationContext();
        Intent service = new Intent(context, XGPushService.class);
        context.startService(service);

        updateNotifyService();

    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mUpdatePushReceiver);

        super.onDestroy();
        MyApp.setMainActivityState(false);
    }

    void updateNotifyService() {
        boolean needPush = AccountInfo.getNeedPush(this);

        if (needPush) {
            String globalKey = MyApp.sUserObject.global_key;
            XGPushManager.registerPush(this, globalKey);
        } else {
            XGPushManager.unregisterPush(this);
        }
    }

    BroadcastReceiver mUpdatePushReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateNotifyService();
        }
    };

    @AfterViews
    void init() {
        UpdateApp updateApp = new UpdateApp(this);
        updateApp.runInBackground();
        updateApp.deleteOldApk();

        mSpinnerAdapter = new MySpinnerAdapter(getLayoutInflater(), maopao_action_types);

        mOnNavigationListener = new ActionBar.OnNavigationListener() {

            String[] strings = getResources().getStringArray(R.array.maopao_action_types);

            @Override
            public boolean onNavigationItemSelected(int position, long itemId) {
                Fragment fragment;
                Bundle bundle = new Bundle();
                mSpinnerAdapter.setCheckPos(position);

                switch (position) {
                    case 1:
                        fragment = new MaopaoListFragment_();
                        bundle.putSerializable("mType", "friends");
                        break;

                    case 2:
                        fragment = new MaopaoListFragment_();
                        bundle.putSerializable("mType", "hot");
                        break;

                    case 3:
                        fragment = new MaopaoListFragment_();
                        bundle.putSerializable("mType", "my");
                        break;

                    case 0:
                    default:
                        fragment = new MaopaoListFragment_();
                        bundle.putSerializable("mType", "time");

                        break;
                }

                fragment.setArguments(bundle);

                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                Log.d("", ft == null ? "is null" : "is good");
                ft.replace(R.id.container, fragment, strings[position]);
                ft.commit();

                return true;
            }
        };

        mNavigationDrawerFragment = (NavigationDrawerFragment_)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        mTitle = drawer_title[0];

        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        restoreActionBar();

        if (mPushUrl != null) {
            Global.openActivityByUri(this, mPushUrl, false);
        }

    }

    int mSelectPos = 0;

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        mTitle = drawer_title[position];

        Fragment fragment = null;
        mSelectPos = position;

        boolean useCustomBar = false;
        switch (position) {
            case 0:
                fragment = new ProjectFragment_();
                break;
            case 1:
                fragment = new TaskFragment_();
                break;
            case 2:
                useCustomBar = true;
                initMaopaoActionbar();
                break;

            case 3:
                fragment = new UsersListFragment_();
                break;

            case 4:
                fragment = new SettingFragment_();
                break;
        }

        getActionBar().setDisplayShowCustomEnabled(useCustomBar);

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        }

    }

    MySpinnerAdapter mSpinnerAdapter;
    ActionBar.OnNavigationListener mOnNavigationListener;

    void initMaopaoActionbar() {
        ActionBar bar = getActionBar();

        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        bar.setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        if (mSelectPos != 2) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        }
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
        actionBar.setIcon(R.drawable.ic_lancher);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {

            if (mSelectPos == 2) {
                getActionBar().setIcon(R.drawable.ic_lancher);
                getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
                MenuInflater menuInflater = getMenuInflater();
//                menuInflater.inflate(R.menu.maopao_add, menu);
            } else {
                restoreActionBar();
            }
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    public void onProjectFragment() {
        startActivity(new Intent(this, LoginActivity_.class));
    }

    class MySpinnerAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private String[] project_activity_action_list;

        public MySpinnerAdapter(LayoutInflater inflater, String[] titles) {
            this.inflater = inflater;
            this.project_activity_action_list = titles;
        }

        int checkPos = 0;

        public void setCheckPos(int pos) {
            checkPos = pos;
        }

        final int spinnerIcons[] = new int[]{
                R.drawable.ic_spinner_maopao_time,
                R.drawable.ic_spinner_maopao_friend,
                R.drawable.ic_spinner_maopao_hot,
                R.drawable.ic_spinner_maopao_my,
        };

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
                convertView.setBackgroundColor(getResources().getColor(R.color.green));
            } else {
                convertView.setBackgroundColor(getResources().getColor(R.color.spinner_black));
            }


            return convertView;
        }
    }

//    class MySpinnerAdapter extends BaseAdapter {
//
//        private LayoutInflater inflater;
//        private String[] project_activity_action_list;
//
//        public MySpinnerAdapter(LayoutInflater inflater, String[] titles) {
//            this.inflater = inflater;
//            this.project_activity_action_list = titles;
//        }
//
//        int checkPos = 0;
//
//        public void setCheckPos(int pos) {
//            checkPos = pos;
//        }
//
//        @Override
//        public int getCount() {
//            return project_activity_action_list.length;
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return position;
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return position;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            if (convertView == null) {
//                convertView = inflater.inflate(R.layout.spinner_layout_head, parent, false);
//            }
//
//            ((TextView) convertView).setText(project_activity_action_list[position]);
//
//            return convertView;
//        }
//
//        @Override
//        public View getDropDownView(int position, View convertView, ViewGroup parent) {
//            if (convertView == null) {
//                convertView = inflater.inflate(R.layout.spinner_layout_item, parent, false);
//            }
//
//            TextView title = (TextView) convertView.findViewById(R.id.title);
//            title.setText(project_activity_action_list[position]);
//
//            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
//            icon.setVisibility(View.GONE);
//
//            if (checkPos == position) {
//                convertView.setBackgroundColor(getResources().getColor(R.color.green));
//            } else {
//                convertView.setBackgroundColor(getResources().getColor(R.color.spinner_black));
//            }
//
//            return convertView;
//        }
//    }

}

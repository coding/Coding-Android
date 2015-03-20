package net.coding.program;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.tencent.android.tpush.XGPushManager;
import com.tencent.android.tpush.service.XGPushService;

import net.coding.program.common.LoginBackground;
import net.coding.program.common.htmltext.URLSpanNoUnderline;
import net.coding.program.maopao.MaopaoListFragment;
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

import java.util.List;

@EActivity(R.layout.activity_main)
public class MainActivity extends BaseActivity
        implements NavigationDrawerFragment_.NavigationDrawerCallbacks {

    public static final String TAG = "MainActivity";

    NavigationDrawerFragment_ mNavigationDrawerFragment;
    String mTitle;

    @Extra
    String mPushUrl;

    @StringArrayRes
    String drawer_title[];

    @StringArrayRes
    String maopao_action_types[];

    public static final String BroadcastPushStyle = "BroadcastPushStyle";

    @ViewById
    ViewGroup drawer_layout;

    boolean mFirstEnter = true;
    private View actionbarCustom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApp.setMainActivityState(true);

        IntentFilter intentFilter = new IntentFilter(BroadcastPushStyle);
        registerReceiver(mUpdatePushReceiver, intentFilter);

//        XGPushConfig.enableDebug(this, true);
        // qq push
        updateNotifyService();
        pushInXiaomi();

        LoginBackground loginBackground = new LoginBackground(this);
        loginBackground.update();

        mFirstEnter = (savedInstanceState == null);

        if (savedInstanceState != null) {
            mSelectPos = savedInstanceState.getInt("pos", 0);
            mTitle = savedInstanceState.getString("mTitle");
        }

        if (mPushUrl != null) {
            URLSpanNoUnderline.openActivityByUri(this, mPushUrl, true);
            mPushUrl = null;
            getIntent().getExtras().remove("mPushUrl");
        }
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(mUpdatePushReceiver);

        super.onDestroy();
    }

    @Override
    public void finish() {
        MyApp.setMainActivityState(false);

        super.finish();
    }

    // 信鸽文档推荐调用，防止在小米手机上收不到推送
    private void pushInXiaomi() {
        Context context = getApplicationContext();
        Intent service = new Intent(context, XGPushService.class);
        context.startService(service);
    }

    private void updateNotifyService() {
        boolean needPush = AccountInfo.getNeedPush(this);

        if (needPush) {
            String globalKey = MyApp.sUserObject.global_key;
            XGPushManager.registerPush(this, globalKey);
        } else {
            XGPushManager.registerPush(this, "*");
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
        Intent intent = new Intent(this, UpdateService.class);
        intent.putExtra(UpdateService.EXTRA_BACKGROUND, true);
        intent.putExtra(UpdateService.EXTRA_WIFI, true);
        intent.putExtra(UpdateService.EXTRA_DEL_OLD_APK, true);
        startService(intent);

        mSpinnerAdapter = new MySpinnerAdapter(getLayoutInflater(), maopao_action_types);

        ActionBar supportActionBar = getSupportActionBar();
        supportActionBar.setCustomView(R.layout.actionbar_custom_spinner);
        actionbarCustom = supportActionBar.getCustomView();
        Spinner spinner = (Spinner) supportActionBar.getCustomView().findViewById(R.id.spinner);
        spinner.setAdapter(mSpinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

                    case 3:
                        fragment = new MaopaoListFragment_();
                        bundle.putSerializable("mType", MaopaoListFragment.Type.my);
                        break;

                    case 0:
                    default:
                        fragment = new MaopaoListFragment_();
                        bundle.putSerializable("mType", MaopaoListFragment.Type.time);

                        break;
                }

                fragment.setArguments(bundle);

                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                Log.d("", ft == null ? "is null" : "is good");
                ft.replace(R.id.container, fragment, strings[position]);
                ft.commit();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mNavigationDrawerFragment = (NavigationDrawerFragment_)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        mTitle = drawer_title[0];

        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        if (mFirstEnter) {
            onNavigationDrawerItemSelected(0);
        }
    }

    int mSelectPos = 0;

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        mSelectPos = position;
        Fragment fragment = null;

        switch (position) {
            case 0:
                fragment = new ProjectFragment_();
                break;
            case 1:
                fragment = new TaskFragment_();
                break;
            case 2:
                // 进入冒泡页面，单独处理
                break;

            case 3:
                fragment = new UsersListFragment_();
                break;

            case 4:
                fragment = new SettingFragment_();
                break;
        }

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        }

        if (position == 2) {
            ActionBar actionBar = getSupportActionBar();
            Spinner spinner;
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(actionbarCustom);
            spinner = (Spinner) actionbarCustom.findViewById(R.id.spinner);
            List<Fragment> fragments = getSupportFragmentManager().getFragments();

            boolean containFragment = false;
            for (Fragment item : fragments) {
                if (item instanceof MaopaoListFragment) {
                    containFragment = true;
                    break;
                }
            }

            if (!containFragment) {
                int pos = spinner.getSelectedItemPosition();
                spinner.getOnItemSelectedListener().onItemSelected(null, null, pos, pos);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("pos", mSelectPos);
//        outState.putSerializable("mPushOpened", mPushOpened);
        outState.putString("mTitle", mTitle);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mSelectPos = savedInstanceState.getInt("pos", 0);
        mTitle = savedInstanceState.getString("mTitle");
        restoreActionBar();
    }

    MySpinnerAdapter mSpinnerAdapter;

    public void restoreActionBar() {
        mTitle = drawer_title[mSelectPos];
        ActionBar actionBar = getSupportActionBar();
        if (mSelectPos != 2) {
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mTitle);
            actionBar.setIcon(R.drawable.ic_lancher);
        } else {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(actionbarCustom);
//             Spinner   spinner = (Spinner) actionbarCustom.findViewById(R.id.spinner);
//            spinner.setSelection(1);
//            spinner.setSelection(0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {

            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
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

    @Override
    public void onBackPressed() {
        exitApp();
    }

    private long exitTime = 0;

    private void exitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            showButtomToast("再按一次退出Coding");
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }
}

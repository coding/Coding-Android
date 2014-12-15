package net.coding.program;


import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.readystatesoftware.viewbadger.BadgeView;

import net.coding.program.common.ClickSmallImage;
import net.coding.program.common.Unread;
import net.coding.program.common.UnreadNotify;
import net.coding.program.common.network.BaseFragment;
import net.coding.program.maopao.MaopaoListFragment;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.UserObject;
import net.coding.program.user.UserDetailActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

@EFragment(R.layout.fragment_navigation_drawer)
public class NavigationDrawerFragment extends BaseFragment {

    private NavigationDrawerCallbacks mCallbacks;

    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;

    private View mFragmentContainerView;

    private boolean mFirstDisplay = true;

    @ViewById
    TextView name;

    @ViewById
    TextView sign;

    @ViewById
    TextView follows;

    @ViewById
    TextView fans;

    final int radioIds[] = {
            R.id.radio0,
            R.id.radio1,
            R.id.radio2,
            R.id.radio3,
            R.id.radio4
    };

    RadioButton radios[] = new RadioButton[radioIds.length];

    @ViewById
    CircleImageView circleIcon;

    BadgeView badgeProject;
    BadgeView badgeMessage;

    // 4.1系统bug，setHasOptionsMenu(true) 如果放在 onCreate 中会报错
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @AfterViews
    void init() {
        UserObject user = AccountInfo.loadAccount(getActivity());
        setControlContent(user);

        for (int i = 0; i < radioIds.length; ++i) {
            radios[i] = (RadioButton) getView().findViewById(radioIds[i]);
            radios[i].setOnClickListener(clickItem);
        }

        radios[0].performClick();
        selectItem(0);

        badgeProject = (BadgeView) getView().findViewById(R.id.badge0);
        badgeProject.setVisibility(View.INVISIBLE);
        badgeMessage = (BadgeView) getView().findViewById(R.id.badge3);
        badgeMessage.setVisibility(View.INVISIBLE);

        if (mFirstDisplay) {
            updateUserinfo();
        }
    }

    @Click
    void circleIcon(View v) {
        new ClickSmallImage(this).onClick(v);
        updateUserinfo();
    }

    View.OnClickListener clickItem = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            for (int i = 0; i < radios.length; ++i) {
                if (v.equals(radios[i])) {
                    selectItem(i);
                } else {
                    radios[i].setChecked(false);
                }
            }
        }
    };

    private void updateNotify() {
        Unread unread = ((MyApp) getActivity().getApplication()).sUnread;

        UnreadNotify.displayNotify(badgeProject, unread.getProject());
        UnreadNotify.displayNotify(badgeMessage, unread.getNotify());
    }

    private void setControlContent(UserObject user) {
        name.setText(user.name);
        sign.setText(user.slogan);
        follows.setText(String.valueOf(user.follows_count));
        fans.setText(String.valueOf(user.fans_count));
        ImageLoader.getInstance().displayImage(user.avatar, circleIcon);

        MaopaoListFragment.ClickImageParam param = new MaopaoListFragment.ClickImageParam(user.avatar);
        circleIcon.setTag(param);
    }

    public void updateUserinfo() {
        UserObject oldUser = AccountInfo.loadAccount(getActivity());
        getNetwork(String.format(HOST, oldUser.global_key), HOST);
    }

    String HOST = Global.HOST + "/api/user/key/%s";

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST)) {
            if (code == 0) {
                UserObject user = new UserObject(respanse.getJSONObject("data"));
                AccountInfo.saveAccount(getActivity(), user);
                MyApp.sUserObject = user;
                setControlContent(user);
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    public void showDrawer(boolean show) {
        if (show) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        } else {
            mDrawerLayout.closeDrawers();
        }
    }

    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(getActivity(),
                mDrawerLayout,
                R.drawable.ic_drawer,
                R.string.empty,
                R.string.empty) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                updateNotify();
                UnreadNotify.update(getActivity());

                if (!isAdded()) {
                    return;
                }

                getActivity().invalidateOptionsMenu();
            }
        };

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void selectItem(int position) {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
    }

    @Click
    void userInfo() {
        UserDetailActivity_
                .intent(this)
                .globalKey(MyApp.sUserObject.global_key)
                .startForResult(RESULT_REQUEST_USERINFO);
    }

    final int RESULT_REQUEST_USERINFO = 21;

    @OnActivityResult(RESULT_REQUEST_USERINFO)
    void onResultUserinfo() {
        updateUserinfo();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mDrawerLayout != null && isDrawerOpen()) {
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item)
                || super.onOptionsItemSelected(item);
    }

    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle("");
        actionBar.setIcon(R.drawable.logo);
    }

    public static interface NavigationDrawerCallbacks {
        void onNavigationDrawerItemSelected(int position);
    }
}

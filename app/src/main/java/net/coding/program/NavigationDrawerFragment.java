package net.coding.program;


import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.readystatesoftware.viewbadger.BadgeView;

import net.coding.program.common.ClickSmallImage;
import net.coding.program.common.Global;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.Unread;
import net.coding.program.common.UnreadNotify;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.maopao.MaopaoListFragment;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.UserObject;
import net.coding.program.setting.FeedbackActivity_;
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
public class NavigationDrawerFragment extends BaseFragment implements UnreadNotify.UnreadNotifyObserver {

    final int radioIds[] = {
            R.id.radio0,
            R.id.radio1,
            R.id.radio2,
            R.id.radio3,
            R.id.radio4
    };
    final int RESULT_REQUEST_USERINFO = 21;
    @ViewById
    TextView name;
    @ViewById
    TextView sign;
    @ViewById
    TextView follows;
    @ViewById
    TextView fans;
    RadioButton radios[] = new RadioButton[radioIds.length];
    @ViewById
    CircleImageView circleIcon;
    BadgeView badgeProject;
    BadgeView badgeMessage;
    String HOST = Global.HOST_API + "/user/key/%s";
    int mSelectMenuPos = 0;
    private NavigationDrawerCallbacks mCallbacks;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private View mFragmentContainerView;
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
    private boolean mFirstDisplay = true;

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

        radios[0].setChecked(true);

        badgeProject = (BadgeView) getView().findViewById(R.id.badge0);
        badgeProject.setVisibility(View.INVISIBLE);
        badgeMessage = (BadgeView) getView().findViewById(R.id.badge3);
        badgeMessage.setVisibility(View.INVISIBLE);

        if (mFirstDisplay) {
            updateUserinfo();
        }
    }

    @Click
    protected final void buttonFeedback(View view) {
        FeedbackActivity_.intent(getActivity()).start();
        mDrawerLayout.closeDrawer(mFragmentContainerView);
    }

    @Click
    void circleIcon(View v) {
        new ClickSmallImage(this).onClick(v);
        updateUserinfo();
    }

    private void updateNotify() {
        if (!isResumed()) {
            return;
        }

        Unread unread = ((MyApp) getActivity().getApplication()).sUnread;

        UnreadNotify.displayNotify(badgeProject, unread.getProject());
        UnreadNotify.displayNotify(badgeMessage, unread.getNotify());
    }

    private void setControlContent(UserObject user) {
        name.setText(user.name);
        sign.setText(user.slogan);
        follows.setText(String.valueOf(user.follows_count));
        fans.setText(String.valueOf(user.fans_count));
        ImageLoader.getInstance().displayImage(user.avatar, circleIcon, ImageLoadTool.options);

        MaopaoListFragment.ClickImageParam param = new MaopaoListFragment.ClickImageParam(user.avatar);
        circleIcon.setTag(param);
    }

    public void updateUserinfo() {
        UserObject oldUser = AccountInfo.loadAccount(getActivity());
        getNetwork(String.format(HOST, oldUser.global_key), HOST);
    }

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

        ActionBar actionBar = getActionBarActivity().getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(getActivity(),
                mDrawerLayout,
                R.string.empty,
                R.string.empty) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (mCallbacks != null) {
                    mCallbacks.onNavigationDrawerItemSelected(mSelectMenuPos);
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
            mSelectMenuPos = position;
        }

    }

    @Click
    void userInfo() {
        UserDetailActivity_
                .intent(this)
                .globalKey(MyApp.sUserObject.global_key)
                .startForResult(RESULT_REQUEST_USERINFO);
    }

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

        UnreadNotify.UnreadNotifySubject.getInstance().registerObserver(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;

        UnreadNotify.UnreadNotifySubject.getInstance().unregisterObserver(this);
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
        ActionBar actionBar = getActionBarActivity().getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowCustomEnabled(false);
    }

    @Override
    public void update() {
        updateNotify();
    }

    public interface NavigationDrawerCallbacks {
        void onNavigationDrawerItemSelected(int position);
    }
}

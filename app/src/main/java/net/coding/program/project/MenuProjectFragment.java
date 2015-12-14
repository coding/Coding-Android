package net.coding.program.project;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.event.EventPosition;
import net.coding.program.model.MenuCount;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import de.greenrobot.event.EventBus;

/**
 * Created by Vernon on 15/11/14.
 */
@EFragment(R.layout.fragment_menu_project)
public class MenuProjectFragment extends BaseFragment implements View.OnClickListener {
    private static final String URL_PROJECT_COUNT = Global.HOST_API + "/project_count";

    @ViewById(R.id.rb_all_project)
    RadioButton rb_all_project;
    @ViewById(R.id.rb_my_build)
    RadioButton rb_my_build;
    @ViewById(R.id.rb_join_project)
    RadioButton rb_join_project;
    @ViewById(R.id.rb_my_intrest)
    RadioButton rb_my_intrest;
    @ViewById(R.id.rb_my_collected)
    RadioButton rb_my_collected;
    @ViewById(R.id.ll_square)
    LinearLayout ll_square;

    public MenuProjectFragment() {
    }

    @AfterViews
    protected void init() {
        setHasOptionsMenu(true);
        rb_all_project.setOnClickListener(this);
        rb_join_project.setOnClickListener(this);
        rb_my_build.setOnClickListener(this);
        rb_my_collected.setOnClickListener(this);
        rb_my_intrest.setOnClickListener(this);
        ll_square.setOnClickListener(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        getNetwork(URL_PROJECT_COUNT, URL_PROJECT_COUNT);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rb_join_project://我参与的项目
                EventBus.getDefault().post(new EventPosition(1));
                break;
            case R.id.rb_my_intrest://我关注的
                EventBus.getDefault().post(new EventPosition(3));
                break;
            case R.id.rb_my_collected://我收藏的
                EventBus.getDefault().post(new EventPosition(4));
                break;
            case R.id.rb_all_project://全部项目
                EventBus.getDefault().post(new EventPosition(0));
                break;
            case R.id.rb_my_build://我创建的
                EventBus.getDefault().post(new EventPosition(2));
                break;
            case R.id.ll_square://项目广场
//                EventBus.getDefault().post(new EventPosition(6));
                ProjectSquareActivity_.intent(this).start();
                break;
        }

    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(URL_PROJECT_COUNT)) {
            if (code == 0) {
                JSONObject jsonObject = respanse.optJSONObject("data");
                MenuCount bean = new MenuCount(jsonObject);
                setData(bean);
            } else {
                showErrorMsg(code, respanse);
            }

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem menuItem = menu.findItem(R.id.action_filter);
        menuItem.setIcon(R.drawable.ic_filter_click);
    }

    private void setData(MenuCount bean) {
        rb_all_project.setText("全部项目 (" + bean.getAll() + ")");
        rb_my_intrest.setText("我关注的 (" + bean.getWatched() + ")");
        rb_my_build.setText("我创建的 (" + bean.getCreated() + ")");
        rb_my_collected.setText("我收藏的 (" + bean.getStared() + ")");
        rb_join_project.setText("我参与的 (" + bean.getJoined() + ")");

    }
}

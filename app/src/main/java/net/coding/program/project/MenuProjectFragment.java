package net.coding.program.project;

import android.widget.RadioButton;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.event.EventPosition;
import net.coding.program.common.model.MenuCount;
import net.coding.program.common.ui.BaseFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Vernon on 15/11/14.
 */
@EFragment(R.layout.fragment_menu_project)
public class MenuProjectFragment extends BaseFragment {

    public static final int POS_MY_CREATE = 2;
    private final String URL_PROJECT_COUNT = Global.HOST_API + "/project_count";
    @ViewById
    RadioButton rb_all_project;
    @ViewById
    RadioButton rb_my_build;
    @ViewById
    RadioButton rb_join_project;

    public MenuProjectFragment() {
    }

    @AfterViews
    protected void init() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        getNetwork(URL_PROJECT_COUNT, URL_PROJECT_COUNT);
    }

    @Click
    void rb_join_project() {//我参与的项目
        EventBus.getDefault().post(new EventPosition(1, "我参与的"));
    }

    @Click
    void rb_all_project() {//全部项目
        EventBus.getDefault().post(new EventPosition(0, "全部项目"));
    }

    @Click
    void rb_my_build() {//我创建的
        EventBus.getDefault().post(new EventPosition(POS_MY_CREATE, "我创建的"));
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

    private void setData(MenuCount bean) {
        rb_all_project.setText("全部项目 (" + bean.getAll() + ")");
        rb_my_build.setText("我创建的 (" + bean.getCreated() + ")");
        rb_join_project.setText("我参与的 (" + bean.getJoined() + ")");
    }
}

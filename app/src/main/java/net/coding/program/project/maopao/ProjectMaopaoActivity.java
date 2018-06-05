package net.coding.program.project.maopao;

import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.LoadMore;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.model.Maopao;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.maopao.MaopaoDetailActivity;
import net.coding.program.maopao.MaopaoDetailActivity_;
import net.coding.program.param.ProjectJumpParam;
import net.coding.program.route.BlankViewDisplay;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_project_maopao)
@OptionsMenu(R.menu.project_maopao)
public class ProjectMaopaoActivity extends BackActivity implements LoadMore {

    private static final int RESULT_ADD = 1;
    private static final int RESULT_EDIT = 2;

    private final String TAG_PROJECT = "TAG_PROJECT";

    protected List<Maopao.MaopaoObject> listData = new ArrayList<>();
    protected BaseAdapter projectMaopaoAdapter;
    @Extra
    ProjectObject projectObject;
    @Extra
    ProjectJumpParam jumpParam;
    @ViewById
    ListView listView;
    @ViewById
    View blankLayout;
    View.OnClickListener clickListItem = v -> {
        Object object = v.getTag();
        if (object instanceof Maopao.MaopaoObject) {
            Maopao.MaopaoObject maopao = (Maopao.MaopaoObject) object;
            MaopaoDetailActivity.ClickParam clickParam = new MaopaoDetailActivity.ClickParam(projectObject.owner_user_name,
                    projectObject.name, String.valueOf(maopao.id));
            MaopaoDetailActivity_.intent(ProjectMaopaoActivity.this)
                    .mClickParam(clickParam)
                    .startForResult(RESULT_EDIT);
        }
    };
    private String projectMaopaoUrl = "";
    private int lastId = Global.UPDATE_ALL_INT;
    View.OnClickListener onClickRetry = v -> onRefresh();
    View.OnClickListener clickDelete = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new AlertDialog.Builder(v.getContext(), R.style.MyAlertDialogStyle)
                    .setMessage("确定删除？")
                    .setPositiveButton("确定", (dialog, which) -> deleteMaopao(v))
                    .setNegativeButton("取消", null)
                    .show();
        }

        void deleteMaopao(View v) {
            Maopao.MaopaoObject maopao = (Maopao.MaopaoObject) v.getTag();

            String url = String.format(Global.HOST_API + "/project/%s/tweet/%s", projectObject.getId(), maopao.id);
            MyAsyncHttpClient.delete(ProjectMaopaoActivity.this, url, new MyJsonResponse(ProjectMaopaoActivity.this) {
                @Override
                public void onMySuccess(JSONObject response) {
                    super.onMySuccess(response);
                    listData.remove(maopao);
                    projectMaopaoAdapter.notifyDataSetChanged();

                    BlankViewDisplay.setBlank(listData.size(), this, true, blankLayout, onClickRetry);

                    showProgressBar(false);
                }

                @Override
                public void onMyFailure(JSONObject response) {
                    super.onMyFailure(response);
                    showProgressBar(false);
                }
            });

            showProgressBar(true);
        }
    };

    View.OnClickListener clickEdit = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Maopao.MaopaoObject maopao = (Maopao.MaopaoObject) v.getTag();
            ProjectMaopaoAddActivity_.intent(ProjectMaopaoActivity.this)
                    .projectObject(projectObject)
                    .maopao(maopao)
                    .startForResult(RESULT_EDIT);
        }
    };

    @AfterViews
    void initProjectMaopaoActivity() {
        if (projectObject != null) {
            initList();
        } else {
            String mProjectUrl = ProjectObject.getHttpProject(jumpParam.user, jumpParam.project);
            getNetwork(mProjectUrl, TAG_PROJECT);
        }
    }

    private void initList() {
        setActionBarTitle("项目公告");

        initAdapter();

        listView.setVisibility(View.INVISIBLE);
        initListItemClick();

        listView.setAdapter(projectMaopaoAdapter);

        onRefresh();
    }

    protected void initAdapter() {
        projectMaopaoAdapter = new ProjectMaopaoAdapter(listData, this, clickDelete, clickEdit, clickListItem, projectObject.isManagerLevel());
    }

    protected void initListItemClick() {
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Maopao.MaopaoObject maopao = listData.get((int) id);
            MaopaoDetailActivity.ClickParam clickParam = new MaopaoDetailActivity.ClickParam(projectObject.owner_user_name,
                    projectObject.name, String.valueOf(maopao.id));
            MaopaoDetailActivity_.intent(ProjectMaopaoActivity.this)
                    .mClickParam(clickParam)
                    .startForResult(RESULT_EDIT);
        });
    }

    @Override
    public void loadMore() {
        if (lastId == 0) {
            return;
        }

        projectMaopaoUrl = String.format(Global.HOST_API + "/project/%s/tweet?last_id=%d&withRaw=true", projectObject.getId(), lastId);
        getNetwork(projectMaopaoUrl, projectMaopaoUrl);
    }

    public void onRefresh() {
        if (projectObject != null) {
            onRefreshReal();
        } else {
            initProjectMaopaoActivity();
        }
    }

    private void onRefreshReal() {
        initSetting();
        lastId = Global.UPDATE_ALL_INT;
        loadMore();
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(projectMaopaoUrl)) {
            if (code == 0) {
                if (lastId == Global.UPDATE_ALL_INT) {
                    listData.clear();
                }

                JSONArray jsonArray = respanse.optJSONArray("data");
                if (jsonArray != null && jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        listData.add(new Maopao.MaopaoObject(jsonArray.optJSONObject(i)));
                    }

                    lastId = listData.get(listData.size() - 1).id;
                } else {
                    lastId = 0;
                }

                listView.setVisibility(View.VISIBLE);

                projectMaopaoAdapter.notifyDataSetChanged();

                BlankViewDisplay.setBlank(listData.size(), this, true, blankLayout, onClickRetry);
            } else {
                showErrorMsg(code, respanse);
                BlankViewDisplay.setBlank(listData.size(), this, false, blankLayout, onClickRetry);
            }

        } else if (tag.equals(TAG_PROJECT)) {
            if (code == 0) {
                projectObject = new ProjectObject(respanse.optJSONObject("data"));
                initProjectMaopaoActivity();
            } else {
                BlankViewDisplay.setBlank(listData.size(), this, code == 0, blankLayout, onClickRetry);
            }
        }
    }

    @OptionsItem
    void actionAdd() {
        ProjectMaopaoAddActivity_.intent(this).projectObject(projectObject).startForResult(RESULT_ADD);
    }

    @OnActivityResult(RESULT_ADD)
    void onResultAdd(int resultCode) {
        if (resultCode == RESULT_OK) {
            onRefresh();
        }
    }

    @OnActivityResult(RESULT_EDIT)
    void onResultEdit(int resultCode) {
        if (resultCode == RESULT_OK) {
            onRefresh();
        }
    }

}

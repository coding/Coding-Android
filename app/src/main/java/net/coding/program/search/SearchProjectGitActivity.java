package net.coding.program.search;

import org.androidannotations.annotations.EActivity;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.url.UrlCreate;
import net.coding.program.project.detail.ProjectGitFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_search_project_git)
public class SearchProjectGitActivity extends BackActivity implements TextWatcher {
    private static final String HOST_GIT_TREE_LIST = "HOST_GIT_TREE_LIST";

    @Extra
    String mProjectPath;

    @Extra
    String peek;

    @Extra
    String mVersion = ProjectGitFragment.MASTER;

    @ViewById
    ListView listView;

    private EditText editText;
    private View btnCancel;

    private TextView tvLength;

    private List<String> fileNames = new ArrayList<>();
    private List<String> searchNames = new ArrayList<>();

    BaseAdapter adapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return searchNames.size();
        }

        @Override
        public Object getItem(int position) {
            return searchNames.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.search_project_git_item, parent,false);
            }
            String name = searchNames.get(position);
            final TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
            tvName.setText(name);
            return convertView;
        }
    };

    @AfterViews
    void init(){
        View actionBar = getLayoutInflater().inflate(R.layout.activity_search_project_git_actionbar, null);
        getSupportActionBar().setCustomView(actionBar);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        btnCancel = actionBar.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> {
            editText.setText("");
            Global.popSoftkeyboard(this, editText, true);
        });

        editText = (EditText) actionBar.findViewById(R.id.editText);
        editText.addTextChangedListener(this);

        View hView = mInflater.inflate(R.layout.search_project_git_header, null);
        tvLength = (TextView) hView.findViewById(R.id.tv_length);
        listView.addHeaderView(hView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        initNetWork();
    }

    private void initNetWork() {
        String host_git_tree_list = UrlCreate.gitTreeList(mProjectPath, mVersion, peek);
        getNetwork(host_git_tree_list, HOST_GIT_TREE_LIST);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_GIT_TREE_LIST)) {
            if (code == 0) {
                fileNames.clear();
                JSONArray jsData = respanse.optJSONArray("data");
                for (int i = 0; i < jsData.length(); i++) {
                    String name = jsData.optString(i);
                    fileNames.add(name);
                }
            }else {
                showErrorMsg(code, respanse);
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        searchNames.clear();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (TextUtils.isEmpty(s)) {
            btnCancel.setVisibility(View.INVISIBLE);
        } else {
            btnCancel.setVisibility(View.VISIBLE);
            searchFile(s.toString());
        }
    }

    private void searchFile(String name) {
        for (String fileName : fileNames) {
            if (fileName.contains(name)) {
                searchNames.add(fileName);
            }
        }

        updateView(name);
    }

    private void updateView(String name) {
        tvLength.setText(name);
        adapter.notifyDataSetChanged();
    }
}

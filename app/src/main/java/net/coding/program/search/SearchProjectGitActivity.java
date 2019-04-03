package net.coding.program.search;

import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.model.GitFileInfoObject;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.url.UrlCreate;
import net.coding.program.common.util.StringUtil;
import net.coding.program.project.detail.GitViewActivity_;
import net.coding.program.project.detail.ProjectGitFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@EActivity(R.layout.activity_search_project_git)
public class SearchProjectGitActivity extends BackActivity implements TextWatcher {
    private static final String HOST_GIT_TREE_LIST = "HOST_GIT_TREE_LIST";

    @Extra
    String mProjectPath;

    @Extra
    String mVersion = ProjectGitFragment.MASTER;

    @ViewById
    ListView listView;

    private EditText editText;
    private View btnCancel;

    private TextView tvLength;

    private List<String> fileNames = new ArrayList<>();
    private List<String> searchNames = new ArrayList<>();
    private String inputName;

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
                convertView = mInflater.inflate(R.layout.search_project_git_item, parent, false);
            }

            final TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
            String name = searchNames.get(position);
            SpannableString s = new SpannableString(name);
            int color = getResources().getColor(R.color.color_FDF2CB);

            Pattern p = Pattern.compile(inputName, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(s);
            while (m.find()) {
                int start = m.start();
                int end = m.end();
                s.setSpan(new BackgroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            tvName.setText(s);

            return convertView;
        }
    };

    @AfterViews
    void init() {
        View actionBar = getLayoutInflater().inflate(R.layout.activity_search_project_git_actionbar, null);
        getSupportActionBar().setCustomView(actionBar);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        btnCancel = actionBar.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> {
            editText.setText("");
            Global.popSoftkeyboard(this, editText, true);
        });

        editText = (EditText) actionBar.findViewById(R.id.editText);
        editText.requestFocus();
        editText.addTextChangedListener(this);

        View hView = mInflater.inflate(R.layout.search_project_git_header, null);
        tvLength = (TextView) hView.findViewById(R.id.tv_length);
        listView.addHeaderView(hView);
        listView.setAdapter(adapter);
        listView.setHeaderDividersEnabled(false);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String path = ((String) parent.getAdapter().getItem(position));
                GitFileInfoObject selectedFile = new GitFileInfoObject(path);
                GitViewActivity_.intent(SearchProjectGitActivity.this).mProjectPath(mProjectPath)
                        .mVersion(mVersion).mGitFileInfoObject(selectedFile).start();
                finish();
            }
        });

        initNetWork();
    }

    private void initNetWork() {
        String host_git_tree_list = UrlCreate.gitTreeList(mProjectPath, mVersion);
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
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    private void searchFile(String name) {
        if (!TextUtils.isEmpty(name)) {
            tvLength.setVisibility(View.VISIBLE);
            for (String fileName : fileNames) {
                if (StringUtil.isExist(fileName, name)) {
                    searchNames.add(fileName);
                }
            }
            updateView(name);
        } else {
            tvLength.setVisibility(View.GONE);
            clearNames();
        }

    }

    private void updateView(String name) {
        String size = String.valueOf(searchNames.size());
        String headStr = this.getString(R.string.search_head, size, name);
        tvLength.setText(headStr);
        adapter.notifyDataSetChanged();
    }

    public void clearNames() {
        searchNames.clear();
        adapter.notifyDataSetChanged();
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
        inputName = s.toString();
        if (TextUtils.isEmpty(s)) {
            btnCancel.setVisibility(View.INVISIBLE);
        } else {
            btnCancel.setVisibility(View.VISIBLE);
        }
        searchFile(s.toString());
    }
}

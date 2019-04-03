package net.coding.program.project.detail.file.v2;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.adapter.SearchFileAdapter;
import net.coding.program.common.Global;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.ViewHolder;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.util.StringUtil;
import net.coding.program.network.model.file.CodingFile;
import net.coding.program.search.HoloUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


@EActivity(R.layout.activity_search_project_git)
public class SearchProjectFileActivity extends BackActivity implements TextWatcher {
    private static final String HOST_GIT_TREE_LIST = "HOST_GIT_TREE_LIST";

    public static List<CodingFile> sCodingFiles = null; // 有的目录下文件太多，使用 intent 传会超出容量

    public static void setsCodingFiles(List<CodingFile> sCodingFiles) {
        SearchProjectFileActivity.sCodingFiles = sCodingFiles;
    }

    @Extra
    ProjectObject project;

    @Extra
    CodingFile folder;

    @ViewById
    ListView listView;

    private EditText editText;
    private View btnCancel;

    private TextView tvLength;

    private List<CodingFile> fileNames = new ArrayList<>();
    private List<CodingFile> searchNames = new ArrayList<>();
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
                convertView = mInflater.inflate(R.layout.search_file_list, parent, false);
            }

            TextView txtTitle = ViewHolder.get(convertView, R.id.txtTitle);
            ImageView icon = ViewHolder.get(convertView, R.id.fileImg);
            TextView txtFileSize = ViewHolder.get(convertView, R.id.txtFileSize);
            TextView txtContent = ViewHolder.get(convertView, R.id.txtContent);

            CodingFile data = searchNames.get(position);

            HoloUtils.setHoloText(txtTitle, data.getName());
            txtTitle.setText(data.name);
            if (data.isFolder()) {
                txtFileSize.setVisibility(View.GONE);
            } else {
                txtFileSize.setVisibility(View.VISIBLE);
                txtFileSize.setText(SearchFileAdapter.getDataSize(data.getSize()));
            }
            final SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
            txtContent.setText(data.owner.name + " 创建于 " + format.format(data.createdAt));

            if (data.isFolder()) {
                if (data.isShareFolder()) {
                    icon.setImageResource(R.drawable.icon_file_folder_share);
                } else {
                    icon.setImageResource(R.drawable.ic_project_git_folder2);
                }
                icon.setBackgroundResource(android.R.color.transparent);
            } else if (data.isImage()) {
                ImageLoadTool.loadFileImage(icon, data.preview, ImageLoadTool.optionsRounded2);
                icon.setBackgroundResource(R.drawable.shape_image_icon_bg);
            } else {
                ImageLoadTool.loadFileImage(icon, "drawable://" + data.getIconResourceId(), ImageLoadTool.optionsRounded2);
                icon.setBackgroundResource(android.R.color.transparent);
            }

            return convertView;
        }
    };

    @AfterViews
    void initSearchProjectFileActivity() {
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
//                String path = ((String) parent.getAdapter().getItem(position));
//                GitFileInfoObject selectedFile = new GitFileInfoObject(path);
//                GitViewActivity_.intent(SearchProjectFileActivity.this).mProjectPath(mProjectPath)
//                        .mVersion(mVersion).mGitFileInfoObject(selectedFile).start();
//                finish();
                CodingFile codingFile = (CodingFile) parent.getAdapter().getItem(position);
                ProjectFileMainActivity.fileItemJump(codingFile, project, SearchProjectFileActivity.this);

            }
        });

        fileNames.addAll(sCodingFiles);
        sCodingFiles = null;
//        initNetWork();
    }

//    @Override
//    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
//        if (tag.equals(HOST_GIT_TREE_LIST)) {
//            if (code == 0) {
//                fileNames.clear();
//                JSONArray jsData = respanse.optJSONArray("data");
//                for (int i = 0; i < jsData.length(); i++) {
//                    String name = jsData.optString(i);
//                    fileNames.add(name);
//                }
//            } else {
//                showErrorMsg(code, respanse);
//            }
//        }
//    }

    private void searchFile(String name) {
        if (!TextUtils.isEmpty(name)) {
            tvLength.setVisibility(View.VISIBLE);
            for (CodingFile fileName : fileNames) {
                if (StringUtil.isExist(fileName.getName(), name)) {
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

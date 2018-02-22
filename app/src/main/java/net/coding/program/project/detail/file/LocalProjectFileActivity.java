package net.coding.program.project.detail.file;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.model.AttachmentFileObject;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.ui.holder.FolderHolder;
import net.coding.program.route.BlankViewDisplay;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@EActivity(R.layout.activity_notify_list2)
public class LocalProjectFileActivity extends BackActivity {

    private static final int RESULT_FILE_LIST = 1;

    @ViewById
    ListView listView;

    @ViewById
    View blankLayout;

    Map<String, ArrayList<File>> data = new HashMap<>();
    private LocalAdapter adapter;
    private String[] setStrings;

    public static ArrayList<File> getListFiles(Object obj) {
        File directory;
        if (obj instanceof File) {
            directory = (File) obj;
        } else {
            directory = new File(obj.toString());
        }
        ArrayList<File> files = new ArrayList<File>();
        if (directory.isFile()) {
            files.add(directory);
            return files;
        } else if (directory.isDirectory()) {
            File[] fileArr = directory.listFiles();
            for (int i = 0; i < fileArr.length; i++) {
                File fileOne = fileArr[i];
                files.addAll(getListFiles(fileOne));
            }
        }
        return files;
    }

    @AfterViews
    protected final void initLocalProjectFileActivity() {
        HashMap<Integer, String> idName = new HashMap<>();
        ArrayList<ProjectObject> projects = AccountInfo.loadProjects(this);
        for (ProjectObject item : projects) {
            idName.put(item.getId(), item.name);
        }

        String cachePath = FileSaveHelp.getFileDownloadAbsolutePath(this);
        List<File> allFiles = getListFiles(cachePath);
        for (File file : allFiles) {
            String fileName = file.getName();
            String[] fileInfo = fileName.split("\\|\\|\\|");
            if (fileInfo.length != AttachmentFileObject.INFO_COUNT) {
                continue;
            }

            int projectId = 0;
            try {
                projectId = Integer.parseInt(fileInfo[0]);
            } catch (Exception e) {
                Global.errorLog(e);
            }

            String name = idName.get(projectId);
            if (name == null) {
                continue;
            }

            ArrayList<File> singleProjectFiles;
            if ((singleProjectFiles = data.get(name)) == null) {
                singleProjectFiles = new ArrayList<>();
                data.put(name, singleProjectFiles);
            }
            singleProjectFiles.add(file);
        }

        listViewAddHeaderSection(listView);
        setStrings = createListData();
        adapter = new LocalAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            String item = (String) adapterView.getItemAtPosition(i);
            LocalFileListActivity_.intent(LocalProjectFileActivity.this)
                    .title(item)
                    .files(data.get(item))
                    .startForResult(RESULT_FILE_LIST);
        });

        BlankViewDisplay.setBlank(data.size(), this, true, blankLayout, null);

    }

    private String[] createListData() {
        Set<String> keySet = data.keySet();
        String[] temp = new String[keySet.size()];
        temp = keySet.toArray(temp);
        return temp;
    }

    @OnActivityResult(RESULT_FILE_LIST)
    void onResultFileList(int result, Intent intent) {
        String title = intent.getStringExtra(LocalFileListActivity.RESULT_INTENT_TITLE);
        ArrayList<File> files = (ArrayList<File>) intent.getSerializableExtra(LocalFileListActivity.RESULT_INTENT_FILES);
        if (files.isEmpty()) {
            data.remove(title);
        } else {
            data.put(title, files);
        }
        setStrings = createListData();
        adapter.notifyDataSetChanged();
    }

    class LocalAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return setStrings.length;
        }

        @Override
        public Object getItem(int position) {
            return setStrings[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FolderHolder holder = FolderHolder.instance(convertView, parent);

            String name = (String) getItem(position);
            int count = data.get(name).size();
            holder.name.setText(String.format("%s (%s)", name, count));
            return holder.getRootView();
        }
    }

}
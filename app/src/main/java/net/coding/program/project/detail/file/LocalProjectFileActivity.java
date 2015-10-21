package net.coding.program.project.detail.file;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.AttachmentFileObject;
import net.coding.program.model.ProjectObject;
import net.coding.program.project.detail.AttachmentsFolderSelectorActivity;

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

@EActivity(R.layout.activity_notify_list)
public class LocalProjectFileActivity extends BackActivity {

    private static final int RESULT_FILE_LIST = 1;

    @ViewById
    ListView listView;

    Map<String, ArrayList<File>> data = new HashMap<>();
    private LocalAdapter adapter;
    private String[] setStrings;

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

//        String ss = "";
//        for (String s : data.keySet()) {
//            ss += s;
//        }
//        Log.d("", ss);
        setStrings = createListData();
        adapter = new LocalAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = (String) adapterView.getItemAtPosition(i);
                LocalFileListActivity_.intent(LocalProjectFileActivity.this)
                        .title(item)
                        .files(data.get(item))
                        .startForResult(RESULT_FILE_LIST);
            }
        });
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
            AttachmentsFolderSelectorActivity.ViewHolder holder =
                    AttachmentsFolderSelectorActivity.ViewHolder.instance(convertView, parent);

            holder.checkBox.setVisibility(View.INVISIBLE);
            holder.more.setVisibility(View.INVISIBLE);
            String name = (String) getItem(position);
            int count = data.get(name).size();
            holder.name.setText(String.format("%s (%d)", name, count));
            return holder.getRootView();
        }
    }

}
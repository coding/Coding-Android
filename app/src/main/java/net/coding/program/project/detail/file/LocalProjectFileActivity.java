package net.coding.program.project.detail.file;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import net.coding.program.R;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.AttachmentFileObject;
import net.coding.program.model.ProjectObject;
import net.coding.program.project.detail.AttachmentsFolderSelectorActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@EActivity(R.layout.activity_notify_list)
public class LocalProjectFileActivity extends BackActivity {

    @ViewById
    ListView listView;

    Map<String, ArrayList<File>> data = new HashMap<>();

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

        String ss = "";
        for (String s : data.keySet()) {
            ss += s;
        }
        Log.d("", ss);


        Set<String> setStrings = data.keySet();
        listView.setAdapter(new LocalAdapter(this, 0, setStrings.toArray(new String[setStrings.size()])));
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

    class LocalAdapter extends ArrayAdapter<String> {

        public LocalAdapter(Context context, int resource, String[] objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AttachmentsFolderSelectorActivity.ViewHolder holder =
                    AttachmentsFolderSelectorActivity.ViewHolder.instance(convertView, parent);

            holder.checkBox.setVisibility(View.INVISIBLE);
            holder.more.setVisibility(View.INVISIBLE);
            String name = getItem(position);
            int count = data.get(name).size();
            holder.name.setText(String.format("%s (%d)", name, count));
            return holder.getRootView();
        }
    }

}
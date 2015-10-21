package net.coding.program.project.detail.file;

import android.content.Intent;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;

import net.coding.program.R;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.model.AttachmentFileObject;
import net.coding.program.model.AttachmentFolderObject;
import net.coding.program.project.detail.AttachmentsDownloadDetailActivity;
import net.coding.program.project.detail.AttachmentsFolderSelectorActivity;
import net.coding.program.project.detail.AttachmentsHtmlDetailActivity_;
import net.coding.program.project.detail.AttachmentsPhotoDetailActivity_;
import net.coding.program.project.detail.AttachmentsTextDetailActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

@EActivity(R.layout.activity_local_file_list)
@OptionsMenu(R.menu.activity_local_file_list)
public class LocalFileListActivity extends BackActivity {

    public static final String RESULT_INTENT_TITLE = "RESULT_INTENT_TITLE";
    public static final String RESULT_INTENT_FILES = "RESULT_INTENT_FILES";

    @Extra
    String title;
    @Extra
    ArrayList<File> files;

    @ViewById
    ListView listView;
    @ViewById
    View common_files_delete;

    private LocalAdapter adapter;

    boolean editMode = false;

    HashSet<Integer> pickItems = new HashSet<>();
    private ActionMode actionMode;

    @AfterViews
    protected void initLocalFileListActivity() {
        getSupportActionBar().setTitle(title);

        adapter = new LocalAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                File fileData = (File) adapterView.getItemAtPosition(i);
//                AttachmentFileObject data = createFileHelp(fileData);
//                if (data.isTxt()) {
//                    AttachmentsTextDetailActivity_
//                            .intent(LocalFileListActivity.this)
//                            .extraFile(fileData)
//                            .start();
//
//                } else if (data.isMd()) {
//                    AttachmentsHtmlDetailActivity_
//                            .intent(LocalFileListActivity.this)
//                            .extraFile(fileData)
//                            .start();
//
//
//                } else if (data.isImage()) {
//                    AttachmentsPhotoDetailActivity_
//                            .intent(LocalFileListActivity.this)
//                            .extraFile(fileData)
//                            .start();
//
//                } else {
////                    AttachmentsDownloadDetailActivity_.intent(AttachmentsActivity.this)
////                            .mProjectObjectId(mProjectObjectId)
////                            .mAttachmentFolderObject(mAttachmentFolderObject)
////                            .mAttachmentFileObject(data)
////                            .mProject(mProject)
////                            .startForResult(FILE_DELETE_CODE);
//
//
//                }

                AttachmentFileObject fileHelp = new AttachmentFileObject();
                String[] split = fileData.getName().split("\\|\\|\\|");
                String name = split[split.length - 1];
                fileHelp.setName(name);

                int pos = name.lastIndexOf(".");
                String fileSuffix;
                if (pos != -1 && pos != name.length() - 1) {
                    fileSuffix = name.substring(pos + 1, name.length());
                } else {
                    fileSuffix = name;
                }

                fileHelp.fileType = fileSuffix.toLowerCase();


                AttachmentFolderObject folder = new AttachmentFolderObject();
                folder.name = fileHelp.getName();

                AttachmentFileObject folderFile = new AttachmentFileObject();
                folderFile.file_id = split[1];
                folderFile.setName(folder.name);

                int projectId = Integer.valueOf(split[0]);

                String extension = folderFile.getName().toLowerCase();
                final String imageType = ".*\\.(gif|png|jpeg|jpg)$";
                final String htmlMdType = ".*\\.(html|htm|markd|markdown|md|mdown)$";
                final String txtType = ".*\\.(sh|txt)$";
                if (extension.matches(imageType)) {
                    AttachmentsPhotoDetailActivity_.intent(LocalFileListActivity.this)
                            .mProjectObjectId(projectId)
                            .mAttachmentFolderObject(folder)
                            .mAttachmentFileObject(folderFile)
                            .mExtraFile(fileData)
                            .start();

                } else if (extension.matches(htmlMdType)) {
                    AttachmentsHtmlDetailActivity_.intent(LocalFileListActivity.this)
                            .mProjectObjectId(projectId)
                            .mAttachmentFolderObject(folder)
                            .mAttachmentFileObject(folderFile)
                            .mExtraFile(fileData)
                            .start();

                } else if (extension.matches(txtType)) {
                    AttachmentsTextDetailActivity_.intent(LocalFileListActivity.this)
                            .mProjectObjectId(projectId)
                            .mExtraFile(fileData)
                            .mAttachmentFolderObject(folder)
                            .mAttachmentFileObject(folderFile)
                            .start();
                } else {
                    AttachmentsDownloadDetailActivity.openFile(LocalFileListActivity.this, fileData);
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(RESULT_INTENT_TITLE, title);
        intent.putExtra(RESULT_INTENT_FILES, files);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    @Click
    void common_files_delete() {
        ArrayList<File> removes = new ArrayList<>();
        for (Integer pos : pickItems) {
            try {
                File file = files.get(pos);
                if (file.delete()) {
                    removes.add(file);
                }
            } catch (Exception e) {
            }
        }
        pickItems.clear();
        files.removeAll(removes);
        adapter.notifyDataSetChanged();

        if (actionMode != null) {
            actionMode.finish();
        }

        showButtomToast("删除成功");

    }

    @OptionsItem
    void action_edit() {
        actionMode = startActionMode(mActionModeCallback);
    }

    class LocalAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return files.size();
        }

        @Override
        public Object getItem(int position) {
            return files.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        CompoundButton.OnCheckedChangeListener checkChange = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                int pos = (int) compoundButton.getTag();
                if (b) {
                    pickItems.add(pos);
                } else {
                    pickItems.remove(pos);
                }
            }
        };

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AttachmentsFolderSelectorActivity.ViewHolder holder =
                    AttachmentsFolderSelectorActivity.ViewHolder.instance(convertView, parent, checkChange);

            if (editMode) {
                holder.checkBox.setVisibility(View.VISIBLE);
            } else {
                holder.checkBox.setVisibility(View.GONE);
            }

            holder.checkBox.setTag(position);
            holder.checkBox.setChecked(pickItems.contains(position));

            holder.more.setVisibility(View.INVISIBLE);
            File data = (File) getItem(position);
            AttachmentFileObject fileHelp = createFileHelp(data);
            holder.name.setText(fileHelp.getName());

            if (fileHelp.isImage()) {
                getImageLoad().loadImage(holder.icon, "file://" + data.getAbsolutePath());
            } else {
                holder.icon.setImageResource(fileHelp.getIconResourceId());
            }

            return holder.getRootView();
        }
    }

    private AttachmentFileObject createFileHelp(File data) {
        AttachmentFileObject fileHelp = new AttachmentFileObject();
        String[] split = data.getName().split("\\|\\|\\|");
        String name = split[split.length - 1];
        fileHelp.setName(name);

        int pos = name.lastIndexOf(".");
        String fileSuffix;
        if (pos != -1 && pos != name.length() - 1) {
            fileSuffix = name.substring(pos + 1, name.length());
        } else {
            fileSuffix = name;
        }

        fileHelp.fileType = fileSuffix.toLowerCase();

        return fileHelp;
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.project_attachment_file_edit, menu);

            common_files_delete.setVisibility(View.VISIBLE);

            editMode = true;
            adapter.notifyDataSetChanged();

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;// Return false if nothing is done
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_all:
                    action_all();
                    return true;
                case R.id.action_inverse:
                    action_inverse();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            common_files_delete.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
    };

    private void action_all() {
        for (int i = 0; i < files.size(); ++i) {
            pickItems.add(i);
        }
        adapter.notifyDataSetChanged();
    }

    private void action_inverse() {
        HashSet<Integer> temp = new HashSet<>();
        for (int i = 0; i < files.size(); ++i) {
            if (!pickItems.contains(i)) {
                temp.add(i);
            }
        }
        pickItems = temp;
        adapter.notifyDataSetChanged();
    }
}

package net.coding.program.project.detail.file.v2;

import android.view.View;
import android.widget.CompoundButton;

import com.marshalchen.ultimaterecyclerview.quickAdapter.easyRegularAdapter;

import net.coding.program.R;
import net.coding.program.network.model.file.CodingFile;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Set;

/**
 * Created by chenchao on 2017/5/15.
 */
public class ProjectFileAdapter extends easyRegularAdapter<CodingFile, ProjectFileHolder> {

    private boolean editMode = false;
    private Set<CodingFile> selectFiles;
    private WeakReference<UpdateMenu> updateMenu;

    private CompoundButton.OnCheckedChangeListener checkListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            CodingFile codingFile = (CodingFile) buttonView.getTag();
            if (isChecked) {
                selectFiles.add(codingFile);
            } else {
                selectFiles.remove(codingFile);
            }
        }
    };

    private View.OnClickListener onMoreClickListener;
    private View.OnClickListener onClickListItem;
    private View.OnLongClickListener onLongClickListItem;

    public void setEditMode(boolean editMode) {
        if (this.editMode != editMode) {
            this.editMode = editMode;
            notifyDataSetChangedCustom();
        }
    }

    public void invert(CodingFile codingFile) {
        if (selectFiles.contains(codingFile)) {
            selectFiles.remove(codingFile);
        } else {
            selectFiles.add(codingFile);
        }
        notifyDataSetChangedCustom();
    }

    public void notifyDataSetChangedCustom() {
        UpdateMenu updateMenu = this.updateMenu.get();
        if (updateMenu != null) updateMenu.update();

        notifyDataSetChanged();
    }

    public boolean isEditMode() {
        return editMode;
    }

    public ProjectFileAdapter(List<CodingFile> list, Set<CodingFile> selectFiles, UpdateMenu updateMenu) {
        super(list);
        this.selectFiles = selectFiles;
        this.updateMenu = new WeakReference<>(updateMenu);
    }

    public ProjectFileAdapter setClickMore(View.OnClickListener click) {
        onMoreClickListener = click;
        return this;
    }

    public ProjectFileAdapter setOnClickListItem(View.OnClickListener onClickListItem) {
        this.onClickListItem = onClickListItem;
        return this;
    }

    public ProjectFileAdapter setOnLongClickListItem(View.OnLongClickListener onLongClickListItem) {
        this.onLongClickListItem = onLongClickListItem;
        return this;
    }

    @Override
    protected int getNormalLayoutResId() {
        return R.layout.project_file_list_item;
    }

    @Override
    protected ProjectFileHolder newViewHolder(View view) {
        ProjectFileHolder holder = new ProjectFileHolder(view);
        holder.checkBox.setOnCheckedChangeListener(checkListener);
        holder.more.setOnClickListener(onMoreClickListener);
        holder.item_layout_root.setOnClickListener(onClickListItem);
        holder.item_layout_root.setOnLongClickListener(onLongClickListItem);
        return holder;
    }

    @Override
    protected void withBindHolder(ProjectFileHolder holder, CodingFile data, int position) {
        holder.bind(data, editMode, selectFiles);
    }

    //    @Override
//    public ProjectFileHolder newFooterHolder(View view) {
//        return new ProjectFileHolder(view);
//    }
//
    @Override
    public ProjectFileHolder newHeaderHolder(View view) {
        return new ProjectFileHolder(view);
    }

//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        return super.onCreateViewHolder(parent, viewType);
//    }
//
//    @Override
//    public UltimateRecyclerviewViewHolder onCreateViewHolder(ViewGroup parent) {
//        return super.onCreateViewHolder(parent);
//    }

    interface UpdateMenu {
        void update();
    }
}

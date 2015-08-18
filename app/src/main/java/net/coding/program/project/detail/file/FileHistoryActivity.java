package net.coding.program.project.detail.file;

import net.coding.program.BackActivity;
import net.coding.program.R;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsMenu;

@EActivity(R.layout.activity_file_history)
@OptionsMenu(R.menu.menu_file_history)
public class FileHistoryActivity extends BackActivity {

    @Extra
    FileDynamicActivity.ProjectFileParam mProjectFileParam;


}

package net.coding.program.user.team;

import android.widget.ImageView;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.model.team.TeamListObject;
import net.coding.program.common.ui.BackActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_team_list_detail)
public class TeamListDetailActivity extends BackActivity {

    @Extra
    TeamListObject team;

    @AfterViews
    void initTeamListDetailActivity() {
    }

    void setHeader(@ViewById ImageView icon, @ViewById TextView title, @ViewById TextView content) {
        imagefromNetwork(icon, team.avatar, ImageLoadTool.optionsRounded2);
        title.setText(team.name);
        content.setText(team.introduction);
    }

    @Click
    void teamProject() {
        TeamProjectListActivity_.intent(this).teamListObject(team).start();
    }

    @Click
    void teamMember() {
        TeamMemberListActivity_.intent(this).teamListObject(team).start();
    }
}

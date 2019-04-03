package net.coding.program.user.team;

import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.model.team.TeamListObject;
import net.coding.program.common.model.team.TeamMember;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.compatible.CodingCompat;
import net.coding.program.network.constant.MemberAuthority;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@EActivity(R.layout.team_member_list)
public class TeamMemberListActivity extends BackActivity {

    private static final String TAG_MEMBERS = "TAG_MEMBERS";

    @Extra
    TeamListObject teamListObject;

    @ViewById
    ListView listView;

    ArrayList<TeamMember> listData = new ArrayList<>();
    SimpleAdapter listAdapter = new SimpleAdapter<TeamMember, ViewHolder>(listData) {
        @Override
        public void bindData(ViewHolder holder, TeamMember data, int postion) {
            MemberAuthority type = MemberAuthority.idToEnum(data.role);
            int iconRes = type.getIcon();
            if (iconRes == 0) {
                holder.ic.setVisibility(View.INVISIBLE);
            } else {
                holder.ic.setVisibility(View.VISIBLE);
                holder.ic.setImageResource(iconRes);
            }

            if (!data.alias.isEmpty()) {
                holder.alias.setText(data.alias);
                holder.alias.setVisibility(View.VISIBLE);
            } else {
                holder.alias.setVisibility(View.GONE);
            }

            holder.name.setText(data.user.name);
            iconfromNetwork(holder.icon, data.user.avatar);

            holder.bottomLine.setVisibility(postion >= getCount() - 1 ? View.INVISIBLE : View.VISIBLE);
        }

        @Override
        public int getItemlayoutId() {
            return R.layout.team_member_list_item;
        }

        @Override
        public ViewHolder createViewHolder(View v) {
            return new ViewHolder(v);
        }
    };

    @AfterViews
    void initTeamMemberListAcitvity() {
        listViewAddHeaderSection(listView);
        listViewAddFootSection(listView);
        listView.setVisibility(View.INVISIBLE);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(((parent, view, position, id) -> {
            TeamMember teamMember = (TeamMember) listAdapter.getItem((int) id);
            CodingCompat.instance().launchUserDetailActivity(this, teamMember.user.global_key);
        }));

        final String url = String.format("%s/team/%s/members", Global.HOST_API, teamListObject.globalkey);
        getNetwork(url, TAG_MEMBERS);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (TAG_MEMBERS.equals(tag)) {
            if (code == 0) {
                JSONArray jsonArray = respanse.optJSONArray("data");
                listData.clear();
                for (int i = 0; i < jsonArray.length(); ++i) {
                    listData.add(new TeamMember(jsonArray.optJSONObject(i)));
                }
                listAdapter.notifyDataSetChanged();

                if (listData.size() > 0) {
                    listView.setVisibility(View.VISIBLE);
                }

            } else {
                showErrorMsg(code, respanse);
            }
        } else {
            super.parseJson(code, respanse, tag, pos, data);
        }
    }

    static class ViewHolder {
        ImageView icon;
        TextView name;
        TextView alias;
        ImageView ic;
        View bottomLine;

        ViewHolder(View v) {
            name = (TextView) v.findViewById(R.id.name);
            alias = (TextView) v.findViewById(R.id.alias);
            ic = (ImageView) v.findViewById(R.id.ic);
            icon = (ImageView) v.findViewById(R.id.icon);
            bottomLine = v.findViewById(R.id.bottomLine);
        }
    }

}

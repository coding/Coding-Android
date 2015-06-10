package net.coding.program.project.git;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.comment.BaseCommentParam;
import net.coding.program.model.Commit;
import net.coding.program.project.detail.ProjectDynamicFragment;
import net.coding.program.project.detail.merge.SimpleData1Adaper;

import java.util.ArrayList;
import java.util.Calendar;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by chenchao on 15/5/29.
 *
 */
public class CommitsAdapter extends SimpleData1Adaper<Commit> implements StickyListHeadersAdapter, SectionIndexer {

    String sToday = "";
    String sYesterday = "";
    private ArrayList<Long> mSectionTitle = new ArrayList<>();
    private ArrayList<Integer> mSectionId = new ArrayList<>();
    public CommitsAdapter(BaseCommentParam param) {
        super(param);

        Calendar calendar = Calendar.getInstance();
        Long today = calendar.getTimeInMillis();
        sToday = Global.mDateFormat.format(today);
        Long yesterday = calendar.getTimeInMillis() - 1000 * 60 * 60 * 24;
        sYesterday = Global.mDateFormat.format(yesterday);
    }

    @Override
    public Object[] getSections() {
        return mSectionTitle.toArray();
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        return sectionIndex;
    }

    @Override
    public int getSectionForPosition(int position) {
        for (int i = 0; i < mSectionId.size(); ++i) {
            if (position < mSectionId.get(i)) {
                return i - 1;
            }
        }

        return mSectionId.size() - 1;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.commit_list_section, parent, false);
            holder.mHead = (TextView) convertView.findViewById(R.id.sectionTitle);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        Long time = mSectionTitle.get(getSectionForPosition(position));
        String s = Global.mDateFormat.format(time);
        if (s.equals(sToday)) {
            s += " (今天)";
        } else if (s.equals(sYesterday)) {
            s += " (昨天)";
        }

        holder.mHead.setText(s);

        return convertView;
    }

    @Override
    public long getHeaderId(int i) {
        return getSectionForPosition(i);
    }

    public void initSection() {
        mSectionTitle.clear();
        mSectionId.clear();

        if (getCount() > 0) {
            mSectionId.add(0);
            Calendar lastTime = Calendar.getInstance();
            lastTime.setTimeInMillis(((Commit) getItem(0)).getCommitTime());
            Calendar nowTime = Calendar.getInstance();
            mSectionTitle.add(lastTime.getTimeInMillis());

            for (int i = 0; i < getCount(); ++i) {
                nowTime.setTimeInMillis(((Commit) getItem(i)).getCommitTime());
                if (ProjectDynamicFragment.isDifferentDay(lastTime, nowTime)) {
                    lastTime.setTimeInMillis(nowTime.getTimeInMillis());
                    mSectionTitle.add(lastTime.getTimeInMillis());
                    mSectionId.add(i);
                }
            }
        }
    }

    @Override
    public void resetData(ArrayList data) {
        super.resetData((ArrayList<Object>) data);
        initSection();
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        initSection();
        super.notifyDataSetChanged();
    }

    class HeaderViewHolder {
        TextView mHead;
    }
}

package net.coding.program.project.detail.wiki;


import android.view.View;

import net.coding.program.R;
import net.coding.program.project.detail.TopicEditFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

@EFragment(R.layout.fragment_topic_edit)
public class WikiEditFragment extends TopicEditFragment {

    @AfterViews
    void initWikiEditFragment() {
        labelBar.setVisibility(View.GONE);
    }
}

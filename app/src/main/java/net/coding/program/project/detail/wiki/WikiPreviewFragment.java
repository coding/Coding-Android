package net.coding.program.project.detail.wiki;


import android.support.annotation.NonNull;
import android.view.View;

import net.coding.program.R;
import net.coding.program.project.detail.TopicPreviewFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

@EFragment(R.layout.fragment_topic_preview)
public class WikiPreviewFragment extends TopicPreviewFragment {

    @AfterViews
    void initWikiPreviewFragment() {
        labelBar.setVisibility(View.GONE);
    }

    @NonNull
    @Override
    protected String getWebViewTempate() {
        return "wiki.html";
    }
}

package net.coding.program.subject;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.coding.program.common.ui.BackActivity;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.model.Subject;
import net.coding.program.subject.adapter.SubjectLastListAdapter;
import net.coding.program.subject.service.ISubjectRecommendObject;
import net.coding.program.subject.util.TopicLastCache;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by david on 15-7-24.
 */
@EActivity(R.layout.activity_subject_create)
public class SubjectNewActivity extends BackActivity {

    final String subjectHotTweetUrl = Global.HOST_API + "/tweet_topic/hot?page=1&pageSize=20";
    final String subjectHotTweetTag = "subject_hot";

    @ViewById
    View emptyView;
    @ViewById
    StickyListHeadersListView listView;

    @ViewById(R.id.topic_create_layout)
    LinearLayout createLayout;
    @ViewById(R.id.topic_create_name)
    TextView topicCreateName;
    @ViewById(R.id.topic_create_btn)
    TextView topicCreateBtn;

    private SubjectLastListAdapter subjectListItemAdapter;
    SearchView editText;

    private List<ISubjectRecommendObject> subjectRecommendObjectList = new ArrayList<>();
    private List<ISubjectRecommendObject> showRecommendObjectList = new ArrayList<>();

    private String mTopicName;


    @AfterViews
    void init() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);

        actionBar.setCustomView(R.layout.activity_search_project_actionbar);

        editText = (SearchView) findViewById(R.id.editText);
        editText.onActionViewExpanded();
        editText.setIconified(false);

        editText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                updateShow(s);
                return true;
            }
        });

        subjectListItemAdapter = new SubjectLastListAdapter(this, showRecommendObjectList);
        listView.setAdapter(subjectListItemAdapter);
        listView.setOnItemClickListener(onItemClickListener);

        topicCreateBtn.setOnClickListener(onClickListener);

        showDialogLoading();
        loadSubjectLastCache();
        loadHotSubjectFromServer();

    }

    private void loadSubjectLastCache() {
        for (String s : TopicLastCache.getInstance(this).getTopicLastCacheList()) {
            Subject.SubjectLastUsedObject lastUsedObject = new Subject.SubjectLastUsedObject(s);
            subjectRecommendObjectList.add(lastUsedObject);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }

    private void loadHotSubjectFromServer() {
        getNetwork(subjectHotTweetUrl, subjectHotTweetTag);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(subjectHotTweetTag)) {
            if (code == 0) {
                JSONArray jsonArray = null;
                jsonArray = respanse.optJSONArray("data");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    Subject.SubjectDescObject projectObject = new Subject.SubjectDescObject(json);
                    subjectRecommendObjectList.add(projectObject);
                    if (i >= 9)
                        break;
                }
            } else {
                showErrorMsg(code, respanse);
            }
        }
        updateShow("");
        hideProgressDialog();
    }

    private void updateShow(String condition) {
        if (subjectRecommendObjectList != null) {
            showRecommendObjectList.clear();
            for (ISubjectRecommendObject subjectRecommendObject : subjectRecommendObjectList) {
                if (TextUtils.isEmpty(condition) || subjectRecommendObject.getName().contains(condition))
                    showRecommendObjectList.add(subjectRecommendObject);
            }
            subjectListItemAdapter.notifyDataSetChanged();
            if (showRecommendObjectList.size() > 0) {
                listView.setVisibility(View.VISIBLE);
                createLayout.setVisibility(View.GONE);
            } else {
                listView.setVisibility(View.GONE);
                topicCreateName.setText(condition);

                createLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position >= 0 && position < subjectRecommendObjectList.size()) {
                ISubjectRecommendObject recommendObject = subjectRecommendObjectList.get(position);
                mTopicName = recommendObject.getName();
                TopicLastCache.getInstance(SubjectNewActivity.this).add(mTopicName);
                finish();
            }
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.topic_create_btn:
                    if (topicCreateName != null && topicCreateName.getText() != null) {
                        mTopicName = topicCreateName.getText().toString();
                        TopicLastCache.getInstance(SubjectNewActivity.this).add(mTopicName);
                        finish();
                    }
                    break;
            }
        }
    };

    @Override
    public void finish() {
        if (!TextUtils.isEmpty(mTopicName)) {
            Intent intent = new Intent();
            intent.putExtra("topic_name", "#" + mTopicName + "#");
            setResult(RESULT_OK, intent);
        }
        super.finish();
    }
}

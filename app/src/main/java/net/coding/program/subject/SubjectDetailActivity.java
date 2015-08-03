package net.coding.program.subject;

import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;

import net.coding.program.BackActivity;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.BlankViewDisplay;
import net.coding.program.common.Global;
import net.coding.program.common.enter.EnterEmojiLayout;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.DynamicObject;
import net.coding.program.model.Maopao;
import net.coding.program.model.Subject;
import net.coding.program.model.UserObject;
import net.coding.program.subject.adapter.SubjectMaopaoListAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.apmem.tools.layouts.FlowLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by david on 15-7-25.
 */
@EActivity(R.layout.activity_subject_detail)
public class SubjectDetailActivity extends BackActivity {


    @Extra
    Subject.SubjectDescObject subjectDescObject;
    @Extra
    int topicId;


    @AfterViews
    protected final void initSubjectDetailActivity() {

        setTitle("话题详情");
        if (subjectDescObject != null || topicId > 0) {
            showSubjectDetailFragment();
        }
    }


    private void showSubjectDetailFragment() {
        Fragment fragment = SubjectDetailFragment_.builder()
                .subjectDescObject(subjectDescObject)
                .topicId(topicId)
                .build();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }

}

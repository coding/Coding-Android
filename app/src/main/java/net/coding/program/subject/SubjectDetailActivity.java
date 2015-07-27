package net.coding.program.subject;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import net.coding.program.BackActivity;
import net.coding.program.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

/**
 * Created by david on 15-7-25.
 */
@EActivity(R.layout.activity_subject_detail)
public class SubjectDetailActivity extends BackActivity {

    @ViewById
    ListView listView;

    View mListHeaderView;

    @AfterViews
    private void init(){

    }


    private void initHeaderView(){
        mListHeaderView = LayoutInflater.from(this).inflate(R.layout.activity_subject_detail_header,null);

    }



}

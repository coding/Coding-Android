package net.coding.program.search;

import android.widget.LinearLayout;
import android.widget.ListView;

import net.coding.program.R;
import net.coding.program.common.network.RefreshBaseFragment;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by chenchao on 2017/3/29.
 */
@EFragment(R.layout.fragment_search_list)
abstract class SearchBaseFragment extends RefreshBaseFragment {

    @ViewById
    ListView listView;

    @ViewById(R.id.emptyView)
    LinearLayout emptyView;


}

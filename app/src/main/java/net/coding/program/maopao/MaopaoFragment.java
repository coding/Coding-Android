package net.coding.program.maopao;

import android.support.v4.app.Fragment;

import net.coding.program.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

/**
 * Created by cc191954 on 14-8-21.
 */

@EFragment(R.layout.fragment_maopao)
public class MaopaoFragment extends Fragment {

    @AfterViews
    void init() {
        initActionbar();
    }

    void initActionbar() {
    }
}

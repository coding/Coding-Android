package net.coding.program.task.board;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import net.coding.program.R;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.network.HttpObserver;
import net.coding.program.network.Network;
import net.coding.program.network.model.task.Board;
import net.coding.program.param.ProjectJumpParam;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EActivity(R.layout.activity_board)
public class TaskBoardActivity extends BackActivity {

    @Extra
    ProjectJumpParam param;

    @ViewById
    ViewPager container;

    Board board;

    @AfterViews
    void initTaskBoardActivity() {
        param = new ProjectJumpParam("/user/1984/project/ccc");
        onRefresh();
    }


    private void onRefresh() {
        Network.getRetrofit(this)
                .getTaskBoard(param.user, param.project)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserver<Board>(this) {
                    @Override
                    public void onSuccess(Board data) {
                        super.onSuccess(data);
                        board = data;

                        bindUI();

                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                    }
                });
    }

    private void bindUI() {
        PagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return new BoardFragment_();
            }

            @Override
            public int getCount() {
                return board.boardLists.size();
            }
        };
        container.setAdapter(adapter);
    }
}

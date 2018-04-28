package net.coding.program.task.board;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import net.coding.program.R;
import net.coding.program.common.event.EventAddBoardList;
import net.coding.program.common.event.EventBoardRefresh;
import net.coding.program.common.event.EventBoardRefreshRequest;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.model.SingleTask;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.network.BaseHttpObserver;
import net.coding.program.network.HttpObserver;
import net.coding.program.network.Network;
import net.coding.program.network.model.Pager;
import net.coding.program.network.model.task.Board;
import net.coding.program.network.model.task.BoardList;
import net.coding.program.param.ProjectJumpParam;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EActivity(R.layout.activity_board)
public class TaskBoardActivity extends BackActivity {

    @Extra
    ProjectJumpParam param;

    @ViewById
    ViewPager container;

    Board board;
    private FragmentPagerAdapter pagerAdapter;

    @AfterViews
    void initTaskBoardActivity() {
//        param = new ProjectJumpParam("/user/1984/project/ccc");
        param = new ProjectJumpParam("/user/ease/project/CodingTest");
        onRefresh();
    }

    @Override
    protected boolean userEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventAdd(EventAddBoardList event) {
        pagerAdapter.addItem(event.data);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventTaskListRequest(EventBoardRefreshRequest event) {
        Network.getRetrofit(this)
                .getTaskBoardList(param.user, param.project, board.id, event.listId, event.page, 10)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserver<Pager<SingleTask>>(this) {
                    @Override
                    public void onSuccess(Pager<SingleTask> data) {
                        super.onSuccess(data);
                        for (BoardList item : board.boardLists) {
                            if (item.id == event.listId) {
                                item.tasks.add(data);
                            }
                        }
                        EventBus.getDefault().post(new EventBoardRefresh(event.listId, event.page, true));
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                        EventBus.getDefault().post(new EventBoardRefresh(event.listId, event.page, false));
                    }
                });
    }

    public BoardList getBoardList(int listId) {
        for (BoardList item : board.boardLists) {
            if (item.id == listId) {
                return item;
            }
        }

        return null;
    }

    public void jumpAddBoardList() {
        AddBoardListActivity_.intent(this)
                .param(param)
                .boardId(board.id)
                .start();
    }

    public void renameTaskList(int listId, String title, BoardFragment fragment) {
         Network.getRetrofit(this)
                .renameTaskBoardList(param.user, param.project, board.id, listId, title)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseHttpObserver(this) {
                    @Override
                    public void onSuccess() {
                        super.onSuccess();

                        for (BoardList item : board.boardLists) {
                            if (item.id == listId) {
                                item.title = title;
                                break;
                            }
                        }

                        fragment.renameTitle(title);

                    }
                });
    }

    public void delteTaskList(int listId) {
        Network.getRetrofit(this)
                .deleteTaskBoardList(param.user, param.project, board.id, listId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseHttpObserver(this) {
                    @Override
                    public void onSuccess() {
                        super.onSuccess();

                        for (BoardList item : board.boardLists) {
                            if (item.id == listId) {
                                board.boardLists.remove(item);
                                pagerAdapter.notifyDataSetChanged();
                                break;
                            }
                        }

                    }
                });
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
                        conversionData();

                        bindUI();
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                    }
                });
    }

    private void conversionData() {
        board.boardLists.add(BoardList.obtainAddBoard());

        if (board.boardLists.size() != 3 ||
                !board.boardLists.get(0).isPending() ||
                !board.boardLists.get(1).isFinished()) {
            return;
        }

        if (AccountInfo.hideInitBoard(this, board.id)) {
            return;
        }

        board.boardLists.add(1, BoardList.obtainWelcomeBoard());
    }

    private void bindUI() {
        pagerAdapter = new FragmentPagerAdapter();
        container.setAdapter(pagerAdapter);
    }

    private class FragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {
        private final int FRAGMENT_ADD = 1000;
        private final int FRAGMENT_WELCOME = 1001;

        public FragmentPagerAdapter() {
            super(TaskBoardActivity.this.getSupportFragmentManager());
        }

        @Override
        public Fragment getItem(int position) {
            long itemId = getItemId(position);
            if (itemId == FRAGMENT_WELCOME) {
                return new WelcomeBoardFragment_();
            } else if (itemId == FRAGMENT_ADD) {
                return new AddBoardListFragment_();
            }
//            BoardList boardList = board.boardLists.get(position);
            return BoardFragment_.builder().boardListId(board.boardLists.get(position).id).build();
        }

        @Override
        public int getCount() {
            return board.boardLists.size();
        }

        public void addItem(BoardList data) {
            int insertPos = board.boardLists.size() - 1;
            board.boardLists.add(insertPos, data);
            if (board.boardLists.get(1).isWelcome()) {
                board.boardLists.remove(1);
            }
            notifyDataSetChanged();
        }

        @Override
        public long getItemId(int position) {
            if (position == getCount() - 1) {
                return FRAGMENT_ADD;
            } else if (position == 1 && board.boardLists.get(1).isWelcome()) {
                return FRAGMENT_WELCOME;
            }
            return super.getItemId(position);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }
    }
}

package net.coding.program.task.board;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import net.coding.program.R;
import net.coding.program.common.GlobalData;
import net.coding.program.common.event.EventAddBoardList;
import net.coding.program.common.event.EventBoardRefresh;
import net.coding.program.common.event.EventBoardRefreshRequest;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.model.SingleTask;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.util.BlankViewHelp;
import net.coding.program.network.BaseHttpObserver;
import net.coding.program.network.HttpObserver;
import net.coding.program.network.Network;
import net.coding.program.network.model.HttpResult;
import net.coding.program.network.model.Pager;
import net.coding.program.network.model.task.Board;
import net.coding.program.network.model.task.BoardList;
import net.coding.program.param.ProjectJumpParam;
import net.coding.program.task.add.TaskAddActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EActivity(R.layout.activity_board)
public class TaskBoardActivity extends BackActivity {

    @Extra
    ProjectJumpParam param;

    @Extra
    ProjectObject projectObject;

    @ViewById
    ViewPager container;

    @ViewById
    BoardIndicatorView indicatorView;

    @ViewById
    View blankLayout;

    BlankViewHelp blankViewHelp = new BlankViewHelp();

    Board board;
    private FragmentPagerAdapter pagerAdapter;

    MenuItem actionAdd;

    @AfterViews
    void initTaskBoardActivity() {
//        param = new ProjectJumpParam("/user/1984/project/ccc");
//        param = new ProjectJumpParam("/user/ease/project/CodingTest");
//        param = new ProjectJumpParam("/user/1984/project/jj");
        blankViewHelp.setBlankLoading(blankLayout, true);
        onRefresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_board_list, menu);
        actionAdd = menu.findItem(R.id.actionAdd);

        return super.onCreateOptionsMenu(menu);
    }

    @OptionsItem
    void actionAdd() {
        BoardList boardParam = board.boardLists.get(container.getCurrentItem());
        TaskAddActivity_.intent(this)
                .mUserOwner(GlobalData.sUserObject)
                .mProjectObject(projectObject)
                .boardParam(boardParam)
                .start();
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

    public BoardList getBoardListFromPos(int pos) {
        if (pos >= board.boardLists.size()) {
            return null;
        }

        return board.boardLists.get(pos);
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
                                indicatorView.setCount(pagerAdapter.getCount(), container.getCurrentItem());
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

                        blankLayout.setVisibility(View.GONE);

                        board = data;

                        // 测试用
                        // AccountInfo.removehideInitBoard(TaskBoardActivity.this, board.id);
                        conversionData();
                        bindUI();
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                        BlankViewHelp.setErrorBlank(blankLayout, v -> onRefresh());
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

    public void hideInitBoard() {
        AccountInfo.saveHideInitBoard(this, board.id);

        for (BoardList item : board.boardLists) {
            if (item.isWelcome()) {
                board.boardLists.remove(item);
                pagerAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    public void createDefaultList() {
        Observable<HttpResult<BoardList>> r0 = Network.getRetrofit(this)
                .addTaskBoardList(param.user, param.project, board.id, "需求分析")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        Observable<HttpResult<BoardList>> r1 = Network.getRetrofit(this)
                .addTaskBoardList(param.user, param.project, board.id, "产品分析")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        Observable<HttpResult<BoardList>> r2 = Network.getRetrofit(this)
                .addTaskBoardList(param.user, param.project, board.id, "开发中")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        Observable<HttpResult<BoardList>> r3 = Network.getRetrofit(this)
                .addTaskBoardList(param.user, param.project, board.id, "产品测试")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        Observable<HttpResult<BoardList>> r4 = Network.getRetrofit(this)
                .addTaskBoardList(param.user, param.project, board.id, "产品上线")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        Observable.zip(r0, r1, r2, r3, r4, (z0, z1, z2, z3, z4) -> {
            pagerAdapter.addItem(z0.data);
            pagerAdapter.addItem(z1.data);
            pagerAdapter.addItem(z2.data);
            pagerAdapter.addItem(z3.data);
            pagerAdapter.addItem(z4.data);
            return true;
        }).subscribe(new Observer<Boolean>() {
            @Override
            public void onCompleted() {
                showProgressBar(false);
            }

            @Override
            public void onError(Throwable e) {
                showProgressBar(false);
                if (e != null && e.getMessage() != null) {
                    showButtomToast(e.getMessage());
                }
            }

            @Override
            public void onNext(Boolean aBoolean) {

            }
        });

        showProgressBar(true);
    }


    private void bindUI() {
        pagerAdapter = new FragmentPagerAdapter();
        container.setAdapter(pagerAdapter);
        indicatorView.setCount(pagerAdapter.getCount(), 0);

        container.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                int newPos = position;
                if (positionOffset > 0.5) {
                    newPos = position + 1;
                    indicatorView.setSelect(newPos);
                } else {
                    indicatorView.setSelect(newPos);
                }

                BoardList select = board.boardLists.get(newPos);
                actionAdd.setVisible(!select.isWelcome() && !select.isFinished() && !select.isAdd());
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
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
            return BoardFragment_.builder().boardPos(position).build();
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

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            indicatorView.setCount(getCount(), container.getCurrentItem());
        }
    }
}

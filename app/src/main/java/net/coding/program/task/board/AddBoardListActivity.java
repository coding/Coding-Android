package net.coding.program.task.board;

import android.support.annotation.NonNull;
import android.widget.EditText;

import net.coding.program.R;
import net.coding.program.common.event.EventAddBoardList;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.network.HttpObserver;
import net.coding.program.network.Network;
import net.coding.program.network.model.task.BoardList;
import net.coding.program.param.ProjectJumpParam;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EActivity(R.layout.activity_add_board)
@OptionsMenu(R.menu.set_password)
public class AddBoardListActivity extends BackActivity {

    @Extra
    ProjectJumpParam param;

    @Extra
    int boardId;

    @ViewById
    EditText name;

    @OptionsItem
    void submit() {
        String nameString = name.getText().toString();
        if (nameString.length() == 0) {
            showButtomToast("请输入列表名");
            return;
        }

        Network.getRetrofit(this)
                .addTaskBoardList(param.user, param.project, boardId, nameString)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserver<BoardList>(this) {
                    @Override
                    public void onSuccess(BoardList data) {
                        super.onSuccess(data);
                        showProgressBar(false);

                        EventBus.getDefault().post(new EventAddBoardList(data));
                        finish();
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                        showProgressBar(false);
                    }
                });

        showProgressBar(true);
    }

}

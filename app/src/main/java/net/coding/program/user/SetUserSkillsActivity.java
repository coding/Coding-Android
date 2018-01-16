package net.coding.program.user;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.NumberPicker;

import com.flyco.roundview.RoundTextView;
import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.model.Skill;
import net.coding.program.common.model.UserObject;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.databinding.ActivityUserEditSkillsBinding;
import net.coding.program.network.HttpObserverRaw;
import net.coding.program.network.Network;
import net.coding.program.network.model.HttpResult;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EActivity(R.layout.activity_user_edit_skills)
@OptionsMenu(R.menu.set_password)
public class SetUserSkillsActivity extends BackActivity {

    final String HOST_USERINFO = Global.HOST_API + "/user/updateInfo";

    UserObject user;

    List<Skill> pickData = new ArrayList<>();
    Map<String, Integer> allSkills = new HashMap<>();

    ActivityUserEditSkillsBinding binding;

    @AfterViews
    protected final void initSetUserTagActivity() {
        binding = ActivityUserEditSkillsBinding.bind(findViewById(R.id.rootLayout));
        user = AccountInfo.loadAccount(this);
        pickData.addAll(user.skills);
        bindingItems();
    }

    @Click
    void itemAdd() {
        if (allSkills.isEmpty()) {
            loadAllSkills();
        } else {
            popDialog();
        }
    }

    void loadAllSkills() {
        Network.getRetrofit(this)
                .getAllSkills()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserverRaw<HttpResult<HashMap<Integer, String>>>(this) {
                    @Override
                    public void onSuccess(HttpResult<HashMap<Integer, String>> data) {
                        super.onSuccess(data);

                        allSkills.clear();
                        for (Integer item : data.data.keySet()) {
                            allSkills.put(data.data.get(item), item);
                        }

                        popDialog();
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                    }
                });
    }

    void popDialog() {
        List<String> dialogFirst = new ArrayList<>();
        for (String item : allSkills.keySet()) {
            boolean find = false;
            int value = allSkills.get(item);
            for (Skill pick : pickData) {
                if (pick.skillId == value) {
                    find = true;
                    break;
                }
            }

            if (!find) {
                dialogFirst.add(item);
            }
        }

        List<String> dialogSecond = new ArrayList<>();
        for (Skill.Grade item : Skill.Grade.values()) {
            dialogSecond.add(item.alics);
        }

        View v = getLayoutInflater().inflate(R.layout.fragment_user_provinces_dialog, null);
        NumberPicker firstPicker = v.findViewById(R.id.provinces);
        NumberPicker secondPicker = v.findViewById(R.id.city);
        firstPicker.setDisplayedValues(dialogFirst.toArray(new String[0]));
        firstPicker.setMinValue(0);
        firstPicker.setMaxValue(dialogFirst.size() - 1);
        secondPicker.setDisplayedValues(dialogSecond.toArray(new String[0]));
        secondPicker.setMinValue(0);
        secondPicker.setMaxValue(dialogSecond.size() - 1);

        new AlertDialog.Builder(this)
                .setTitle("选择技能")
                .setView(v)
                .setPositiveButton("确定", (dialog, which) -> {
                    String pickFirst = dialogFirst.get(firstPicker.getValue());
                    String pickSecond = dialogSecond.get(secondPicker.getValue());

                    Skill skill = new Skill();
                    skill.skillName = pickFirst;
                    skill.skillId = allSkills.get(pickFirst);
                    skill.level = Skill.Grade.aliceToEnum(pickSecond).id;

                    pickData.add(skill);
                    bindingItems();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void bindingItems() {
        binding.itemLayout.removeAllViews();
        for (Skill item : pickData) {
            RoundTextView itemView = (RoundTextView) getLayoutInflater().inflate(R.layout.user_edit_skill_item, binding.itemLayout, false);
            itemView.setText(item.toString());
            binding.itemLayout.addView(itemView);
            itemView.setOnClickListener(v -> {
                pickData.remove(item);
                binding.itemLayout.removeView(v);
            });
        }
    }

    @OptionsItem
    void submit() {
        RequestParams params = new RequestParams();

        try {
            params.put("id", user.id);
            params.put("name", user.name);
            params.put("sex", user.sex);

            params.put("birthday", user.birthday);
            params.put("location", user.location);
            params.put("company", user.company);
            params.put("slogan", user.slogan);
            if (!TextUtils.isEmpty(user.introduction)) {
                params.put("introduction", user.introduction);
            }
            params.put("job", user.job);
            params.put("tags", user.tags);
            String[] skillParam = Skill.generateParam(pickData);
            params.put("skills", skillParam);

            postNetwork(HOST_USERINFO, params, HOST_USERINFO);
        } catch (Exception e) {
            showMiddleToast(e.toString());
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_USERINFO)) {
            if (code == 0) {
                user = new UserObject(respanse.getJSONObject("data"));
                AccountInfo.saveAccount(this, user);
                showButtomToast("修改成功");
                setResult(Activity.RESULT_OK);
                //AccountInfo.saveAccount(this, user);
                finish();
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

}

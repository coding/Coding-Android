package net.coding.program.setting

import android.view.View
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.enterprise.enterprise_fragment_main_setting.*
import net.coding.program.R
import net.coding.program.common.GlobalData
import net.coding.program.common.model.EnterpriseInfo
import net.coding.program.common.ui.BaseFragment
import net.coding.program.common.util.PermissionUtil
import net.coding.program.compatible.CodingCompat
import net.coding.program.project.detail.file.LocalProjectFileActivity_
import org.androidannotations.annotations.AfterViews
import org.androidannotations.annotations.Click
import org.androidannotations.annotations.EFragment

/**
 * Created by chenchao on 2017/1/5.
 * 企业版的设置主界面
 */
@EFragment(R.layout.enterprise_fragment_main_setting)
open class EnterpriseMainSettingFragment : BaseFragment() {

    @AfterViews
    internal fun initEnterpriseMainSettingFragment() {
        initMainSettingFragment()

        setToolbar("我的", R.id.toolbar)
        var visible = View.GONE
        if (EnterpriseInfo.instance().canManagerEnterprise()) {
            visible = View.VISIBLE
            companyName!!.text = EnterpriseInfo.instance().name
        }

        itemManager!!.visibility = visible
        itemManagerDivide!!.visibility = visible

        setHasOptionsMenu(false)
    }

    @Click
    fun itemManager() {
        EnterpriseAccountActivity_.intent(this).start()
    }

    private fun initMainSettingFragment() {
        bindDataUserinfo()
    }

    override fun onStart() {
        super.onStart()

        loadUser()
        bindDataUserinfo()
    }

    private fun bindDataUserinfo() {
        val me = GlobalData.sUserObject
        iconfromNetwork(userIcon, me.avatar)
        userIcon?.tag = me
        userName.text = me.name
        userGK.text = me.global_key
    }

    private fun loadUser() {
    }

    @Click
    fun itemLocalFile() {
        RxPermissions(activity!!)
                .request(*PermissionUtil.STORAGE)
                .subscribe { granted ->
                    if (granted!!) {
                        LocalProjectFileActivity_.intent(this).start()
                    }
                }
    }

    @Click
    fun itemHelp() {
        val url = "https://e.coding.net/help"
        val title = getString(R.string.title_activity_help)
        HelpActivity_.intent(this).url(url).title(title).start()
    }

    @Click
    fun userLayout() {
        CodingCompat.instance().launchMyDetailActivity(activity)
    }

    @Click
    fun itemSetting() {
        SettingActivity_.intent(this).start()
    }

    @Click
    fun itemAbout() {
        AboutActivity_.intent(this).start()
    }


}

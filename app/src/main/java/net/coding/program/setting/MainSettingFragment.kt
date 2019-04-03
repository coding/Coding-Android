package net.coding.program.setting


import android.Manifest
import android.content.Intent
import android.view.View
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.fragment_main_setting.*
import kotlinx.android.synthetic.main.top_tip.*
import net.coding.program.R
import net.coding.program.UserDetailEditActivity_
import net.coding.program.common.*
import net.coding.program.common.model.user.ServiceInfo
import net.coding.program.common.ui.BaseFragment
import net.coding.program.common.util.PermissionUtil
import net.coding.program.compatible.CodingCompat
import net.coding.program.mall.MallIndexActivity_
import net.coding.program.network.HttpObserver
import net.coding.program.network.Network
import net.coding.program.project.ProjectFragment
import net.coding.program.project.detail.file.LocalProjectFileActivity_
import net.coding.program.user.AddFollowActivity_
import net.coding.program.user.UserPointActivity_
import org.androidannotations.annotations.AfterViews
import org.androidannotations.annotations.Click
import org.androidannotations.annotations.EFragment
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

@EFragment(R.layout.fragment_main_setting)
open class MainSettingFragment : BaseFragment() {

    @AfterViews
    fun initMainSettingFragment() {
        initMenuItem()

        // 企业版没有商城
        itemShop?.showBadge(RedPointTip.show(activity, RedPointTip.Type.SettingShop_P460))

        bindDataUserinfo()
    }

    open fun initMenuItem() {
        mainSettingToolbar?.inflateMenu(R.menu.main_setting)
        mainSettingToolbar?.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.actionAddFollow) {
                actionAddFollow()
            }
            true
        }
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

        if (GlobalData.isEnterprise() ) {
            return
        }

        topTip?.visibility = View.GONE

        globalKey.text = me.global_key
        itemAccount.text2.visibility = View.VISIBLE
        itemAccount.text2.text = "${me.points_left} 码币"
        itemAccount.text2.setTextColor(CodingColor.fontBlue)
        itemAccount.text2.textSize = 13f
    }

    private fun bindData(serviceInfo: ServiceInfo) {
        projectCount.text = "${serviceInfo.privateProject} / ${serviceInfo.privateProjectMax}"
        teamCount.text = "${serviceInfo.publicProject} / ${serviceInfo.publicProjectMax}"
    }

    private fun loadUser() {
        if (!GlobalData.isEnterprise()) { // 平台版才需要调用这个 API
            Network.getRetrofit(activity)
                    .serviceInfo()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : HttpObserver<ServiceInfo>(activity) {
                        override fun onSuccess(data: ServiceInfo?) {
                            super.onSuccess(data)
                            bindData(data!!)
                        }

                        override fun onError(e: Throwable?) {
                            super.onError(e)
                        }
                    })
        }
    }

    @Click
    fun projectLayout() {
        jumpProjectList(ProjectFragment.ProjectType.Private)
    }

    fun jumpProjectList(type: ProjectFragment.ProjectType) {
        val intent = Intent(activity, MyCreateProjectListActivity::class.java)
        intent.putExtra("extra", type)
        startActivity(intent)
    }

    @Click
    fun teamLayout() {
        jumpProjectList(ProjectFragment.ProjectType.Public)
    }

    @Click
    fun itemAccount() {
        UserPointActivity_.intent(this).start()
    }

    @Click
    fun itemShop() {
        RedPointTip.markUsed(activity, RedPointTip.Type.SettingShop_P460)
        itemShop?.showBadge(false)

        MallIndexActivity_.intent(this).start()
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
        val url = "https://coding.net/help/"
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

    @Click
    fun topTipText() {
        UserDetailEditActivity_
                .intent(this)
                .start()
    }

    @Click
    fun closeTipButton() {
        topTip?.visibility = View.GONE
    }

    fun actionAddFollow() {
        AddFollowActivity_.intent(this).start()
    }

}

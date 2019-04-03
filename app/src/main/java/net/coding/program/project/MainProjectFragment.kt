package net.coding.program.project

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.fragment_main_project.*
import kotlinx.android.synthetic.main.top_tip.*
import net.coding.program.R
import net.coding.program.common.Global
import net.coding.program.common.GlobalCommon
import net.coding.program.common.GlobalData
import net.coding.program.common.event.EventFilter
import net.coding.program.common.event.EventPosition
import net.coding.program.common.ui.BaseFragment
import net.coding.program.common.umeng.UmengEvent
import net.coding.program.common.util.PermissionUtil
import net.coding.program.login.auth.QRScanActivity
import net.coding.program.maopao.MaopaoAddActivity_
import net.coding.program.network.HttpObserverRaw
import net.coding.program.network.Network
import net.coding.program.network.model.common.AppVersion
import net.coding.program.project.init.create.ProjectCreateActivity_
import net.coding.program.search.SearchProjectActivity_
import net.coding.program.task.add.TaskAddActivity_
import net.coding.program.terminal.TerminalActivity
import net.coding.program.user.AddFollowActivity_
import org.androidannotations.api.builder.FragmentBuilder
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class MainProjectFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main_project, container, false)
    }

    internal fun initMainProjectFragment() {
        toolbarTitle.text = "我的项目"
        mainProjectToolbar.inflateMenu(R.menu.menu_fragment_project)
        mainProjectToolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_create_friend -> action_create_friend()
                R.id.action_create -> action_create()
                R.id.action_create_task -> action_create_task()
                R.id.action_create_maopao -> action_create_maopao()
                R.id.action_scan -> action_scan()
                R.id.action_2fa -> action_2fa()
                R.id.action_search -> action_search()
            }
            true
        }

        val fragment = ProjectFragment_()
        childFragmentManager.beginTransaction()
                .add(R.id.container, fragment)
                .commit()
    }

    private fun getVersion(): Int {
        try {
            val pInfo = activity!!.getPackageManager().getPackageInfo(activity!!.getPackageName(), 0);
            return pInfo.versionCode
        } catch (e: java.lang.Exception) {
            Global.errorLog(e)
        }

        return 1
    }

    // 检查客户端是否有更新
    private fun checkNeedUpdate() {
        updateTip()

        Network.getRetrofit(activity)
                .getAppVersion()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : HttpObserverRaw<AppVersion>(activity) {
                    override fun onSuccess(data: AppVersion) {
                        super.onSuccess(data)
                        activity!!.getSharedPreferences("version", Context.MODE_PRIVATE)
                                .edit()
                                .putInt("NewAppVersion", data.build)
                                .apply()

                        updateTip()
                    }

                    override fun onFail(errorCode: Int, error: String) {}
                })
    }

    private fun updateTip() {
        val versionCode = getVersion()
        val share = activity!!.getSharedPreferences("version", Context.MODE_PRIVATE)
        val newVersion = share.getInt("NewAppVersion", 0)

        if (versionCode < newVersion) {
            topTip.visibility = View.VISIBLE
            closeTipButton.setOnClickListener { topTip.visibility = View.GONE }
            topTipText.setText(R.string.tip_update_app)
            topTip.setOnClickListener { Global.updateByMarket(activity) }
        } else {
            topTip.visibility = View.GONE
        }
    }

    private fun soldOutTip() {
        topTip.visibility = View.VISIBLE
        closeTipButton.setOnClickListener { topTip.visibility = View.GONE }
        topTipText.setText(R.string.sold_out_app)
        topTip.setOnClickListener { Global.updateByMarket(activity) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        terminalClick.setOnClickListener { terminalClick() }
        toolbarTitle.setOnClickListener { toolbarTitle() }
        initMainProjectFragment()

        soldOutTip()
    }

    internal fun toolbarTitle() {
        EventBus.getDefault().post(EventFilter(0))
    }

    internal fun terminalClick() {
        val i = Intent(activity, TerminalActivity::class.java)
        startActivity(i)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(eventPosition: EventPosition) {
        toolbarTitle?.text = eventPosition.title
    }


    internal fun action_create_friend() {
        umengEvent(UmengEvent.LOCAL, "快捷添加好友")
        AddFollowActivity_.intent(this).start()
    }

    internal fun action_create() {
        umengEvent(UmengEvent.LOCAL, "快捷创建项目")
        ProjectCreateActivity_.intent(this).start()
    }

    internal fun action_create_task() {
        umengEvent(UmengEvent.LOCAL, "快捷创建任务")
        TaskAddActivity_.intent(this).mUserOwner(GlobalData.sUserObject).start()
    }

    internal fun action_create_maopao() {
        umengEvent(UmengEvent.LOCAL, "快捷创建冒泡")
        MaopaoAddActivity_.intent(this).start()
    }

    internal fun action_scan() {
        RxPermissions(activity!!)
                .request(Manifest.permission.CAMERA)
                .subscribe { granted ->
                    if (granted!!) {
                        val intent = Intent(activity, QRScanActivity::class.java)
                        intent.putExtra(QRScanActivity.EXTRA_OPEN_AUTH_LIST, true)
                        startActivity(intent)
                    }
                }
    }

    internal fun action_2fa() {
        RxPermissions(activity!!)
                .request(*PermissionUtil.CAMERA)
                .subscribe { granted ->
                    if (granted!!) {
                        GlobalCommon.start2FAActivity(activity)
                    }
                }
    }

    internal fun action_search() {
        SearchProjectActivity_.intent(this!!).start()
        activity!!.overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out)
    }

    class FragmentBuilder_ : FragmentBuilder<MainProjectFragment.FragmentBuilder_, MainProjectFragment>() {

        override fun build(): net.coding.program.project.MainProjectFragment {
            val fragment_ = MainProjectFragment()
            fragment_.arguments = args
            return fragment_
        }
    }
}

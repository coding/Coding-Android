package net.coding.program.project

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_main_project.*
import net.coding.program.R
import net.coding.program.common.GlobalCommon
import net.coding.program.common.GlobalData
import net.coding.program.common.event.EventFilter
import net.coding.program.common.event.EventPosition
import net.coding.program.common.ui.BaseFragment
import net.coding.program.common.umeng.UmengEvent
import net.coding.program.common.util.PermissionUtil
import net.coding.program.login.auth.QRScanActivity
import net.coding.program.maopao.MaopaoAddActivity_
import net.coding.program.project.init.create.ProjectCreateActivity_
import net.coding.program.search.SearchProjectActivity_
import net.coding.program.task.add.TaskAddActivity_
import net.coding.program.terminal.TerminalActivity
import net.coding.program.user.AddFollowActivity_
import org.androidannotations.api.builder.FragmentBuilder
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainProjectFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_main_project, container, false)
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

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        terminalClick.setOnClickListener { terminalClick() }
        toolbarTitle.setOnClickListener { view -> toolbarTitle(view) }
        initMainProjectFragment()
    }

    internal fun toolbarTitle(v: View) {
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
        toolbarTitle!!.text = eventPosition.title
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
        if (!PermissionUtil.checkCamera(activity)) {
            return
        }

        val intent = Intent(activity, QRScanActivity::class.java)
        intent.putExtra(QRScanActivity.EXTRA_OPEN_AUTH_LIST, false)
        startActivity(intent)
    }

    internal fun action_2fa() {
        if (!PermissionUtil.checkCamera(activity)) {
            return
        }

        GlobalCommon.start2FAActivity(activity)
    }

    internal fun action_search() {
        SearchProjectActivity_.intent(this).start()
        activity.overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out)
    }

    class FragmentBuilder_ : FragmentBuilder<MainProjectFragment.FragmentBuilder_, MainProjectFragment>() {

        override fun build(): net.coding.program.project.MainProjectFragment {
            val fragment_ = MainProjectFragment()
            fragment_.arguments = args
            return fragment_
        }
    }
}

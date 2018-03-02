package net.coding.program.setting


import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import net.coding.program.R
import net.coding.program.UserDetailEditActivity_
import net.coding.program.common.Global
import net.coding.program.common.GlobalData
import net.coding.program.common.RedPointTip
import net.coding.program.common.model.AccountInfo
import net.coding.program.common.model.user.ServiceInfo
import net.coding.program.common.ui.BaseFragment
import net.coding.program.common.util.PermissionUtil
import net.coding.program.common.widget.ListItem1
import net.coding.program.compatible.CodingCompat
import net.coding.program.mall.MallIndexActivity_
import net.coding.program.project.detail.file.LocalProjectFileActivity_
import net.coding.program.user.AddFollowActivity_
import net.coding.program.user.UserPointActivity_
import net.coding.program.user.team.TeamListActivity_
import org.androidannotations.annotations.AfterViews
import org.androidannotations.annotations.Click
import org.androidannotations.annotations.EFragment
import org.androidannotations.annotations.ViewById
import org.json.JSONException
import org.json.JSONObject

@EFragment(R.layout.fragment_main_setting)
open class MainSettingFragment : BaseFragment() {
    internal val url = Global.HOST_API + "/user/service_info"

    @ViewById
    protected lateinit var userName: TextView
    @ViewById
    protected lateinit var userGK: TextView
    @ViewById
    protected lateinit var projectCount: TextView
    @ViewById
    protected lateinit var teamCount: TextView
    @ViewById
    protected lateinit var mainSettingToolbar: Toolbar
    @ViewById(R.id.itemShop)
    protected lateinit var itemShop: ListItem1
    @ViewById
    protected lateinit var userIcon: ImageView
    @ViewById(R.id.topTip)
    protected lateinit var topTip: View

    var serviceInfo: ServiceInfo? = null

    @AfterViews
    fun initMainSettingFragment() {
        initMenuItem()

        // 企业版没有商城
        if (itemShop != null) {
            itemShop!!.showBadge(RedPointTip.show(activity, RedPointTip.Type.SettingShop_P460))
        }

        bindDataUserinfo()
    }

    protected fun initMenuItem() {
        mainSettingToolbar!!.inflateMenu(R.menu.main_setting)
        mainSettingToolbar!!.setOnMenuItemClickListener { item ->
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
        userName!!.text = me.name
        userGK!!.text = String.format("个性后缀：%s", me.global_key)
        iconfromNetwork(userIcon, me.avatar)
        userIcon!!.tag = me

        if (GlobalData.isEnterprise() || me.isFillInfo || me.isHighLevel) {
            if (topTip != null) {
                topTip!!.visibility = View.GONE
            }
        }
    }

    private fun bindData() {
        if (serviceInfo == null) {
            serviceInfo = ServiceInfo(AccountInfo.getGetRequestCacheData(activity, url))
        }

        projectCount!!.text = (serviceInfo!!.publicProject + serviceInfo!!.privateProject).toString()
        teamCount!!.text = serviceInfo!!.team.toString()
    }

    internal fun loadUser() {
        if (!GlobalData.isEnterprise()) { // 平台版才需要调用这个 API
            getNetwork(url, TAG_SERVICE_INFO)
        }
    }

    @Throws(JSONException::class)
    override fun parseJson(code: Int, respanse: JSONObject, tag: String, pos: Int, data: Any) {
        if (tag == TAG_SERVICE_INFO) {
            if (code == 0) {
                serviceInfo = ServiceInfo(respanse.optJSONObject("data"))
                bindData()
            } else {
                showErrorMsg(code, respanse)
            }
        }
        super.parseJson(code, respanse, tag, pos, data)
    }

    @Click
    public fun projectLayout() {
        MyCreateProjectListActivity_.intent(this).start()
    }

    @Click
    public fun teamLayout() {
        TeamListActivity_.intent(this).start()
    }

    @Click
    public fun itemAccount() {
        UserPointActivity_.intent(this).start()
    }

    @Click
    public fun itemShop() {
        RedPointTip.markUsed(activity, RedPointTip.Type.SettingShop_P460)
        itemShop!!.showBadge(false)

        MallIndexActivity_.intent(this).start()
    }

    @Click
    public fun itemLocalFile() {
        if (!PermissionUtil.writeExtralStorage(activity)) {
            return
        }

        LocalProjectFileActivity_.intent(this).start()
    }

    @Click
    public fun itemHelp() {
        val url = "https://coding.net/help/doc/mobile"
        val title = getString(R.string.title_activity_help)
        HelpActivity_.intent(this).url(url).title(title).start()
    }

    @Click
    public fun userLayout() {
        CodingCompat.instance().launchMyDetailActivity(activity)
    }

    @Click
    public fun itemSetting() {
        SettingActivity_.intent(this).start()
    }

    @Click
    public fun itemAbout() {
        AboutActivity_.intent(this).start()
    }

    @Click
    public fun topTipText() {
        UserDetailEditActivity_
                .intent(this)
                .start()
    }

    @Click
    public fun closeTipButton() {
        topTip!!.visibility = View.GONE
    }

    fun actionAddFollow() {
        AddFollowActivity_.intent(this).start()
    }

    companion object {

        private val TAG_SERVICE_INFO = "TAG_SERVICE_INFO"
    }

}

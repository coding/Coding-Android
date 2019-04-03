package net.coding.program.setting

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import net.coding.program.R
import net.coding.program.common.Global
import net.coding.program.common.ui.BackActivity
import org.androidannotations.annotations.Click
import org.androidannotations.annotations.EActivity

@EActivity(R.layout.activity_enterprise_support)
open class EnterpriseSupportActivity : BackActivity() {

    @Click
    fun itemPhone() {
        dialPhoneNumber(this, "400-930-9163")
    }

    @Click
    fun itemEmail() {
        composeEmail(this, arrayOf("enterprise@coding.net"))
    }

    @Click
    fun itemQQ() = try {
        val uri1 = "mqqwpa://im/chat?chat_type=wpa&uin=2847276903&version=1"
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri1)))
    } catch (e: Exception) {
        showDialog("已复制 QQ 号到剪贴板",
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        Global.copy(this@EnterpriseSupportActivity, "2847276903")
                    }
                })
    }

    fun dialPhoneNumber(activity: Activity, phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phoneNumber")
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        }
    }

    fun composeEmail(context: Context, addresses: Array<String>) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:") // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses)
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }
}

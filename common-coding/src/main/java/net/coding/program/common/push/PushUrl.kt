package net.coding.program.common.push

import android.net.Uri

/**
 * Created by chenchao on 16/1/18.
 */
public object PushUrl {

    public fun is2faLink(link: String): Boolean {
        val uri = Uri.parse(link)
        return uri.path == "/app_intercept/show_2fa"
    }
}

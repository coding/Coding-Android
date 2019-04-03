package net.coding.program.terminal

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_terminal.*
import net.coding.program.common.ui.BackActivity

class TerminalActivity : BackActivity() {

    lateinit var fixationKeys: List<View>

    lateinit var lastSelectRadio: View

    var ctrlPressed = false
    var altPressed = false

    private val colorSelect = 0xFFBBC2CA.toInt()
    private val colorNormal = 0xFFFFFFFF.toInt()
    private val colorSelectExt = 0xFFA7B0BD.toInt()

    var showLogin = false

    val clickKeyButton = View.OnClickListener {
        val tag = it.tag
        if (tag is KeyItem) {
            when (tag) {
                KeyItem.CTRL -> {
                    ctrlPressed = !ctrlPressed
                    it.setBackgroundColor(if (ctrlPressed) colorNormal else colorSelect)
                }
                KeyItem.ALT -> {
                    altPressed = !altPressed
                    it.setBackgroundColor(if (altPressed) colorNormal else colorSelect)
                }
                KeyItem.ESC -> {
                    showLogin = !showLogin
                    if (showLogin) {
                        var url = "http://ide.xiayule.net"
                        url = "http://ide.xiayule.net"
                        loadUrl(url)
                        showMiddleToast("打开登录页面")
                    } else {
                        var url = "http://ide.test:8060"
                        url = "http://ide.test:8060 "
                        loadUrl(url)
                        showMiddleToast("打开Terminal")
                    }
                }
                else -> {
                    webView.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, tag.value))
                    webView.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, tag.value))
                }
            }
        }
    }

    fun loadUrl(url: String) {
//        url = "http://192.168.0.212:8060"
//        url = "http://www.baidu.com"
//        url = "http://ide.xiayule.net"
        webView.settings.let {
            it.javaScriptEnabled = true
            it.domStorageEnabled = true
            it.setAppCachePath(cacheDir.absolutePath)
            it.allowFileAccess = true
            it.setAppCacheEnabled(true)
            it.cacheMode
        }

        webView.loadUrl(url)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terminal)

        maskView.setOnClickListener { showKeyExt(false) }

        fixationKeys = listOf(keyEsc, keyCtrl, keyAlt, keyTab, keyMore)
        val fixationItems = listOf(KeyItem.ESC, KeyItem.CTRL, KeyItem.ALT, KeyItem.TAB, KeyItem.MORE)
        val action: (Pair<View, KeyItem>) -> Unit = {
            val view = it.first
            view.setTag(it.second)
            if ((view is TextView)) {
                view.setText(it.second.text)
            } else if ((view is ImageView)) {
                view.setImageResource(it.second.icon)
            }

            if (view == keyMore) {
                view.setOnClickListener { showKeyExt(maskView.visibility != View.VISIBLE) }
            } else {
                view.setOnClickListener(clickKeyButton)
            }
        }

        fixationKeys.zip(fixationItems).forEach(action)

        initKeyExt()
        initWebView()

        edit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                Logger.d("CKEY ww ");
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Logger.d("CKEY ww ");
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Logger.d("CKEY ww ");
            }
        })
    }

    private fun initWebView() {
        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (view != null && url != null) {
                    loadUrl(url)
                    return true
                }
                return false
            }
        }

        loadUrl("http://ide.test:8060")
    }

    private fun initKeyExt() {
        val first = listOf(KeyItem.SLASH, KeyItem.MINUS, KeyItem.VER_LINE, KeyItem.AT)
        val second = listOf(KeyItem.WAVY_LINE, KeyItem.POINT, KeyItem.COLON, KeyItem.SEMICOLON)
        val third = listOf(KeyItem.UP, KeyItem.DOWN, KeyItem.LEFT, KeyItem.RIGHT)

        val inflater = LayoutInflater.from(this)

        listOf(first, second, third).forEach { lineIt ->
            val lineLayout = LinearLayout(this)
            layoutExt.addView(lineLayout)
            val radioButton = inflater.inflate(R.layout.key_ext_image_item, lineLayout, false)
            radioButton.setBackgroundResource(R.mipmap.key_radio_normal)
            lastSelectRadio = radioButton

            lineLayout.addView(radioButton)

            val lineButtons = arrayListOf<View>()
            lineIt.forEach {
                val itemView = inflater.inflate(R.layout.key_ext_item, lineLayout, false) as TextView
                itemView.setTag(it)
                itemView.setOnClickListener(clickKeyButton)
                itemView.text = it.text

                lineLayout.addView(itemView)
                lineButtons.add(itemView)
            }

            radioButton.tag = lineButtons

            radioButton.setOnClickListener {
                lastSelectRadio.radioSelect(false)
                lastSelectRadio = it
                lastSelectRadio.radioSelect(true)
            }
        }
        lastSelectRadio.performClick()
        maskView.performClick()
    }

    fun showKeyExt(show: Boolean) {
        val visable = if (show) View.VISIBLE else View.GONE
        maskView.visibility = visable
        layoutExt.visibility = visable
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        Logger.d("CKEY" + event)
        return super.dispatchKeyEvent(event)
    }

    override fun dispatchKeyShortcutEvent(event: KeyEvent?): Boolean {
        Logger.d("CKEY" + event)
        return super.dispatchKeyShortcutEvent(event)
    }

    override fun dispatchGenericMotionEvent(ev: MotionEvent?): Boolean {
        Logger.d("CKEY" + ev)
        return super.dispatchGenericMotionEvent(ev)
    }

    override fun dispatchTrackballEvent(ev: MotionEvent?): Boolean {
        Logger.d("CKEY" + ev)
        return super.dispatchTrackballEvent(ev)
    }

    fun View.radioSelect(select: Boolean) {
        val radioBg = if (select) R.mipmap.key_radio_select else R.mipmap.key_radio_normal
        val keyBg = if (select) colorSelectExt else colorNormal

        this.setBackgroundResource(radioBg)
        val tag = this.tag
        if (tag is ArrayList<*>) {
            layoutSwitch.removeAllViews()

            val inflater = LayoutInflater.from(context)
            tag.forEach {
                if (it is View) {
                    it.setBackgroundColor(keyBg)

                    val keyItem = it.tag
                    if (keyItem is KeyItem) {
                        val button = inflater.inflate(R.layout.key_item_text, layoutSwitch, false)
                        button.setTag(keyItem)
                        if (button is TextView) {
                            button.setText(keyItem.text)
                        }
                        layoutSwitch.addView(button)
                        button.setOnClickListener(clickKeyButton)
                    }
                }
            }


        }
    }
}

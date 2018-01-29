package net.coding.program.terminal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_terminal.*
import net.coding.program.common.ui.BackActivity

class TerminalActivity : BackActivity() {

    lateinit var fixationKeys: List<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terminal)

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
        }
        fixationKeys.zip(fixationItems).forEach(action)

        val first = listOf(KeyItem.SLASH, KeyItem.MINUS, KeyItem.VER_LINE, KeyItem.AT)
        val second = listOf(KeyItem.WAVY_LINE, KeyItem.POINT, KeyItem.COLON, KeyItem.SEMICOLON)
        val third = listOf(KeyItem.UP, KeyItem.DOWN, KeyItem.LEFT, KeyItem.RIGHT)

        val inflater = LayoutInflater.from(this)
        listOf(first, second, third).forEach { lineIt ->
            val lineLayout = LinearLayout(this)
            layoutExt.addView(lineLayout)
            val radioButton = inflater.inflate(R.layout.key_ext_image_item, lineLayout, false)
            lineLayout.addView(radioButton)
            lineIt.forEach {
                val itemView = inflater.inflate(R.layout.key_ext_item, lineLayout, false)
                itemView.setTag(it)
                if (itemView is TextView) {
                    itemView.setText(it.text)
                }
                lineLayout.addView(itemView)
            }
        }
    }

    private fun clickKeyItem(v: View) {
        val tag = v.tag
        if (tag is KeyItem) {
            edit.p
        }
    }
}

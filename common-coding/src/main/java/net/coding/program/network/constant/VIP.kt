package net.coding.program.network.constant

import com.google.gson.annotations.SerializedName

import net.coding.program.R

import java.io.Serializable

/**
 * Created by chenchao on 2017/5/31.
 */
enum class VIP constructor(var id: Int, var alias: String, var icon: Int) : Serializable {

    @SerializedName("1")
    normal(1, "普通会员", 0),
    @SerializedName("2")
    silver(2, "银牌会员", 0),
    @SerializedName("3")
    gold(3, "黄金会员", R.drawable.member_gold),
    @SerializedName("4")
    diamond(4, "钻石会员", R.drawable.member_diamond),
    @SerializedName("5")
    tencent(5, "", 0);

    val isPayed: Boolean
        get() = id >= 3

    companion object {

        fun id2Enum(id: Int): VIP {
            for (item in VIP.values()) {
                if (item.id == id) {
                    return item
                }
            }

            return normal
        }
    }
}

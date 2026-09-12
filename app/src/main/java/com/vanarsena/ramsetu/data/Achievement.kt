package com.vanarsena.ramsetu.data

import com.vanarsena.ramsetu.R

enum class Achievement(
    val id: String,
    val titleRes: Int,
    val descRes: Int
) {
    FIRST_STONE("first_stone", R.string.ach_first_title, R.string.ach_first_desc),
    SCORE_TEN("score_10", R.string.ach_score_ten_title, R.string.ach_score_ten_desc),
    COMBO_TEN("combo_10", R.string.ach_combo_ten_title, R.string.ach_combo_ten_desc),
    COMBO_FIFTY("combo_50", R.string.ach_combo_fifty_title, R.string.ach_combo_fifty_desc),
    COMBO_HUNDRED("combo_100", R.string.ach_combo_hundred_title, R.string.ach_combo_hundred_desc),
    SPEED_FIFTEEN("speed_1_5", R.string.ach_speed_15_title, R.string.ach_speed_15_desc),
    SPEED_TWO("speed_2", R.string.ach_speed_2_title, R.string.ach_speed_2_desc),
    SPEED_THREE("speed_3", R.string.ach_speed_3_title, R.string.ach_speed_3_desc),
    TOTAL_FIVE_HUNDRED("total_500", R.string.ach_total_title, R.string.ach_total_desc),
    NEW_RECORD("new_record", R.string.ach_record_title, R.string.ach_record_desc)
}

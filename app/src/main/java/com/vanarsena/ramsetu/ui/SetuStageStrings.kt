package com.vanarsena.ramsetu.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.vanarsena.ramsetu.R
import com.vanarsena.ramsetu.engine.SetuStage

@Composable
fun setuStageShortName(stage: SetuStage): String = when (stage) {
    SetuStage.DAY1 -> stringResource(R.string.stage_day1_short)
    SetuStage.DAY2 -> stringResource(R.string.stage_day2_short)
    SetuStage.DAY3 -> stringResource(R.string.stage_day3_short)
    SetuStage.DAY4 -> stringResource(R.string.stage_day4_short)
    SetuStage.DAY5 -> stringResource(R.string.stage_day5_short)
    SetuStage.YATRA -> stringResource(R.string.stage_yatra_short)
}

@Composable
fun setuStageBanner(stage: SetuStage): String = when (stage) {
    SetuStage.DAY1 -> stringResource(R.string.stage_banner_day1)
    SetuStage.DAY2 -> stringResource(R.string.stage_banner_day2)
    SetuStage.DAY3 -> stringResource(R.string.stage_banner_day3)
    SetuStage.DAY4 -> stringResource(R.string.stage_banner_day4)
    SetuStage.DAY5 -> stringResource(R.string.stage_banner_day5)
    SetuStage.YATRA -> stringResource(R.string.stage_banner_yatra)
}

package dev.akexorcist.flipfoldcounter.ui.instruction

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InstructionScreen(backStack: SnapshotStateList<Any>) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(state = pagerState) {
            Text(text = "Instruction Step $it")
        }
    }
}

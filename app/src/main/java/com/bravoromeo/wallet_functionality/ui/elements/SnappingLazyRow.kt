package com.bravoromeo.wallet_functionality.ui.elements

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bravoromeo.wallet_functionality.R
import com.bravoromeo.wallet_functionality.ui.theme.Wallet_functionalityTheme
import com.bravoromeo.wallet_functionality.viewmodel.AppViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState

@Preview(showBackground = true)
@Composable
fun PreviewSnappingLazyRow(){
    Wallet_functionalityTheme {
        SnappingLazyRow(items = listOf(0,1)){}
    }
}
@OptIn(ExperimentalPagerApi::class)
@Composable
fun SnappingLazyRow(
    modifier: Modifier = Modifier,
    items: List<Any>,
    onPageChange: (Int) -> Unit
){
    val state = rememberPagerState()
    
    HorizontalPager(
        state = state,
        count = items.size,
        contentPadding = PaddingValues(horizontal = 16.dp),
        itemSpacing = 4.dp
    ) {page ->  
        when(items[page]){
            0 -> CardView()
            1 -> CardView(cardImageResourceId = R.drawable.solred_card_2_900)
            else -> CardView()
        }
    }

    LaunchedEffect(state.currentPage){
        onPageChange.invoke(state.currentPage)
    }
}
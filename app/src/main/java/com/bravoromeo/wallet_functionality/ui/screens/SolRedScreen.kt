package com.bravoromeo.wallet_functionality.ui.screens

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bravoromeo.wallet_functionality.R
import com.bravoromeo.wallet_functionality.ui.elements.BalanceTextBox
import com.bravoromeo.wallet_functionality.ui.elements.CardView
import com.bravoromeo.wallet_functionality.ui.elements.SnappingLazyRow
import com.bravoromeo.wallet_functionality.ui.elements.WalletButton
import com.bravoromeo.wallet_functionality.ui.theme.Wallet_functionalityTheme
import com.bravoromeo.wallet_functionality.viewmodel.AppViewModel
import kotlinx.coroutines.launch

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewSolRedScreen(){
    Wallet_functionalityTheme {
        SolRedScreen()
    }
}

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun SolRedScreen(
    modifier: Modifier = Modifier,
    viewModel: AppViewModel? = null,
    context: Context? = null,
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = modifier
                .fillMaxSize()
        ) {
            SnappingLazyRow(items = listOf(0,1)){ viewModel?.setCurrentCardType(it) }
            Spacer(modifier = modifier.height(20.dp))
            BalanceTextBox(viewModel = viewModel, modifier = modifier.width(230.dp))
            Spacer(modifier = modifier.height(20.dp))
            if (viewModel?.appState?.isWalletAvailable != false) {
                WalletButton {
                    if (context != null) {
                        viewModel?.saveSolRedCardToWallet(context = context)
                    }
                }
            }

            //Code snippet below is for testing purposes only
            /*val testCardId = "HeJNd93HK8Ycvs0T1TXKr"
            Button(
                onClick = {
                    if (context != null){
                        viewModel?.sendMessageToSolRedCard(
                            cardId = testCardId,//"e9gItv1eZ9FRWTbENzZnt",
                            context = context
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 1.dp
                ),
                modifier = modifier
                    .padding(top = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Image(imageVector = Icons.Default.Send, contentDescription = "", modifier = modifier.padding(end = 8.dp))
                    Text(text = "Enviar mensaje a SolRed Card") }
            }
            Button(
                onClick = {
                    if (context != null){
                        viewModel?.updateRedSolCard(
                            cardId = testCardId,//"e9gItv1eZ9FRWTbENzZnt",
                            balance = 10,//(viewModel.appState.cardBalance * 10000000L).toInt(),//10 EUR
                            context = context
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 1.dp
                ),
                modifier = modifier
                    .padding(top = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Image(imageVector = Icons.Default.Settings, contentDescription = "", modifier = modifier.padding(end = 8.dp))
                    Text(text = "Modificar SolRed Card") }
            }

            Button(
                onClick = {
                    if (context != null){
                        viewModel?.updateSolRedBalanceCardClass(context = context)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 1.dp
                ),
                modifier = modifier
                    .padding(top = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Image(imageVector = Icons.Default.Settings, contentDescription = "", modifier = modifier.padding(end = 8.dp))
                    Text(text = "Modificar SolRed Class") }
            }*/
        }
    }
}

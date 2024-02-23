package com.bravoromeo.wallet_functionality.ui.screens

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bravoromeo.wallet_functionality.ui.elements.BalanceTextBox
import com.bravoromeo.wallet_functionality.ui.elements.CardView
import com.bravoromeo.wallet_functionality.ui.elements.WalletButton
import com.bravoromeo.wallet_functionality.ui.theme.Wallet_functionalityTheme
import com.bravoromeo.wallet_functionality.viewmodel.AppViewModel

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
            CardView()
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
            /*Button(
                onClick = {
                    if (context != null){
                        viewModel?.updateRedSolCard(cardId = "e9gItv1eZ9FRWTbENzZnt", balance = (viewModel.appState.cardBalance * 1000L).toInt(), context = context)
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

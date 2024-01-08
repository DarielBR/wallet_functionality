package com.bravoromeo.wallet_functionality.ui.elements

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bravoromeo.wallet_functionality.R
import com.bravoromeo.wallet_functionality.ui.theme.Wallet_functionalityTheme

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewWalletBrandElements(){
    Wallet_functionalityTheme {
        Surface(
            modifier = Modifier.padding(8.dp)
        ) {
            WalletButton(){}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletButton(
    modifier: Modifier= Modifier,
    onClick: () -> Unit
){
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
            hoveredElevation = 2.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp,
            focusedElevation = 1.dp,
            draggedElevation = 0.dp
        )
    ) {
        Image(painter=painterResource(id=R.drawable.wallet_button_eses), contentDescription="")
    }
}
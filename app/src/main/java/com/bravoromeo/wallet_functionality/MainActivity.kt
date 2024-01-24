@file:OptIn(ExperimentalComposeUiApi::class)

package com.bravoromeo.wallet_functionality

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.twotone.Edit
import androidx.compose.material.icons.twotone.Email
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bravoromeo.wallet_functionality.ui.elements.WalletButton
import com.bravoromeo.wallet_functionality.ui.theme.Wallet_functionalityTheme
import com.bravoromeo.wallet_functionality.viewmodel.AppViewModel
import com.google.android.gms.pay.Pay
import com.google.android.gms.pay.PayClient

class MainActivity : ComponentActivity() {
    private lateinit var walletClient: PayClient
    private val addToGoogleWalletRequestCode: Int = 1000
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onCreate(savedInstanceState: Bundle?) {
        walletClient = Pay.getClient(this)
        super.onCreate(savedInstanceState)
        val viewModel = AppViewModel(walletClient = walletClient, requestCode = addToGoogleWalletRequestCode)
        setContent {
            Wallet_functionalityTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier=Modifier.fillMaxSize(),
                    color=MaterialTheme.colorScheme.background
                ) {
                    Greeting(
                        viewModel = viewModel,
                        context = this
                    )
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == addToGoogleWalletRequestCode){
            when (resultCode){
                RESULT_OK -> {
                    //TODO show Toast with a Success message
                    Toast.makeText(this, "Pass successfully saved to Google Wallet!", Toast.LENGTH_SHORT).show()
                }
                RESULT_CANCELED -> {
                    //TODO show Toast with a Cancelled by User message
                    Toast.makeText(this, "Action cancelled by user. No pass added to wallet.", Toast.LENGTH_LONG).show()
                }
                PayClient.SavePassesResult.SAVE_ERROR -> data?.let{intentData ->
                    val errorMessage = intentData.getStringExtra(PayClient.EXTRA_API_ERROR_MESSAGE)
                    Log.e("SavePassResult", errorMessage.toString())
                }
                else -> {
                    Log.e("SavePassResult", "Unknown error")
                }
            }
        }
    }
}

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun Greeting(
    modifier: Modifier = Modifier,
    viewModel: AppViewModel? = null,
    context: Context? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize()
    ){
        Divider(
            thickness = Dp.Hairline,
            modifier = modifier
                .padding(horizontal = 24.dp)
                .padding(vertical = 8.dp)
        )

        Button(
            onClick = { viewModel?.createDemoClass2(context = context!!) },
            modifier = modifier
                .width(300.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(0.dp)
            ) {
                Image(
                    imageVector = Icons.Default.Create,
                    contentDescription = ""
                )
                Text(
                    text = "Crear nueva clase generica modo demo",
                    textAlign = TextAlign.Center,
                    modifier = modifier
                        .padding(start = 4.dp)
                )
            }
        }
        Button(
            onClick = { viewModel?.updateDemoClass2(context = context!!) },
            modifier = modifier
                .width(300.dp)
                .padding(vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(0.dp)
            ) {
                Image(imageVector = Icons.Outlined.Edit, contentDescription = "")
                Text(
                    text = "Modificar clase Generica modo demo",
                    textAlign = TextAlign.Center,
                    modifier = modifier
                        .padding(start = 4.dp)
                )
            }
        }
        if (viewModel?.appState?.isWalletAvailable ?: true) {
            WalletButton {
                if (context != null) {
                    viewModel?.savePassToWallet(context = context)
                }
            }
        }
        Divider(
            thickness = Dp.Hairline,
            modifier = modifier
                .padding(horizontal = 24.dp)
                .padding(vertical = 8.dp)
        )
        Button(
            onClick = { viewModel?.createLoyaltyDemoClass(context = context!!) },
            modifier = modifier
                .width(300.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(0.dp)
            ) {
                Image(imageVector = Icons.Default.Create, contentDescription = "")
                Text(
                    text = "Crear nueva clase Fidelidad modo demo",
                    textAlign = TextAlign.Center,
                    modifier = modifier
                        .padding(start = 4.dp)
                )
            }
        }

        Button(
            onClick = { viewModel?.updateLoyaltyDemoClass(context = context!!) },
            modifier = modifier
                .width(300.dp)
                .padding(vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(0.dp)
            ) {
                Image(imageVector = Icons.Outlined.Edit, contentDescription = "")
                Text(
                    text = "Modificar clase Fidelidad modo demo",
                    textAlign = TextAlign.Center,
                    modifier = modifier
                        .padding(start = 4.dp)
                )
            }
        }
        Button(
            onClick = { viewModel?.addMessageToLoyaltyClass(context = context!!) },
            modifier = modifier
                .width(300.dp)
                .padding(bottom = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(0.dp)
            ) {
                Image(imageVector = Icons.Default.Email, contentDescription = "")
                Text(
                    text = "Agregar mensaje a clase Fidelidad modo demo",
                    textAlign = TextAlign.Center,
                    modifier = modifier
                        .padding(start = 4.dp)
                )
            }
        }
        Button(
            onClick = { viewModel?.addMessageToLoyaltyPass(
                context = context!!,
                passId = viewModel.appState.currentLoyaltyPassId
            ) },
            modifier = modifier
                .width(300.dp)
                .padding(bottom = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(0.dp)
            ) {
                Image(imageVector = Icons.TwoTone.Email, contentDescription = "")
                Text(
                    text = "Agregar mensaje a pase Fidelidad modo demo",
                    textAlign = TextAlign.Center,
                    modifier = modifier
                        .padding(start = 4.dp)
                )
            }
        }
        Button(
            onClick = { viewModel?.updateLoyaltyPass(
                context = context!!,
                passId = viewModel.appState.currentLoyaltyPassId
            ) },
            modifier = modifier
                .width(300.dp)
                .padding(bottom = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(0.dp)
            ) {
                Image(imageVector = Icons.TwoTone.Edit, contentDescription = "")
                Text(
                    text = "Modificar pase Fidelidad modo demo",
                    textAlign = TextAlign.Center,
                    modifier = modifier
                        .padding(start = 4.dp)
                )
            }
        }
        val keyboardController = LocalSoftwareKeyboardController.current//Experimental API
        OutlinedTextField(
            value = viewModel?.appState?.currentLoyaltyPassId ?: "",
            onValueChange = { viewModel?.onCurrentLoyaltyPassIdChange(newValue = it) },
            keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
            modifier = modifier
                .padding(bottom = 8.dp)
        )
        if (viewModel?.appState?.isWalletAvailable ?: true) {
            WalletButton {
                if (context != null) {
                    viewModel?.saveLoyaltyPassToWallet(context = context)
                }
            }
        }
        Divider(
            thickness = Dp.Hairline,
            modifier = modifier
                .padding(horizontal = 24.dp)
                .padding(vertical = 8.dp)
        )
    }
}

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Preview(showBackground=true)
@Composable
fun GreetingPreview() {
    Wallet_functionalityTheme {
        Greeting()
    }
}
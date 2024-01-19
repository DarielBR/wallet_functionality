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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
                        context = this,
                        activity = this
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
    context: Context? = null,
    activity: Activity? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize()
    ){
        Button(onClick = { viewModel?.createDemoClass2(context = context!!) }) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(0.dp)
            ) {
                Image(imageVector = Icons.Default.Menu, contentDescription = "")
                Text(text = "Crear nueva clase generica modo demo")
            }
        }
        if (viewModel?.appState?.isWalletAvailable ?: true) WalletButton {
            if (activity != null) {
                viewModel?.savePassToWallet(activity = activity)
                //viewModel?.updateClassAtWallet(activity = activity)
            }
        }
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
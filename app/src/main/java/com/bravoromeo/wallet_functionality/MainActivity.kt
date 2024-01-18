package com.bravoromeo.wallet_functionality

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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

@Composable
fun Greeting(
    modifier: Modifier = Modifier,
    viewModel: AppViewModel? = null,
    activity: Activity? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize()
    ){
        Text(
            text="Hello!",
            modifier=modifier
                .padding(bottom = 12.dp)
        )
        if (viewModel?.appState?.isWalletAvailable ?: true) WalletButton {
            if (activity != null) {
                viewModel?.savePassToWallet(activity = activity)
                //viewModel?.updateClassAtWallet(activity = activity)
            }
        }
    }
}

@Preview(showBackground=true)
@Composable
fun GreetingPreview() {
    Wallet_functionalityTheme {
        Greeting()
    }
}
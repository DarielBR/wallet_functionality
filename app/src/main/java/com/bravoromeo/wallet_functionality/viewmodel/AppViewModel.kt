package com.bravoromeo.wallet_functionality.viewmodel

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.android.gms.pay.Pay
import com.google.android.gms.pay.PayApiAvailabilityStatus
import com.google.android.gms.pay.PayClient
import java.util.Date
import java.util.UUID
import kotlin.random.Random

class AppViewModel (
    private val activity: Activity
): ViewModel(){
    private var walletClient: PayClient= Pay.getClient(activity)

    var appState by mutableStateOf(AppState())
        private set

    init {
        fetchWalletApiStatus()
    }

    private fun fetchWalletApiStatus(){
        walletClient.getPayApiAvailabilityStatus(PayClient.RequestType.SAVE_PASSES)
            .addOnSuccessListener {status ->
                if (status == PayApiAvailabilityStatus.AVAILABLE)appState = appState.copy(isWalletAvailable = true)
            }
            .addOnFailureListener {
                //TODO show warning message to user about the availability of Wallet
                appState = appState.copy(isWalletAvailable = false)
            }
    }

    //This data should be obtained via repositories, its hard-coded here for learning purposes only
    private val issuerEmail = "dbombinorevuelta@gmail.com"
    private val issuerId = "3388000000022299939"
    private val passClass = "3388000000022299939.1a9245d0-5a53-4f6f-aef1-3e8296d4a26a"
    private val passId = UUID.randomUUID().toString()

    private val newObjectJson = """
    {
      "iss": "$issuerEmail",
      "aud": "google",
      "typ": "savetowallet",
      "iat": ${Date().time / 1000L},
      "origins": [],
      "payload": {
        "genericObjects": [
          {
            "id": "$issuerId.$passId",
            "classId": "$passClass",
            "genericType": "GENERIC_TYPE_UNSPECIFIED",
            "hexBackgroundColor": "#4285f4",
            "logo": {
              "sourceUri": {
                "uri": "https://storage.googleapis.com/wallet-lab-tools-codelab-artifacts-public/pass_google_logo.jpg"
              }
            },
            "cardTitle": {
              "defaultValue": {
                "language": "en",
                "value": "Google I/O '22  [DEMO ONLY]"
              }
            },
            "subheader": {
              "defaultValue": {
                "language": "en",
                "value": "Attendee"
              }
            },
            "header": {
              "defaultValue": {
                "language": "en",
                "value": "Alex McJacobs"
              }
            },
            "barcode": {
              "type": "QR_CODE",
              "value": "$passId"
            },
            "heroImage": {
              "sourceUri": {
                "uri": "https://storage.googleapis.com/wallet-lab-tools-codelab-artifacts-public/google-io-hero-demo-only.jpg"
              }
            },
            "textModulesData": [
              {
                "header": "POINTS",
                "body": "${Random.nextInt(0, 9999)}",
                "id": "points"
              },
              {
                "header": "CONTACTS",
                "body": "${Random.nextInt(1, 99)}",
                "id": "contacts"
              }
            ]
          }
        ]
      }
    }
    """
    fun savePassToWallet(){
        walletClient.savePasses(newObjectJson, activity, appState.addToGoogleWalletRequestCode)
    }
}

data class AppState (
    var isWalletAvailable: Boolean = false,
    var addToGoogleWalletRequestCode: Int = 999
)
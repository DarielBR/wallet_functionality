package com.bravoromeo.wallet_functionality.viewmodel

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.runtime.getValue
import androidx.compose.runtime.internal.composableLambdaInstance
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bravoromeo.wallet_functionality.repositories.google_wallet.WalletApiConfig
import com.bravoromeo.wallet_functionality.repositories.google_wallet.WalletRepository
import com.google.android.gms.pay.PayApiAvailabilityStatus
import com.google.android.gms.pay.PayClient
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import kotlin.random.Random

class AppViewModel (
    private var walletClient: PayClient,
    private var requestCode: Int
): ViewModel(){
    private var walletRepository =  WalletRepository()
    var appState by mutableStateOf(AppState())
        private set

    init {
        walletRepository = WalletRepository()
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
    private val issuerId = "3388000000022308286"//"3388000000022299939"
    private val passClass = "3388000000022308286.class_generic_demo"//"3388000000022299939.1a9245d0-5a53-4f6f-aef1-3e8296d4a26a"
    private val passId = UUID.randomUUID().toString()

    private val newObjectJson = """
    {
      "iss": "$issuerEmail",
      "aud": "google",
      "typ": "savetowallet",
      "iat": ${Date().time / 1000L},
      "origins": ["www.diusframi.es"],
      "payload": {
        "genericObjects": [
          {
            "id": "$issuerId.$passId",
            "classId": "$passClass",
            "genericType": "GENERIC_TYPE_UNSPECIFIED",
            "hexBackgroundColor": "#001C83",
            "logo": {
              "sourceUri": {
                "uri": "https://raw.githubusercontent.com/DarielBR/wallet_functionality/master/online_resources/logo_color.png"
              }
            },
            "cardTitle": {
              "defaultValue": {
                "language": "es",
                "value": "TARJETA DE FIDELIDAD"
              }
            },
            "subheader": {
              "defaultValue": {
                "language": "es",
                "value": "USUARIO"
              }
            },
            "header": {
              "defaultValue": {
                "language": "es",
                "value": "John Doe"
              }
            },
            "barcode": {
              "type": "QR_CODE",
              "value": "$passId",
              "alternateText":"$passId"
            },
            "heroImage": {
              "sourceUri": {
                "uri": "https://raw.githubusercontent.com/DarielBR/wallet_functionality/master/online_resources/logo_dius_color.png"
              }
            },
            "textModulesData": [
              {
                "header": "ID",
                "body": "$passId",
                "id": "id"
              },
              {
                "header": "PUNTOS",
                "body": "${Random.nextInt(1, 99)}",
                "id": "points"
              }
            ]
          }
        ]
      }
    }
    """

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun savePassToWallet(context: Context){
        //val unsignedJWT = walletRepository.createUnsignedJWT("", context)
        val unsignedJWT = walletRepository.createPassAndUnsignedJWT("0987654321-qwertyuiop")
        if (unsignedJWT != null) {
            val activity = context as? Activity
            if (activity != null){
                walletClient.savePasses(unsignedJWT, activity, requestCode)
            }
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun saveLoyaltyPassToWallet(context: Context){
        //val unsignedJWT = walletRepository.createUnsignedJWT("", context)
        val unsignedJWT = walletRepository.createLoyaltyPassAndUnsignedJWT(appState.currentLoyaltyPassId)
        if (unsignedJWT != null) {
            val activity = context as? Activity
            if (activity != null){
                walletClient.savePasses(unsignedJWT, activity, requestCode)
            }
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun createDemoClass2(context: Context){
        viewModelScope.launch {
            walletRepository.createGenericClass(context = context) { /*TODO show message via Toast*/ }
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun createLoyaltyDemoClass(context: Context){
        viewModelScope.launch {
            walletRepository.createLoyaltyDemoClass(context = context) { /*TODO show message via Toast*/ }
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun updateDemoClass2(context: Context){
        viewModelScope.launch {
            walletRepository.updateGenericClass(context = context){/*TODO show message via Toast*/}
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun updateLoyaltyDemoClass(context: Context){
        viewModelScope.launch {
            walletRepository.updateLoyaltyClass(context = context){/*TODO idem*/}
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun updateLoyaltyPass(passId: String, context: Context){
        viewModelScope.launch {
            walletRepository.updateLoyaltyPass(context = context, passId = passId){}
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun addMessageToLoyaltyClass(context: Context){
        viewModelScope.launch {
            walletRepository.addMessageToLoyaltyClass(context = context){/*TODO: idem*/}
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun addMessageToLoyaltyPass(context: Context, passId: String){
        viewModelScope.launch {
            walletRepository.addMessageToLoyaltyPass(passId = passId, context = context){/*TODO: idem*/}
        }
    }

    /*region State Handling*/
    fun onCurrentLoyaltyPassIdChange(newValue: String){
        appState = appState.copy(currentLoyaltyPassId = newValue)
    }
    /*endregion*/
}

data class AppState (
    var isWalletAvailable: Boolean = false,
    var currentLoyaltyPassId: String = "loyalty-1234567890"

)
/*
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
            "hexBackgroundColor": "#001C83",
            "cardTitle": {
              "defaultValue": {
                "language": "es",
                "value": "GRUPO Diusframi"
              }
            },
            "subheader": {
              "defaultValue": {
                "language": "es",
                "value": "DEMO WALLET PASS"
              }
            },
            "header": {
              "defaultValue": {
                "language": "es",
                "value": "MUESTRA PARA PRUEBA"
              }
            },
            "barcode": {
              "type": "QR_CODE",
              "value": "$passId"
            },
            "heroImage": {
              "sourceUri": {
                "uri": "https://www.diusframi.es/wp-content/uploads/2022/03/logo_dius_color.png"
              }
            },
            "textModulesData": [
              {
                "header": "CAMPO1",
                "body": "${Random.nextInt(0, 9999)}",
                "id": "points"
              },
              {
                "header": "CAMPO2",
                "body": "${Random.nextInt(1, 99)}",
                "id": "contacts"
              }
            ]
          }
        ]
      }
    }
    """
    "genericClasses": [
          {
            "id": "$passClass",
            "classTemplateInfo": {
              "cardTemplateOverride": {
                "cardRowTemplateInfos": [
                  {
                    "oneItem": {
                      "item" : {
                        "firstValue" : {
                          "fields": [
                            {
                              "fieldPath": "object.textModulesData['id']",
                            }
                          ]
                        }
                      }
                    },
                    "oneItem": {
                      "item": {
                        "firstValue": {
                          "fields": [
                            {
                              "fieldPath": "object.textModulesDta['points']",
                            }
                          ]
                        }
                      }
                    }
                  }
                ]
              }
            }
          }
        ],
    */
package com.bravoromeo.wallet_functionality.repositories.google_wallet

import android.content.Context
import android.net.http.HttpException
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Date

class WalletSolRed {
    private val googleAuthorization = GoogleAuthorization()
    private val walletApiConfig = WalletApiConfig()

    private val solRedClassSuffix = "solred_loyalty_"//wrongly set for safety.
    private val classEndpoint = "loyaltyClass"
    private val objectEndpoint = "loyaltyObject"

    /*region API Functionality*/
    /**
     * Creates a new Loyalty class via Google Wallet Client REST API.
     *
     * @param onResult a lambda function to retrieve information related to function result.
     * @throws HttpException for Http request to the API Endpoint.
     */
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun createSolRedClass(
        context: Context,
        onResult: (String) -> Unit
    ){
        //AssetsManager instance to access necessary to access the Google Service Account key.json file
        val inputStream = googleAuthorization.getInputStream(context)
        //Load Google Credentials to access the REST API
        val googleCredentials: GoogleCredentials?
        if (inputStream != null) {
            googleCredentials = googleAuthorization.createGoogleCredentials(inputStream)
        } else {
            onResult.invoke("Process failure while creating Google Credentials.")
            Log.e("Key.Json file", "inputStream is not loading key.json content.")
            return
        }
        //To access the Wallet Client REST API a signed session must be created.
        val httpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val accessToken = googleCredentials.accessToken?.tokenValue
                val newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $accessToken")
                    .build()
                chain.proceed(newRequest)
            }
            .build()

        //Create Json structure for the genericClass
        val classId = "${walletApiConfig.issuerID}.$solRedClassSuffix"
        val classJson = createClassJson()
        //Same as before, http request wont be allowed in the main thread.
        GlobalScope.launch(Dispatchers.IO){
            try {
                val getRequest = Request.Builder()
                    .url("${walletApiConfig.baseApiRESTUrl}/$classEndpoint/$classId")
                    .get()
                    .build()
                httpClient.newCall(getRequest).execute().use { response ->
                    if (response.isSuccessful) { //class exists
                        onResult.invoke("Class $solRedClassSuffix already exists for Issuer ${walletApiConfig.issuerID}'s account.")
                        Log.e("Google Wallet REST API", "class $solRedClassSuffix already exists.")
                        return@launch//exiting the function
                    }
                    //if the program reached this point: the class does not exist.
                    val postRequest = Request.Builder()
                        .url("${walletApiConfig.baseApiRESTUrl}/$classEndpoint")
                        .post(classJson.toRequestBody("application/json".toMediaTypeOrNull()))
                        .build()

                    httpClient.newCall(postRequest).execute().use { postResponse ->
                        if (postResponse.isSuccessful) {
                            onResult.invoke("Class $classId created successfully for the Issuer ${walletApiConfig.issuerID}'s account.")
                            Log.e("Google Wallet REST API", "class $solRedClassSuffix created successfully.")
                            return@launch
                        } else {
                            onResult.invoke("Failure creating the class $solRedClassSuffix")
                            Log.e("Google Wallet REST API", "failure creating the class $solRedClassSuffix.")
                            return@launch
                        }
                    }
                }
            } catch (e: HttpException) {
                onResult.invoke("Failure while creating Loyalty Class.")
                Log.e("Google Wallet REST API", "error: ${e.localizedMessage}")
            }
        }
    }
    /**
     * Updates a generic class vía Google Wallet Client REST API.
     *
     * @param onResult a lambda function for retrieve information related to the function result.
     * @throws HttpException for Http requests to the API Endpoint.
     */
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun updateSolRedClass(
        context: Context,
        onResult: (String) -> Unit
    ){
        //AssetsManager instance to access necessary to access the Google Service Account key.json file
        val inputStream = googleAuthorization.getInputStream(context)
        //Load Google Credentials to access the REST API
        val googleCredentials: GoogleCredentials?
        if (inputStream != null) {
            googleCredentials = googleAuthorization.createGoogleCredentials(inputStream)
        } else {
            onResult.invoke("Process failure while creating Google Credentials.")
            Log.e("Key.Json file", "inputStream is not loading key.json content.")
            return
        }
        //To access the Wallet Client REST API a signed session must be created.
        val httpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val accessToken = googleCredentials.accessToken?.tokenValue
                val newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $accessToken")
                    .build()
                chain.proceed(newRequest)
            }
            .build()

        //Create Json structure for the genericClass
        val classId = "${walletApiConfig.issuerID}.$solRedClassSuffix"
        val classJson = createClassJson()
        //Http request wont be allowed in the main thread.
        GlobalScope.launch(Dispatchers.IO){
            try {
                val getRequest = Request.Builder()
                    .url("${walletApiConfig.baseApiRESTUrl}/$classEndpoint/$classId")
                    .get()
                    .build()
                httpClient.newCall(getRequest).execute().use { responseToGet ->
                    if (responseToGet.isSuccessful) { //class exists, and update can be made
                        Log.e("Google Wallet REST API", "Get to SolRed class Endpoint successful, ${responseToGet.networkResponse}")
                        val putRequest = Request.Builder()
                            .url("${walletApiConfig.baseApiRESTUrl}/$classEndpoint/$classId")
                            .put(classJson.toRequestBody("application/json".toMediaTypeOrNull()))
                            .build()
                        httpClient.newCall(putRequest).execute().use { responseToPut ->
                            if (responseToPut.isSuccessful){
                                onResult.invoke("Class $solRedClassSuffix updated successfully.")
                                Log.e("Google Wallet REST API", "class $solRedClassSuffix updated successfully.")
                                return@launch
                            }else{
                                onResult.invoke("Failure while updating class $solRedClassSuffix")
                                Log.e("Google Wallet REST API", "failure while updating class $solRedClassSuffix: ${responseToPut.networkResponse}.")
                                return@launch
                            }
                        }
                    } else {//class does not exist, update cannot be done.
                        onResult.invoke("Class $solRedClassSuffix do not exists for the Issuer ${walletApiConfig.issuerID}'s account.")
                        Log.e("Google Wallet REST API", "class $solRedClassSuffix do not exists.")
                        return@launch
                    }
                }
            } catch (e: HttpException) {
                onResult.invoke("Failure while creating Generic Class.")
                Log.e("Google Wallet REST API", "error: ${e.localizedMessage}")
            }
        }
    }
    /**
     * Updates a loyalty pass vía Google Wallet Client REST API.
     * @param cardId unique card identifier, also used to generate QR code.
     * @param balance balance following the Wallet API specification for "money" type.
     * @param onResult a lambda function for retrieve information related to the function result.
     * @throws HttpException for Http requests to the API Endpoint.
     */
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun updateSolRedCard(
        cardId: String,
        balance: Int,
        solredLoyaltyClass: Int,
        context: Context,
        onResult: (String) -> Unit
    ){
        //AssetsManager instance to access necessary to access the Google Service Account key.json file
        val inputStream = googleAuthorization.getInputStream(context)
        //Load Google Credentials to access the REST API
        val googleCredentials: GoogleCredentials?
        if (inputStream != null) {
            googleCredentials = googleAuthorization.createGoogleCredentials(inputStream)
        } else {
            onResult.invoke("Process failure while creating Google Credentials.")
            Log.e("Key.Json file", "inputStream is not loading key.json content.")
            return
        }
        //To access the Wallet Client REST API a signed session must be created.
        val httpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val accessToken = googleCredentials.accessToken?.tokenValue
                val newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $accessToken")
                    .build()
                chain.proceed(newRequest)
            }
            .build()

        //Create Json structure for the genericClass
        val solRedCardId = "${walletApiConfig.issuerID}.$cardId"
        val solRedCardJson = createSolRedCardJson(cardId = cardId, balance = balance, solredLoyaltyClass = solredLoyaltyClass)
        //Http request wont be allowed in the main thread.
        GlobalScope.launch(Dispatchers.IO){
            try {
                val getRequest = Request.Builder()
                    .url("${walletApiConfig.baseApiRESTUrl}/$objectEndpoint/$solRedCardId")
                    .get()
                    .build()
                httpClient.newCall(getRequest).execute().use { responseToGet ->
                    if (responseToGet.isSuccessful) { //class exists, and update can be made
                        Log.e("Google Wallet REST API", "Get to SolRed Card endpoint successful, ${responseToGet.networkResponse}")
                        val putRequest = Request.Builder()
                            .url("${walletApiConfig.baseApiRESTUrl}/$objectEndpoint/$solRedCardId")
                            .put(solRedCardJson.toRequestBody("application/json".toMediaTypeOrNull()))
                            .build()
                        httpClient.newCall(putRequest).execute().use { responseToPut ->
                            if (responseToPut.isSuccessful){
                                onResult.invoke("Card $cardId updated successfully.")
                                Log.e("Google Wallet REST API", "card $cardId updated successfully.")
                                return@launch
                            }else{
                                onResult.invoke("Failure while updating card $cardId")
                                Log.e("Google Wallet REST API", "failure while updating card $cardId: ${responseToPut.networkResponse}.")
                                return@launch
                            }
                        }
                    } else {//class does not exist, update cannot be done.
                        onResult.invoke("Card $cardId do not exists for the Issuer ${walletApiConfig.issuerID}'s account.")
                        Log.e("Google Wallet REST API", "card do not exists.")
                        return@launch
                    }
                }
            } catch (e: HttpException) {
                onResult.invoke("Failure while getting SolRed Card.")
                Log.e("Google Wallet REST API", "error: ${e.localizedMessage}")
            }
        }
    }

    /*endregion*/

    /*region Json Functionality*/
    private fun createClassJson(): String{
        return """
        {
            "id": "${walletApiConfig.issuerID}.$solRedClassSuffix",
            "issuerName": "REPSOL",
            "reviewStatus": "UNDER_REVIEW",
            "programName": "SolRed Points Card",
            "multipleDevicesAndHoldersAllowedStatus": "ONE_USER_ALL_DEVICES",
            "enableSmartTap": 1,
            "redemptionIssuers": ['${walletApiConfig.issuerID}'],
            "securityAnimation": {
                "animationType": "FOIL_SHIMMER"
            },
            "programLogo": {
                "sourceUri": {
                    "uri": "https://raw.githubusercontent.com/DarielBR/wallet_functionality/master/online_resources/solred_logo_solid.png"
                },
                "contentDescription": {
                    "defaultValue": {
                        "language": "en-US",
                        "value": "LOGO_IMAGE_DESCRIPTION"
                    }
                }
            },
            "hexBackgroundColor": "#024975",
            "heroImage": {
                "sourceUri": {
                    "uri": "https://raw.githubusercontent.com/DarielBR/wallet_functionality/master/online_resources/solred_2_hero.png"
                },
                "contentDescription": {
                    "defaultValue": {
                        "language": "en-US",
                        "value": "HERO_IMAGE_DESCRIPTION"
                    }
                }
            }
        }
        """.trimIndent()
    }

    fun createSolRedCardUnsignedJWT(cardId: String, balance: Int, solredLoyaltyClass: Int): String {
        return """
        {
          "iss": "dbombinorevuelta@gmail.com",
          "aud": "google",
          "typ": "savetowallet",
          "iat": "${Date().time / 1000L}",
          "origins": ["www.diusframi.es"],
          "payload": {
            "loyaltyObjects": [${createSolRedCardJson(cardId = cardId, balance = balance, solredLoyaltyClass = solredLoyaltyClass)}]
          }
        }
        """
    }

    private fun createSolRedCardJson(
        cardId: String,
        balance: Int? = null,
        solredLoyaltyClass: Int,
    ): String{
        return if(solredLoyaltyClass == 0) """
        {
            "id": "${walletApiConfig.issuerID}.$cardId",
            "classId": "${walletApiConfig.issuerID}.$solRedClassSuffix${solredLoyaltyClass + 1}",
            "state": "ACTIVE",
            "loyaltyPoints": {
                "balance":{
                    "money": {
                        "micros": $balance,
                        "currencyCode": "EUR"
                    }
                },
                "localizedLabel": {
                    "defaultValue": {
                        "language": "es-ES",
                        "value": "Balance"
                    }
                }
            },
            "barcode": {
                "type": "QR_CODE",
                "value": "$cardId",
                "alternateText": "$cardId"
            },
            "groupingInfo": {
                "sortIndex": 1,
                "groupingId": "solred_loyalty_01"
            },
            "textModulesData": [
                {
                    "header": "Balance",
                    "body": $balance, 
                    "id": "balance"
                }
            ]
        }            
        """.trimIndent()
        else """
        {
            "id": "${walletApiConfig.issuerID}.$cardId",
            "classId": "${walletApiConfig.issuerID}.$solRedClassSuffix${solredLoyaltyClass + 1}",
            "state": "ACTIVE",
            "loyaltyPoints": {
                "balance":{
                    "int": $balance
                },
                "localizedLabel": {
                    "defaultValue": {
                        "language": "es-ES",
                        "value": "Balance"
                    }
                }
            },
            "barcode": {
                "type": "QR_CODE",
                "value": "$cardId",
                "alternateText": "$cardId"
            },
            "groupingInfo": {
                "sortIndex": 1,
                "groupingId": "solred_loyalty_01"
            },
            "textModulesData": [
                {
                    "header": "Balance",
                    "body": $balance, 
                    "id": "balance"
                }
            ]
        }
        """.trimIndent()
    }
    /*endregion*/
}
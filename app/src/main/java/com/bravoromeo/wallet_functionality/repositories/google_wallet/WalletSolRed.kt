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

class WalletSolRed {
    val googleAuthorization = GoogleAuthorization()
    val walletApiConfig = WalletApiConfig()

    private val solRedClassSuffix = "solred_loyalty_1"
    private val classEndpoint = "loyaltyClass"
    private val objectEndpoint = "loyaltyObject"

    /*region API Functionality*/
    /**
     * Creates a new Loyalty class via Google Wallet Client REST API.
     *
     * @param newLoyaltyClass a string with the Loyalty class data in a json structure.
     * @param onResult a lambda function to retrieve information related to function result.
     * @throws HttpException for Http request to the API Endpoint.
     */
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun createLoyaltyDemoClass(
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
                    .url("${walletApiConfig.baseApiRESTUrl}/loyaltyClass/$newLoyaltyClassId")
                    .get()
                    .build()
                httpClient.newCall(getRequest).execute().use { response ->
                    if (response.isSuccessful) { //class exists
                        onResult.invoke("Class $newLoyaltyClassId already exists for Issuer $issuerId's account.")
                        Log.e("Google Wallet REST API", "class $newLoyaltyClassId already exists.")
                        return@launch//exiting the function
                    }
                    //if the program reached this point: the class does not exist.
                    val postRequest = Request.Builder()
                        .url("${walletApiConfig.baseApiRESTUrl}/loyaltyClass")
                        .post(newLoyaltyClassJson.toRequestBody("application/json".toMediaTypeOrNull()))
                        .build()

                    httpClient.newCall(postRequest).execute().use { postResponse ->
                        if (postResponse.isSuccessful) {
                            onResult.invoke("Class $newLoyaltyClassId created successfully for the Issuer $issuerId's account.")
                            Log.e("Google Wallet REST API", "class $newLoyaltyClassId created successfully.")
                            return@launch
                        } else {
                            onResult.invoke("Failure creating the class $newLoyaltyClassId")
                            Log.e("Google Wallet REST API", "failure creating the class $newLoyaltyClassId.")
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
    /*endregion*/

    /*region Json Functionality*/
    private fun createClassJson(): String{
        return """
        {
            "id": "${walletApiConfig.issuerID}.$solRedClassSuffix",
            "issuerName": "Grupo Diusframi",
            "reviewStatus": "UNDER_REVIEW",
            "programName": "Sol Red Balance Card",
            "multipleDevicesAndHoldersAllowedStatus": "ONE_USER_ALL_DEVICES",
            "enableSmartTap": 1,
            "redemptionIssuers": ['${walletApiConfig.issuerID}'],
            "securityAnimation": {
                "animationType": "FOIL_SHIMMER"
            },
            "programLogo": {
                "sourceUri": {
                    "uri": "https://raw.githubusercontent.com/DarielBR/wallet_functionality/master/online_resources/solred_logo.png"
                },
                "contentDescription": {
                    "defaultValue": {
                        "language": "en-US",
                        "value": "LOGO_IMAGE_DESCRIPTION"
                    }
                }
            },
            "hexBackgroundColor": "#001c83",
            "heroImage": {
                "sourceUri": {
                    "uri": "https://raw.githubusercontent.com/DarielBR/wallet_functionality/master/online_resources/hero_image.png"
                },
                "contentDescription": {
                    "defaultValue": {
                        "language": "en-US",
                        "value": "HERO_IMAGE_DESCRIPTION"
                    }
                }
            },
            "classTemplateInfo":{
                "cardTemplateOverride":{
                    "cardRowTemplateInfos":[
                        {   
                            "oneItem":{
                                "item":{
                                    "firstValue":{
                                        "fields":[
                                            {
                                                "fieldPath":"object.textModulesData['pass_id']"
                                            }
                                        ]
                                    }
                                }
                            }
                        },
                        {
                            "oneItem":{
                                "item":{
                                    "firstValue":{
                                        "fields":[
                                            {
                                                "fieldPath":"object.textModulesData['points']"
                                            }
                                        ]
                                    }
                                }
                            }
                        },
                        {
                            "oneItem":{
                                "item":{
                                    "firstValue":{
                                        "fields":[
                                            {
                                                "fieldPath":"object.textModulesData['expiration']"
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
        """.trimIndent()
    }
    /*endregion*/
}
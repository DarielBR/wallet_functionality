package com.bravoromeo.wallet_functionality.repositories.google_wallet

import android.content.Context
import android.net.http.HttpException
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import com.google.api.services.walletobjects.model.LoyaltyPointsBalance
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.io.InputStream
import java.util.Date
import kotlin.random.Random

class WalletRepository() {
    //Define constants
    private val issuerId = "3388000000022308286"
    private val classSuffix = "class_generic_demo"
    private val loyaltyClassSuffix = "class_loyalty_demo_2"
    private val classId = "$issuerId.$classSuffix"
    private val walletApiConfig = WalletApiConfig()

    /*region Common*/
    /**
     * Access to the Assets Manager to retrieve the key.json file necessary to create credentials
     *
     * @param context TODO: must be resolved using DI (further refactoring).
     * @return a key.json file InputStream
     */
    private fun getInputStream(context: Context): InputStream?{
        val assetManager = context.assets
        return try {
            assetManager.open("wallet-functionality-cf582752e480.json")
        } catch (e: IOException){
            e.printStackTrace()
            null
        }
    }

    /**
     * Creates Google OAuth credentials from a given key in a json format.
     *
     * @param inputStream containing a json string with a private security key
     * @return GoogleCredentials
     */
    private fun createGoogleCredentials(inputStream: InputStream): GoogleCredentials{
        return runBlocking {
            withContext(Dispatchers.IO){
                GoogleCredentials.fromStream(inputStream)
                    .createScoped(walletApiConfig.walletScopes).also {
                        it.refresh()
                    }
            }
        }
    }
    /*endregion*/

    /*region Generic*/
    /**
     * (For Development Scenario Only) Creates a Generic Class in Google Pay & Wallet Console using
     * Wallet REST API and Google OAuth for authentication.
     */
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun createGenericClass(
        newGenericClass: String? = null,
        context: Context,
        onResult: (String) -> Unit
    ){
        //AssetsManager instance to access necessary to access the Google Service Account key.json file
        val inputStream = getInputStream(context)
        //Load Google Credentials to access the REST API
        val googleCredentials: GoogleCredentials?
        if (inputStream != null) {
            googleCredentials = createGoogleCredentials(inputStream)
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
        val genericClass = demoClassJson(classId = classId)
        //Same as before, http request wont be allowed in the main thread.
        GlobalScope.launch(Dispatchers.IO){
            try {
                val getRequest = Request.Builder()
                    .url("${walletApiConfig.baseApiRESTUrl}/genericClass/$classId")
                    .get()
                    .build()
                httpClient.newCall(getRequest).execute().use { response ->
                    if (response.isSuccessful) { //class exists
                        onResult.invoke("Class $classSuffix already exists for Issuer $issuerId's account.")
                        Log.e("Google Wallet REST API", "class $classId already exists.")
                        return@launch//exiting the function
                    }
                    //if the program reached this point: the class does not exist.
                    val postRequest = Request.Builder()
                        .url("${walletApiConfig.baseApiRESTUrl}/genericClass")
                        .post(genericClass.toRequestBody("application/json".toMediaTypeOrNull()))
                        .build()

                    httpClient.newCall(postRequest).execute().use { postResponse ->
                        if (postResponse.isSuccessful) {
                            onResult.invoke("Class $classSuffix created successfully for the Issuer $issuerId's account.")
                            Log.e("Google Wallet REST API", "class $classId created successfully.")
                            return@launch
                        } else {
                            onResult.invoke("Failure creating the class $classId")
                            Log.e("Google Wallet REST API", "failure creating the class $classId.")
                            return@launch
                        }
                    }
                }
            } catch (e: HttpException) {
                onResult.invoke("Failure while creating Generic Class.")
                Log.e("Google Wallet REST API", "error: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Updates a generic class vía Google Wallet Client REST API.
     *
     * @param modifiedGenericClass a string with the class data in a json structure.
     * @param onResult a lambda function for retrieve information related to the function result.
     * @throws HttpException for Http requests to the API Endpoint.
     */
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun updateGenericClass(
        modifiedGenericClass: String? = null,
        context: Context,
        onResult: (String) -> Unit
    ){
        //AssetsManager instance to access necessary to access the Google Service Account key.json file
        val inputStream = getInputStream(context)
        //Load Google Credentials to access the REST API
        val googleCredentials: GoogleCredentials?
        if (inputStream != null) {
            googleCredentials = createGoogleCredentials(inputStream)
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
        val genericClass = demoClassJson(classId = classId)
        //Http request wont be allowed in the main thread.
        GlobalScope.launch(Dispatchers.IO){
            try {
                val getRequest = Request.Builder()
                    .url("${walletApiConfig.baseApiRESTUrl}/genericClass/$classId")
                    .get()
                    .build()
                httpClient.newCall(getRequest).execute().use { responseToGet ->
                    if (responseToGet.isSuccessful) { //class exists, and update can be made
                        val putRequest = Request.Builder()
                            .url("${walletApiConfig.baseApiRESTUrl}/genericClass/$classId")
                            .put(genericClass.toRequestBody("application/json".toMediaTypeOrNull()))
                            .build()
                        httpClient.newCall(putRequest).execute().use { responseToPut ->
                            if (responseToPut.isSuccessful){
                                onResult.invoke("Class $classId updated successfully.")
                                Log.e("Google Wallet REST API", "class $classId updated successfully.")
                                return@launch
                            }else{
                                onResult.invoke("Failure while updating class $classSuffix")
                                Log.e("Google Wallet REST API", "failure while updating $classId class.")
                                return@launch
                            }
                        }
                    } else {//class does not exist, update cannot be done.
                        onResult.invoke("Class $classSuffix do not exists for the Issuer $issuerId's account.")
                        Log.e("Google Wallet REST API", "class $classId do not exists.")
                        return@launch
                    }
                }
            } catch (e: HttpException) {
                onResult.invoke("Failure while creating Generic Class.")
                Log.e("Google Wallet REST API", "error: ${e.localizedMessage}")
            }
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun updateSolRedClass(
        context: Context,
        onResult: (String) -> Unit
    ){
        //AssetsManager instance to access necessary to access the Google Service Account key.json file
        val inputStream = getInputStream(context)
        //Load Google Credentials to access the REST API
        val googleCredentials: GoogleCredentials?
        if (inputStream != null) {
            googleCredentials = createGoogleCredentials(inputStream)
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
        val solRedClassJson = solredClassJson()
        //Http request wont be allowed in the main thread.
        GlobalScope.launch(Dispatchers.IO){
            try {
                val getRequest = Request.Builder()
                    .url("${walletApiConfig.baseApiRESTUrl}/genericClass/$issuerId.solred_balance_card")
                    .get()
                    .build()
                httpClient.newCall(getRequest).execute().use { responseToGet ->
                    if (responseToGet.isSuccessful) { //class exists, and update can be made
                        val putRequest = Request.Builder()
                            .url("${walletApiConfig.baseApiRESTUrl}/genericClass/$issuerId.solred_balance_card")
                            .put(solRedClassJson.toRequestBody("application/json".toMediaTypeOrNull()))
                            .build()
                        httpClient.newCall(putRequest).execute().use { responseToPut ->
                            if (responseToPut.isSuccessful){
                                onResult.invoke("Class SolRed updated successfully.")
                                Log.e("Google Wallet REST API", "class SolRed updated successfully.")
                                return@launch
                            }else{
                                onResult.invoke("Failure while updating class SolRed")
                                Log.e("Google Wallet REST API", "failure while updating SolRed class.")
                                return@launch
                            }
                        }
                    } else {//class does not exist, update cannot be done.
                        onResult.invoke("Class SolRed do not exists for the Issuer $issuerId's account.")
                        Log.e("Google Wallet REST API", "class SolRed do not exists.")
                        return@launch
                    }
                }
            } catch (e: HttpException) {
                onResult.invoke("Failure while updating SolRed Class.")
                Log.e("Google Wallet REST API", "error: ${e.localizedMessage}")
            }
        }
    }
    fun demoClassJson(classId: String): String{
        val result = """
        {
            "id": "$classId",
            "enableSmartTap": 1,
            "redemptionIssuers": ['3388000000022308286'],
            "multipleDevicesAndHoldersAllowedStatus": 'ONE_USER_ALL_DEVICES',
            "classTemplateInfo": {
                "cardTemplateOverride": {
                    "cardRowTemplateInfos": [
                        {
                            "oneItem": {
                                "item": {
                                    "firstValue": {
                                        "fields": [
                                            {
                                                "fieldPath": "object.textModulesData['id']",
                                            }
                                        ]
                                    }
                                }
                            }
                        },
                        {
                            "oneItem": {
                                "item": {
                                    "firstValue": {
                                        "fields": [
                                            {
                                                "fieldPath": "object.textModulesData['points']",
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
        """
        return result
    }

    /*fun solredClassJson(): String{
        val result = """
        {
            "id": "3388000000022308286.solred_balance_card",
            "enableSmartTap": 1,
            "redemptionIssuers": ['3388000000022308286'],
            "multipleDevicesAndHoldersAllowedStatus": 'ONE_USER_ALL_DEVICES',
            "imageModulesData": [
                {
                    "mainImage": {
                        "sourceUri": {
                            "uri": "https://raw.githubusercontent.com/DarielBR/wallet_functionality/master/online_resources/solred_card_1.png"
                        },
                        "contentDescription": {
                            "defaultValue": {
                                "language": "en-US",
                                "value": "CARD_ART"
                            }
                        }
                    },
                    "id": "card_art_01"
                }
            ]
        }
        """
        return result
    }*/
    fun solredClassJson(): String{
        val result = """
        {
            "id": "$issuerId.solred_balance_card",
            "enableSmartTap": 1,
            "redemptionIssuers": ['3388000000022308286'],
            "multipleDevicesAndHoldersAllowedStatus": 'ONE_USER_ALL_DEVICES',
            "classTemplateInfo": {
                "cardTemplateOverride": {
                    "cardRowTemplateInfos": [
                        {
                            "oneItem": {
                                "item": {
                                    "firstValue": {
                                        "fields": [
                                            {
                                                "fieldPath": "object.textModulesData['balance']"
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
        """
        return result
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun createGenericPass(
        context: Context,
        onResult: (String?) -> Unit
    ){
        //val randomID = UUID.randomUUID().toString()
        val staticPassID = "1234567890-qwertyuiop"
        //val passID = "$issuerId.$randomID"
        val passID = "$issuerId.$staticPassID"
        val assetsManager = context.assets
        var fileInputStream: InputStream? = try {
            assetsManager.open("wallet-functionality-faebe65ba462.json")
        }catch (e: IOException){
            Log.e("Key.json fiel", "error: ${e.localizedMessage}")
            onResult.invoke(null)
            //onReturn.invoke(null)
            return
        }
        val googleCredentials: GoogleCredentials?
        if (fileInputStream != null){
            runBlocking{
                val tempGoogleCredentials = withContext(Dispatchers.IO){
                    GoogleCredentials.fromStream(fileInputStream)
                        .createScoped(walletApiConfig.walletScopes).also {
                            it.refresh()
                        }
                }
                googleCredentials = tempGoogleCredentials
            }
        }else{
            //onResult.invoke("Process failure while creating Google Credentials.")
            Log.e("Key.Json file", "inputStream is not loading key.json content.")
            onResult.invoke(null)
            return
        }
        //In this point we are ready to create a httpClient with Google Authentication.
        val httpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val accessToken = googleCredentials?.accessToken?.tokenValue
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()
                chain.proceed(request)
            }
            .build()

        //Creating the Generic Pass Object json
        val genericPass = """
        {
          "id": $passID,
          "classId": "$classId",
          "logo": {
            "sourceUri": {
              "uri": "https://raw.githubusercontent.com/DarielBR/wallet_functionality/master/online_resources/logo_color.png",
            },
            "contentDescription": {
              "defaultValue": {
                "language": "en-US",
                "value": "LOGO_IMAGE_DESCRIPTION",
              },
            },
          },
          "cardTitle": {
            "defaultValue": {
              "language": "en-US",
              "value": "Grupo Diusframi",
            },
          },
          "subheader": {
            "defaultValue": {
              "language": "en-US",
              "value": "USER",
            },
          },
          "header": {
            "defaultValue": {
              "language": "en-US",
              "value": "Doe, John",
            },
          },
          "textModulesData": [
            {
              "id": "id",
              "header": "ID",
              "body": "$staticPassID",
            },
            {
              "id": "points",
              "header": "POINTS",
              "body": "${Random.nextInt(1,99)}",
            },
          ],
          "barcode": {
            "type": "QR_CODE",
            "value": "BARCODE_VALUE",
            "alternateText": "$staticPassID",
          },
          "hexBackgroundColor": "#001c83",
          "heroImage": {
            "sourceUri": {
              "uri": "https://raw.githubusercontent.com/DarielBR/wallet_functionality/master/online_resources/hero_image.png",
            },
            "contentDescription": {
              "defaultValue": {
                "language": "en-US",
                "value": "HERO_IMAGE_DESCRIPTION",
              },
            },
          },
        }
        """
        //Creating pass object in the Wallet Client REST API
        GlobalScope.launch(Dispatchers.IO) {
            try{
                val getRequest = Request.Builder()
                    .url("${walletApiConfig.baseApiRESTUrl}/genericObject/$staticPassID")
                    .get()
                    .build()
                httpClient.newCall(getRequest).execute().use { responseToGet ->
                    if (responseToGet.isSuccessful){
                        Log.e("Google Wallet REST API", "pass already exists.")
                        onResult.invoke(passID)
                        return@launch
                    }
                    val postRequest = Request.Builder()
                        .url("${walletApiConfig.baseApiRESTUrl}/genericObject")
                        .post(genericPass.toRequestBody("application/json".toMediaTypeOrNull()))
                        .build()
                    httpClient.newCall(postRequest).execute().use { responseToPost ->
                        if (responseToPost.isSuccessful){
                            //onResult.invoke("Pass $passID created successfully for the Issuer $issuerId's account.")
                            Log.e("Google Wallet REST API", "pass created successfully.")
                            onResult.invoke(passID)
                            return@launch
                        } else {
                            //onResult.invoke("Failure creating the pass $passID")
                            Log.e("Google Wallet REST API", "failure creating the pass.")
                            onResult.invoke(null)
                            return@launch
                        }
                    }
                }
            } catch (e: HttpException){
                //onResult.invoke("Failure while creating Generic Pass Object.")
                Log.e("Google Wallet REST API", "error: ${e.localizedMessage}")
                onResult.invoke(null)
                return@launch
            }
        }
    }

    /**
     *Creates a signed JWT token to me used via Web or other types of front-end application different
     * from an Android Application. For Android Applications the SDK must be used with and unsigned token.
     */
    fun createSignedTokenLink(passId: String): String{
        //TODO: create functionality to sign a JWT and obtain a http address with a token.
        return ""
    }
    /*endregion*/

    /* region Loyalty */

    /**
     * Creates a new Loyalty class via Google Wallet Client REST API.
     *
     * @param newLoyaltyClass a string with the Loyalty class data in a json structure.
     * @param onResult a lambda function to retrieve information related to function result.
     * @throws HttpException for Http request to the API Endpoint.
     */
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun createLoyaltyDemoClass(
        newLoyaltyClass: String? = null,
        context: Context,
        onResult: (String) -> Unit
    ){
        //AssetsManager instance to access necessary to access the Google Service Account key.json file
        val inputStream = getInputStream(context)
        //Load Google Credentials to access the REST API
        val googleCredentials: GoogleCredentials?
        if (inputStream != null) {
            googleCredentials = createGoogleCredentials(inputStream)
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
        val newLoyaltyClassId = "$issuerId.$loyaltyClassSuffix"
        val newLoyaltyClassJson = createDemoLoyaltyClassJson(issuerId = issuerId, classId = loyaltyClassSuffix)
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

    /**
     * Updates a generic class vía Google Wallet Client REST API.
     *
     * @param modifiedGenericClass a string with the class data in a json structure.
     * @param onResult a lambda function for retrieve information related to the function result.
     * @throws HttpException for Http requests to the API Endpoint.
     */
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun updateLoyaltyClass(
        modifiedGenericClass: String? = null,
        context: Context,
        onResult: (String) -> Unit
    ){
        //AssetsManager instance to access necessary to access the Google Service Account key.json file
        val inputStream = getInputStream(context)
        //Load Google Credentials to access the REST API
        val googleCredentials: GoogleCredentials?
        if (inputStream != null) {
            googleCredentials = createGoogleCredentials(inputStream)
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
        val loyaltyClassId = "$issuerId.$loyaltyClassSuffix"
        val loyaltyClass = demoLoyaltyClassJson(classId = classId)
        //Http request wont be allowed in the main thread.
        GlobalScope.launch(Dispatchers.IO){
            try {
                val getRequest = Request.Builder()
                    .url("${walletApiConfig.baseApiRESTUrl}/loyaltyClass/$loyaltyClassId")
                    .get()
                    .build()
                httpClient.newCall(getRequest).execute().use { responseToGet ->
                    if (responseToGet.isSuccessful) { //class exists, and update can be made
                        Log.e("Google Wallet REST API", "Get to loyaltyClass Endpoint successful, ${responseToGet.networkResponse}")
                        val putRequest = Request.Builder()
                            .url("${walletApiConfig.baseApiRESTUrl}/loyaltyClass/$loyaltyClassId")
                            .put(loyaltyClass.toRequestBody("application/json".toMediaTypeOrNull()))
                            //.patch(loyaltyClass.toRequestBody("application/json".toMediaTypeOrNull()))
                            .build()
                        httpClient.newCall(putRequest).execute().use { responseToPut ->
                            if (responseToPut.isSuccessful){
                                onResult.invoke("Class $classSuffix updated successfully.")
                                Log.e("Google Wallet REST API", "class updated successfully.")
                                return@launch
                            }else{
                                onResult.invoke("Failure while updating class $classSuffix")
                                Log.e("Google Wallet REST API", "failure while updating class: ${responseToPut.networkResponse}.")
                                return@launch
                            }
                        }
                    } else {//class does not exist, update cannot be done.
                        onResult.invoke("Class $classSuffix do not exists for the Issuer $issuerId's account.")
                        Log.e("Google Wallet REST API", "class do not exists.")
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
     *
     * @param modifiedPass a string with the pass data in a json structure.
     * @param passId a string with the pass object id with the form <ISSUERID>.<PASSID>
     * @param onResult a lambda function for retrieve information related to the function result.
     * @throws HttpException for Http requests to the API Endpoint.
     */
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun updateLoyaltyPass(
        passId: String,
        modifiedPass: String? = null,
        context: Context,
        onResult: (String) -> Unit
    ){
        //AssetsManager instance to access necessary to access the Google Service Account key.json file
        val inputStream = getInputStream(context)
        //Load Google Credentials to access the REST API
        val googleCredentials: GoogleCredentials?
        if (inputStream != null) {
            googleCredentials = createGoogleCredentials(inputStream)
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
        val loyaltyPassId = "$issuerId.$passId"
        val loyaltyPass = createLoyaltyPass(passObjectId = passId, points = 10)
        //Http request wont be allowed in the main thread.
        GlobalScope.launch(Dispatchers.IO){
            try {
                val getRequest = Request.Builder()
                    .url("${walletApiConfig.baseApiRESTUrl}/loyaltyObject/$loyaltyPassId")
                    .get()
                    .build()
                httpClient.newCall(getRequest).execute().use { responseToGet ->
                    if (responseToGet.isSuccessful) { //class exists, and update can be made
                        Log.e("Google Wallet REST API", "Get to loyaltyPass Endpoint successful, ${responseToGet.networkResponse}")
                        val putRequest = Request.Builder()
                            .url("${walletApiConfig.baseApiRESTUrl}/loyaltyObject/$loyaltyPassId")
                            .put(loyaltyPass.toRequestBody("application/json".toMediaTypeOrNull()))
                            .build()
                        httpClient.newCall(putRequest).execute().use { responseToPut ->
                            if (responseToPut.isSuccessful){
                                onResult.invoke("Pass $loyaltyPassId updated successfully.")
                                Log.e("Google Wallet REST API", "pass updated successfully.")
                                return@launch
                            }else{
                                onResult.invoke("Failure while updating pass $loyaltyPassId")
                                Log.e("Google Wallet REST API", "failure while updating pass: ${responseToPut.networkResponse}.")
                                return@launch
                            }
                        }
                    } else {//class does not exist, update cannot be done.
                        onResult.invoke("Pass $loyaltyPassId do not exists for the Issuer $issuerId's account.")
                        Log.e("Google Wallet REST API", "pass do not exists.")
                        return@launch
                    }
                }
            } catch (e: HttpException) {
                onResult.invoke("Failure while getting Loyalty pass.")
                Log.e("Google Wallet REST API", "error: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Adds a message to a Loyalty Class via Google Wallet REST API.
     *
     * @param messageJSON a string with the message data ina json structure.
     * @param classId receipt of the message.
     * @param onResult a lambda function for retrieve information related to the function result.
     * @throws HttpException for Http requests to the API Endpoint.
     */
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun addMessageToLoyaltyClass(
        messageJSON: String? = null,
        classId: String? = null,
        context: Context,
        onResult: (String) -> Unit
    ){
        val message: String = messageJSON ?: createMessage(
            messageId = "loyalty_message_001",
            messageBody = "Message Body."
        )
        val loyaltyClassId: String = classId ?: "$issuerId.$loyaltyClassSuffix"

        val inputStream = getInputStream(context)
        val googleCredentials: GoogleCredentials?
        if (inputStream != null) {
            googleCredentials = createGoogleCredentials(inputStream)
        } else {
            onResult.invoke("Process failure while creating Google Credentials.")
            Log.e("Key.Json file", "inputStream is not loading key.json content.")
            return
        }
        val httpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val accessToken = googleCredentials.accessToken.tokenValue
                val newRequest = chain.request().newBuilder()
                    .header("Authorization", "Bearer $accessToken")
                    .build()
                chain.proceed(newRequest)
            }
            .build()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val getRequest = Request.Builder()
                    .url("${walletApiConfig.baseApiRESTUrl}/loyaltyClass/$loyaltyClassId")
                    .get()
                    .build()
                httpClient.newCall(getRequest).execute().use { getResponse ->
                    if (getResponse.isSuccessful){//Loyalty class exists: message can be send.
                        val postRequest = Request.Builder()
                            .url("${walletApiConfig.baseApiRESTUrl}/loyaltyClass/$loyaltyClassId/addMessage")
                            .post(message.toRequestBody("application/json".toMediaTypeOrNull()))
                            .build()
                        httpClient.newCall(postRequest).execute().use { postResponse ->
                            if (postResponse.isSuccessful){
                                onResult.invoke("Success on adding message to class $classSuffix")
                                Log.e("Google Wallet REST API", "Success on adding message to class: ${postResponse.networkResponse}.")
                                return@launch
                            } else {
                                onResult.invoke("Failure while adding message to class $classSuffix")
                                Log.e("Google Wallet REST API", "failure while adding message to class: ${postResponse.networkResponse}.")
                                return@launch
                            }
                        }
                    }else{
                        onResult.invoke("Class $classSuffix do not exists for the Issuer $issuerId's account.")
                        Log.e("Google Wallet REST API", "class do not exists.")
                        return@launch
                    }
                }
            }catch (e: HttpException){
                onResult.invoke("Failure while adding message to Loyalty class $loyaltyClassSuffix.")
                Log.e("Google Wallet REST API", "add message error: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Lists all Loyalty classes created in our Issuer Account at Google Wallet API via REST API Endpoint.
     * @param onResult a lambda function for retrieve information related to the function result.
     * @return a list with all classes created for our Issuer Account
     * @throws HttpException
     * @see "https://developers.google.com/wallet/reference/rest/v1/loyaltyclass/list"
     */

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun listLoyaltyClasses(
        context: Context,
        onResult: (String) -> Unit
    ){
        val inputStream = getInputStream(context)
        val googleCredentials: GoogleCredentials?
        if (inputStream != null) {
            googleCredentials = createGoogleCredentials(inputStream)
        } else {
            onResult.invoke("Process failure while creating Google Credentials.")
            Log.e("Key.Json file", "inputStream is not loading key.json content.")
            return
        }
        val httpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val accessToken = googleCredentials.accessToken.tokenValue
                val newRequest = chain.request().newBuilder()
                    .header("Authorization", "Bearer $accessToken")
                    .build()
                chain.proceed(newRequest)
            }
            .build()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = HttpUrl.Builder()
                    .scheme("https")
                    .host("walletobjects.googleapis.com")
                    .addPathSegment("walletobjects")
                    .addPathSegment("v1")
                    .addPathSegment("loyaltyClass")
                    .addQueryParameter("issuerId", issuerId)
                    .build()
                val getRequest = Request.Builder()
                    .url(url)//.url("${walletApiConfig.baseApiRESTUrl}/loyaltyClass")
                    .get()
                    .build()
                httpClient.newCall(getRequest).execute().use { getResponse ->
                    if (getResponse.isSuccessful){
                        onResult.invoke("Success getting the classes: ${getResponse.body}")
                        Log.e("Google Wallet REST API", "Success getting the classes: ${getResponse.body}.")
                        return@launch
                    }else{
                        onResult.invoke("Failure while getting the Loyalty Classes.")
                        Log.e("Google Wallet REST API", "Failure while getting the classes: ${getResponse.networkResponse}.")
                        return@launch
                    }
                }
            } catch (e: HttpException) {
                onResult.invoke("Failure while fetching list of Loyalty Classes.")
                Log.e("Google Wallet REST API", "error: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Adds a message to a Loyalty Pass via Google Wallet REST API.
     *
     * @param messageJSON a string with the message data ina json structure.
     * @param passId receipt of the message.
     * @param onResult a lambda function for retrieve information related to the function result.
     * @throws HttpException for Http requests to the API Endpoint.
     */
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun addMessageToLoyaltyPass(
        messageJSON: String? = null,
        passId: String,
        context: Context,
        onResult: (String) -> Unit
    ){
        val message: String = messageJSON ?: createMessage(
            messageId = "loyalty_message_001",
            messageBody = "Message Body."
        )
        val loyaltyPassId: String = "$issuerId.$passId"

        val inputStream = getInputStream(context)
        val googleCredentials: GoogleCredentials?
        if (inputStream != null) {
            googleCredentials = createGoogleCredentials(inputStream)
        } else {
            onResult.invoke("Process failure while creating Google Credentials.")
            Log.e("Key.Json file", "inputStream is not loading key.json content.")
            return
        }
        val httpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val accessToken = googleCredentials.accessToken.tokenValue
                val newRequest = chain.request().newBuilder()
                    .header("Authorization", "Bearer $accessToken")
                    .build()
                chain.proceed(newRequest)
            }
            .build()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val getRequest = Request.Builder()
                    .url("${walletApiConfig.baseApiRESTUrl}/loyaltyObject/$loyaltyPassId")
                    .get()
                    .build()
                httpClient.newCall(getRequest).execute().use { getResponse ->
                    if (getResponse.isSuccessful){//Loyalty class exists: message can be send.
                        val postRequest = Request.Builder()
                            .url("${walletApiConfig.baseApiRESTUrl}/loyaltyObject/$loyaltyPassId/addMessage")
                            .post(message.toRequestBody("application/json".toMediaTypeOrNull()))
                            .build()
                        httpClient.newCall(postRequest).execute().use { postResponse ->
                            if (postResponse.isSuccessful){
                                onResult.invoke("Success on adding message to pass $loyaltyPassId")
                                Log.e("Google Wallet REST API", "Success on adding message to pass: ${postResponse.networkResponse}.")
                                return@launch
                            } else {
                                onResult.invoke("Failure while adding message to pass $loyaltyPassId")
                                Log.e("Google Wallet REST API", "failure while adding message to pass: ${postResponse.networkResponse}.")
                                return@launch
                            }
                        }
                    }else{
                        onResult.invoke("Class $classSuffix do not exists for the Issuer $issuerId's account.")
                        Log.e("Google Wallet REST API", "class do not exists.")
                        return@launch
                    }
                }
            }catch (e: HttpException){
                onResult.invoke("Failure while adding message to Loyalty Pass $loyaltyPassId.")
                Log.e("Google Wallet REST API", "add message error: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Expires a Loyalty Pass via Google Wallet REST API. (passes are not quite erased permanently.)
     *
     * @param passId receipt of the message.
     * @param onResult a lambda function for retrieve information related to the function result.
     * @throws HttpException for Http requests to the API Endpoint.
     */
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun expireLoyaltyPass(
        passId: String,
        context: Context,
        onResult: (String) -> Unit
    ){
        val loyaltyPassId: String = "$issuerId.$passId"
        val patchString = createLoyaltyPassExpiration(passId)

        val inputStream = getInputStream(context)
        val googleCredentials: GoogleCredentials?
        if (inputStream != null) {
            googleCredentials = createGoogleCredentials(inputStream)
        } else {
            onResult.invoke("Process failure while creating Google Credentials.")
            Log.e("Key.Json file", "inputStream is not loading key.json content.")
            return
        }
        val httpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val accessToken = googleCredentials.accessToken.tokenValue
                val newRequest = chain.request().newBuilder()
                    .header("Authorization", "Bearer $accessToken")
                    .build()
                chain.proceed(newRequest)
            }
            .build()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val getRequest = Request.Builder()
                    .url("${walletApiConfig.baseApiRESTUrl}/loyaltyObject/$loyaltyPassId")
                    .get()
                    .build()
                httpClient.newCall(getRequest).execute().use { getResponse ->
                    if (getResponse.isSuccessful){//Loyalty class exists: message can be send.
                        val postRequest = Request.Builder()
                            .url("${walletApiConfig.baseApiRESTUrl}/loyaltyObject/$loyaltyPassId")
                            .patch(patchString.toRequestBody("application/json".toMediaTypeOrNull()))
                            .build()
                        httpClient.newCall(postRequest).execute().use { patchResponse ->
                            if (patchResponse.isSuccessful){
                                onResult.invoke("Success on expiring pass $loyaltyPassId")
                                Log.e("Google Wallet REST API", "Success on expiring pass: ${patchResponse.networkResponse}.")
                                return@launch
                            } else {
                                onResult.invoke("Failure while expiring pass $loyaltyPassId")
                                Log.e("Google Wallet REST API", "failure while expiring pass: ${patchResponse.networkResponse}.")
                                return@launch
                            }
                        }
                    }else{
                        onResult.invoke("Pass $loyaltyPassId do not exists for the Issuer $issuerId's account.")
                        Log.e("Google Wallet REST API", "pass do not exists.")
                        return@launch
                    }
                }
            }catch (e: HttpException){
                onResult.invoke("Failure while adding message to Loyalty Pass $loyaltyPassId.")
                Log.e("Google Wallet REST API", "add message error: ${e.localizedMessage}")
            }
        }
    }

    private fun createDemoLoyaltyClassJson(issuerId: String, classId: String): String{
        return """
        {
            "programName": "Loyalty Demo Program",
            "programLogo": {
                "sourceUri": {
                    "uri": "https://raw.githubusercontent.com/DarielBR/wallet_functionality/master/online_resources/logo_color.png"
                },
                "contentDescription": {
                    "defaultValue": {
                        "language": "en-US",
                        "value": ""
                    }
                }
            },
            "id": "$issuerId.$classId",
            "issuerName": "Grupo Diusframi",
            "reviewStatus": "UNDER_REVIEW",
            "hexBackgroundColor": "#ffffff"
        }
        """.trimIndent()
    }

    private fun demoLoyaltyClassJson(classId: String): String{
        return """
        {
            "id": "$classId",
            "issuerName": "Grupo Diusframi",
            "reviewStatus": "UNDER_REVIEW",
            "programName": "Loyalty Demo Program",
            "multipleDevicesAndHoldersAllowedStatus": "ONE_USER_ALL_DEVICES",
            "enableSmartTap": 1,
            "redemptionIssuers": ['$issuerId'],
            "securityAnimation": {
                "animationType": "FOIL_SHIMMER"
            },
            "programLogo": {
                "sourceUri": {
                    "uri": "https://raw.githubusercontent.com/DarielBR/wallet_functionality/master/online_resources/logo_color.png"
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

    fun createPassAndUnsignedJWT(passObjectId: String): String {
        val innerClassId = classId
        //val innerClassId = "$issuerId.solred_balance_card"
        return """
        {
          "iss": "dbombinorevuelta@gmail.com",
          "aud": "google",
          "typ": "savetowallet",
          "iat": ${Date().time / 1000L},
          "origins": ["www.diusframi.es"],
          "payload": {
            "genericObjects": [
              {
                "id": "$issuerId.$passObjectId",
                "classId": "$innerClassId",
                "genericType": "GENERIC_TYPE_UNSPECIFIED",
                "hexBackgroundColor": "#001c83",
                "logo": {
                  "sourceUri": {
                    "uri": "https://raw.githubusercontent.com/DarielBR/wallet_functionality/master/online_resources/logo_color.png"
                  }
                },
                "cardTitle": {
                  "defaultValue": {
                    "language": "es",
                    "value": "Grupo Diusframi"
                  }
                },
                "subheader": {
                  "defaultValue": {
                    "language": "es",
                    "value": "USER"
                  }
                },
                "header": {
                  "defaultValue": {
                    "language": "es",
                    "value": "Doe, Jhon"
                  }
                },
                "barcode": {
                  "type": "QR_CODE",
                  "value": "$passObjectId",
                  "alternateText":"$passObjectId"
                },
                "heroImage": {
                  "sourceUri": {
                    "uri": "https://raw.githubusercontent.com/DarielBR/wallet_functionality/master/online_resources/hero_image.png"
                  }
                },
                "textModulesData": [
                  {
                    "header": "ID",
                    "body": "$passObjectId",
                    "id": "id"
                  },
                  {
                    "header": "POINTS",
                    "body": "${Random.nextInt(1, 99)}",
                    "id": "points"
                  }
                ]
              }
            ]
          }
        }
        """
    }

    private fun createLoyaltyPass(
        passObjectId: String,
        classId: String? = null,
        userName: String? = null,
        points: Int? = null
    ): String{
        val loyaltyClassId = classId ?: "$issuerId.$loyaltyClassSuffix"
        val userNameValue = userName ?: "John Doe"
        val pointsValue = points ?: 10

        return """
        {
            "id": "$issuerId.$passObjectId",
            "classId": "$loyaltyClassId",
            "state": "ACTIVE",
            "loyaltyPoints": {
              "balance": {
                "int": $points
              },
              "localizedLabel": {
                "defaultValue": {
                  "language": "en-US",
                    "value": "Puntos de recompensa"
                  }
              }
            },
            "barcode": {
              "type": "QR_CODE",
              "value": "$passObjectId",
              "alternateText": "$passObjectId"
            },
            "groupingInfo": {
              "sortIndex": 1,
              "groupingId": "loyalty-05"
            },
            "textModulesData": [
                {
                    "header": "PASS ID",
                    "body": "$passObjectId,
                    "id": "pass_id"
                },
                {
                    "header": "POINTS LABEL",
                    "body": $pointsValue, 
                    "id": "points"
                }
            ]
        }            
        """.trimIndent()
    }

    fun createSolRedCardAndUnsignedJWTOnOne(passObjectId: String, balance: String): String {
        //val innerClassId = classId
        val innerClassId = "$issuerId.solred_balance_card"
        return """
        {
            "iss": "dbombinorevuelta@gmail.com",
            "aud": "google",
            "typ": "savetowallet",
            "iat": ${Date().time / 1000L},
            "origins": ["www.diusframi.es"],
            "payload": {
                "genericObjects": [
                    {
                        "id": "$issuerId.$passObjectId",
                        "classId": "$innerClassId",
                        "genericType": "GENERIC_TYPE_UNSPECIFIED",
                        "hexBackgroundColor": "#ffffff",
                        "logo": {
                            "sourceUri": {
                                "uri": "https://raw.githubusercontent.com/DarielBR/wallet_functionality/master/online_resources/logo_color.png"
                            }
                        },
                        "cardTitle": {
                            "defaultValue": {
                                "language": "es",
                                "value": "SolRed"
                            }
                        },
                        "header": {
                            "defaultValue": {
                                "language": "es",
                                "value": "Debit Card"
                            }
                        },
                        "barcode": {
                            "type": "QR_CODE",
                            "value": "$passObjectId",
                            "alternateText":"$passObjectId"
                        },
                        "heroImage": {
                            "sourceUri": {
                                "uri": "https://raw.githubusercontent.com/DarielBR/wallet_functionality/master/online_resources/hero_image.png"
                            }
                        },
                        "textModulesData": [
                            {
                                "header": "BALANCE",
                                "body": "$balance",
                                "id": "balance"
                            }
                        ]
                    }
                ]
            }
        }
        """
    }
    private fun createSolRedCardJson(
        passObjectId: String,
        classId: String? = null,
        userName: String? = null,
        points: Int? = null
    ): String{
        return """
        {
                "id": "$issuerId.$passObjectId",
                "classId": "$issuerId.solred_balance_card",
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
                    "value": "Grupo Diusframi"
                  }
                },
                "subheader": {
                  "defaultValue": {
                    "language": "es",
                    "value": "USER"
                  }
                },
                "header": {
                  "defaultValue": {
                    "language": "es",
                    "value": "Doe, Jhon"
                  }
                },
                "barcode": {
                  "type": "QR_CODE",
                  "value": "$passObjectId",
                  "alternateText":"$passObjectId"
                },
                "heroImage": {
                  "sourceUri": {
                    "uri": "https://raw.githubusercontent.com/DarielBR/wallet_functionality/master/online_resources/hero_image.png"
                  }
                },
                "textModulesData": [
                  {
                    "header": "ID",
                    "body": "$passObjectId",
                    "id": "id"
                  },
                  {
                    "header": "POINTS",
                    "body": "${Random.nextInt(1, 99)}",
                    "id": "points"
                  }
                ]
        }            
        """.trimIndent()
    }

    /**
    "wideLogo": {
    "sourceUri": {
    "uri": "https://raw.githubusercontent.com/DarielBR/wallet_functionality/master/online_resources/solred_card_1.png"
    },
    "contentDescription": {
    "defaultValue": {
    "language": "en",
    "value": "WIDE_LOGO_IMAGE_DESCRIPTION"
    }
    }
    },

     */


    private fun createMessage(messageId: String, messageBody: String): String{
        return """
        {
            "message": {
                "header": "MessageHeader",
                "body": "$messageBody",
                "id": "$messageId",
                "messageType": "TEXT"
            }
        }
        """.trimIndent()
    }

    fun createLoyaltyPassExpiration(
        passObjectId: String,
        userName: String? = null
    ): String{
        val userNameValue = userName ?: "John Doe"
        return """
        {
            "id": "$issuerId.$passObjectId",
            "state": "EXPIRED",
            "textModulesData": [
                {
                    "header": "PASS ID",
                    "body": "$passObjectId",
                    "id": "pass_id"
                },
                {
                    "header": "EXPIRATION LABEL",
                    "body": "Expiration info", 
                    "id": "expiration"
                }
            ]
        }            
        """.trimIndent()
    }

    private fun createLoyaltyPassMessageExpiration(): String{
        return """
        {
            "message": {
                "header": "MESSAGE EXPIRATION HEADER",
                "body": "This pass has been expired by administrators",
                "id": "msg_loy_exp_01",
                "messageType": "TEXT"
            }
        }
        """.trimIndent()
    }

    fun createLoyaltyPassAndUnsignedJWT(passObjectId: String): String {
        return """
        {
          "iss": "dbombinorevuelta@gmail.com",
          "aud": "google",
          "typ": "savetowallet",
          "iat": "${Date().time / 1000L}",
          "origins": ["www.diusframi.es"],
          "payload": {
            "loyaltyObjects": [${createLoyaltyPass(passObjectId)}]
          }
        }
        """
    }

    fun createSolRedCardAndUnsignedJWT(passObjectId: String): String {
        return """
        {
          "iss": "dbombinorevuelta@gmail.com",
          "aud": "google",
          "typ": "savetowallet",
          "iat": "${Date().time / 1000L}",
          "origins": ["www.diusframi.es"],
          "payload": {
            "loyaltyObjects": [${createSolRedCardJson(passObjectId)}]
          }
        }
        """
    }
    /* endregion */
}


package com.bravoromeo.wallet_functionality.repositories.google_wallet

import android.content.Context
import android.net.http.HttpException
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.compose.ui.graphics.Outline
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.walletobjects.Walletobjects
import com.google.api.services.walletobjects.model.CardRowOneItem
import com.google.api.services.walletobjects.model.CardRowTemplateInfo
import com.google.api.services.walletobjects.model.CardTemplateOverride
import com.google.api.services.walletobjects.model.ClassTemplateInfo
import com.google.api.services.walletobjects.model.FieldReference
import com.google.api.services.walletobjects.model.FieldSelector
import com.google.api.services.walletobjects.model.GenericClass
import com.google.api.services.walletobjects.model.TemplateItem
import java.io.FileInputStream
import java.lang.Exception
import com.google.auth.*
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.io.InputStream

class WalletRepository() {
    //Define constants
    private val issuerId = "3388000000022308286"
    private val classSuffix = "class_generic_demo_3"
    private val classId = "$issuerId.$classSuffix"

    private val walletApiConfig = WalletApiConfig()

    /*private val assetManager = context.assets
    private val inputStream = assetManager.open("wallet-functionality-faebe65ba462.json")
    private val keyFilePath = System.getenv()["GOOGLE_APPLICATION_CREDENTIALS"] as InputStream ?: inputStream*/

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun createGenericClass(
        context: Context,
        onResult: (String) -> Unit
    ){
        val assetManager = context.assets
        //val inputStream = assetManager.open("wallet-functionality-faebe65ba462.json")
        //val keyFilePath = System.getenv()["GOOGLE_APPLICATION_CREDENTIALS"] as InputStream ?: inputStream
        val inputStream: InputStream? = try {
            assetManager.open("wallet-functionality-faebe65ba462.json")
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
        var googleCredentials: GoogleCredentials? = null
        if (inputStream != null) {
            runBlocking{
                val tempGoogleCredentials = withContext(Dispatchers.IO) {
                    GoogleCredentials.fromStream(inputStream)
                        .createScoped(walletApiConfig.walletScopes).also {
                            it.refresh()
                        }
                }
                googleCredentials = tempGoogleCredentials
            }
        // Continue processing
        } else {
            // Handle the case where the file couldn't be opened
            Log.e("Key.Json file", "inputStream is not loading key.json content.")
            return
        }

        /*val googleCredentials = GoogleCredentials.fromStream(inputStream)//originally keyFilePath
            .createScoped(walletApiConfig.walletScopes)
        googleCredentials.refreshIfExpired()*/

        val httpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val accessToken = googleCredentials?.accessToken?.tokenValue
                val newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $accessToken")
                    .build()
                chain.proceed(newRequest)
            }
            .build()

        //Create Json structure for the genericClass
        val genericClass = """
        {
          "id": "$classId",
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
                          },
                        ],
                      },
                    },
                  },
                },
                {
                  "oneItem": {
                    "item": {
                      "firstValue": {
                        "fields": [
                          {
                            "fieldPath": "object.textModulesData['points']",
                          },
                        ],
                      },
                    },
                  },
                },
              ],
            },
          },
        }
        """
        //action with the REST API
        GlobalScope.launch(Dispatchers.IO){
            try {
                val getRequest = Request.Builder()
                    .url(walletApiConfig.baseApiRESTUrl)
                    .get()
                    .build()
                httpClient.newCall(getRequest).execute().use { response ->
                    if (response.isSuccessful) { //class exists
                        onResult.invoke("Class $classSuffix already exists for Issuer $issuerId's account.")
                        Log.e("Google Wallet REST API", "class already exists.")
                        return@launch//exiting the function
                    }
                    //if the function reach this point: the class does not exist.
                    val postRequest = Request.Builder()
                        .url("${walletApiConfig.baseApiRESTUrl}/genericClass")
                        .post(genericClass.toRequestBody("application/json".toMediaTypeOrNull()))
                        .build()

                    httpClient.newCall(postRequest).execute().use { postResponse ->
                        if (postResponse.isSuccessful) {
                            onResult.invoke("Class $classSuffix created successfully for the Issuer $issuerId's account.")
                            Log.e("Google Wallet REST API", "class created successfully.")
                            return@launch
                        } else {
                            onResult.invoke("Failure creating the class $classSuffix")
                            Log.e("Google Wallet REST API", "failure creating the class.")
                            return@launch
                        }
                    }

                }
            } catch (e: HttpException) {
                Log.e("Google Wallet REST API", "error: ${e.localizedMessage}")
            }
        }
    }
}

/*
class DemoGeneric{
    private val walletApiConfig = WalletApiConfig()
    init {
        */
/**
         * TODO: Write comments to this function in order to explain thoroughly how the Google Auth works,
         * and all the previous necessary steps needed to configure the Service Account,
         * link that to the Wallet API via a key.json file, set up that Service Account as a authorized user
         * in the Google PAY & Wallet API Console, and all the other requisites that must be meet to make
         * this work.
         *//*


        keyFilePath = System.getenv()["GOOGLE_APPLICATION_CREDENTIALS"] ?: walletApiConfig.serviceAccountFile
        try {
            auth()
        }catch (e: Exception){
            throw RuntimeException(e)
        }
    }

    fun auth(){
        val currentCredentials = GoogleCredentials.fromStream(FileInputStream(keyFilePath))
            .createScoped(walletApiConfig.walletScopes)
        currentCredentials.refresh()
        credentials = currentCredentials

        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        walletService = Walletobjects.Builder(
            httpTransport,
            GsonFactory.getDefaultInstance(),
            HttpCredentialsAdapter(credentials)
        )
            .setApplicationName("com.bravoromeo.wallet_functionality")
            .build()
    }

    companion object{
        var keyFilePath: String? = null
        var credentials: GoogleCredentials? = null
        var walletService: Walletobjects? = null
    }

    */
/**
     * TODO: WRITE DOCUMENTATION
     *//*

    fun createClass(
        onResult: (String) -> Unit
    ){
        val issuerId = "3388000000022308286"
        val classSuffix = "class_generic_demo_2"
        val classId = "$issuerId.$classSuffix"

        //Checking if the generic class exist in account
        try{
            walletService?.genericclass()?.get(classId)?.execute()
            onResult.invoke("class $classSuffix already exist for Issuer $issuerId account.")
            return
        }catch (e: GoogleJsonResponseException){
            if (e.statusCode != 404){
                onResult.invoke(e.localizedMessage ?: "Unknown error while fetching the $classId from Issuer with ID $issuerId account.")
                return
            }
        }

        //NOS QUEDAMOS AQUI: HAY QUE DETERMINAR EN LA DOCUEMNTACION COMO CREAR DE MANERA CORRECTA UNA CLASE EN CON LA CLIENT API,
        //PRIMERO PROBAR ASI SOLO CON EL ID DE LA CLASE. LUEGO GENERAR UN PASE PARA ESA CLASE Y HACER LA ASOCIACION DESDE EL BOTÓN
        //SI NO FUNICOINA, HABRA QUE VER COMO GENERAR LA CLASE CON TODAS LAS PROPIEDADES REQUERIDAS
        //Reaching this point, the generic class does not exist, proceed to create it

        val newClass = GenericClass()
        newClass.setMultipleDevicesAndHoldersAllowedStatus("ONE_USER_ALL_DEVICES")

        try {
            walletService?.genericclass()?.insert(newClass)?.execute()
            onResult.invoke("class $classSuffix created for Issuer $issuerId account.")
            return
        }catch (e: GoogleJsonResponseException){
            onResult.invoke(e.localizedMessage ?: "Unknown error while inserting new $classId into Issuer with ID $issuerId account.")
            return
        }
    }
}*/

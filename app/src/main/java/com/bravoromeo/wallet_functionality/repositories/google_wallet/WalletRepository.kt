package com.bravoromeo.wallet_functionality.repositories.google_wallet

import androidx.compose.ui.graphics.Outline
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
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

class WalletRepository {
    //Define constants
    private val issuerId = "3388000000022308286"
    private val classSuffix = "class_generic_demo_2"
    private val classId = "$issuerId.$classSuffix"

    private val walletApiConfig = WalletApiConfig()
}

class DemoGeneric{
    private val walletApiConfig = WalletApiConfig()
    init {
        /**
         * TODO: Write comments to this function in order to explain thoroughly how the Google Auth works,
         * and all the previous necessary steps needed to configure the Service Account,
         * link that to the Wallet API via a key.json file, set up that Service Account as a authorized user
         * in the Google PAY & Wallet API Console, and all the other requisites that must be meet to make
         * this work.
         */

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

    /**
     * TODO: WRITE DOCUMENTATION
     */
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
}
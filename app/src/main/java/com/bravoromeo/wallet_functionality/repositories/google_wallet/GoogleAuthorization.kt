package com.bravoromeo.wallet_functionality.repositories.google_wallet

import android.content.Context
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream

class GoogleAuthorization {
    private val walletApiConfig = WalletApiConfig()
    /**
     * Access to the Assets Manager to retrieve the key.json file necessary to create credentials
     *
     * @param context TODO: must be resolved using DI (further refactoring).
     * @return a key.json file InputStream
     */
    fun getInputStream(context: Context): InputStream?{
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
    fun createGoogleCredentials(inputStream: InputStream): GoogleCredentials {
        return runBlocking {
            withContext(Dispatchers.IO){
                GoogleCredentials.fromStream(inputStream)
                    .createScoped(walletApiConfig.walletScopes).also {
                        it.refresh()
                    }
            }
        }
    }
}
package com.bravoromeo.wallet_functionality.repositories.google_wallet

/**
 * This class is used to store necessary parameters to access the Google Wallet REST API
 */
data class WalletApiConfig(
    val issuerID: String = "3388000000022308286",
    val serviceAccountMail: String = "wallet-functionality-1@wallet-functionality.iam.gserviceaccount.com",
    val serviceAccountFile: String = "C:\\Users\\dbombino\\AndroidStudioProjects\\research\\wallet_functionality_files\\iam_service_account_keys\\wallet-functionality-faebe65ba462.json",
    val origins: List<String> = listOf("www.diusframi.es"),
    val audience: String = "google",
    val jwtType: String = "savetowallet",
    val walletScopes: List<String> = listOf("https://www.googleapis.com/auth/wallet_object.issuer"),
    val baseApiRESTUrl: String = "https://walletobjects.googleapis.com/walletobjects/v1"
)
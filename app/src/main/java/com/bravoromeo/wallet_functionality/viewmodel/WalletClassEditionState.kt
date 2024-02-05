package com.bravoromeo.wallet_functionality.viewmodel

data class WalletClassEditionState(
    var name: String = "",
    var id: String = "",
    var enableSmartTap: Boolean = false,
    var redemptionIssuers: List<String> = emptyList(),
)

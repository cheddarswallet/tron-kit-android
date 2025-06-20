package io.cheddarswallet.tronkit.decoration

import io.cheddarswallet.tronkit.models.Address

open class Event(
    val transactionHash: ByteArray,
    val contractAddress: Address
) {
    open fun tags(userAddress: Address): List<String> = listOf()
}

data class TokenInfo(val tokenName: String, val tokenSymbol: String, val tokenDecimal: Int)

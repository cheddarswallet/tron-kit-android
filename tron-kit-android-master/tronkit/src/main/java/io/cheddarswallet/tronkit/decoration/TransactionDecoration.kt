package io.cheddarswallet.tronkit.decoration

import io.cheddarswallet.tronkit.models.Address

open class TransactionDecoration {
    open fun tags(userAddress: Address): List<String> = listOf()
}

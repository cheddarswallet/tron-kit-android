package io.cheddarswallet.tronkit.decoration.trc20

import io.cheddarswallet.tronkit.models.Address
import io.cheddarswallet.tronkit.decoration.TransactionDecoration
import io.cheddarswallet.tronkit.models.TransactionTag
import java.math.BigInteger

class ApproveTrc20Decoration(
    val contractAddress: Address,
    val spender: Address,
    val value: BigInteger
) : TransactionDecoration() {

    override fun tags(userAddress: Address): List<String> =
        listOf(contractAddress.hex, TransactionTag.TRC20_APPROVE)
}

package io.cheddarswallet.tronkit.decoration.trc20

import io.cheddarswallet.tronkit.decoration.TokenInfo
import io.cheddarswallet.tronkit.decoration.TransactionDecoration
import io.cheddarswallet.tronkit.models.Address
import io.cheddarswallet.tronkit.models.TransactionTag
import java.math.BigInteger

class OutgoingTrc20Decoration(
    val contractAddress: Address,
    val to: Address,
    val value: BigInteger,
    val sentToSelf: Boolean,
    val tokenInfo: TokenInfo?
) : TransactionDecoration() {

    override fun tags(userAddress: Address) = listOf(
        contractAddress.base58,
        TransactionTag.TRC20_TRANSFER,
        TransactionTag.trc20Outgoing(contractAddress.base58),
        TransactionTag.OUTGOING,
        TransactionTag.toAddress(to.hex)
    )

}

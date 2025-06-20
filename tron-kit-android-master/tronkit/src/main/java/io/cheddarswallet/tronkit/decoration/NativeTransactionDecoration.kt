package io.cheddarswallet.tronkit.decoration

import io.cheddarswallet.tronkit.models.Address
import io.cheddarswallet.tronkit.models.Contract
import io.cheddarswallet.tronkit.models.TransactionTag
import io.cheddarswallet.tronkit.models.TransferAssetContract
import io.cheddarswallet.tronkit.models.TransferContract

class NativeTransactionDecoration(
    val contract: Contract
) : TransactionDecoration() {

    override fun tags(userAddress: Address): List<String> {
        val tags = mutableListOf<String>()

        when (contract) {
            is TransferContract -> {
                tags.add(TransactionTag.TRX_COIN)

                if (contract.ownerAddress == userAddress) {
                    tags.add(TransactionTag.TRX_COIN_OUTGOING)
                    tags.add(TransactionTag.OUTGOING)
                    tags.add(TransactionTag.toAddress(contract.toAddress.hex))
                }
                if (contract.toAddress == userAddress) {
                    tags.add(TransactionTag.TRX_COIN_INCOMING)
                    tags.add(TransactionTag.INCOMING)
                    tags.add(TransactionTag.fromAddress(contract.ownerAddress.hex))
                }
            }

            is TransferAssetContract -> {
                tags.add(TransactionTag.TRC10)

                if (contract.ownerAddress == userAddress) {
                    tags.add(TransactionTag.trc10Outgoing(contract.assetName))
                    tags.add(TransactionTag.OUTGOING)
                    tags.add(TransactionTag.toAddress(contract.toAddress.hex))
                }
                if (contract.toAddress == userAddress) {
                    tags.add(TransactionTag.trc10Incoming(contract.assetName))
                    tags.add(TransactionTag.INCOMING)
                    tags.add(TransactionTag.fromAddress(contract.ownerAddress.hex))
                }
            }

            else -> {}
        }

        return tags
    }

}

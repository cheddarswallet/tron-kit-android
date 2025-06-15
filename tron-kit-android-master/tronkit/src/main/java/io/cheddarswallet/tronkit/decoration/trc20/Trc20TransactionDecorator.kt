package io.cheddarswallet.tronkit.decoration.trc20

import io.cheddarswallet.tronkit.models.Address
import io.cheddarswallet.tronkit.contracts.trc20.ApproveMethod
import io.cheddarswallet.tronkit.contracts.ContractMethodFactories
import io.cheddarswallet.tronkit.contracts.trc20.ApproveMethodFactory
import io.cheddarswallet.tronkit.contracts.trc20.TransferMethod
import io.cheddarswallet.tronkit.contracts.trc20.TransferMethodFactory
import io.cheddarswallet.tronkit.decoration.Event
import io.cheddarswallet.tronkit.decoration.ITransactionDecorator
import io.cheddarswallet.tronkit.decoration.TransactionDecoration
import io.cheddarswallet.tronkit.hexStringToByteArray
import io.cheddarswallet.tronkit.models.InternalTransaction
import io.cheddarswallet.tronkit.models.TriggerSmartContract

class Trc20TransactionDecorator(
    private val userAddress: Address
) : ITransactionDecorator {

    private val factories = ContractMethodFactories()

    init {
        factories.registerMethodFactories(listOf(TransferMethodFactory, ApproveMethodFactory))
    }

    override fun decoration(
        contract: TriggerSmartContract,
        internalTransactions: List<InternalTransaction>,
        events: List<Event>
    ): TransactionDecoration? {
        val contractMethod = factories.createMethodFromInput(contract.data.hexStringToByteArray())

        return when {
            contractMethod is TransferMethod && contract.ownerAddress == userAddress -> {
                val tokenInfo =
                    (events.firstOrNull { it is Trc20TransferEvent && it.contractAddress == contract.contractAddress } as? Trc20TransferEvent)?.tokenInfo
                OutgoingTrc20Decoration(
                    contractAddress = contract.contractAddress,
                    to = contractMethod.to,
                    value = contractMethod.value,
                    sentToSelf = contractMethod.to == userAddress,
                    tokenInfo = tokenInfo
                )
            }

            contractMethod is ApproveMethod -> {
                ApproveTrc20Decoration(
                    contractAddress = contract.contractAddress,
                    spender = contractMethod.spender,
                    value = contractMethod.value
                )
            }

            else -> null
        }
    }

}

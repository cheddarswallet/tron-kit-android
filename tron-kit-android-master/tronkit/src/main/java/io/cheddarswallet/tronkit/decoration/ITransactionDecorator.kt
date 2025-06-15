package io.cheddarswallet.tronkit.decoration

import io.cheddarswallet.tronkit.models.InternalTransaction
import io.cheddarswallet.tronkit.models.TriggerSmartContract

interface ITransactionDecorator {
    fun decoration(
        contract: TriggerSmartContract,
        internalTransactions: List<InternalTransaction>,
        events: List<Event>
    ): TransactionDecoration?
}

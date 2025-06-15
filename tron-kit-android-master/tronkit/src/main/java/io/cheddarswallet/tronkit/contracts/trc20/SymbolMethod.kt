package io.cheddarswallet.tronkit.contracts.trc20

import io.cheddarswallet.tronkit.contracts.ContractMethod


class SymbolMethod: ContractMethod() {
    override var methodSignature = "symbol()"
    override fun getArguments() = listOf<Any>()
}

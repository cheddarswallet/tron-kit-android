package io.cheddarswallet.tronkit.contracts.trc20

import io.cheddarswallet.tronkit.contracts.ContractMethod


class DecimalsMethod: ContractMethod() {
    override var methodSignature = "decimals()"
    override fun getArguments() = listOf<Any>()
}

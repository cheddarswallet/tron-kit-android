package io.cheddarswallet.tronkit.contracts.trc20

import io.cheddarswallet.tronkit.contracts.ContractMethod


class NameMethod: ContractMethod() {
    override var methodSignature = "name()"
    override fun getArguments() = listOf<Any>()
}

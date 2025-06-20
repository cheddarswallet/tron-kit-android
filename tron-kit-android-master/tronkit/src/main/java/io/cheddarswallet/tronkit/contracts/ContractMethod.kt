package io.cheddarswallet.tronkit.contracts

open class ContractMethod {
    val methodId: ByteArray by lazy { ContractMethodHelper.getMethodId(methodSignature) }

    protected open val methodSignature: String = ""

    fun encodedABI(): ByteArray {
        return ContractMethodHelper.encodedABI(methodId, getArguments())
    }

    open fun getArguments(): List<Any> = listOf()
}

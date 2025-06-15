package io.cheddarswallet.tronkit.contracts.trc20

import io.cheddarswallet.tronkit.models.Address
import io.cheddarswallet.tronkit.contracts.ContractMethodFactory
import io.cheddarswallet.tronkit.contracts.ContractMethodHelper
import io.cheddarswallet.tronkit.toBigInteger

object ApproveMethodFactory : ContractMethodFactory {

    override val methodId = ContractMethodHelper.getMethodId(ApproveMethod.methodSignature)

    override fun createMethod(inputArguments: ByteArray): ApproveMethod {
        val address = Address.fromRawWithoutPrefix(inputArguments.copyOfRange(12, 32))
        val value = inputArguments.copyOfRange(32, 64).toBigInteger()

        return ApproveMethod(address, value)
    }

}

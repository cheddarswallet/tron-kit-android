package io.cheddarswallet.tronkit.contracts.trc20

import io.cheddarswallet.tronkit.models.Address
import io.cheddarswallet.tronkit.contracts.ContractMethod
import java.math.BigInteger

class ApproveMethod(val spender: Address, val value: BigInteger) : ContractMethod() {

    override val methodSignature = Companion.methodSignature
    override fun getArguments() = listOf(spender, value)

    companion object {
        const val methodSignature = "approve(address,uint256)"
    }

}

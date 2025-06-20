package io.cheddarswallet.tronkit.contracts

import io.cheddarswallet.tronkit.toInt

open class ContractMethodFactories {

    private val methodFactories = mutableMapOf<Int, ContractMethodFactory>()

    fun registerMethodFactories(factories: List<ContractMethodFactory>) {
        factories.forEach { factory ->
            if (factory is ContractMethodsFactory) {
                factory.methodIds.forEach { methodId ->
                    methodFactories[methodId.toInt()] = factory
                }
            } else {
                methodFactories[factory.methodId.toInt()] = factory
            }
        }
    }

    fun createMethodFromInput(input: ByteArray): ContractMethod? {
        if (input.size < 4) return null

        val methodId = input.copyOfRange(0, 4)
        val methodFactory = methodFactories[methodId.toInt()]

        return methodFactory?.createMethod(input.copyOfRange(4, input.size))
    }
}

package io.cheddarswallet.tronkit.models

import io.cheddarswallet.tronkit.decoration.TransactionDecoration

class FullTransaction(
    val transaction: Transaction,
    val decoration: TransactionDecoration
)

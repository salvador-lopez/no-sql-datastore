package com.slopez.nosqldatastore.application.service

internal class OperationAgainstKeyHoldingWrongTypeOfValueException(
    override val message: String = "WRONGTYPE Operation against a key holding the wrong kind of value"
) : Exception(message)


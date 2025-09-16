package me.santio.nexus.data

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

fun buildByteArray(block: DataOutputStream.() -> Unit): ByteArray {
    return ByteArrayOutputStream().apply {
        DataOutputStream(this).apply(block)
    }.toByteArray()
}

fun ByteArray.input() = DataInputStream(ByteArrayInputStream(this))
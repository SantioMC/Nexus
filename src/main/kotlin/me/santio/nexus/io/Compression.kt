package me.santio.nexus.io

import com.github.luben.zstd.Zstd
import me.santio.nexus.data.buildByteArray
import me.santio.nexus.data.input

object Compression {

    fun compress(data: ByteArray, level: Int = Zstd.defaultCompressionLevel()): ByteArray {
        val compressed = Zstd.compress(data, level)
        return buildByteArray {
            writeInt(data.size)
            write(compressed)
        }
    }

    fun decompress(data: ByteArray): ByteArray {
        val reader = data.input()
        val buffer = ByteArray(reader.readInt())

        Zstd.decompress(buffer, reader.readAllBytes())
        return buffer
    }
}

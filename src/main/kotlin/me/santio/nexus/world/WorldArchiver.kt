package me.santio.nexus.world

import java.io.File
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

internal object WorldArchiver {

    val save = listOf("poi", "region")

    fun archive(folder: File, output: OutputStream) {
        val archive = ZipOutputStream(output)
        val children = save.map { folder.resolve(it) }.filter { it.exists() }

        fun addEntry(file: File) {
            check(file.isFile) { "The file provided in #addEntry(File) must be a file!" }

            val entry = ZipEntry(file.toRelativeString(folder))
            archive.putNextEntry(entry)
            archive.write(file.readBytes())
            archive.closeEntry()
        }

        for (child in children) {
            if (child.isFile) {
                addEntry(child)
                continue
            }

            child.walkTopDown().filter { it.isFile }.forEach { addEntry(it) }
        }

        output.close()
    }

    @Suppress("JvmTaintAnalysis") // - Acceptable
    fun restore(folder: File, data: ByteArray) = ZipInputStream(data.inputStream()).use { stream ->
        // Delete existing data in the folder
        save.map { folder.resolve(it) }.forEach { it.deleteRecursively() }

        while (true) {
            val entry = stream.nextEntry ?: break
            val path = folder.resolve(entry.name)
            if (!path.startsWith(folder)) continue
            if (!path.parentFile.exists()) path.parentFile.mkdirs()

            path.outputStream().use { stream.copyTo(it) }
            stream.closeEntry()
        }
    }

}
package me.santio.nexus.world

import com.google.auto.service.AutoService
import io.minio.PutObjectArgs
import io.minio.messages.Tags
import me.santio.nexus.service.Preload
import java.time.Duration
import java.time.Instant
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

// Surely, there must be a better way
@AutoService(Preload::class)
class WorldPreload: Preload {
    override fun preload() {
        Class.forName(Duration::class.java.name)
        Class.forName(Instant::class.java.name)
        Class.forName(ZipOutputStream::class.java.name)
        Class.forName(ZipEntry::class.java.name)
        Class.forName(PutObjectArgs.Builder::class.java.name)
        Class.forName(FileTreeWalk::class.java.name)
        Class.forName(Tags::class.java.name)
        Class.forName("kotlin.io.FilePathComponents")
        Class.forName("kotlin.io.FileTreeWalk\$FileTreeWalkIterator\$TopDownDirectoryState")
        Class.forName("kotlin.sequences.SequencesKt")
        Class.forName("kotlin.sequences.FilteringSequence")
        Class.forName("kotlin.sequences.FilteringSequence\$iterator$1")
    }
}
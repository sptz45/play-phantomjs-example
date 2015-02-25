package util

import java.io.{ InputStream, IOException }
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.file.{ Files, FileVisitResult, Path, SimpleFileVisitor, StandardOpenOption }
import java.nio.file.attribute.BasicFileAttributes

object FileUtils {

  def ensureDirectoryExists(dir: Path): Unit = {
    if (Files.exists(dir)) {
      if (Files.isDirectory(dir)) return
      else throw new IOException(s"The path '$dir' exists and it's not a directory!")
    }
    Files.createDirectories(dir)
  }

  def saveBytesAsync(bytes: Array[Byte], file: Path): Unit = {
    import StandardOpenOption._
    val out = AsynchronousFileChannel.open(file, WRITE, CREATE, TRUNCATE_EXISTING)
    out.write(ByteBuffer.wrap(bytes), 0)
  }

  def deleteRecursive(dir: Path): Unit = {
    Files.walkFileTree(dir, new SimpleFileVisitor[Path] {
      override def visitFile(file: Path, attrs: BasicFileAttributes) = {
        Files.delete(file)
        FileVisitResult.CONTINUE
      }
      override def postVisitDirectory(dir: Path , exc: IOException ) = {
        Files.delete(dir)
        FileVisitResult.CONTINUE
      }
    })
    Files.deleteIfExists(dir)
  }

  def list(dir: Path): Seq[Path] = {
    import scala.collection.JavaConverters._
    Files.list(dir).iterator.asScala.toSeq
  }

  def inputStreamOf(file: Path): InputStream = Files.newInputStream(file, StandardOpenOption.READ)
}
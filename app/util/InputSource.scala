package util

import java.io.{ InputStream, ByteArrayInputStream }
import java.nio.file.Path

trait InputSource {
  def getInputStream(): InputStream
}

object InputSource {

  def apply(bytes: Array[Byte]): InputSource = new InputSource {
    def getInputStream() = new ByteArrayInputStream(bytes)
  }

  def apply(file: Path): InputSource = new InputSource {
    def getInputStream() = FileUtils.inputStreamOf(file)
  }
}
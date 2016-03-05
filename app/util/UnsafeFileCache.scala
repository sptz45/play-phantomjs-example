package util

import java.nio.file.{ Path, Paths }
import scala.collection.mutable
import play.api.libs.Codecs


class UnsafeFileCache(cacheDir: String) {

  private val cacheDirectory = {
    val dir = Paths.get(cacheDir)
    FileUtils.ensureDirectoryExists(dir)
    dir
  }

  private var nextSeqNo = 0
  private var activeRegion, passiveRegion, deletedRegion: CacheRegion = EmptyCacheRegion

  def getFile(url: String): Option[Path] = {
    activeRegion.getFile(url).orElse(passiveRegion.getFile(url))
  }

  def putFile(url: String, bytes: Array[Byte]): Unit = {
    activeRegion.putFile(url, bytes)
  }

  def evictOldEntries(): Unit = {
    deletedRegion.purge()
    deletedRegion = passiveRegion
    passiveRegion = activeRegion
    activeRegion = newCacheRegion()
  }

  def evictAllEntries(): Unit = {
    deletedRegion.purge()
    passiveRegion.purge()
    activeRegion.purge()
    nextSeqNo = 0
    init()
  }

  def init(): Unit = {
    val existingRegions = FileUtils.list(cacheDirectory)
      .map(_.getFileName.toString)
      .filter(_.charAt(0).isDigit)
      .map(_.toInt)
      .sorted(Ordering[Int].reverse)

    if (existingRegions.length > 0) {
      activeRegion = newCacheRegion(existingRegions(0))
    } else {
      activeRegion = newCacheRegion()
    }

    if (existingRegions.length > 1) {
      passiveRegion = newCacheRegion(existingRegions(1))
    }

    if (existingRegions.length > 2) {
      deletedRegion = newCacheRegion(existingRegions(2))
    }
  }

  private def newCacheRegion(seqNo: Int = nextSeqNo) = {
    val region = new PathCacheRegion(cacheDirectory, seqNo)
    nextSeqNo += 1
    region
  }
}


private trait CacheRegion {
  def getFile(url: String): Option[Path]
  def putFile(url: String, bytes: Array[Byte]): Unit
  def purge(): Unit
}

private object EmptyCacheRegion extends CacheRegion {
  def getFile(url: String) = None
  def putFile(url: String, bytes: Array[Byte]) = sys.error("Cannot add file to EmptyCacheRegion")
  def purge() = ()
}

private class PathCacheRegion(cacheDir: Path, val seqNo: Int) extends CacheRegion {

  private val regionDir = {
    val dir = cacheDir.resolve(seqNo.toString)
    FileUtils.ensureDirectoryExists(dir)
    dir
  }

  private val cache = mutable.Set[String]()

  loadExistingFiles()

  def getFile(url: String): Option[Path] = {
    val filename = filenameOf(url)
    if (cache.contains(filename))
      Option(regionDir.resolve(filename))
    else
      None
  }

  def putFile(url: String, bytes: Array[Byte]): Unit = {
    val filename = filenameOf(url)
    if (!cache.contains(filename)) {
      cache += filename
      FileUtils.saveBytesAsync(bytes, regionDir.resolve(filename))
    }
  }

  def purge(): Unit = {
    FileUtils.deleteRecursive(regionDir)
    cache.clear()
  }

  private def filenameOf(url: String) = Codecs.sha1(url)

  private def loadExistingFiles() = {
    cache ++= FileUtils.list(regionDir).filterNot(_.startsWith(".")).map(_.getFileName.toString)
  }
}

package util

import scala.sys.process._
import org.apache.commons.codec.binary.Base64

object PhantomJs {

  def getWebsiteAsImage(url: String): Array[Byte] = {
    val cmd = s"$phantomjs scripts/capture.js $url"
    val base64Img = cmd.!!
    Base64.decodeBase64(base64Img.getBytes)
  }

  def getVersion(): String = s"$phantomjs --version".!!

  private def phantomjs = AppConfig.phantomJs
}

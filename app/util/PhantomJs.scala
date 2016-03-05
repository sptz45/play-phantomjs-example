package util

import scala.sys.process._
import org.apache.commons.codec.binary.Base64

class PhantomJs(execPath: String) {

  def getWebsiteAsImage(url: String): Array[Byte] = {
    val cmd = s"$execPath scripts/capture.js $url"
    val base64Img = cmd.!!
    Base64.decodeBase64(base64Img.getBytes)
  }

  def getVersion(): String = s"$execPath --version".!!
}

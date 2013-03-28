package gov.gsa.faq.api.security

import javax.xml.bind.DatatypeConverter

class BasicAuth {

  def decode(auth: String): Array[String] = {

    if (auth!=null) {
      val base64 = auth.replaceFirst("[B|b]asic ", "")
      val decodedBytes = DatatypeConverter.parseBase64Binary(base64)

      if (decodedBytes == null || decodedBytes.length == 0) {
        null
      } else {
        new String(decodedBytes).split(":", 2)
      }
    } else {
      null
    }
  }
}

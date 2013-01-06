package gov.gsa.faq.api

object Constants {

  val DATA_DIR = "/opt/jboss-ews-1.0/tomcat6/data/faqapi"
  val LOG_PATH = DATA_DIR + "/logs/faqapi.log"
  val XML_PATH = DATA_DIR + "/FAQ_EN.xml"
  val XML_PATH_ES = DATA_DIR + "/FAQ_ES.xml"
  val XML_PATHS = Array(XML_PATH,XML_PATH_ES)
  val CMS_ID_MAP = DATA_DIR + "/id.map"
  val SERVICES_PROPS_NAME = "services"
  val SERVICES_PROPS = DATA_DIR + "/" + SERVICES_PROPS_NAME +".properties"
}

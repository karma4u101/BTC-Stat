package se.media4u101.lib

import dispatch._ , Defaults._
import net.liftweb.common._
import net.liftweb.json.JsonAST._
import net.liftweb.util.{ Props }

object RestProxy extends Loggable {
  
  val poolAccountProfile:Box[String] = Props.get("pool.account.profile") 
  val blockchainInfoAddress:Box[String] = Props.get("blockchain.info.address")
  
  def fetchKapitonData() : JValue = {    
    kapitonJson
  }
  
  def fetchSlushPoolStat() : JValue = {    
    slushPoolJson
  }   
  
  def fetchWalletData() : JValue = {      
    walletJson
  }
  
  def fetchMtgoxSEK() : JValue = {
    mtgoxSEKJson
  }
  
  def fetchMtgoxUSD() : JValue = {  
    mtgoxUSDJson
  } 
  
  def fetchMtgoxEUR() : JValue = {  
    mtgoxEURJson
  }   

  def fetchAccProfileData():JValue = {    
    accProfileJson
  }  
  
  private[media4u101] def doScheduleJob():Unit = this.synchronized {
        doScheduledFetchAccProfileData()
        doScheduledfetchKapitonData()
        doScheduledfetchSlushPoolStat()
        doScheduledfetchWalletData()
        doScheduledfetchMtgoxSEK()
        doScheduledfetchMtgoxUSD()
        doScheduledfetchMtgoxEUR()    
  }  
 
    //side effect method could this be done in some other way ???
  private def doScheduledFetchAccProfileData():Unit = {
    import net.liftweb.json.JsonParser._
    //logger.debug("BTCRestHelper::fetchAccProfileData() start") 
    val start: Long = System.currentTimeMillis 
    val mySecureHost = host("mining.bitcoin.cz").secure
    val myRequest = mySecureHost / "accounts" / "profile" / "json" / poolAccountProfile.get 
    val response = Http(myRequest OK as.String)
    val json = parse(response())  
    val stop: Long = System.currentTimeMillis
    logger.debug("BTCRestHelper::fetchAccProfileData() done took "+((stop-start))+"ms")     
    accProfileJson = json 
  }  
  
  private def doScheduledfetchKapitonData() : Unit = {
    import net.liftweb.json.JsonParser._  
    //logger.debug("BTCRestHelper::fetchKapitonData() start")  
    val start: Long = System.currentTimeMillis
    val mySecureHost = host("kapiton.se").secure
    val myRequest = mySecureHost / "api" / "0" / "ticker" 
    val response = Http(myRequest OK as.String)
    val json = parse(response())  
    val stop: Long = System.currentTimeMillis
    logger.debug("BTCRestHelper::fetchKapitonData() done took "+((stop-start))+"ms")      
    kapitonJson = json
  }    
    
  private def doScheduledfetchSlushPoolStat() : Unit = {
    import net.liftweb.json.JsonParser._  
    //logger.debug("BTCRestHelper::fetchSlushPoolStat() start")  
    val start: Long = System.currentTimeMillis
    val mySecureHost = host("mining.bitcoin.cz").secure
    val myRequest = mySecureHost / "stats" / "json" / "" 
    val response = Http(myRequest OK as.String)
    val json = parse(response())  
    val stop: Long = System.currentTimeMillis
    logger.debug("BTCRestHelper::fetchSlushPoolStat() done took "+((stop-start))+"ms")   
    slushPoolJson = json     
  }
  
  private def doScheduledfetchWalletData() : Unit = {
    import net.liftweb.json.JsonParser._  
    //logger.debug("BTCRestHelper::fetchWalletData() start")  
    val start: Long = System.currentTimeMillis
    val mySecureHost = host("blockchain.info").secure
    val myRequest = mySecureHost / "address" / blockchainInfoAddress.get <<? ("format" -> "json") :: Nil
    val response = Http(myRequest OK as.String).option
    val json:JValue = parse(response().getOrElse("")) 
    val stop: Long = System.currentTimeMillis
    logger.debug("BTCRestHelper::fetchWalletData() start done took "+((stop-start))+"ms") 
    walletJson = json       
  }
  
  private def doScheduledfetchMtgoxSEK() : Unit = {
    import net.liftweb.json.JsonParser._  
    //logger.debug("BTCRestHelper::fetchMtgoxSEK() start") 
    val start: Long = System.currentTimeMillis
    val myHost = host("www.mtgox.com").secure
    val myRequest = myHost / "api" / "2" / "BTCSEK" / "money" / "ticker_fast"
    val response = Http(myRequest OK as.String)
    val json = parse(response())  
    val stop: Long = System.currentTimeMillis
    logger.debug("BTCRestHelper::fetchMtgoxSEK() done took "+((stop-start))+"ms")  
    mtgoxSEKJson = json    
  }
  
  private def doScheduledfetchMtgoxUSD() : Unit = {
    import net.liftweb.json.JsonParser._  
    //logger.debug("BTCRestHelper::fetchMtgoxUSD() start") 
    val start: Long = System.currentTimeMillis
    val myHost = host("www.mtgox.com").secure
    val myRequest = myHost / "api" / "2" / "BTCUSD" / "money" / "ticker_fast"
    val response = Http(myRequest OK as.String)
    val json = parse(response())  
    val stop: Long = System.currentTimeMillis
    logger.debug("BTCRestHelper::fetchMtgoxUSD() done took "+((stop-start))+"ms") 
    mtgoxUSDJson = json    
  }

  private def doScheduledfetchMtgoxEUR() : Unit = {
    import net.liftweb.json.JsonParser._  
    //logger.debug("BTCRestHelper::fetchMtgoxEUR() start") 
    val start: Long = System.currentTimeMillis
    val myHost = host("www.mtgox.com").secure
    val myRequest = myHost / "api" / "2" / "BTCEUR" / "money" / "ticker_fast"
    val response = Http(myRequest OK as.String)
    val json = parse(response())  
    val stop: Long = System.currentTimeMillis
    logger.debug("BTCRestHelper::fetchMtgoxEUR() done took "+((stop-start))+"ms")     
    mtgoxEURJson = json    
  }   
  
  @volatile private var mtgoxEURJson:JValue = null
  @volatile private var mtgoxUSDJson:JValue = null
  @volatile private var mtgoxSEKJson:JValue = null
  @volatile private var walletJson:JValue = null
  @volatile private var slushPoolJson:JValue = null
  @volatile private var kapitonJson:JValue = null
  @volatile private var accProfileJson:JValue = null
  
}
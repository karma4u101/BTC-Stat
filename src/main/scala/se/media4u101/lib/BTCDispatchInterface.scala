package se.media4u101.lib

import dispatch._ , Defaults._
import net.liftweb.common._
import net.liftweb.json.JsonAST._

trait BTCDispatchInterface extends Loggable {

  protected def doFetchAccProfileData(poolAccountProfile:Box[String]):JValue = {
    import net.liftweb.json.JsonParser._
    val start: Long = System.currentTimeMillis 
    val mySecureHost = host("mining.bitcoin.cz").secure
    val myRequest = mySecureHost / "accounts" / "profile" / "json" / poolAccountProfile.get 
    val response = Http(myRequest OK as.String)
    val json = parse(response())  
    val stop: Long = System.currentTimeMillis
    logger.debug("BTCDispatchInterface::fetchAccProfileData() done took "+((stop-start))+"ms")     
    json 
  }  
    
  protected def doFetchKapitonData():JValue = {
    import net.liftweb.json.JsonParser._  
    val start: Long = System.currentTimeMillis
    val mySecureHost = host("kapiton.se").secure
    val myRequest = mySecureHost / "api" / "0" / "ticker" 
    val response = Http(myRequest OK as.String)
    val json = parse(response())  
    val stop: Long = System.currentTimeMillis
    logger.debug("BTCDispatchInterface::fetchKapitonData() done took "+((stop-start))+"ms")      
    json
  }    
    
  protected def doFetchSlushPoolStat():JValue = {
    import net.liftweb.json.JsonParser._  
    val start: Long = System.currentTimeMillis
    val mySecureHost = host("mining.bitcoin.cz").secure
    val myRequest = mySecureHost / "stats" / "json" / "" 
    val response = Http(myRequest OK as.String)
    val json = parse(response())  
    val stop: Long = System.currentTimeMillis
    logger.debug("BTCDispatchInterface::fetchSlushPoolStat() done took "+((stop-start))+"ms")   
    json     
  }
  
  protected def doFetchWalletData(blockchainInfoAddress:Box[String]):JValue = {
    import net.liftweb.json.JsonParser._  
    val start: Long = System.currentTimeMillis
    val mySecureHost = host("blockchain.info").secure
    val myRequest = mySecureHost / "address" / blockchainInfoAddress.get <<? ("format" -> "json") :: Nil
    val response = Http(myRequest OK as.String).option
    val json:JValue = parse(response().getOrElse("")) 
    val stop: Long = System.currentTimeMillis
    logger.debug("BTCDispatchInterface::fetchWalletData() start done took "+((stop-start))+"ms") 
    json       
  }
  
  protected def doFetchMtgoxSEK():JValue = {
    import net.liftweb.json.JsonParser._  
    val start: Long = System.currentTimeMillis
    val myHost = host("www.mtgox.com").secure
    val myRequest = myHost / "api" / "2" / "BTCSEK" / "money" / "ticker_fast"
    val response = Http(myRequest OK as.String)
    val json = parse(response())  
    val stop: Long = System.currentTimeMillis
    logger.debug("BTCDispatchInterface::fetchMtgoxSEK() done took "+((stop-start))+"ms")  
    json    
  }
  
  protected def doFetchMtgoxUSD():JValue = {
    import net.liftweb.json.JsonParser._  
    val start: Long = System.currentTimeMillis
    val myHost = host("www.mtgox.com").secure
    val myRequest = myHost / "api" / "2" / "BTCUSD" / "money" / "ticker_fast"
    val response = Http(myRequest OK as.String)
    val json = parse(response())  
    val stop: Long = System.currentTimeMillis
    logger.debug("BTCDispatchInterface::fetchMtgoxUSD() done took "+((stop-start))+"ms") 
    json    
  }

  protected def doFetchMtgoxEUR():JValue = {
    import net.liftweb.json.JsonParser._  
    val start: Long = System.currentTimeMillis
    val myHost = host("www.mtgox.com").secure
    val myRequest = myHost / "api" / "2" / "BTCEUR" / "money" / "ticker_fast"
    val response = Http(myRequest OK as.String)
    val json = parse(response())  
    val stop: Long = System.currentTimeMillis
    logger.debug("BTCDispatchInterface::fetchMtgoxEUR() done took "+((stop-start))+"ms")     
    json    
  }       
    
}
package se.media4u101.lib

import dispatch._ , Defaults._
import net.liftweb.common._
import net.liftweb.json.JsonAST._

trait BTCDispatchInterface extends Loggable{
  
  protected def doFetchAccProfileData(poolAccountProfile:Box[String]):JValue = {
    import net.liftweb.json.JsonParser._
    val start: Long = System.currentTimeMillis 
    val mySecureHost = host("mining.bitcoin.cz").secure
    val myRequest = mySecureHost / "accounts" / "profile" / "json" / poolAccountProfile.get 
    val response = Http(myRequest OK as.String).either
    //Unexpected response status: 502
    val json = parse((response().right).getOrElse(""))
    if(response().isLeft){ 
      val err = response().left.get 
      logger.error("BTCDispatchInterface::doFetchAccProfileData got a exception: "+err.getMessage())
      this.handleResponseError(err) 
    }
    val stop: Long = System.currentTimeMillis
    logger.info("BTCDispatchInterface::fetchAccProfileData() done took "+((stop-start))+"ms")     
    json 
  }  
  
//  protected def doFetchKapitonData():JValue = {
//    import net.liftweb.json.JsonParser._  
//    val start: Long = System.currentTimeMillis
//    val mySecureHost = host("kapiton.se").secure
//    val myRequest = mySecureHost / "api" / "0" / "ticker" 
//    val response = Http(myRequest OK as.String)
//    val json = parse(response())  
//    val stop: Long = System.currentTimeMillis
//    logger.debug("BTCDispatchInterface::fetchKapitonData() done took "+((stop-start))+"ms")      
//    json
//  }    
    
  protected def doFetchSlushPoolStat():JValue = {
    import net.liftweb.json.JsonParser._  
    val start: Long = System.currentTimeMillis
    val mySecureHost = host("mining.bitcoin.cz").secure
    val myRequest = mySecureHost / "stats" / "json" / "" 
    val response = Http(myRequest OK as.String).either
    val json = parse((response().right).getOrElse(""))
    if(response().isLeft){ 
      val err = response().left.get 
      logger.error("BTCDispatchInterface::doFetchSlushPoolStat got a exception: "+err.getMessage())
      this.handleResponseError(err) 
    }
    val stop: Long = System.currentTimeMillis
    logger.info("BTCDispatchInterface::fetchSlushPoolStat() done took "+((stop-start))+"ms")   
    json     
  }
  
  protected def doFetchWalletData(blockchainInfoAddress:Box[String]):JValue = {
    import net.liftweb.json.JsonParser._  
    val start: Long = System.currentTimeMillis
    val mySecureHost = host("blockchain.info").secure
    val myRequest = mySecureHost / "address" / blockchainInfoAddress.get <<? ("format" -> "json") :: Nil
    val response = Http(myRequest OK as.String).either
    logger.info("BTCDispatchInterface::fetchWalletData() about to fetch response")
    val theRes = response()
    logger.info("BTCDispatchInterface::fetchWalletData() response fetched ... about to parse response")
    val json = parse { theRes.right getOrElse "" }
    logger.info("BTCDispatchInterface::fetchWalletData() parsing response done ... about to check for errors")
    if(theRes.isLeft){ 
      val err = theRes.left.get 
      logger.error("BTCDispatchInterface::doFetchWalletData got a exception: "+err.getMessage())
      this.handleResponseError(err) 
    }    
    val stop: Long = System.currentTimeMillis
    logger.info("BTCDispatchInterface::fetchWalletData() done took "+((stop-start))+"ms") 
    json       
   
  }
  
  protected def doFetchMtgoxSEK():JValue = {
    import net.liftweb.json.JsonParser._  
    val start: Long = System.currentTimeMillis
    val myHost = host("www.mtgox.com").secure
    //val myRequest = myHost / "api" / "2" / "BTCSEK" / "money" / "ticker"
    val myRequest = myHost / "api" / "2" / "BTCSEK" / "money" / "ticker_fast"    
    val response = Http(myRequest OK as.String).either
    val json = parse((response().right).getOrElse(""))
    if(response().isLeft){ 
      val err = response().left.get 
      logger.error("BTCDispatchInterface::doFetchMtgoxSEK got a exception: "+err.getMessage())
      this.handleResponseError(err) 
    }
    val stop: Long = System.currentTimeMillis
    logger.info("BTCDispatchInterface::fetchMtgoxSEK() done took "+((stop-start))+"ms")  
    json    
  }
  
  protected def doFetchMtgoxUSD():JValue = {
    import net.liftweb.json.JsonParser._  
    val start: Long = System.currentTimeMillis
    val myHost = host("www.mtgox.com").secure
    val myRequest = myHost / "api" / "2" / "BTCUSD" / "money" / "ticker_fast"
    val response = Http(myRequest OK as.String).either
    val json = parse((response().right).getOrElse(""))
    if(response().isLeft){ 
      val err = response().left.get 
      logger.error("BTCDispatchInterface::doFetchMtgoxUSD got a exception: "+err.getMessage())
      this.handleResponseError(err) 
    }
    val stop: Long = System.currentTimeMillis
    logger.info("BTCDispatchInterface::fetchMtgoxUSD() done took "+((stop-start))+"ms") 
    json    
  }

  protected def doFetchMtgoxEUR():JValue = {
    import net.liftweb.json.JsonParser._  
    val start: Long = System.currentTimeMillis
    val myHost = host("www.mtgox.com").secure
    val myRequest = myHost / "api" / "2" / "BTCEUR" / "money" / "ticker_fast"
    val response = Http(myRequest OK as.String).either
    val json = parse((response().right).getOrElse(""))
    if(response().isLeft){ 
      val err = response().left.get 
      logger.error("BTCDispatchInterface::doFetchMtgoxEUR got a exception: "+err.getMessage())
      this.handleResponseError(err) 
    }
    val stop: Long = System.currentTimeMillis
    logger.info("BTCDispatchInterface::fetchMtgoxEUR() done took "+((stop-start))+"ms")     
    json    
  }       
  
  private def handleResponseError(err:Throwable):String = { 
    import net.liftweb.json.JsonParser._ 
    import net.liftweb.json.JsonAST._
    import net.liftweb.json.JsonDSL._
    import net.liftweb.json.DefaultFormats
    val errmes = err.getMessage();
    val json = ("error" -> "" )
    //val jret = """{ "error": """+errmes+""" }""" 
    json.toString
    ""
  }  
}

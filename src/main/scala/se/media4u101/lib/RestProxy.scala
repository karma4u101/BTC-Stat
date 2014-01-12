package se.media4u101.lib

import dispatch._ , Defaults._
import net.liftweb.common._
import net.liftweb.json.JsonAST._
import net.liftweb.util.{ Props }

object RestProxy extends BTCDispatchInterface {
  
  val poolAccountProfile:Box[String] = Props.get("pool.account.profile") 
  val blockchainInfoAddress:Box[String] = Props.get("blockchain.info.address")
  
//  def fetchKapitonData() : JValue = {    
//    kapitonJson
//  }
  
  def fetchSlushPoolStat() : JValue = {    
    slushPoolJson
  }   
  
  def fetchWalletData() : JValue = {      
    walletJson
  }
  
  def fetchMtgoxSEK() : JValue = {
    mtgoxSEKJson
  }
  
  def fetchMtgoxSEKPrev() : JValue = {
    mtgoxSEKPrevJson
  }  
  
  def fetchMtgoxUSD() : JValue = {  
    mtgoxUSDJson
  } 
  
  def fetchMtgoxUSDPrev() : JValue = {  
    mtgoxUSDPrevJson
  }   
  
  def fetchMtgoxEUR() : JValue = {  
    mtgoxEURJson
  } 
  
  def fetchMtgoxEURPrev() : JValue = {  
    mtgoxEURPrevJson
  }   

  def fetchAccProfileData():JValue = {    
    accProfileJson
  }  
  
  def fetchServerSystemMillis():JValue = {
    serverDateTimeJson
  }
  
  private[media4u101] def doScheduleJob():Unit = this.synchronized {
        doScheduledFetchAccProfileData()
        //doScheduledfetchKapitonData()
        doScheduledfetchSlushPoolStat()
        doScheduledfetchWalletData()
        doScheduledfetchMtgoxSEK()
        doScheduledfetchMtgoxUSD()
        doScheduledfetchMtgoxEUR()   
        doScheduledfetchBTCServerSystemMillis()
  }  
 
  private def doScheduledfetchBTCServerSystemMillis():Unit = {
    serverDateTimeJson = getServerSystemMillis()
  }
  
  private def getServerSystemMillis():JValue = {
    import net.liftweb.json.JsonDSL._
    val now: Long = System.currentTimeMillis
    ("btc_server_system_millis" -> now )
  } 
  
    //side effect method could this be done in some other way ???
  private def doScheduledFetchAccProfileData():Unit = {   
    accProfileJson = doFetchAccProfileData(poolAccountProfile)
  }  
  
//  private def doScheduledfetchKapitonData() : Unit = {    
//    kapitonJson = doFetchKapitonData()
//  }    
    
  private def doScheduledfetchSlushPoolStat() : Unit = {
    slushPoolJson = doFetchSlushPoolStat()     
  }
  
  private def doScheduledfetchWalletData() : Unit = {
    walletJson = doFetchWalletData(blockchainInfoAddress)       
  }
  
  private def doScheduledfetchMtgoxSEK() : Unit = { 
    val newval = doFetchMtgoxSEK()  
    if(mtgoxSEKJson != null){
    mtgoxSEKPrevJson = mtgoxSEKJson
    }else{
      mtgoxSEKPrevJson = newval
    }
    mtgoxSEKJson = newval
  }
  
  private def doScheduledfetchMtgoxUSD() : Unit = {
    val newval = doFetchMtgoxUSD()  
    if(mtgoxUSDJson != null){
    mtgoxUSDPrevJson = mtgoxUSDJson
    }else{
      mtgoxUSDPrevJson = newval
    }
    mtgoxUSDJson = newval    
  }

  private def doScheduledfetchMtgoxEUR() : Unit = {   
    val newval = doFetchMtgoxEUR()  
    if(mtgoxEURJson != null){
    mtgoxEURPrevJson = mtgoxEURJson
    }else{
      mtgoxEURPrevJson = newval
    }
    mtgoxEURJson = newval    
  }   
  
  @volatile private var mtgoxEURJson:JValue = null
  @volatile private var mtgoxEURPrevJson:JValue = null
  @volatile private var mtgoxUSDJson:JValue = null
  @volatile private var mtgoxUSDPrevJson:JValue = null
  @volatile private var mtgoxSEKJson:JValue = null
  @volatile private var mtgoxSEKPrevJson:JValue = null
  @volatile private var walletJson:JValue = null
  @volatile private var slushPoolJson:JValue = null
//  @volatile private var kapitonJson:JValue = null
  @volatile private var accProfileJson:JValue = null
  @volatile private var serverDateTimeJson:JValue = null
  
}
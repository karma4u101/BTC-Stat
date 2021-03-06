package se.media4u101.lib

import scala.xml.{ NodeSeq, Text }
import net.liftweb.common._
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._  
import net.liftweb.json.DefaultFormats
import net.liftweb.util.{ Props }

trait BTCRestHelper extends Loggable {

  implicit val formats = DefaultFormats
  
  lazy val forwardSplitValue = Props.getInt("revenue.forward.split", 1)
  
  /*
  def getKapitonData():JValue = {
    //logger.debug("BTCRestHelper::getKapitonData()")    
    val data = RestProxy.fetchKapitonData()
    val kdata = this.getMergedKapitonData(data)
    kdata     
  }
  */
  
  def getSlushPoolStatData():JValue = {
    //logger.debug("BTCRestHelper::getSlushPoolStat()")   
    val data = RestProxy.fetchSlushPoolStat()
    val spdata = this.getMergedSlushPoolStatData(data)
    spdata    
  }
  
  def getWalletData(userLoggedIn:Boolean):JValue = {
    //logger.debug("BTCRestHelper::getWalletData()") 
    val data = RestProxy.fetchWalletData() 
    val wdata = this.getMergedWalletData(data,userLoggedIn)  
    wdata
  }
  
  def getServerDateTime():JValue = {
    val millis = RestProxy.fetchServerSystemMillis()
    val datetime = this.getMergedServerDateTime(millis)
    datetime
  }  
  
  def getAccProfileData():JValue = {
    //logger.debug("BTCRestHelper::getAccProfileData()")
    val apdata = RestProxy.fetchAccProfileData()
    val mtgoxSEK  = RestProxy.fetchMtgoxSEK()
    val mtgoxUSD = RestProxy.fetchMtgoxUSD() 
    val mtgoxEUR = RestProxy.fetchMtgoxEUR()
    val accdata = this.getMergedAccProfileData(apdata)
    val sekdata = this.getMergedMtgoxSEKData(accdata,mtgoxSEK)
    val usddata = this.getMergedMtgoxUSDData(accdata,mtgoxUSD)
    val eurdata = this.getMergedMtgoxEURData(accdata,mtgoxEUR)
    val data = accdata merge sekdata merge usddata merge eurdata
    data
  }  
  
  /*Methods below is doing some internal calculations and json manipulations, no other external references used */
  private def getMergedServerDateTime(millis:JValue):JValue = {
    val dateTime = this.getFormatedServerDateTime(millis)
    //val dateTime = this.simpelBigIntTransform( (for {JField("btc_server_system_millis",JInt(v)) <- millis } yield v).headOption, "btc_server_dt")
    dateTime
  }
  
  private def getFormatedServerDateTime(data:JValue) :JValue = {
    val millis : Option[BigInt] = (for {JField("btc_server_system_millis",JInt(millis)) <- data } yield millis).headOption
    val date = new java.util.Date(millis.get.toLong)
    val formatter = new java.text.SimpleDateFormat("dd MMM 'at' HH:mm z") 
    val fdate = formatter.format(date)
    ("btc_server_dt" -> fdate)
  }
  
  /*
  private def getMergedKapitonData(kdata:JValue):JValue = {
    //logger.debug("BTCRestHelper::getMergedKapitonData()")
    val ask   = this.kaptionTickerAsk(kdata)
    val bid   = this.kaptionTickerBid(kdata)
    val hi    = this.kaptionTickerHi(kdata)
    val low   = this.kaptionTickerLow(kdata)
    val mave  = this.kaptionTickerMonthAverage(kdata)
    val mvol  = this.kaptionTickerMonthVolym(kdata)
    val price = this.kaptionTickerPrice(kdata)
    val vol   = this.kaptionTickerVol(kdata)
    val data = ask merge bid merge hi merge low merge mave merge mvol merge price merge vol
    data
  }
  */
  
  private def getMergedSlushPoolStatData(data:JValue):JValue = {
    //logger.debug("BTCRestHelper::getMergedSlushPoolStatData()")  
    val round      = this.simpelStringTransform((for { JField("round_started",JString(value)) <- data } yield value ).headOption,"sps_round_started")
    val stratum    = this.simpelStringTransform((for { JField("active_stratum",JString(active_stratum)) <- data } yield active_stratum ).headOption,"sps_active_stratum")
    val workers    = this.simpelStringTransform( (for { JField("active_workers",JString(value)) <- data } yield value ).headOption, "sps_active_workers") 
    val ghashshare = this.simpelStringTransform( (for { JField("ghashes_ps",JString(s)) <- data } yield s ).headOption, "sps_ghashes_ps")
    val duration   = this.simpelStringTransform( (for { JField("round_duration",JString(s)) <- data } yield s ).headOption, "sps_round_duration")
    val score      = this.simpelStringTransform( (for { JField("score"         ,JString(s)) <- data } yield s ).headOption, "sps_score")
    val sharesCDF  = this.simpelStringTransform( (for { JField("shares_cdf"    ,JString(s)) <- data } yield s ).headOption ,"sps_shares_cdf")
    val shares     = this.simpelBigIntTransform( (for { JField("shares"        ,JInt(i)) <- data } yield i ).headOption, "sps_shares")
    val luck1      = this.slushPoolStatLuck( (for { JField("luck_1"        ,JString(s)) <- data } yield s ).headOption, "sps_luck_1")
    val luck7      = this.slushPoolStatLuck( (for { JField("luck_7"        ,JString(s)) <- data } yield s ).headOption, "sps_luck_7")
    val luck30     = this.slushPoolStatLuck( (for { JField("luck_30"       ,JString(s)) <- data } yield s ).headOption, "sps_luck_30")
    val thashshare = this.slushPoolStatTHashesPoolShare( (for { JField("ghashes_ps",JString(s)) <- data } yield s ).headOption,"sps_thashes_ps" )
    val retval = round merge stratum merge workers merge ghashshare merge luck1 merge luck7 merge luck30 merge duration merge score merge shares merge sharesCDF merge thashshare  
    retval
  }
  
  private def getMergedWalletData(wdata:JValue,userLoggedIn:Boolean):JValue = {
    //logger.debug("BTCRestHelper::getMergedWalletData()")
    val ntx     = this.walletNrTransactions(wdata)
    val recived = this.walletTotalReceived(wdata,userLoggedIn)
    val balance = this.walletFinalBalance(wdata,userLoggedIn)
    val split   = this.walletForwardSplit(wdata,userLoggedIn)

    val mtgoxSEK  = RestProxy.fetchMtgoxSEK() 
    val mtgoxSEKPrev = RestProxy.fetchMtgoxSEKPrev()
    val mtgoxUSD = RestProxy.fetchMtgoxUSD() 
    val mtgoxUSDPrev = RestProxy.fetchMtgoxUSDPrev()
    val mtgoxEUR = RestProxy.fetchMtgoxEUR()  
    val mtgoxEURPrev = RestProxy.fetchMtgoxEURPrev()
    val sekdata = this.getMergedWalletMtgoxSEKData(wdata,mtgoxSEK,mtgoxSEKPrev,userLoggedIn)
    val usddata = this.getMergedWalletMtgoxUSDData(wdata,mtgoxUSD,mtgoxUSDPrev,userLoggedIn)
    val eurdata = this.getMergedWalletMtgoxEURData(wdata,mtgoxEUR,mtgoxEURPrev,userLoggedIn)      
    
    val data = ntx merge recived merge balance merge split merge sekdata merge usddata merge eurdata
    data
  }
  
  
  private def getMergedAccProfileData(accdata:JValue):JValue = {
    //logger.debug("BTCRestHelper::getMergedAccProfileData()")
    val accum   = this.rewardAccumBTC(accdata)
    val data = accdata merge accum 
    data
  }
    
  private def getMergedWalletMtgoxSEKData(wdata:JValue,mtgox:JValue,prevmtgox:JValue,userLoggedIn:Boolean) : JValue = {
    val wbal   = this.walletFinalBalanceSEK(wdata,mtgox,userLoggedIn)
    val wtot   = this.walletTotalReceivedSEK(wdata,mtgox,userLoggedIn)
    val wsplit = this.walletForwardSplitSEK(wdata,mtgox,userLoggedIn)
    val splitchange = this.currencyShortTrendSEKChange(mtgox,prevmtgox)
    val data = wbal merge wtot merge wsplit merge splitchange
    data
  }  
   
  private def getMergedWalletMtgoxUSDData(wdata:JValue,mtgox:JValue,prevmtgox:JValue,userLoggedIn:Boolean) : JValue = {
    val wbal   = this.walletFinalBalanceUSD(wdata,mtgox,userLoggedIn)
    val wtot   = this.walletTotalReceivedUSD(wdata,mtgox,userLoggedIn)
    val wsplit = this.walletForwardSplitUSD(wdata,mtgox,userLoggedIn)
    val splitchange = this.currencyShortTrendUSDChange(mtgox,prevmtgox)
    val data = wbal merge wtot merge wsplit merge splitchange
    data
  }
  
  private def getMergedWalletMtgoxEURData(wdata:JValue,mtgox:JValue,prevmtgox:JValue,userLoggedIn:Boolean) : JValue = {
    val wbal   = this.walletFinalBalanceEUR(wdata,mtgox,userLoggedIn)
    val wtot   = this.walletTotalReceivedEUR(wdata,mtgox,userLoggedIn)
    val wsplit = this.walletForwardSplitEUR(wdata,mtgox,userLoggedIn)
    val splitchange = this.currencyShortTrendEURChange(mtgox,prevmtgox)
    val data = wbal merge wtot merge wsplit merge splitchange
    data
  }  
  
  private def getMergedMtgoxSEKData(accdata:JValue,mtgox:JValue) : JValue = {
    //logger.debug("BTCRestHelper::getMergedMtgoxSEKData()")    
    val conf   = this.confirmedRewardSEK(accdata, mtgox)
    val unconf = this.unConfirmedRewardSEK(accdata, mtgox)
    val est    = this.estimatedRewardSEK(accdata, mtgox)
    val tot    = this.rewardAccumTotalSEK(accdata, mtgox)
    val data = conf merge unconf merge est merge tot
    data
  }
  
  private def getMergedMtgoxUSDData(accdata:JValue,mtgox:JValue) : JValue = {
    //logger.debug("BTCRestHelper::getMergedMtgoxUSDData()")
    val conf   = this.confirmedRewardUSD(accdata, mtgox)
    val unconf = this.unConfirmedRewardUSD(accdata, mtgox)
    val est    = this.estimatedRewardUSD(accdata, mtgox)
    val tot    = this.rewardAccumTotalUSD(accdata, mtgox)
    val data = conf merge unconf merge est merge tot
    data
  } 
  
  private def getMergedMtgoxEURData(accdata:JValue,mtgox:JValue) : JValue = {
    //logger.debug("BTCRestHelper::getMergedMtgoxEURData()")    
    val conf   = this.confirmedRewardEUR(accdata, mtgox)
    val unconf = this.unConfirmedRewardEUR(accdata, mtgox)
    val est    = this.estimatedRewardEUR(accdata, mtgox)
    val tot    = this.rewardAccumTotalEUR(accdata, mtgox)
    val data = conf merge unconf merge est merge tot
    data
  }   
  

  /*--------General transforms start (is not working)--------*/
  
  private def simpelStringTransform(exp: Option[String], outName:String  ):JValue = 
    exp match {
    case Some(value) => (outName -> value)      
    case None => (outName -> "NaN")      
  }
  
  private def simpelBigIntTransform(exp: Option[BigInt], outName:String  ):JValue = 
    exp match {
    case Some(value) => (outName -> value)      
    case None => (outName -> "NaN")      
  }    
  
 /*--------General transforms-end-------*/ 
  
  /*-------------Account Profile data start ------------------*/
  
  private def rewardAccumBTC(data:JValue):JValue = {
    //logger.debug("BTCRestHelper::rewardAccumBTC()") 
    val confirmed:Option[String] = (for {JField("confirmed_reward",JString(confirmed_reward)) <- data } yield confirmed_reward).headOption
    val unconfirmed:Option[String] = (for {JField("unconfirmed_reward",JString(unconfirmed_reward)) <- data } yield unconfirmed_reward).headOption
    if(confirmed.isDefined && unconfirmed.isDefined){
      val tot=confirmed.get.toDouble+unconfirmed.get.toDouble
      ("accum_reward" -> tot )
    }else{
      ("accum_reward" -> "NaN" ) 
    }
  }

  private def estimatedRewardSEK(accdata:JValue,mtgox:JValue):JValue = {
    //logger.debug("BTCRestHelper::estimatedRewardSEK()")     
    val estimated:Option[String] = (for {JField("estimated_reward",JString(estimated_reward)) <- accdata } yield estimated_reward).headOption
    val last:Option[String] = (for {
      JField("data",JObject(list)) <- mtgox 
      JField("last_all",JObject(list2))  <- list
      JField("value",JString(value)) <- list2
      } yield value  ).headOption 
     if(estimated.isDefined && last.isDefined){
       val sum = estimated.get.toDouble * last.get.toDouble
        val f:java.text.DecimalFormat = new java.text.DecimalFormat()
        f.setRoundingMode(java.math.RoundingMode.HALF_UP)
        f.setMaximumFractionDigits(0)        
        f.setCurrency(java.util.Currency.getInstance("SEK"))  
        val fsum=f.format(sum)        
       ("estimated_reward_SEK" -> fsum )
     }else{
       ("estimated_reward_SEK" -> "NaN" )
     }
  }
  
  private def confirmedRewardSEK(accdata:JValue,mtgox:JValue):JValue = {
    //logger.debug("BTCRestHelper::confirmedRewardSEK()")     
    val confirmed:Option[String] =(for { JField("confirmed_reward",JString(confirmed_reward)) <- accdata } yield confirmed_reward ).headOption
    val last:Option[String] = (for {
      JField("data",JObject(list)) <- mtgox 
      JField("last_all",JObject(list2))  <- list
      JField("value",JString(value)) <- list2
      } yield value  ).headOption 
      if(confirmed.isDefined && last.isDefined){
        val sum = confirmed.get.toDouble * last.get.toDouble
        val f:java.text.DecimalFormat = new java.text.DecimalFormat()
        f.setRoundingMode(java.math.RoundingMode.HALF_UP)
        f.setMaximumFractionDigits(0)        
        f.setCurrency(java.util.Currency.getInstance("SEK"))  
        val fsum=f.format(sum)         
        ("confirmed_reward_SEK" -> fsum )
      }else{
        ("confirmed_reward_SEK" -> "NaN" )
      }
  }
  
  private def unConfirmedRewardSEK(accdata:JValue,mtgox:JValue):JValue = {
    //logger.debug("BTCRestHelper::unConfirmedRewardSEK()")     
    val unconfirmed:Option[String] =(for { JField("unconfirmed_reward",JString(unconfirmed_reward)) <- accdata } yield unconfirmed_reward ).headOption
    val last:Option[String] = (for {
      JField("data",JObject(list)) <- mtgox 
      JField("last_all",JObject(list2))  <- list
      JField("value",JString(value)) <- list2
      } yield value  ).headOption 
      if(unconfirmed.isDefined && last.isDefined){
        val sum = unconfirmed.get.toDouble * last.get.toDouble
        val f:java.text.DecimalFormat = new java.text.DecimalFormat()
        f.setRoundingMode(java.math.RoundingMode.HALF_UP)
        f.setMaximumFractionDigits(0)        
        f.setCurrency(java.util.Currency.getInstance("SEK"))  
        val fsum=f.format(sum)         
        ("unconfirmed_reward_SEK" -> fsum )
      }else{
        ("unconfirmed_reward_SEK" -> "NaN" )
      }
  }    
  
  private def rewardAccumTotalSEK(accdata:JValue,mtgox:JValue):JValue = {
    //logger.debug("BTCRestHelper::rewardAccumTotalSEK()")     
    val confirmed:Option[String] =(for { JField("confirmed_reward",JString(confirmed_reward)) <- accdata } yield confirmed_reward ).headOption
    val unconfirmed:Option[String] =(for { JField("unconfirmed_reward",JString(unconfirmed_reward)) <- accdata } yield unconfirmed_reward ).headOption
    val last:Option[String] = (for {
      JField("data",JObject(list)) <- mtgox 
      JField("last_all",JObject(list2))  <- list
      JField("value",JString(value)) <- list2
      } yield value  ).headOption 
      if(unconfirmed.isDefined && confirmed.isDefined && last.isDefined){
        val sum = (unconfirmed.get.toDouble + confirmed.get.toDouble) * last.get.toDouble
        val f:java.text.DecimalFormat = new java.text.DecimalFormat()
        f.setRoundingMode(java.math.RoundingMode.HALF_UP)
        f.setMaximumFractionDigits(0)        
        f.setCurrency(java.util.Currency.getInstance("SEK"))  
        val fsum=f.format(sum)        
        ("accum_reward_SEK" -> fsum )
      }else{
        ("accum_reward_SEK" -> "NaN" )
      }
  }  
    
  private def estimatedRewardUSD(accdata:JValue,mtgox:JValue):JValue = {
    //logger.debug("BTCRestHelper::estimatedRewardUSD()")     
    val estimated:Option[String] = (for {JField("estimated_reward",JString(estimated_reward)) <- accdata } yield estimated_reward).headOption
    val last:Option[String] = (for {
      JField("data",JObject(list)) <- mtgox 
      JField("last_all",JObject(list2))  <- list
      JField("value",JString(value)) <- list2
      } yield value  ).headOption 
     if(estimated.isDefined && last.isDefined){
       val sum = estimated.get.toDouble * last.get.toDouble
        val f:java.text.DecimalFormat = new java.text.DecimalFormat()
        f.setRoundingMode(java.math.RoundingMode.HALF_UP)
        f.setMaximumFractionDigits(0)        
        f.setCurrency(java.util.Currency.getInstance("USD"))  
        val fsum=f.format(sum)       
       ("estimated_reward_USD" -> fsum )
     }else{
       ("estimated_reward_USD" -> "NaN" )
     }    
  }
  
  private def confirmedRewardUSD(accdata:JValue,mtgox:JValue):JValue = {
    //logger.debug("BTCRestHelper::confirmedRewardUSD()")     
    val confirmed:Option[String] =(for { JField("confirmed_reward",JString(confirmed_reward)) <- accdata } yield confirmed_reward ).headOption
    val last:Option[String] = (for {
      JField("data",JObject(list)) <- mtgox 
      JField("last_all",JObject(list2))  <- list
      JField("value",JString(value)) <- list2
      } yield value  ).headOption 
      if(confirmed.isDefined && last.isDefined){
        val sum = confirmed.get.toDouble * last.get.toDouble
        val f:java.text.DecimalFormat = new java.text.DecimalFormat()
        f.setRoundingMode(java.math.RoundingMode.HALF_UP)
        f.setMaximumFractionDigits(0)        
        f.setCurrency(java.util.Currency.getInstance("USD"))  
        val fsum=f.format(sum)        
        ("confirmed_reward_USD" -> fsum )
      }else{
        ("confirmed_reward_USD" -> "NaN" )
      }    
  }
  
  private def unConfirmedRewardUSD(accdata:JValue,mtgox:JValue):JValue = {
    //logger.debug("BTCRestHelper::unConfirmedRewardUSD()")     
    val unconfirmed:Option[String] =(for { JField("unconfirmed_reward",JString(unconfirmed_reward)) <- accdata } yield unconfirmed_reward ).headOption
    val last:Option[String] = (for {
      JField("data",JObject(list)) <- mtgox 
      JField("last_all",JObject(list2))  <- list
      JField("value",JString(value)) <- list2
      } yield value  ).headOption 
      if(unconfirmed.isDefined && last.isDefined){
        val sum = unconfirmed.get.toDouble * last.get.toDouble
        val f:java.text.DecimalFormat = new java.text.DecimalFormat()
        f.setRoundingMode(java.math.RoundingMode.HALF_UP)
        f.setMaximumFractionDigits(0)        
        f.setCurrency(java.util.Currency.getInstance("USD"))  
        val fsum=f.format(sum)        
        ("unconfirmed_reward_USD" -> fsum )
      }else{
        ("unconfirmed_reward_USD" -> "NaN" )
      }    
  }    
  
  private def rewardAccumTotalUSD(accdata:JValue,mtgox:JValue):JValue = {
    //logger.debug("BTCRestHelper::rewardAccumTotalUSD()")     
    val confirmed:Option[String] =(for { JField("confirmed_reward",JString(confirmed_reward)) <- accdata } yield confirmed_reward ).headOption
    val unconfirmed:Option[String] =(for { JField("unconfirmed_reward",JString(unconfirmed_reward)) <- accdata } yield unconfirmed_reward ).headOption
    val last:Option[String] = (for {
      JField("data",JObject(list)) <- mtgox 
      JField("last_all",JObject(list2))  <- list
      JField("value",JString(value)) <- list2
      } yield value  ).headOption 
      if(unconfirmed.isDefined && confirmed.isDefined && last.isDefined){
        val sum = (unconfirmed.get.toDouble + confirmed.get.toDouble) * last.get.toDouble
        val f:java.text.DecimalFormat = new java.text.DecimalFormat()
        f.setRoundingMode(java.math.RoundingMode.HALF_UP)
        f.setMaximumFractionDigits(0)        
        f.setCurrency(java.util.Currency.getInstance("USD"))  
        val fsum=f.format(sum)        
        ("accum_reward_USD" -> fsum )
      }else{
        ("accum_reward_USD" -> "NaN" )
      }    
  }
  
  private def estimatedRewardEUR(accdata:JValue,mtgox:JValue):JValue = {
    //logger.debug("BTCRestHelper::estimatedRewardEUR()")     
    val estimated:Option[String] = (for {JField("estimated_reward",JString(estimated_reward)) <- accdata } yield estimated_reward).headOption
    val last:Option[String] = (for {
      JField("data",JObject(list)) <- mtgox 
      JField("last_all",JObject(list2))  <- list
      JField("value",JString(value)) <- list2
      } yield value  ).headOption 
     if(estimated.isDefined && last.isDefined){
       val sum = estimated.get.toDouble * last.get.toDouble
        val f:java.text.DecimalFormat = new java.text.DecimalFormat()
        f.setRoundingMode(java.math.RoundingMode.HALF_UP)
        f.setMaximumFractionDigits(0)        
        f.setCurrency(java.util.Currency.getInstance("EUR"))  
        val fsum=f.format(sum)       
       ("estimated_reward_EUR" -> fsum )
     }else{
       ("estimated_reward_EUR" -> "NaN" )
     }     
  }
  
  private def confirmedRewardEUR(accdata:JValue,mtgox:JValue):JValue = {
    //logger.debug("BTCRestHelper::confirmedRewardEUR()")         
    val confirmed:Option[String] =(for { JField("confirmed_reward",JString(confirmed_reward)) <- accdata } yield confirmed_reward ).headOption
    val last:Option[String] = (for {
      JField("data",JObject(list)) <- mtgox 
      JField("last_all",JObject(list2))  <- list
      JField("value",JString(value)) <- list2
      } yield value  ).headOption 
      if(confirmed.isDefined && last.isDefined){
        val sum = confirmed.get.toDouble * last.get.toDouble
        val f:java.text.DecimalFormat = new java.text.DecimalFormat()
        f.setRoundingMode(java.math.RoundingMode.HALF_UP)
        f.setMaximumFractionDigits(0)        
        f.setCurrency(java.util.Currency.getInstance("EUR"))  
        val fsum=f.format(sum)        
        ("confirmed_reward_EUR" -> fsum )
      }else{
        ("confirmed_reward_EUR" -> "NaN" )
      }     
  }
  
  private def unConfirmedRewardEUR(accdata:JValue,mtgox:JValue):JValue = {
    //logger.debug("BTCRestHelper::unConfirmedRewardEUR()")     
    val unconfirmed:Option[String] =(for { JField("unconfirmed_reward",JString(unconfirmed_reward)) <- accdata } yield unconfirmed_reward ).headOption
    val last:Option[String] = (for {
      JField("data",JObject(list)) <- mtgox 
      JField("last_all",JObject(list2))  <- list
      JField("value",JString(value)) <- list2
      } yield value  ).headOption 
      if(unconfirmed.isDefined && last.isDefined){
        val sum = unconfirmed.get.toDouble * last.get.toDouble
        val f:java.text.DecimalFormat = new java.text.DecimalFormat()
        f.setRoundingMode(java.math.RoundingMode.HALF_UP)
        f.setMaximumFractionDigits(0)        
        f.setCurrency(java.util.Currency.getInstance("EUR"))  
        val fsum=f.format(sum)        
        ("unconfirmed_reward_EUR" -> fsum )
      }else{
        ("unconfirmed_reward_EUR" -> "NaN" )
      }     
  }    
  
  private def rewardAccumTotalEUR(accdata:JValue,mtgox:JValue):JValue = {
    //logger.debug("BTCRestHelper::rewardAccumTotalEUR()")     
    val confirmed:Option[String] =(for { JField("confirmed_reward",JString(confirmed_reward)) <- accdata } yield confirmed_reward ).headOption
    val unconfirmed:Option[String] =(for { JField("unconfirmed_reward",JString(unconfirmed_reward)) <- accdata } yield unconfirmed_reward ).headOption
    val last:Option[String] = (for {
      JField("data",JObject(list)) <- mtgox 
      JField("last_all",JObject(list2))  <- list
      JField("value",JString(value)) <- list2
      } yield value  ).headOption 
      if(unconfirmed.isDefined && confirmed.isDefined && last.isDefined){
        val sum = (unconfirmed.get.toDouble + confirmed.get.toDouble) * last.get.toDouble
        val f:java.text.DecimalFormat = new java.text.DecimalFormat()
        f.setRoundingMode(java.math.RoundingMode.HALF_UP)
        f.setMaximumFractionDigits(0)        
        f.setCurrency(java.util.Currency.getInstance("EUR"))  
        val fsum=f.format(sum)        
        ("accum_reward_EUR" -> fsum )
      }else{
        ("accum_reward_EUR" -> "NaN" )
      }     
  }    
  
  /*-------------Account Profile data end ------------------*/

  /*-------------Wallet data start ------------------*/
  
  private def walletForwardSplit(wdata:JValue,userLoggedIn:Boolean):JValue = userLoggedIn match {
    case true => {   
      val balance:Option[BigInt] = (for {JField("final_balance",JInt(final_balance)) <- wdata } yield final_balance).headOption
      val totres:Option[BigInt] = (for {JField("total_received",JInt(total_received)) <- wdata } yield total_received).headOption
      if(balance.isDefined && totres.isDefined){
        val split = (totres.get.toDouble - balance.get.toDouble) / (forwardSplitValue*100000000) //forward split in 3 parts
        ("wallet_forward_split" -> split)
      }else{
        ("wallet_forward_split" -> "NaN")
      }         
    }
    case false => ("wallet_forward_split" -> "Hidden")
  }
  
  private def currencyShortTrendSEKChange(now:JValue,prev:JValue):JValue = {
  
      val lastNow:Option[String] = (for {
        JField("data",JObject(list)) <- now 
        JField("last_all",JObject(list2))  <- list
        JField("value",JString(value)) <- list2
      } yield value  ).headOption 
      val lastPrev:Option[String] = (for {
        JField("data",JObject(list)) <- prev 
        JField("last_all",JObject(list2))  <- list
        JField("value",JString(value)) <- list2
      } yield value  ).headOption       
      if(lastNow.isDefined && lastPrev.isDefined){
        val nld = lastNow.get.toDouble
        val pld = lastPrev.get.toDouble
        //logger.debug("BTCRestHelper::walletForwardSplitSEKChange nld="+nld+" pld="+pld)
        if(nld>pld){
          ("currency_short_trend_sek_change" -> "up")
        }else if(nld<pld){
          ("currency_short_trend_sek_change" -> "down")
        }else{
          ("currency_short_trend_sek_change" -> "eq")
        }
      }else{
        ("currency_short_trend_sek_change" -> "eq")
      }
  }
  
  private def currencyShortTrendEURChange(now:JValue,prev:JValue):JValue = {
  
      val lastNow:Option[String] = (for {
        JField("data",JObject(list)) <- now 
        JField("last_all",JObject(list2))  <- list
        JField("value",JString(value)) <- list2
      } yield value  ).headOption 
      val lastPrev:Option[String] = (for {
        JField("data",JObject(list)) <- prev 
        JField("last_all",JObject(list2))  <- list
        JField("value",JString(value)) <- list2
      } yield value  ).headOption       
      if(lastNow.isDefined && lastPrev.isDefined){
        val nld = lastNow.get.toDouble
        val pld = lastPrev.get.toDouble
        //logger.debug("BTCRestHelper::walletForwardSplitSEKChange nld="+nld+" pld="+pld)
        if(nld>pld){
          ("currency_short_trend_eur_change" -> "up")
        }else if(nld<pld){
          ("currency_short_trend_eur_change" -> "down")
        }else{
          ("currency_short_trend_eur_change" -> "eq")
        }
      }else{
        ("currency_short_trend_eur_change" -> "eq")
      }
  }  
  
  private def currencyShortTrendUSDChange(now:JValue,prev:JValue):JValue = {
  
      val lastNow:Option[String] = (for {
        JField("data",JObject(list)) <- now 
        JField("last_all",JObject(list2))  <- list
        JField("value",JString(value)) <- list2
      } yield value  ).headOption 
      val lastPrev:Option[String] = (for {
        JField("data",JObject(list)) <- prev 
        JField("last_all",JObject(list2))  <- list
        JField("value",JString(value)) <- list2
      } yield value  ).headOption       
      if(lastNow.isDefined && lastPrev.isDefined){
        val nld = lastNow.get.toDouble
        val pld = lastPrev.get.toDouble
        //logger.debug("BTCRestHelper::walletForwardSplitSEKChange nld="+nld+" pld="+pld)
        if(nld>pld){
          ("currency_short_trend_usd_change" -> "up")
        }else if(nld<pld){
          ("currency_short_trend_usd_change" -> "down")
        }else{
          ("currency_short_trend_usd_change" -> "eq")
        }
      }else{
        ("currency_short_trend_usd_change" -> "eq")
      }
  }    
  
  private def walletForwardSplitSEK(wdata:JValue,mtgox:JValue,userLoggedIn:Boolean):JValue = userLoggedIn match {
    case true => {   
      val balance:Option[BigInt] = (for {JField("final_balance",JInt(final_balance)) <- wdata } yield final_balance).headOption
      val totres:Option[BigInt] = (for {JField("total_received",JInt(total_received)) <- wdata } yield total_received).headOption
      val last:Option[String] = (for {
        JField("data",JObject(list)) <- mtgox 
        JField("last_all",JObject(list2))  <- list
        JField("value",JString(value)) <- list2
      } yield value  ).headOption 
      if(balance.isDefined && totres.isDefined && last.isDefined){
        val split = ((totres.get.toDouble - balance.get.toDouble) / (3*100000000)) * last.get.toDouble
        val f:java.text.DecimalFormat = new java.text.DecimalFormat()
        f.setRoundingMode(java.math.RoundingMode.HALF_UP)
        f.setMaximumFractionDigits(0)
        f.setCurrency(java.util.Currency.getInstance("SEK"))
        val fsplit = f.format(split)        
        ("wallet_forward_split_sek" -> fsplit)
      }else{
        ("wallet_forward_split_sek" -> "NaN")
      }
    }
    case false => ("wallet_forward_split_sek" -> "Hidden")
  }    
  
  private def walletForwardSplitUSD(wdata:JValue,mtgox:JValue,userLoggedIn:Boolean):JValue = userLoggedIn match {
    case true => { 
      val balance:Option[BigInt] = (for {JField("final_balance",JInt(final_balance)) <- wdata } yield final_balance).headOption
      val totres:Option[BigInt] = (for {JField("total_received",JInt(total_received)) <- wdata } yield total_received).headOption
      val last:Option[String] = (for {
        JField("data",JObject(list)) <- mtgox 
        JField("last_all",JObject(list2))  <- list
        JField("value",JString(value)) <- list2
        } yield value  ).headOption 
      if(balance.isDefined && totres.isDefined && last.isDefined){
        val split = ((totres.get.toDouble - balance.get.toDouble) / (3*100000000)) * last.get.toDouble
        val f:java.text.DecimalFormat = new java.text.DecimalFormat()
        f.setRoundingMode(java.math.RoundingMode.HALF_UP)
        f.setMaximumFractionDigits(0)        
        f.setCurrency(java.util.Currency.getInstance("USD"))
        val fsplit = f.format(split)          
        ("wallet_forward_split_usd" -> fsplit)
      }else{
        ("wallet_forward_split_usd" -> "NaN")
      } 
    }
    case false => ("wallet_forward_split_usd" -> "Hidden")
  } 
  
  private def walletForwardSplitEUR(wdata:JValue,mtgox:JValue,userLoggedIn:Boolean):JValue = userLoggedIn match {
    case true => { 
      val balance:Option[BigInt] = (for {JField("final_balance",JInt(final_balance)) <- wdata } yield final_balance).headOption
      val totres:Option[BigInt] = (for {JField("total_received",JInt(total_received)) <- wdata } yield total_received).headOption
      val last:Option[String] = (for {
        JField("data",JObject(list)) <- mtgox 
        JField("last_all",JObject(list2))  <- list
        JField("value",JString(value)) <- list2
        } yield value  ).headOption 
      if(balance.isDefined && totres.isDefined && last.isDefined){
        val split = ((totres.get.toDouble - balance.get.toDouble) / (3*100000000)) * last.get.toDouble
        val f:java.text.DecimalFormat = new java.text.DecimalFormat()
        f.setRoundingMode(java.math.RoundingMode.HALF_UP)
        f.setMaximumFractionDigits(0)        
        f.setCurrency(java.util.Currency.getInstance("EUR"))
        val fsplit = f.format(split)          
        ("wallet_forward_split_eur" -> fsplit)
      }else{
        ("wallet_forward_split_eur" -> "NaN")
      }
    }
    case false => ("wallet_forward_split_eur" -> "Hidden")
  }
  
  private def walletFinalBalance(wdata:JValue,userLoggedIn:Boolean):JValue = userLoggedIn match {
    case true => { 
      val balance:Option[BigInt] = (for {JField("final_balance",JInt(final_balance)) <- wdata } yield final_balance).headOption
      if(balance.isDefined){
        val totBtc = balance.get.toDouble / 100000000
        ("wallet_final_balance" -> totBtc)
      }else{
        ("wallet_final_balance" -> "NaN")
      }
    }
    case false => ("wallet_final_balance" -> "Hidden")
  }
    
  
  private def walletFinalBalanceSEK(wdata:JValue,mtgox:JValue,userLoggedIn:Boolean):JValue = userLoggedIn match {
    case true => { 
     val balance:Option[BigInt] = (for {JField("final_balance",JInt(final_balance)) <- wdata } yield final_balance).headOption
     val last:Option[String] = (for {
       JField("data",JObject(list)) <- mtgox 
       JField("last_all",JObject(list2))  <- list
       JField("value",JString(value)) <- list2
       } yield value  ).headOption 
       if(balance.isDefined && last.isDefined){
         val sum = (balance.get.toDouble / 100000000) * last.get.toDouble
         val f:java.text.DecimalFormat = new java.text.DecimalFormat()
         f.setRoundingMode(java.math.RoundingMode.HALF_UP)
         f.setMaximumFractionDigits(0)        
         f.setCurrency(java.util.Currency.getInstance("SEK"))
         val fsum = f.format(sum)
         ("wallet_final_balance_sek" -> fsum )
       }else{
         ("wallet_final_balance_sek" -> "NaN" )
       }
    }
    case false => ("wallet_final_balance_sek" -> "Hidden" )
  }   
  
  private def walletFinalBalanceUSD(wdata:JValue,mtgox:JValue,userLoggedIn:Boolean):JValue = userLoggedIn match {
    case true => { 
      val balance:Option[BigInt] = (for {JField("final_balance",JInt(final_balance)) <- wdata } yield final_balance).headOption
      val last:Option[String] = (for {
        JField("data",JObject(list)) <- mtgox 
        JField("last_all",JObject(list2))  <- list
        JField("value",JString(value)) <- list2
        } yield value  ).headOption 
      if(balance.isDefined && last.isDefined){
        val sum = (balance.get.toDouble / 100000000) * last.get.toDouble
        val f:java.text.DecimalFormat = new java.text.DecimalFormat()
        f.setRoundingMode(java.math.RoundingMode.HALF_UP)
        f.setMaximumFractionDigits(0)        
        f.setCurrency(java.util.Currency.getInstance("USD"))        
        val fsum=f.format(sum)
        ("wallet_final_balance_usd" -> fsum )
      }else{
        ("wallet_final_balance_usd" -> "NaN" )
      }
    }
    case false => ("wallet_final_balance_usd" -> "Hidden" )
  }  
  
  private def walletFinalBalanceEUR(wdata:JValue,mtgox:JValue,userLoggedIn:Boolean):JValue = userLoggedIn match {
    case true => { 
      val balance:Option[BigInt] = (for {JField("final_balance",JInt(final_balance)) <- wdata } yield final_balance).headOption
      val last:Option[String] = (for {
        JField("data",JObject(list)) <- mtgox 
        JField("last_all",JObject(list2))  <- list
        JField("value",JString(value)) <- list2
        } yield value  ).headOption 
      if(balance.isDefined && last.isDefined){
        val sum = (balance.get.toDouble / 100000000) * last.get.toDouble
        val f:java.text.DecimalFormat = new java.text.DecimalFormat()
        f.setRoundingMode(java.math.RoundingMode.HALF_UP)
        f.setMaximumFractionDigits(0)        
        f.setCurrency(java.util.Currency.getInstance("EUR"))    
        val fsum=f.format(sum)
        ("wallet_final_balance_eur" -> fsum )
      }else{
        ("wallet_final_balance_eur" -> "NaN" )
      }
    }
    case false => ("wallet_final_balance_eur" -> "Hidden" )    
  }

  private def walletTotalReceived(wdata: JValue,userLoggedIn:Boolean):JValue = userLoggedIn match {
    case true => {    
      val received: Option[BigInt] = (for { JField("total_received", JInt(total_received)) <- wdata } yield total_received).headOption
      if (received.isDefined) {
        val totBtc = received.get.toDouble / 100000000
        ("wallet_total_received" -> totBtc)
      } else {
        ("wallet_total_received" -> "NaN")
      }
    }
    case false => ("wallet_total_received" -> "Hidden" )        
  }  
  
  private def walletTotalReceivedSEK(wdata:JValue,mtgox:JValue,userLoggedIn:Boolean):JValue = userLoggedIn match {
    case true => {    
      val received:Option[BigInt] = (for {JField("total_received",JInt(total_received)) <- wdata } yield total_received).headOption
      val last:Option[String] = (for {
        JField("data",JObject(list)) <- mtgox 
        JField("last_all",JObject(list2))  <- list
        JField("value",JString(value)) <- list2
        } yield value  ).headOption 
      if(received.isDefined && last.isDefined){
        val sum = (received.get.toDouble / 100000000) * last.get.toDouble
        val f:java.text.DecimalFormat = new java.text.DecimalFormat()
        f.setRoundingMode(java.math.RoundingMode.HALF_UP)
        f.setMaximumFractionDigits(0)        
        f.setCurrency(java.util.Currency.getInstance("SEK")) 
        val fsum=f.format(sum)
        ("wallet_total_received_sek" -> fsum )
      }else{
        ("wallet_total_received_sek" -> "NaN" )
      }
    }
    case false => ("wallet_total_received_sek" -> "Hidden" )            
  }    
  
  private def walletTotalReceivedUSD(wdata:JValue,mtgox:JValue,userLoggedIn:Boolean):JValue = userLoggedIn match {
    case true => {       
      val received:Option[BigInt] = (for {JField("total_received",JInt(total_received)) <- wdata } yield total_received).headOption
      val last:Option[String] = (for {
        JField("data",JObject(list)) <- mtgox 
        JField("last_all",JObject(list2))  <- list
        JField("value",JString(value)) <- list2
        } yield value  ).headOption 
      if(received.isDefined && last.isDefined){
        val sum = (received.get.toDouble / 100000000) * last.get.toDouble
        val f:java.text.DecimalFormat = new java.text.DecimalFormat()
        f.setRoundingMode(java.math.RoundingMode.HALF_UP)
        f.setMaximumFractionDigits(0)        
        f.setCurrency(java.util.Currency.getInstance("USD"))   
        val fsum=f.format(sum)
        ("wallet_total_received_usd" -> fsum )
      }else{
        ("wallet_total_received_usd" -> "NaN" )
      }
    }
    case false => ("wallet_total_received_usd" -> "Hidden" )                
  }    
  
  private def walletTotalReceivedEUR(wdata:JValue,mtgox:JValue,userLoggedIn:Boolean):JValue = userLoggedIn match {
    case true => {               
      val received:Option[BigInt] = (for {JField("total_received",JInt(total_received)) <- wdata } yield total_received).headOption
      val last:Option[String] = (for {
        JField("data",JObject(list)) <- mtgox 
        JField("last_all",JObject(list2))  <- list
        JField("value",JString(value)) <- list2
        } yield value  ).headOption 
      if(received.isDefined && last.isDefined){
        val sum = (received.get.toDouble / 100000000) * last.get.toDouble
        val f:java.text.DecimalFormat = new java.text.DecimalFormat()
        f.setRoundingMode(java.math.RoundingMode.HALF_UP)
        f.setMaximumFractionDigits(0)        
        f.setCurrency(java.util.Currency.getInstance("EUR"))  
        val fsum=f.format(sum)
        ("wallet_total_received_eur" -> fsum )
      }else{
        ("wallet_total_received_eur" -> "NaN" )
      }
    }  
    case false => ("wallet_total_received_eur" -> "Hidden" )                    
  }  
  
  private def walletNrTransactions(wdata:JValue):JValue = {
    //logger.debug("BTCRestHelper::walletNrTransactions()")     
    val n_trans:Option[BigInt]  = (for { JField("n_tx",JInt(n_trans)) <- wdata } yield n_trans).headOption
    if (n_trans.isDefined){
      ("wallet_no_transactions" -> n_trans.get)
    }else{
      ("wallet_no_transactions" -> "NaN")
    }
  }  

  /*-------------Wallet data end ------------------*/

  /*-------------SlushPool stat data start ------------------*/
  
  private def slushPoolStatTHashesPoolShare(exp: Option[String],outName:String):JValue = 
    exp match {
      case Some(value) => {
      val res = value.toDouble / 1000
      (outName -> res)
    }
    case None => (outName -> "NaN")
  }  
  
  private def slushPoolStatScore(exp: Option[String], outName:String  ):JValue = 
    exp match {
    case Some(value) => (outName -> value)      
    case None => (outName -> "NaN")      
  }
  
  private def slushPoolStatLuck(expr:Option[String],outName:String):JValue = 
    expr match {
    case Some(value) => {
      val res = (value.toDouble*100).round
      (outName -> res) }
    case None => (outName -> "NaN")      
  }   

  /*-------------SlushPool stat data end ------------------*/
  
  /*-------------Kapiton data ------------------*/
/*
  private def kaptionTickerPrice(data:JValue):JValue = {
    val value:Option[Double] =(for { JField("price",JDouble(price)) <- data } yield price ).headOption
    if(value.isDefined){
      ("kapiton_ticker_price" -> value.get)      
    }else{
      ("kapiton_ticker_price" -> "NaN")      
    }     
  }
  
  private def kaptionTickerBid(data:JValue):JValue = {
    val value:Option[Double] =(for { JField("bid",JDouble(bid)) <- data } yield bid ).headOption
    if(value.isDefined){
      ("kapiton_ticker_bid" -> value.get)      
    }else{
      ("kapiton_ticker_bid" -> "NaN")      
    }     
  }
  
  private def kaptionTickerAsk(data:JValue):JValue = {
    val value:Option[Double] =(for { JField("ask",JDouble(ask)) <- data } yield ask ).headOption
    if(value.isDefined){
      ("kapiton_ticker_ask" -> value.get)      
    }else{
      ("kapiton_ticker_ask" -> "NaN")      
    }     
  }
  
  private def kaptionTickerHi(data:JValue):JValue = {
    val value:Option[BigInt] =(for { JField("hi",JInt(hi)) <- data } yield hi ).headOption
    if(value.isDefined){
      ("kapiton_ticker_hi" -> value.get)      
    }else{
      ("kapiton_ticker_hi" -> "NaN")      
    }    
  }
  
  private def kaptionTickerLow(data:JValue):JValue = {
    val value:Option[BigInt] =(for { JField("low",JInt(low)) <- data } yield low ).headOption
    if(value.isDefined){
      ("kapiton_ticker_low" -> value.get)      
    }else{
      ("kapiton_ticker_low" -> "NaN")      
    }        
  }
  
  private def kaptionTickerVol(data:JValue):JValue = {
    val value:Option[BigInt] =(for { JField("vol",JInt(vol)) <- data } yield vol ).headOption
    if(value.isDefined){
      ("kapiton_ticker_vol" -> value.get)      
    }else{
      ("kapiton_ticker_vol" -> "NaN")      
    }    
  }
  
  private def kaptionTickerMonthAverage(data:JValue):JValue = {
    val value:Option[Double] =(for { JField("moavg",JDouble(moavg)) <- data } yield moavg ).headOption
    if(value.isDefined){
      ("kapiton_ticker_moavg" -> value.get)      
    }else{
      ("kapiton_ticker_moavg" -> "NaN")      
    }     
  }
  
  private def kaptionTickerMonthVolym(data:JValue):JValue = {
    val value:Option[Double] =(for { JField("movol",JDouble(movol)) <- data } yield movol ).headOption
    if(value.isDefined){
      ("kapiton_ticker_movol" -> value.get)      
    }else{
      ("kapiton_ticker_movol" -> "NaN")      
    }     
  }
*/
  /*-------------Kapiton data end ------------------*/

}
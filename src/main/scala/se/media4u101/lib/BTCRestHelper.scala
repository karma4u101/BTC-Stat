package se.media4u101.lib

import scala.xml.{ NodeSeq, Text }
import net.liftweb.common._
import net.liftweb.json.JsonAST._
import net.liftweb.json.DefaultFormats

trait BTCRestHelper extends Loggable {

  implicit val formats = DefaultFormats
  
  def getKapitonData():JValue = {
    //logger.debug("BTCRestHelper::getKapitonData()")    
    val data = RestProxy.fetchKapitonData()
    val kdata = this.getMergedKapitonData(data)
    kdata     
  }
  
  def getSlushPoolStatData():JValue = {
    //logger.debug("BTCRestHelper::getSlushPoolStat()")   
    val data = RestProxy.fetchSlushPoolStat()
    val spdata = this.getMergedSlushPoolStatData(data)
    spdata    
  }
  
  def getWalletData():JValue = {
    //logger.debug("BTCRestHelper::getWalletData()") 
    val data = RestProxy.fetchWalletData()
    val wdata = this.getMergedWalletData(data)
    wdata
  }
  
  def getAccProfileData():JValue = {
    import net.liftweb.json.JsonDSL._
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
  
  private def getMergedKapitonData(kdata:JValue):JValue = {
    import net.liftweb.json.JsonDSL._
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
  
  private def getMergedSlushPoolStatData(data:JValue):JValue = {
    import net.liftweb.json.JsonDSL._
    //logger.debug("BTCRestHelper::getMergedSlushPoolStatData()")    
    val round      = this.SlushPoolRoundStarted(data)
    val stratum    = this.SlushPoolStatActivetSratum(data)
    val workers    = this.SlushPoolStatActiveWorkers(data)
    val ghashshare = this.SlushPoolStatGHashesPoolShare(data)    
   val luck1      = this.SlushPoolStatLuck1(data)
    val luck7      = this.SlushPoolStatLuck7(data)
    val luck30     = this.SlushPoolStatLuck30(data)
    val duration   = this.SlushPoolStatRoundDuration(data)
    val score      = this.SlushPoolStatScore(data)
    val shares     = this.SlushPoolStatShares(data)
    val sharesCDF  = this.SlushPoolStatSharesCDF(data)
    val thashshare = this.SlushPoolStatTHashesPoolShare(data)
    val retval = round merge stratum merge workers merge ghashshare merge luck1 merge luck7 merge luck30 merge duration merge score merge shares merge sharesCDF merge thashshare  
    retval
  }
  
  private def getMergedWalletData(wdata:JValue):JValue = {
    import net.liftweb.json.JsonDSL._
    //logger.debug("BTCRestHelper::getMergedWalletData()")
    val ntx     = this.walletNrTransactions(wdata)
    val recived = this.walletTotalReceived(wdata)
    val balance = this.walletFinalBalance(wdata)
    val split   = this.walletForwardSplit(wdata)
    val data = ntx merge recived merge balance merge split 
    data
  }
  
  
  private def getMergedAccProfileData(accdata:JValue):JValue = {
    import net.liftweb.json.JsonDSL._
    //logger.debug("BTCRestHelper::getMergedAccProfileData()")
    val accum   = this.rewardAccumBTC(accdata)
    val data = accdata merge accum
    data
  }
  
  private def getMergedMtgoxSEKData(accdata:JValue,mtgox:JValue) : JValue = {
    import net.liftweb.json.JsonDSL._
    //logger.debug("BTCRestHelper::getMergedMtgoxSEKData()")    
    val conf   = this.confirmedRewardSEK(accdata, mtgox)
    val unconf = this.unConfirmedRewardSEK(accdata, mtgox)
    val est    = this.estimatedRewardSEK(accdata, mtgox)
    val tot    = this.rewardAccumTotalSEK(accdata, mtgox)
    val data = conf merge unconf merge est merge tot
    data
  }
  
  private def getMergedMtgoxUSDData(accdata:JValue,mtgox:JValue) : JValue = {
    import net.liftweb.json.JsonDSL._
    //logger.debug("BTCRestHelper::getMergedMtgoxUSDData()")
    val conf   = this.confirmedRewardUSD(accdata, mtgox)
    val unconf = this.unConfirmedRewardUSD(accdata, mtgox)
    val est    = this.estimatedRewardUSD(accdata, mtgox)
    val tot    = this.rewardAccumTotalUSD(accdata, mtgox)
    val data = conf merge unconf merge est merge tot
    data
  } 
  
  private def getMergedMtgoxEURData(accdata:JValue,mtgox:JValue) : JValue = {
    import net.liftweb.json.JsonDSL._
    //logger.debug("BTCRestHelper::getMergedMtgoxEURData()")    
    val conf   = this.confirmedRewardEUR(accdata, mtgox)
    val unconf = this.unConfirmedRewardEUR(accdata, mtgox)
    val est    = this.estimatedRewardEUR(accdata, mtgox)
    val tot    = this.rewardAccumTotalEUR(accdata, mtgox)
    val data = conf merge unconf merge est merge tot
    data
  }   
  

  /*--------General transforms start (is not working)--------*/
  
    
 /*--------General transforms-end-------*/ 
  
  /*-------------Account Profile data start ------------------*/
  
  
  private def rewardAccumBTC(data:JValue):JValue = {
    import net.liftweb.json.JsonDSL._
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

  //SEK
  private def estimatedRewardSEK(accdata:JValue,mtgox:JValue):JValue = {
    import net.liftweb.json.JsonDSL._
    //logger.debug("BTCRestHelper::estimatedRewardSEK()")     
    val estimated:Option[String] = (for {JField("estimated_reward",JString(estimated_reward)) <- accdata } yield estimated_reward).headOption
    val last:Option[String] = (for {
      JField("data",JObject(list)) <- mtgox 
      JField("last_all",JObject(list2))  <- list
      JField("value",JString(value)) <- list2
      } yield value  ).headOption 
     if(estimated.isDefined && last.isDefined){
       val sum = estimated.get.toDouble * last.get.toDouble
       ("estimated_reward_SEK" -> sum )
     }else{
       ("estimated_reward_SEK" -> "NaN" )
     }
  }
  
  private def confirmedRewardSEK(accdata:JValue,mtgox:JValue):JValue = {
    import net.liftweb.json.JsonDSL._
    //logger.debug("BTCRestHelper::confirmedRewardSEK()")     
    val confirmed:Option[String] =(for { JField("confirmed_reward",JString(confirmed_reward)) <- accdata } yield confirmed_reward ).headOption
    val last:Option[String] = (for {
      JField("data",JObject(list)) <- mtgox 
      JField("last_all",JObject(list2))  <- list
      JField("value",JString(value)) <- list2
      } yield value  ).headOption 
      if(confirmed.isDefined && last.isDefined){
        val sum = confirmed.get.toDouble * last.get.toDouble
        ("confirmed_reward_SEK" -> sum )
      }else{
        ("confirmed_reward_SEK" -> "NaN" )
      }
  }
  
  private def unConfirmedRewardSEK(accdata:JValue,mtgox:JValue):JValue = {
    import net.liftweb.json.JsonDSL._
    //logger.debug("BTCRestHelper::unConfirmedRewardSEK()")     
    val unconfirmed:Option[String] =(for { JField("unconfirmed_reward",JString(unconfirmed_reward)) <- accdata } yield unconfirmed_reward ).headOption
    val last:Option[String] = (for {
      JField("data",JObject(list)) <- mtgox 
      JField("last_all",JObject(list2))  <- list
      JField("value",JString(value)) <- list2
      } yield value  ).headOption 
      if(unconfirmed.isDefined && last.isDefined){
        val sum = unconfirmed.get.toDouble * last.get.toDouble
        ("unconfirmed_reward_SEK" -> sum )
      }else{
        ("unconfirmed_reward_SEK" -> "NaN" )
      }
  }    
  
  private def rewardAccumTotalSEK(accdata:JValue,mtgox:JValue):JValue = {
    import net.liftweb.json.JsonDSL._
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
        ("accum_reward_SEK" -> sum )
      }else{
        ("accum_reward_SEK" -> "NaN" )
      }
  }  
    
  //USD
  private def estimatedRewardUSD(accdata:JValue,mtgox:JValue):JValue = {
    import net.liftweb.json.JsonDSL._
    //logger.debug("BTCRestHelper::estimatedRewardUSD()")     
    val estimated:Option[String] = (for {JField("estimated_reward",JString(estimated_reward)) <- accdata } yield estimated_reward).headOption
    val last:Option[String] = (for {
      JField("data",JObject(list)) <- mtgox 
      JField("last_all",JObject(list2))  <- list
      JField("value",JString(value)) <- list2
      } yield value  ).headOption 
     if(estimated.isDefined && last.isDefined){
       val sum = estimated.get.toDouble * last.get.toDouble
       ("estimated_reward_USD" -> sum )
     }else{
       ("estimated_reward_USD" -> "NaN" )
     }    
  }
  
  private def confirmedRewardUSD(accdata:JValue,mtgox:JValue):JValue = {
    import net.liftweb.json.JsonDSL._
    //logger.debug("BTCRestHelper::confirmedRewardUSD()")     
    val confirmed:Option[String] =(for { JField("confirmed_reward",JString(confirmed_reward)) <- accdata } yield confirmed_reward ).headOption
    val last:Option[String] = (for {
      JField("data",JObject(list)) <- mtgox 
      JField("last_all",JObject(list2))  <- list
      JField("value",JString(value)) <- list2
      } yield value  ).headOption 
      if(confirmed.isDefined && last.isDefined){
        val sum = confirmed.get.toDouble * last.get.toDouble
        ("confirmed_reward_USD" -> sum )
      }else{
        ("confirmed_reward_USD" -> "NaN" )
      }    
  }
  
  private def unConfirmedRewardUSD(accdata:JValue,mtgox:JValue):JValue = {
    import net.liftweb.json.JsonDSL._
    //logger.debug("BTCRestHelper::unConfirmedRewardUSD()")     
    val unconfirmed:Option[String] =(for { JField("unconfirmed_reward",JString(unconfirmed_reward)) <- accdata } yield unconfirmed_reward ).headOption
    val last:Option[String] = (for {
      JField("data",JObject(list)) <- mtgox 
      JField("last_all",JObject(list2))  <- list
      JField("value",JString(value)) <- list2
      } yield value  ).headOption 
      if(unconfirmed.isDefined && last.isDefined){
        val sum = unconfirmed.get.toDouble * last.get.toDouble
        ("unconfirmed_reward_USD" -> sum )
      }else{
        ("unconfirmed_reward_USD" -> "NaN" )
      }    
  }    
  
  private def rewardAccumTotalUSD(accdata:JValue,mtgox:JValue):JValue = {
    import net.liftweb.json.JsonDSL._
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
        ("accum_reward_USD" -> sum )
      }else{
        ("accum_reward_USD" -> "NaN" )
      }    
  }
  
  //EUR
  private def estimatedRewardEUR(accdata:JValue,mtgox:JValue):JValue = {
    import net.liftweb.json.JsonDSL._
    //logger.debug("BTCRestHelper::estimatedRewardEUR()")     
    val estimated:Option[String] = (for {JField("estimated_reward",JString(estimated_reward)) <- accdata } yield estimated_reward).headOption
    val last:Option[String] = (for {
      JField("data",JObject(list)) <- mtgox 
      JField("last_all",JObject(list2))  <- list
      JField("value",JString(value)) <- list2
      } yield value  ).headOption 
     if(estimated.isDefined && last.isDefined){
       val sum = estimated.get.toDouble * last.get.toDouble
       ("estimated_reward_EUR" -> sum )
     }else{
       ("estimated_reward_EUR" -> "NaN" )
     }     
  }
  
  private def confirmedRewardEUR(accdata:JValue,mtgox:JValue):JValue = {
    import net.liftweb.json.JsonDSL._
    //logger.debug("BTCRestHelper::confirmedRewardEUR()")         
    val confirmed:Option[String] =(for { JField("confirmed_reward",JString(confirmed_reward)) <- accdata } yield confirmed_reward ).headOption
    val last:Option[String] = (for {
      JField("data",JObject(list)) <- mtgox 
      JField("last_all",JObject(list2))  <- list
      JField("value",JString(value)) <- list2
      } yield value  ).headOption 
      if(confirmed.isDefined && last.isDefined){
        val sum = confirmed.get.toDouble * last.get.toDouble
        ("confirmed_reward_EUR" -> sum )
      }else{
        ("confirmed_reward_EUR" -> "NaN" )
      }     
  }
  
  private def unConfirmedRewardEUR(accdata:JValue,mtgox:JValue):JValue = {
    import net.liftweb.json.JsonDSL._
    //logger.debug("BTCRestHelper::unConfirmedRewardEUR()")     
    val unconfirmed:Option[String] =(for { JField("unconfirmed_reward",JString(unconfirmed_reward)) <- accdata } yield unconfirmed_reward ).headOption
    val last:Option[String] = (for {
      JField("data",JObject(list)) <- mtgox 
      JField("last_all",JObject(list2))  <- list
      JField("value",JString(value)) <- list2
      } yield value  ).headOption 
      if(unconfirmed.isDefined && last.isDefined){
        val sum = unconfirmed.get.toDouble * last.get.toDouble
        ("unconfirmed_reward_EUR" -> sum )
      }else{
        ("unconfirmed_reward_EUR" -> "NaN" )
      }     
  }    
  
  private def rewardAccumTotalEUR(accdata:JValue,mtgox:JValue):JValue = {
    import net.liftweb.json.JsonDSL._
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
        ("accum_reward_EUR" -> sum )
      }else{
        ("accum_reward_EUR" -> "NaN" )
      }     
  }    
  
  /*-------------Account Profile data end ------------------*/

  /*-------------Wallet data start ------------------*/
  
  private def walletForwardSplit(wdata:JValue):JValue = {
    import net.liftweb.json.JsonDSL._      
    //logger.debug("BTCRestHelper::walletForwardSplit()")     
    val balance:Option[BigInt] = (for {JField("final_balance",JInt(final_balance)) <- wdata } yield final_balance).headOption
    val totres:Option[BigInt] = (for {JField("total_received",JInt(total_received)) <- wdata } yield total_received).headOption
    if(balance.isDefined && totres.isDefined){
      val split = (totres.get.toDouble - balance.get.toDouble) / (3*100000000) //forward split in 3 parts
      ("wallet_forward_split" -> split)
    }else{
      ("wallet_forward_split" -> "NaN")
    }         
  }
  
  private def walletFinalBalance(wdata:JValue):JValue = {
    import net.liftweb.json.JsonDSL._      
    //logger.debug("BTCRestHelper::walletFinalBalance()")     
    val balance:Option[BigInt] = (for {JField("final_balance",JInt(final_balance)) <- wdata } yield final_balance).headOption
    if(balance.isDefined){
      val totBtc = balance.get.toDouble / 100000000
      ("wallet_final_balance" -> totBtc)
    }else{
      ("wallet_final_balance" -> "NaN")
    }    
  }
  
  private def walletTotalReceived(wdata:JValue):JValue = {
    import net.liftweb.json.JsonDSL._      
    //logger.debug("BTCRestHelper::walletTotalReceived()")     
    val received:Option[BigInt] = (for {JField("total_received",JInt(total_received)) <- wdata } yield total_received).headOption
    if(received.isDefined){
      val totBtc = received.get.toDouble / 100000000
      ("wallet_total_received" -> totBtc)
    }else{
      ("wallet_total_received" -> "NaN")
    }
  }
  
  private def walletNrTransactions(wdata:JValue):JValue = {
    import net.liftweb.json.JsonDSL._      
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

  private def slushPoolStatLuck(data:JValue,inFieldName:String):Option[String] = {
    import net.liftweb.json.JsonDSL._     
    val value:Option[String] =(for { JField(inFieldName,JString(value)) <- data } yield value ).headOption
    value          
  }   
  
  private def SlushPoolStatRoundDuration(spdata:JValue):JValue    = {
    import net.liftweb.json.JsonDSL._      
    val duration:Option[String] =(for { JField("round_duration",JString(round_duration)) <- spdata } yield round_duration ).headOption
    if(duration.isDefined){
      ("sps_round_duration" -> duration)
    }else{
      ("sps_round_duration" -> "NaN")
    }
  }
  private def SlushPoolStatGHashesPoolShare(spdata:JValue):JValue = {
    import net.liftweb.json.JsonDSL._      
    val ghashes:Option[String] =(for { JField("ghashes_ps",JString(ghashes_ps)) <- spdata } yield ghashes_ps ).headOption
    if(ghashes.isDefined){
      ("sps_ghashes_ps" -> ghashes.get)
    }else{
      ("sps_ghashes_ps" -> "NaN")
    }    
  }
  
  private def SlushPoolStatTHashesPoolShare(spdata:JValue):JValue = {
    import net.liftweb.json.JsonDSL._     
    val ghashes:Option[String] =(for { JField("ghashes_ps",JString(ghashes_ps)) <- spdata } yield ghashes_ps ).headOption
    if(ghashes.isDefined){
      val thashes = ghashes.get.toDouble / 1000
      ("sps_thashes_ps" -> thashes)
    }else{
      ("sps_thashes_ps" -> "NaN")
    }
  }
  
  
  private def SlushPoolStatShares(spdata:JValue):JValue = {
    import net.liftweb.json.JsonDSL._     
    val shares:Option[BigInt] =(for { JField("shares",JInt(shares)) <- spdata } yield shares ).headOption
    if(shares.isDefined){
      ("sps_shares" -> shares.get)
    }else{
      ("sps_shares" -> "NaN")
    }
  }
  
  private def SlushPoolStatActiveWorkers(spdata:JValue):JValue = {
    import net.liftweb.json.JsonDSL._     
    val workers:Option[String] =(for { JField("active_workers",JString(active_workers)) <- spdata } yield active_workers ).headOption
    if(workers.isDefined){
      ("sps_active_workers" -> workers.get)      
    }else{
      ("sps_active_workers" -> "NaN")      
    }
  }
  
  private def SlushPoolStatActivetSratum(spdata:JValue):JValue = {
    import net.liftweb.json.JsonDSL._     
    val value:Option[String] =(for { JField("active_stratum",JString(active_stratum)) <- spdata } yield active_stratum ).headOption
    if(value.isDefined){
      ("sps_active_stratum" -> value.get)      
    }else{
      ("sps_active_stratum" -> "NaN")      
    }    
  }
  
  private def SlushPoolStatScore(spdata:JValue):JValue = {
    import net.liftweb.json.JsonDSL._     
    val value:Option[String] =(for { JField("score",JString(score)) <- spdata } yield score ).headOption
    if(value.isDefined){
      ("sps_score" -> value.get)      
    }else{
      ("sps_score" -> "NaN")      
    }     
  }
  

  
  private def SlushPoolRoundStarted(spdata:JValue):JValue = {
    import net.liftweb.json.JsonDSL._     
    val value:Option[String] =(for { JField("round_started",JString(round_started)) <- spdata } yield round_started ).headOption
    if(value.isDefined){
      logger.debug("BTCRestHelper::SlushPoolRoundStarted value.get='"+value.get+"'")
      ("sps_round_started" -> value.get)      
    }else{
      ("sps_round_started" -> "NaN")      
    }     
  }

  private def slushPoolStatLuck(data:JValue,inFieldName:String,outFiledName:String):JValue = {
    import net.liftweb.json.JsonDSL._     
    val value:Option[String] =(for { JField(inFieldName,JString(value)) <- data } yield value ).headOption
    if(value.isDefined){
      val luck = (value.get.toDouble*100).round
      (outFiledName -> luck)      
    }else{
      (outFiledName -> "NaN")      
    }           
  }    
  
  private def SlushPoolStatLuck7(spdata:JValue):JValue = {
    import net.liftweb.json.JsonDSL._     
    val value:Option[String] =(for { JField("luck_7",JString(luck_7)) <- spdata } yield luck_7 ).headOption
    if(value.isDefined){
      val luck = (value.get.toDouble*100).round
      ("sps_luck_7" -> luck)      
    }else{
      ("sps_luck_7" -> "NaN")      
    }     
  }
  
  private def SlushPoolStatLuck30(spdata:JValue):JValue = {
    import net.liftweb.json.JsonDSL._     
    val value:Option[String] =(for { JField("luck_30",JString(luck_30)) <- spdata } yield luck_30 ).headOption
    if(value.isDefined){
      val luck = (value.get.toDouble*100).round
      ("sps_luck_30" -> luck)      
    }else{
      ("sps_luck_30" -> "NaN")      
    }        
  }
  
  
  private def SlushPoolStatLuck1(spdata:JValue):JValue = {
    import net.liftweb.json.JsonDSL._     
    val value:Option[String] =(for { JField("luck_1",JString(luck_1)) <- spdata } yield luck_1 ).headOption
    if(value.isDefined){
      val luck = (value.get.toDouble*100).round
      ("sps_luck_1" -> luck)      
    }else{
      ("sps_luck_1" -> "NaN")      
    }          
  }
  
  private def SlushPoolStatSharesCDF(spdata:JValue):JValue = {
    import net.liftweb.json.JsonDSL._     
    val value:Option[String] =(for { JField("shares_cdf",JString(shares_cdf)) <- spdata } yield shares_cdf ).headOption
    if(value.isDefined){
      ("sps_shares_cdf" -> value.get)      
    }else{
      ("sps_shares_cdf" -> "NaN")      
    }     
  }

  /*-------------SlushPool stat data end ------------------*/
  
  /*-------------Kapiton data end ------------------*/

  private def kaptionTickerPrice(data:JValue):JValue = {
    import net.liftweb.json.JsonDSL._     
    val value:Option[Double] =(for { JField("price",JDouble(price)) <- data } yield price ).headOption
    if(value.isDefined){
      ("kapiton_ticker_price" -> value.get)      
    }else{
      ("kapiton_ticker_price" -> "NaN")      
    }     
  }
  
  private def kaptionTickerBid(data:JValue):JValue = {
   import net.liftweb.json.JsonDSL._     
    val value:Option[Double] =(for { JField("bid",JDouble(bid)) <- data } yield bid ).headOption
    if(value.isDefined){
      ("kapiton_ticker_bid" -> value.get)      
    }else{
      ("kapiton_ticker_bid" -> "NaN")      
    }     
  }
  
  private def kaptionTickerAsk(data:JValue):JValue = {
   import net.liftweb.json.JsonDSL._     
    val value:Option[Double] =(for { JField("ask",JDouble(ask)) <- data } yield ask ).headOption
    if(value.isDefined){
      ("kapiton_ticker_ask" -> value.get)      
    }else{
      ("kapiton_ticker_ask" -> "NaN")      
    }     
  }
  
  private def kaptionTickerHi(data:JValue):JValue = {
   import net.liftweb.json.JsonDSL._     
    val value:Option[BigInt] =(for { JField("hi",JInt(hi)) <- data } yield hi ).headOption
    if(value.isDefined){
      ("kapiton_ticker_hi" -> value.get)      
    }else{
      ("kapiton_ticker_hi" -> "NaN")      
    }    
  }
  
  private def kaptionTickerLow(data:JValue):JValue = {
   import net.liftweb.json.JsonDSL._     
    val value:Option[BigInt] =(for { JField("low",JInt(low)) <- data } yield low ).headOption
    if(value.isDefined){
      ("kapiton_ticker_low" -> value.get)      
    }else{
      ("kapiton_ticker_low" -> "NaN")      
    }        
  }
  
  private def kaptionTickerVol(data:JValue):JValue = {
   import net.liftweb.json.JsonDSL._     
    val value:Option[BigInt] =(for { JField("vol",JInt(vol)) <- data } yield vol ).headOption
    if(value.isDefined){
      ("kapiton_ticker_vol" -> value.get)      
    }else{
      ("kapiton_ticker_vol" -> "NaN")      
    }    
  }
  
  private def kaptionTickerMonthAverage(data:JValue):JValue = {
   import net.liftweb.json.JsonDSL._     
    val value:Option[Double] =(for { JField("moavg",JDouble(moavg)) <- data } yield moavg ).headOption
    if(value.isDefined){
      ("kapiton_ticker_moavg" -> value.get)      
    }else{
      ("kapiton_ticker_moavg" -> "NaN")      
    }     
  }
  
  private def kaptionTickerMonthVolym(data:JValue):JValue = {
   import net.liftweb.json.JsonDSL._     
    val value:Option[Double] =(for { JField("movol",JDouble(movol)) <- data } yield movol ).headOption
    if(value.isDefined){
      ("kapiton_ticker_movol" -> value.get)      
    }else{
      ("kapiton_ticker_movol" -> "NaN")      
    }     
  }
  
  
  /*-------------Kapiton data end ------------------*/

}
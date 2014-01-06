package se.media4u101.snippet

import se.media4u101.lib._
import se.media4u101.lib.EmptyRoundTrip

import net.liftweb._
import common._
import http.{RoundTripInfo, RoundTripHandlerFunc, S}
import json.JsonAST.{JString, JArray, JValue}

import net.liftweb.util.Schedule
import net.liftweb.util.Helpers._
//import net.liftweb.actor.LiftActor
import net.liftweb.http.SessionVar
import se.media4u101.lib.SessionChecker

object isLoggedIn extends SessionVar[Boolean](false)

trait RoundTrips extends EmptyRoundTrip with BTCRestHelper with Loggable {
  
  protected def doAccProfileRT(value : JValue, func : RoundTripHandlerFunc) : Unit = {
    logger.debug("RoundTrips::doAccProfileRT()")
    func.send(getAccProfileData())
  }  
  
  protected def doWalletRT(value : JValue, func : RoundTripHandlerFunc) : Unit = {
    logger.debug("RoundTrips::doWalletRT()")
    val userLoggedIn = S.loggedIn_?
    func.send(getWalletData(userLoggedIn))  
  }  
  
  protected def doSlushPoolStatRT(value : JValue, func : RoundTripHandlerFunc) : Unit = {
    logger.debug("RoundTrips::doSlushPoolStatRT()")
    func.send(getSlushPoolStatData())   
  }
  
  protected def doKapitonRT(value : JValue, func : RoundTripHandlerFunc) : Unit = {
    logger.debug("RoundTrips::doKapitonRT()")
    func.send(getKapitonData())
  }  
  
  protected def doServerDateTimeRT(value : JValue, func : RoundTripHandlerFunc) : Unit = {
    logger.debug("RoundTrips::doServerDateTimeRT()")
    func.send(getServerDateTime())
  } 
  
  //Simple hardcoded login function
  protected def doLoginRT(value : JValue, func : RoundTripHandlerFunc) : Unit = {
    import net.liftweb.json.JsonParser._
    import net.liftweb.json.JsonAST.{JObject, JField, JString, JArray, JValue}
    implicit val formats = net.liftweb.json.DefaultFormats  
    
    val pval = parse(value.extract[String])
    val name:Option[String] =(for { JField("name",JString(v)) <- pval } yield v ).headOption
    val pw:Option[String] =(for { JField("password",JString(v)) <- pval } yield v ).headOption

    import net.liftweb.json.JsonDSL._
    //the only correct login value is hardcoded here
    if(name.get.equals("poj") && pw.get.equals("pojpoj")){
      isLoggedIn.set(true)
    }else{
      isLoggedIn.set(false)
    }
    
    val auth = if(S.loggedIn_?) {
      parse("""{"loggedIn":true}""") 
    }else{
      parse("""{"loggedIn":false}""") 
    }
    func.send(auth)
  }  
  
  private val roundtrips : List[RoundTripInfo] = List("doAccProfileRT" -> doAccProfileRT _ ,
      "doWalletRT" -> doWalletRT _ ,
      "doSlushPoolStatRT" -> doSlushPoolStatRT _,
      "doKapitonRT" -> doKapitonRT _,
      "doServerDateTimeRT" -> doServerDateTimeRT _,
      "doLoginRT" -> doLoginRT _)
  abstract override def getRoundTrips = super.getRoundTrips ++ roundtrips  
  
}


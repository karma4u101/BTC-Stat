package se.media4u101.snippet

import se.media4u101.lib._
import se.media4u101.lib.EmptyRoundTrip

import net.liftweb._
import common._
import http.{RoundTripInfo, RoundTripHandlerFunc, S}
import json.JsonAST.{JString, JArray, JValue}

import net.liftweb.util.Schedule
import net.liftweb.util.Helpers._
import net.liftweb.actor.LiftActor
import se.media4u101.lib.SessionChecker

trait RoundTrips extends EmptyRoundTrip with BTCRestHelper with Loggable {
  
  var continue:Boolean = true;
  
  protected def doAccProfileRT(value : JValue, func : RoundTripHandlerFunc) : Unit = {
    logger.debug("RoundTrips::doAccProfileRT()")
    func.send(getAccProfileData())
  }  
  
  protected def doWalletRT(value : JValue, func : RoundTripHandlerFunc) : Unit = {
    logger.debug("RoundTrips::doWalletRT()")
    func.send(getWalletData())  
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
  
  private val roundtrips : List[RoundTripInfo] = List("doAccProfileRT" -> doAccProfileRT _ ,
      "doWalletRT" -> doWalletRT _ ,
      "doSlushPoolStatRT" -> doSlushPoolStatRT _,
      "doKapitonRT" -> doKapitonRT _,
      "doServerDateTimeRT" -> doServerDateTimeRT _ )
  abstract override def getRoundTrips = super.getRoundTrips ++ roundtrips  
  
}


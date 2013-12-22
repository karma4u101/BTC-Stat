package se.media4u101.lib

import _root_.net.liftweb._
import http._
import util._
import Helpers._
import common._

object SessionChecker extends Function2[Map[String, SessionInfo], SessionInfo => Unit, Unit] with Logger 
{ 
  def defaultKillWhen = 180000L 
  // how long do we wait to kill single browsers 
  @volatile var killWhen = defaultKillWhen 
  @volatile var killCnt = 0 
  def apply(sessions: Map[String, SessionInfo], 
            destroyer: SessionInfo => Unit): Unit = { 
    val cutoff = millis - 180000L 
    sessions.foreach { 
      case (name, si @ SessionInfo(session, agent, _, cnt, lastAccess)) => 
        if (cnt <= killCnt && lastAccess < cutoff) { 
          info("Purging "+agent) 
          destroyer(si) 
        } 
    } 
  } 
}  
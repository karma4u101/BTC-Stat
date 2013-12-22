package se.media4u101.snippet

import scala.xml._
import net.liftweb._
import http._
import http.js.JsCmds._
import http.js.JE.JsRaw

/*This snippet is injecting the roundtrip scripts into the tail of the html page body.*/
class RoundTripInjector extends RoundTrips {
  
  def render() : NodeSeq = {
    val functions = ((for {
      session <- S.session
    } yield <lift:tail>{Script(
        JsRaw(s"var myRTFunctions = ${session.buildRoundtrip(getRoundTrips).toJsCmd}").cmd
        )}</lift:tail>) openOr NodeSeq.Empty)
    functions
  }  

}
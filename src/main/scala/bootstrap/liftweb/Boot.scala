package bootstrap.liftweb

import net.liftweb._
import actor._
import util._
import Helpers._
import common._
import http._
import sitemap._
import Loc._

import net.liftweb.util.{ Props }
import net.liftweb.http.provider.HTTPRequest

import net.liftmodules.{FoBo}
import se.media4u101.lib.{ MyScheduledRestTask,SessionChecker}
import se.media4u101.snippet.RuntimeStats


/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {
    // where to search snippet
    LiftRules.addToPackages("code")
    LiftRules.addToPackages("se.media4u101")

    //def sitemapMutators = User.sitemapMutator
    //The SiteMap is built in the Site object bellow 
    LiftRules.setSiteMapFunc(() => Site.sitemap)
    
    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    
    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))

    //Init the FoBo - Front-End Toolkit module, 
    //see http://liftweb.net/lift_modules for more info
    FoBo.InitParam.JQuery=FoBo.JQuery1102  
    FoBo.InitParam.ToolKit=FoBo.Bootstrap301
    FoBo.InitParam.ToolKit=FoBo.FontAwesome401
    FoBo.InitParam.ToolKit=FoBo.AngularJS122
    FoBo.InitParam.ToolKit=FoBo.AJSNGGrid207
    FoBo.InitParam.ToolKit=FoBo.AJSUIBootstrap070
    FoBo.InitParam.ToolKit=FoBo.Pace0415
    FoBo.init()       
    
    MyScheduledRestTask ! MyScheduledRestTask.DoIt
    LiftRules.unloadHooks.append(() => MyScheduledRestTask ! MyScheduledRestTask.Stop)    
   
   SessionMaster.sessionCheckFuncs = SessionMaster.sessionCheckFuncs ::: List(SessionChecker)
    // Dump information about session every 10 minutes
    SessionMaster.sessionWatchers = SessionInfoDumper :: SessionMaster.sessionWatchers
    LiftRules.snippetDispatch.append(Map("runtime_stats" -> RuntimeStats))

    // Dump browser information each time a new connection is made
    LiftSession.onBeginServicing = BrowserLogger.haveSeenYou _ :: LiftSession.onBeginServicing    
    
  }
  
  //https://btc-e.com/
  //https://kapiton.se/
  //https://www.mtgox.com/
  object Site {
    import scala.xml._

    val home       = Menu.i("Home") / "index"
    
    val testing    = Menu(Loc("Testing",
        Link(List("testing"),true, "/index-old"),
        S.loc("Testing", Text("Testing")),
        LocGroup("lg1","top1") ))
                   
    val preev  = Menu(Loc("Preev", 
        ExtLink("http://preev.com/btc/sek"),
        S.loc("Preev", Text("Preev")),
        LocGroup("lg1"),
        FoBo.TBLocInfo.LinkTargetBlank ))
        
    val btcCharts  = Menu(Loc("BitCoinCharts", 
        ExtLink("http://bitcoincharts.com/"), 
        S.loc("BitCoinCharts",
        Text("BitCoin charts")),
        LocGroup("lg1"),
        FoBo.TBLocInfo.LinkTargetBlank ))
        
    val btcE  = Menu(Loc("btcE", 
        ExtLink("https://btc-e.com/"), 
        S.loc("btcE", Text("BTC-E")), 
        LocGroup("lg1"),
        FoBo.TBLocInfo.LinkTargetBlank ))
        
//    val kapiton  = Menu(Loc("kapiton", 
//        ExtLink("https://kapiton.se/"), 
//        S.loc("Kapiton", Text("Kapiton")), 
//        LocGroup("lg1"),
//        FoBo.TBLocInfo.LinkTargetBlank ))
        
    val mtgox  = Menu(Loc("mtgox", 
        ExtLink("https://www.mtgox.com/"), 
        S.loc("mtgox", Text("MT.GOX")), 
        LocGroup("lg1"),
        FoBo.TBLocInfo.LinkTargetBlank ))
        
    val slushPoolStats  = Menu(Loc("SlushPoolStats", 
        ExtLink("https://mining.bitcoin.cz/stats/"), 
        S.loc("SlushPoolStats", 
        Text("Slush Pool Stats")), 
        LocGroup("lg1"),
        FoBo.TBLocInfo.LinkTargetBlank ))

        //http://btctrading.wordpress.com/
    val btctrading  = Menu(Loc("BTCTraiding", 
        ExtLink("http://btctrading.wordpress.com/"),
        S.loc("BTCTraiding", Text("BTC Traiding Signals")),
        LocGroup("lg1"),
        FoBo.TBLocInfo.LinkTargetBlank ))
        
    def sitemap = SiteMap(
          home >> LocGroup("lg1","top1"),
         /* testing , */
          preev,
          btcCharts,
          slushPoolStats,
          btcE,
          /*kapiton,*/
          btctrading,
          mtgox
        )    
  } 
  
}

object BrowserLogger extends Loggable {
  object HaveSeenYou extends SessionVar(false)

  def haveSeenYou(session: LiftSession, request: Req) {
    if (!HaveSeenYou.is) {
      logger.info("Created session "+session.uniqueId+" IP: {"+request.request.remoteAddress+"} UserAgent: {{"+request.userAgent.openOr("N/A")+"}}")
      HaveSeenYou(true)
    }
  }
}

//see https://www.assembla.com/wiki/show/liftweb/Sessions
object SessionInfoDumper extends LiftActor with Loggable { 
  private var lastTime = millis 
  private def cyclePeriod = 5 minute 
  import se.media4u101.lib.SessionChecker 
  protected def messageHandler = 
    { 
      case SessionWatcherInfo(sessions) => 
        if ((millis - cyclePeriod) > lastTime) { 
          lastTime = millis 
          val rt = Runtime.getRuntime 
          rt.gc 
          RuntimeStats.lastUpdate = now 
          RuntimeStats.totalMem = rt.totalMemory 
          RuntimeStats.freeMem = rt.freeMemory 
          RuntimeStats.sessions = sessions.size 
          val percent = (RuntimeStats.freeMem * 100L) / RuntimeStats.totalMem 
          // get more aggressive about purging if we're 
          // at less than 35% free memory 
          if (percent < 35L) { 
            SessionChecker.killWhen /= 2L 
    if (SessionChecker.killWhen < 5000L) 
      SessionChecker.killWhen = 5000L 
            SessionChecker.killCnt *= 2 
          } else { 
            SessionChecker.killWhen *= 2L 
    if (SessionChecker.killWhen > 
                SessionChecker.defaultKillWhen) 
     SessionChecker.killWhen = SessionChecker.defaultKillWhen 
            val newKillCnt = SessionChecker.killCnt / 2 
    if (newKillCnt > 0) SessionChecker.killCnt = newKillCnt 
          } 
          val dateStr: String = now.toString 
          logger.info("[BTC-STAT MEMDEBUG] At " + dateStr + " Number of open sessions: " + sessions.size) 
          logger.info("[BTC-STAT MEMDEBUG] Free Memory: " + pretty(RuntimeStats.freeMem)) 
          logger.info("[BTC-STAT MEMDEBUG] Total Memory: " + pretty(RuntimeStats.totalMem)) 
          logger.info("[BTC-STAT MEMDEBUG] Kill Interval: " + (SessionChecker.killWhen / 1000L)) 
          logger.info("[BTC-STAT MEMDEBUG] Kill Count: " + (SessionChecker.killCnt)) 
        } 
    } 
  private def pretty(in: Long): String = 
    if (in > 1000L) pretty(in / 1000L) + "," + (in % 1000L) 
    else in.toString 
} 

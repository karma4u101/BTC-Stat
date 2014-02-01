package se.media4u101.lib

import net.liftweb.util.Schedule
import net.liftweb.actor.LiftActor
import net.liftweb.util.Helpers._
import net.liftweb.common._

object BTCScheduledRestTask extends LiftActor with Loggable {
  
  case class DoJob()
  case class Stop()
  
  private var stopped = false
  
   def messageHandler = {  
     case DoJob => 
       if (!stopped) {
        logger.info("BTCScheduledRestTask.DoIt start about to schedule the job")
        Schedule.schedule(this, DoJob, 2 minutes)
        logger.info("BTCScheduledRestTask.DoIt starting job")
        
        RestProxy.doScheduleJob() 
        
        logger.info("BTCScheduledRestTask.DoIt end")
        }else{
          logger.debug("BTCScheduledRestTask.DoIt Oh I have been asked to stop oki doki bye bye")
        }
     case Stop => {
       logger.debug("BTCScheduledRestTask.Stop Lift is shuting down so asking this service to stop.")
       stopped = true
     }
   }
 
}
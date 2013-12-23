package se.media4u101.lib

import net.liftweb.util.Schedule
import net.liftweb.actor.LiftActor
import net.liftweb.util.Helpers._
import net.liftweb.common._

object MyScheduledRestTask extends LiftActor with Loggable {
  
  case class DoIt()
  case class Stop()
  
  private var stopped = false
  
   def messageHandler = {  
     case DoIt => 
       if (!stopped) {
        logger.debug("MyScheduledTask.DoIt start about to schedule the job")
        Schedule.schedule(this, DoIt, 2 minutes)
        logger.debug("MyScheduledTask.DoIt starting job")
        
        RestProxy.doScheduleJob() 
        
        logger.debug("MyScheduledTask.DoIt end")
        }else{
          logger.debug("MyScheduledTask.DoIt Oh I have been asked to stop oki doki bye bye")
        }
     case Stop => {
       logger.debug("MyScheduledTask.Stop Lift is shuting down so asking this service to stop.")
       stopped = true
     }
   }
 
}
package jobs

import akka.actor._
import models.generated.Tables._
import com.mohiva.play.silhouette.api._
import javax.inject.Inject
import models.services.UserService
import org.joda.time.DateTime
import utils.Logger

class LastLoginUpdater @Inject() (
  system: ActorSystem,
  eventBus: EventBus,
  userService: UserService
) extends Actor with Logger {
  /**
   * Constructor that subscribes to [[LoginEvent]] notifications
   */
  {
    eventBus.subscribe(system.actorOf(Props(this)), classOf[LoginEvent[UserRow]])
  }

  /**
   * Receives a notification each time an user has logged In
   */
  override def receive = {
    case loginEvent: LoginEvent[_] => userService.update(loginEvent.identity.asInstanceOf[UserRow].copy(lastLogin = Option(DateTime.now)))
    case _ => logger.warn("Unexpected event received")
  }
}

object LastLoginUpdater {
  def props = Props[LastLoginUpdater]
}

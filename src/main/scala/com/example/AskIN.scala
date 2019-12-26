package com.example

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.util.Timeout
import scala.concurrent.duration._
import scala.util.{Failure, Success}

//This is also an interaction pattern
//grab pictures from
//https://doc.akka.io/docs/akka/snapshot/typed/interaction-patterns.html#request-response-with-ask-between-two-actors
object AI  {


  def apply() =
    Behaviors.receiveMessage[HalCommand] {
    case OpenThePodBayDoorsPlease(respondTo) =>
      respondTo ! HalResponse(s"I'm sorry, ${respondTo.path.name}. I'm afraid I can't do that.")
      Behaviors.same
    case WaitTo(ref) =>
      Behaviors.same
  }

  sealed trait HalCommand

  case class OpenThePodBayDoorsPlease(respondTo: ActorRef[HalResponse]) extends HalCommand

  case class WaitTo(ref: ActorRef[HalCommand]) extends HalCommand

  case class HalResponse(message: String) extends HalCommand

}


object Astronaut {

  import com.example.AI._

  def apply(ai: ActorRef[HalCommand]) =
    Behaviors.setup[CustomerCommand] { context =>
      implicit val timeout: Timeout = 3.seconds
      Behaviors.receiveMessage {
        case AdaptedResponse(message) =>
          context.log.info(s"Got response from $ai: {}", message)
          Behaviors.same
        case AskOpenMeTheDoor(ref) =>
          //In classic you had to do the type
          //(coffeeHouse ? CoffeeHouse.GetStatus).mapTo[CoffeeHouse.Status] onComplete {
          //  case Success(status) => log.info("Status: guest count = {}", status.guestCount)
          //  case Failure(error)  => log.error(error, "Can't get status!")
          //}
          context.ask(ai)(OpenThePodBayDoorsPlease) {
            //this will send the message to self
            case Success(HalResponse(message)) => AdaptedResponse(message)
            case Failure(ex) => AdaptedResponse("Request failed")
          }
          Behaviors.same

      }
    }

  sealed trait CustomerCommand

  case class AdaptedResponse(message: String) extends CustomerCommand

  case class AskOpenMeTheDoor(ref: ActorRef[HalCommand]) extends CustomerCommand


}

object Odyseey {

  import com.example.AI._
  import com.example.Astronaut._

  def apply(): Behavior[Start] = {
    Behaviors.setup { context =>
      val ai: ActorRef[HalCommand] = context.spawn(AI(), "Hal")
      val astronaut = context.spawn(Astronaut(ai), "Dave")

      Behaviors.receiveMessage {
        case s: Start => {
          astronaut ! AskOpenMeTheDoor(ai)
          Behaviors.same
        }
      }
    }
  }

  case class Start()

}

object MoviesFabricApp extends App {

  import com.example.Odyseey._

  val system: ActorSystem[Start] = ActorSystem(Odyseey(), "StanleyKubrik")
  system ! Start()
  Thread.sleep(100)
  system.terminate()

}


package com.example

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.util.Timeout
import com.example.Bar.Start
import com.example.Customer.OpenMeTheDoor
import com.example.Porter.{HalCommand, HalResponse, OpenThePodBayDoorsPlease}

import scala.concurrent.duration._
import scala.util.{Failure, Success}

//This is also an interaction pattern
//grab pictures from
//https://doc.akka.io/docs/akka/snapshot/typed/interaction-patterns.html#request-response-with-ask-between-two-actors
object Porter {


  def apply() = Behaviors.receiveMessage[HalCommand] {
    case OpenThePodBayDoorsPlease(respondTo) =>
      respondTo ! HalResponse("I'm sorry, Dave. I'm afraid I can't do that.")
      Behaviors.same
    case WaitTo(ref) =>
      Behaviors.same
  }

  sealed trait HalCommand

  case class OpenThePodBayDoorsPlease(respondTo: ActorRef[HalResponse]) extends HalCommand

  case class WaitTo(ref: ActorRef[HalCommand]) extends HalCommand

  case class HalResponse(message: String) extends HalCommand

}


object Customer {

  def apply(hal: ActorRef[HalCommand]) =
    Behaviors.setup[CustomerCommand] { context =>
      // Note: The second parameter list takes a function `ActorRef[T] => Message`,
      // as OpenThePodBayDoorsPlease is a case class it has a factory apply method
      // that is what we are passing as the second parameter here it could also be written
      // as `ref => OpenThePodBayDoorsPlease(ref)` TODO ask team akka, `ref => OpenThePodBayDoorsPlease(ref)` is not compiling
      //TODO val backendResponseMapper: ActorRef[Backend.Response] =
      //      context.messageAdapter(rsp => WrappedBackendResponse(rsp))

      implicit val timeout: Timeout = 3.seconds
      Behaviors.receiveMessage {
        // the adapted message ends up being processed like any other
        // message sent to the actor
        case AdaptedResponse(message) =>
          context.log.info("Got response from hal: {}", message)
          Behaviors.same
        case OpenMeTheDoor(ref) =>
          //never register Future callbacks like onComplete or map accessing
          //mutable actor state!
          //TODO QUESTION: The future callback is because it will use a thread of the pool of the dispatcher???
          context.ask(hal)(OpenThePodBayDoorsPlease) {
            //this will send the message to self
            case Success(HalResponse(message)) => AdaptedResponse(message)
            case Failure(ex) => AdaptedResponse("Request failed")
          }
          Behaviors.same

      }
    }

  sealed trait CustomerCommand

  case class AdaptedResponse(message: String) extends CustomerCommand

  case class OpenMeTheDoor(ref: ActorRef[HalCommand]) extends CustomerCommand


}

object Bar {

  def apply(): Behavior[Start] = {
    Behaviors.setup { context =>
      val porter: ActorRef[HalCommand] = context.spawn(Porter(), "porter")
      val customer = context.spawn(Customer(porter), "david")

      Behaviors.receiveMessage {
        //can I wait to say open the door please?
        case s: Start => {
          customer ! OpenMeTheDoor(porter)
          Behaviors.same
        }
      }
    }
  }

  case class Start()

}

object AskFabricApp extends App {

  val system: ActorSystem[Start] = ActorSystem(Bar(), "bar")
  system ! Start()
  system.terminate()

}




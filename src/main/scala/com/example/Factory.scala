package com.example

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import com.example.Guardian.Start
import com.example.SeriousMan.{HallowingMessage, Treat, Trick}


object Guardian {

  def apply(): Behavior[Start] =
    Behaviors.setup { context =>
      //initial tasks like of instance spawn
      context.log.info("setting up!")
      val actorRef: ActorRef[HallowingMessage] = context.spawn(SeriousMan(), "helloWorld3")
      Behaviors.receiveMessage { message =>
        //Actor creation, spawn and actor ref typed
        context.log.info(s"got $message, I should get going")
        actorRef ! Treat("chocolate")
        actorRef ! Trick("There is an English, an American and a French")
        Behaviors.same
      }
    }

  //why can't I pass an object?
  case class Start(goForIt: String)

}

object FactoryApp extends App {

  val system: ActorSystem[Start] = ActorSystem(Guardian(), "guardian")
  system ! Start("some'")
  system ! Start("some'")
  Thread.sleep(5000)
  system.terminate()

}


object SeriousMan {

  def apply(): Behavior[HallowingMessage] =
    Behaviors.receive { (context, message) =>
      message match {
        case Trick(joke) =>
          context.log.info(s"don't find it funny. Take your '$joke' joke back")
           Behaviors.stopped
        case Treat(content) =>
          context.log.info(s"thank you for the $content")
           Behaviors.same
      }
     
    }

  sealed trait HallowingMessage

  case class Treat(content: String) extends HallowingMessage

  case class Trick(joke: String) extends HallowingMessage

}



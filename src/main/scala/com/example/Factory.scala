package com.example

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import com.example.Guardian.Start
import com.example.SeriousMan.{Present, Stuff, Trick}



object Guardian {

  //why can't I pass an object?
  case class Start(goForIt: String)

  def apply(): Behavior[Start] =
    Behaviors.setup{ (context ) =>
      //initial tasks like of instance spawn
      val actorRef:ActorRef[Stuff] = context.spawn(SeriousMan(),"helloWorld3")

      Behaviors.receiveMessage{ ( message) =>
        //Actor creation, spawn and actor ref typed
        actorRef ! Trick("There is an English, an American and a French")
        actorRef ! Present("chocolate")
        actorRef ! Trick("There is an English, an American and a French")
        actorRef ! Present("Million Pounds")
        Behavior.same
      }
    }

}

object FactoryApp extends App {

  val system: ActorSystem[Start] = ActorSystem(Guardian(), "guardian")
  system ! Start("some'")

}


object SeriousMan {

  def apply(): Behavior[Stuff] =
    Behaviors.receive { (context, message) =>
      message match {
        case Trick(joke) =>
          context.log.info(s"don't find it funny. Take your '$joke' joke back")
        case Present(content) =>
          context.log.info(s"thank you for the $content")
      }
      Behavior.stopped
    }

  sealed trait Stuff

  case class Present(content: String) extends Stuff

  case class Trick(joke: String) extends Stuff

}



package com.example

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}



object SpawnerGuardian {

  import NoJokeMan._

  sealed trait Command
  //why can't I pass an object?
  case class Start(goForIt: String) extends Command

  def apply(): Behavior[Command] =
    Behaviors.receive { (context, _) =>
      //Actor creation, spawn and actor ref typed
      val actorRef:ActorRef[NoJokeMan.Command] = context.spawn(NoJokeMan(),"helloWorld3")
      actorRef ! Trick("There is an English, an American and a French")
      actorRef ! Treat("Million Pounds")
      Behavior.same
    }
}

object SpawnApp extends App {

  import SpawnerGuardian._

  val system: ActorSystem[SpawnerGuardian.Command] = ActorSystem(SpawnerGuardian(), "guardian")
  system ! Start("some' ")
  //possible problem
  //create too many actor with
  //system ! Start("another")

}


object  NoJokeMan {

  def apply(): Behavior[NoJokeMan.Command] =
    Behaviors.receive { (context, message) =>
      message match {
        case Trick(joke) =>
          context.log.info(s"don't find it funny. Take your '$joke' joke back")
          Behavior.stopped
        case Treat(content) =>
          context.log.info(s"thank you for the $content")
          Behavior.same
      }

    }

  sealed trait Command

  case class Treat(content: String) extends Command

  case class Trick(joke: String) extends Command

}



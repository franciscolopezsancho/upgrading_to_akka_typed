package com.example

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}


object HelloWorld3 {


  //be aware of the signature here
  def apply(): Behavior[HallowingMessage] =
    Behaviors.receive { (context, message) =>
      message match {
        case Trick(joke) =>
          context.log.info(s"don't find it funny. Take your '$joke' joke back")
        case Trait(content) =>
          context.log.info(s"thank you for the $content")
      }
      Behavior.stopped
    }

  //recommended to be called Command
  sealed trait HallowingMessage

  case class Trait(content: String) extends HallowingMessage

  case class Trick(joke: String) extends HallowingMessage

}


object HelloWorld3App extends App {

  import HelloWorld3._

  //be aware of the signature here
  val system: ActorSystem[HallowingMessage] = ActorSystem(HelloWorld3(), "helloWorldMain")
  system ! Trick("There is an English, an American and a French")
  system.terminate()

}



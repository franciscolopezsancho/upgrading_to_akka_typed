package com.example

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import com.example.HelloWorld3.{Stuff, Trick}



object HelloWorld3 {

  //be aware of the signature here
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
  //recommended to be called Command
  sealed trait Stuff

  case class Present(content: String) extends Stuff

  case class Trick(joke: String) extends Stuff


}


object HelloWorld3App extends App {

  //be aware of the signature here
  val system: ActorSystem[Stuff] = ActorSystem(HelloWorld3(), "helloWorldMain")
  system ! Trick("There is an English, an American and a French")

}



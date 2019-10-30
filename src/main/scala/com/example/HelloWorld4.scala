package com.example

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import com.example.HelloWorld4.{Present, Stuff, Trick}

//STATE
object HelloWorld4 {

  def apply(): Behavior[Stuff] =
    doYourThing(1)

  private def doYourThing(patience: Int): Behavior[Stuff] =
    Behaviors.receive { (context, message) =>
      message match {
        case Trick(joke) =>
          context.log.info(s"that joke is not to funny to be honest")
          if (patience == 0) {
            context.log.info("Please leave me alone")
            Behavior.stopped
          } else {
            doYourThing(patience - 1)
            //functional style, In and out!
            //we don't need explicit behavior here, it's implicit
          }
        case Present(content) =>
          context.log.info(s"thank you for the $content")
          Behavior.same
      }
    }

  sealed trait Stuff

  case class Present(content: String) extends Stuff

  case class Trick(joke: String) extends Stuff


}


object HelloWorld4App extends App {

  val system: ActorSystem[Stuff] = ActorSystem(HelloWorld4(), "helloWorldMain")
  system ! Trick("There is an English, an American and a French")
  system ! Present("chocolate")
  system ! Trick("There is an English, an American and a French")
  system ! Present("Million Pounds")
  //bare in mind how the system takes the message before it stops. Stopping is the actor not the App



}



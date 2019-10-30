package com.example

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}


object HelloWorld {

  def apply(): Behavior[String] =
    //factory 1
    Behaviors.receive { (context, message) =>
      context.log.info(s"the message is.... $message")
      //factory 2
      Behavior.stopped
    }
}


object HelloWorldApp extends App {

  val system: ActorSystem[String] = ActorSystem(HelloWorld(), "helloWorldMain")
  system ! "Hello"
  system.terminate()

}

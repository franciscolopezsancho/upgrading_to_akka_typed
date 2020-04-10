package com.example

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}


object HelloWorld {

  //type inference
  def apply(): Behaviors.Receive[String] =
  //factory 1
  //type inference
    Behaviors.receive{ (context, message) =>
      context.log.info(s"the message is.... $message")
      //factory 2
      Behaviors.stopped
    }
}



object HelloWorldApp extends App {

  val system: ActorSystem[String] = ActorSystem(HelloWorld(), "helloWorldMain")
  system ! "Hello"
  system ! "Hello"
  Thread.sleep(1000)
  //be aware that this could terminate before it receives the second message

}

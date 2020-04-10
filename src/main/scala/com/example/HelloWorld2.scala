package com.example

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import com.example.HelloWorld2.Strong

//the beauty of types
object HelloWorld2 {

  def apply(): Behavior[String] =
    Behaviors.receive { (context, message) =>
      message match {
        case s:String =>
          context.log.info(s"the message is.... $message")
          Behaviors.stopped
//        case Strong(content) =>
//          context.log.info(s"the message is.... $content")
//          Behavior.stopped
      }
    }

  case class Strong(content: String)
}


object HelloWorld2App extends App {

  val system: ActorSystem[String] = ActorSystem(HelloWorld2(), "helloWorldMain")
   system ! "Hello"
  // system ! Strong("hi")

}



package com.example

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}

//the beauty of types
//object HelloWorld2 {
//
//  def apply(): Behavior[String] =
//    Behaviors.receive { (context, message) =>
//      message match {
//          ///why do I need the s here to avoid
//          // Java class java.lang.String is not a value
//          //[error]         case String =>
//        case s:String =>
//          context.log.info(s"the message is.... $message")
//        case Present(content) =>
//          context.log.info(s"the message is.... $content")
//      }
//      Behavior.stopped
//    }
//
//  case class Present(content: String)
//}


//object HelloWorld2App extends App {
//
//  val system: ActorSystem[String] = ActorSystem(HelloWorld2(), "helloWorldMain")
//  system ! "Hello"
//
//}


//case class Present(content: String) could I do this?
//type myType = String || Present
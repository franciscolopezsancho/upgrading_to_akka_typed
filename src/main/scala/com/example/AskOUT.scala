package com.example

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

//This is also an interaction pattern
//grab pictures from
//https://doc.akka.io/docs/akka/snapshot/typed/interaction-patterns.html#request-response-with-ask-between-two-actors


object CookieFabric {

  def apply(): Behaviors.Receive[CookieFabric.GiveMeCookies] =
    Behaviors.receiveMessage { message =>
//      Thread.sleep(message.count * 4000)
      message.replyTo ! CookiesBox(message.count)
      Behaviors.same
    }

  sealed trait Command

  case class GiveMeCookies(count: Int, replyTo: ActorRef[Reply]) extends Command

  sealed trait Reply

  case class CookiesBox(count: Int) extends Reply


}

object CookieFabricApp extends App {


  import akka.actor.typed.scaladsl.AskPattern._
  import akka.util.Timeout

  implicit val timeout: Timeout = 3.seconds

  val cookieFabric: ActorSystem[CookieFabric.GiveMeCookies] = ActorSystem(CookieFabric(), "helloWorldMain")
  implicit val scheduler = cookieFabric.scheduler

  val result: Future[CookieFabric.Reply] = cookieFabric.ask(ref => CookieFabric.GiveMeCookies(4, ref))

  // the response callback will be executed on this execution context
  // with great power comes....
  implicit val ec = cookieFabric.executionContext


  result.onComplete {
    case Success(CookieFabric.CookiesBox(count)) => println(s"Yay, $count cookies!")
    case Failure(ex) => println(s"Boo! didn't get cookies: ${ex.getMessage}")
  }

}







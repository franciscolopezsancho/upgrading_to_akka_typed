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
      Thread.sleep(message.count * 1000)
      message.replyTo ! Cookies(message.count)
      Behaviors.same
    }

  sealed trait Command {}

  sealed trait Reply

  case class GiveMeCookies(count: Int, replyTo: ActorRef[Reply]) extends Command

  case class Cookies(count: Int) extends Reply


}


object CookieFabricApp extends App {


  import akka.actor.typed.scaladsl.AskPattern._
  import akka.util.Timeout

  implicit val timeout: Timeout = 3.seconds

  val cookieFabric: ActorSystem[CookieFabric.GiveMeCookies] = ActorSystem(CookieFabric(), "helloWorldMain")
  implicit val scheduler = cookieFabric.scheduler

  //Checkout the timeout each cookie takes a seccond
                                          //adapter anonymo
  val result: Future[CookieFabric.Reply] = cookieFabric.ask(ref => CookieFabric.GiveMeCookies(4, ref))

  // the response callback will be executed on this execution context
  //TODO elaborate on that
  implicit val ec = cookieFabric.executionContext

  result.onComplete {
    case Success(CookieFabric.Cookies(count)) => println(s"Yay, $count cookies!")
    case Failure(ex) => println(s"Boo! didn't get cookies: ${ex.getMessage}")
  }


  //In actor code where some API returned a Future use pipeToSelf
  //to turn it into a message
  //Never
  //do Await.result in production code!
  //register Future callbacks like onComplete or map accessing
  //mutable actor state!
}


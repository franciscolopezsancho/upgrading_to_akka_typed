//#full-example
package com.example

import akka.actor.SupervisorStrategy.{Decider, Restart, Stop}
import akka.actor.{Actor, ActorInitializationException, ActorKilledException, ActorLogging, ActorRef, ActorSystem, DeathPactException, OneForOneStrategy, Props, SupervisorStrategy}

object Greeter {

  def props(message: String, printerActor: ActorRef): Props = Props(new Greeter(message, printerActor))

  final case class WhoToGreet(who: String)

  case object Greet

}

class Greeter(message: String, printerActor: ActorRef) extends Actor with ActorLogging {

  import Greeter._
  import Printer._

  var greeting = ""

  def receive = {
    case WhoToGreet(who) =>
      greeting = message + ", " + who
    case Greet =>
      log.info("I'm greeting you")
      printerActor ! Greeting(greeting)
  }
}

object Printer {

  def props: Props = Props[Printer]

  final case class Greeting(greeting: String)

}

class Printer extends Actor with ActorLogging {

  import Printer._

  final val myDefaultDecider: Decider = {
    case _: ActorInitializationException => Stop
    case _: ActorKilledException => Stop
    case _: DeathPactException => Stop
    case _: Exception => Restart
  }
  override val supervisorStrategy = OneForOneStrategy()(myDefaultDecider)
  //SupervisorStrategy.defaultStrategy

  def receive = {
    case Greeting(greeting) =>
      context.stop(sender())
      log.info("Greeting received (from " + sender() + "): " + greeting)
  }
}

object AkkaQuickstart extends App {

  import Greeter._

  // Create the 'helloAkka' actor system
  val system: ActorSystem = ActorSystem("helloAkka")

  // Create the printer actor
  val printer: ActorRef = system.actorOf(Printer.props, "printerActor")

  // Create the 'greeter' actors

  val howdyGreeter: ActorRef =
    system.actorOf(Greeter.props("Howdy", printer), "howdyGreeter")
  val helloGreeter: ActorRef =
    system.actorOf(Greeter.props("Hello", printer), "helloGreeter")
  val goodDayGreeter: ActorRef =
    system.actorOf(Greeter.props("Good day", printer), "goodDayGreeter")

  howdyGreeter ! WhoToGreet("Akka")
  howdyGreeter ! Greet

  howdyGreeter ! WhoToGreet("Lightbend")
  howdyGreeter ! Greet

  helloGreeter ! WhoToGreet("Scala")
  helloGreeter ! Greet

  goodDayGreeter ! WhoToGreet("Play")
  goodDayGreeter ! Greet
  //be aware not error is thrown
  goodDayGreeter ! "hi there"
}

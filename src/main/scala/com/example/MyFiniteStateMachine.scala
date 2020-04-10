package com.example

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import com.example.MyFiniteStateMachine.{Treat, Trick}

//why FSM and not infinite state machine? any implementation of that
object MyFiniteStateMachine {

  //what options do we have
  //
  def apply(): Behavior[Command] =
    goodMood()

  def goodMood(): Behavior[Command] =
    Behaviors.receive { (context, message) =>
      message match {
        case Trick(joke) =>
          context.log.info(s"don't find it funny. Take your '$joke' joke back")
          badMood()
        case Treat(content) =>
          context.log.info(s"thank you for the $content")
          Behaviors.same
      }
    }

  def badMood(): Behavior[Command] = {
    Behaviors.receive { (context, message) =>
      message match {
        case Trick(joke) =>
          context.log.info(s"don't find it funny. Take your '$joke' joke back")
          Behaviors.stopped
        case Treat(content) =>
          context.log.info(s"thank you for the $content")
          goodMood()
      }
    }
  }

  sealed trait Command

  //if not final what would happen? some other message you don't handle!
  final case class Treat(content: String) extends Command

  final case class Trick(joke: String) extends Command

}

object NonTuringMachineApp extends App {

  //be aware of the signature here
  val system: ActorSystem[MyFiniteStateMachine.Command] = ActorSystem(MyFiniteStateMachine(), "helloWorldMain")
  system ! Trick("take this spider")
  system ! Treat("chocolate")
  system ! Trick("aweful joke")
  system ! Trick("aweful joke")
  system ! Treat("chocolate")
  Thread.sleep(100)
  system.terminate()

}

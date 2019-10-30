package com.example

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}


object Parent {

  def apply(): Behavior[Command] = {
    def updated(children: Map[String, ActorRef[Child.Command]]): Behavior[Command] = {
      Behaviors.receive { (context, command) =>
        command match {
          case DelegateToChild(name, childCommand) =>
            children.get(name) match {

              case Some(ref) =>
                ref ! childCommand
                context.watchWith(ref, ChildTerminated(name))
                Behaviors.same
              case None =>
                val ref = context.spawn(Child(), name)
                ref ! childCommand
                updated(children + (name -> ref))
            }
            //shouldn't this be in PostStop? and also
          case ChildTerminated(name) =>
            updated(children - name)
        }
      }
    }

    updated(Map.empty)
  }

  sealed trait Command

  case class DelegateToChild(name: String, message: Child.Command) extends Command

  private case class ChildTerminated(name: String) extends Command


}



object Child {

  sealed trait Command
  case object Command1 extends Command
  case object Command2 extends Command

  def apply():Behavior[Command] = {
    Behaviors.same
  }

}
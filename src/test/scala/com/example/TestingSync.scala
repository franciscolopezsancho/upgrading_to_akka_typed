package com.example


import akka.actor.testkit.typed.CapturedLogEvent
import akka.actor.testkit.typed.Effect.{Spawned, SpawnedAnonymous}
import akka.actor.testkit.typed.scaladsl.{BehaviorTestKit, TestInbox}
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Behaviors
import org.scalatest.{Matchers, WordSpec}
import org.slf4j.event.Level


object SyncTestingExample {

  val childActor = Behaviors.receiveMessage[String] { _ =>
    Behaviors.same[String]
  }


  object Hello {

    def apply(): Behaviors.Receive[Command] = Behaviors.receivePartial {
      case (context, CreateChild(name)) =>
        context.spawn(childActor, name)
        Behaviors.same
      case (context, CreateAnonymousChild) =>
        context.spawnAnonymous(childActor)
        Behaviors.same
      case (context, SayHelloToChild(childName)) =>
        val child: ActorRef[String] = context.spawn(childActor, childName)
        child ! "hello"
        Behaviors.same
      case (context, SayHelloToAnonymousChild) =>
        val child: ActorRef[String] = context.spawnAnonymous(childActor)
        child ! "hello stranger"
        Behaviors.same
      case (_, SayHello(who)) =>
        who ! "hello"
        Behaviors.same
      case (context, LogAndSayHello(who)) =>
        context.log.info("Saying hello to {}", who.path.name)
        who ! "hello"
        Behaviors.same
    }

    sealed trait Command

    case class CreateChild(childName: String) extends Command

    case class SayHelloToChild(childName: String) extends Command

    case class SayHello(who: ActorRef[String]) extends Command

    case class LogAndSayHello(who: ActorRef[String]) extends Command

    case object CreateAnonymousChild extends Command

    case object SayHelloToAnonymousChild extends Command

  }

}


class SyncTestingExampleSpec extends WordSpec with Matchers {

  import SyncTestingExample._

  "Typed actor synchronous testing" must {

    "record spawning" in {
      val testKit = BehaviorTestKit(Hello())
      testKit.run(Hello.CreateChild("child"))
      testKit.expectEffect(Spawned(childActor, "child"))
    }

    "record spawning anonymous" in {
      val testKit = BehaviorTestKit(Hello())
      testKit.run(Hello.CreateAnonymousChild)
      testKit.expectEffect(SpawnedAnonymous(childActor))
    }

    "record message sends" in {
      val testKit = BehaviorTestKit(Hello())
      val inbox = TestInbox[String]()
      testKit.run(Hello.SayHello(inbox.ref))
      inbox.expectMessage("hello")
    }

    "send a message to a spawned child" in {
      //Another use case is sending a message to a child actor you can do this by looking up the TestInbox for a child actor
      val testKit = BehaviorTestKit(Hello())
      testKit.run(Hello.SayHelloToChild("child"))
      val childInbox = testKit.childInbox[String]("child")
      childInbox.expectMessage("hello")
    }

    "send a message to an anonymous spawned child" in {
      val testKit = BehaviorTestKit(Hello())
      testKit.run(Hello.SayHelloToAnonymousChild)
      val child = testKit.expectEffectType[SpawnedAnonymous[String]]

      val childInbox = testKit.childInbox(child.ref)
      childInbox.expectMessage("hello stranger")
    }

//    "log a message to the logger" in {
//      val testKit = BehaviorTestKit(Hello())
//      val inbox = TestInbox[String]("Inboxer")
//      testKit.run(Hello.LogAndSayHello(inbox.ref))
//      testKit.logEntries() shouldBe Seq(CapturedLogEvent(Level.DEBUG,"Saying hello to Inboxer"))
//    }
  }
}


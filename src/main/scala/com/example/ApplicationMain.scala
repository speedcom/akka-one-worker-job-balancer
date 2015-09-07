package com.example

import akka.actor.ActorSystem
import scala.collection.immutable.Range

object ApplicationMain extends App {

  val system = ActorSystem("MyActorSystem")

  val masterActor = system.actorOf(MasterActor.props, MasterActor.name)

  JobProducer.produceJobs(ids = (1 to 5)).foreach {
    job =>
      println(s"generated job with id ${job.id}")
      masterActor ! MasterActor.NewJob(job)
  }

  system.awaitTermination()
}

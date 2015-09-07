package com.example

import akka.actor.{Actor, ActorLogging, Props}
import scala.collection.immutable.{Queue, Range}

class MasterActor extends Actor with ActorLogging {
  import MasterActor._

  var queue = Queue.empty[Job]
  var worker = context.actorOf(WorkerActor.props, WorkerActor.name)

  var acks = 0

  def receive = {
    case NewJob(job) =>
      log.info(s"master got new job with id ${job.id} while worker IS NOT busy")
      context.become(beasyWorkerReceive)
      worker ! WorkerActor.NewJob(job)
    case Ack if acks == 0 =>
      acks = 1
      JobProducer.produceJobs(ids = (6 to 10)).foreach {
        job =>
          log.info(s"generated job with id ${job.id}")
          self ! MasterActor.NewJob(job)
      }
    case Ack =>
      log.info(s"rest in piece world... ")
      context.system.shutdown
  }

  def beasyWorkerReceive: Receive = {
    case NewJob(job)                =>
      log.info(s"master got new job with id ${job.id} while worker IS busy")
      queue = queue :+ job
    case GimmeJob if queue.size > 0 =>
      worker ! WorkerActor.NewJob(queue.head)
      queue = queue.tail
    case GimmeJob                   =>
      worker ! WorkerActor.NoJobActually
      context.unbecome()
  }

}

object MasterActor {

  def props = Props[MasterActor]
  def name = "master-name"

  sealed trait MasterMsg
  case class NewJob(job: Job) extends MasterMsg
  case object GimmeJob extends MasterMsg
  case object Ack extends MasterMsg
}
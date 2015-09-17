package com.example

import akka.actor.{Actor, ActorLogging, Props}
import scala.collection.immutable.{Queue, Range}

class MasterActor extends Actor with ActorLogging {
  import MasterActor._

  var worker = context.actorOf(WorkerActor.props, WorkerActor.name)

  def receive = freeWorkerReceive(Queue.empty[Job])

  def freeWorkerReceive(jobs: Queue[Job]): Receive = {
    case NewJob(job)     =>
      log.info(s"master got new job with id ${job.id} while worker IS NOT busy")
      context.become(beasyWorkerReceive(jobs))
      worker ! WorkerActor.NewJob(job)
    case Ack             =>
      log.info(s"rest in piece world... ")
      context.system.shutdown
    case WorkerBusy(job) =>
      log.info(s"Worker is busy. Going to save job in queue")
      context.become(beasyWorkerReceive(jobs :+ job))
  }

  def beasyWorkerReceive(jobs: Queue[Job]): Receive = {
    case NewJob(job)                =>
      log.info(s"master got new job with id ${job.id} while worker IS busy")
      context.become(beasyWorkerReceive(jobs :+ job))
    case GimmeJob if jobs.size > 0  =>
      worker ! WorkerActor.NewJob(jobs.head)
      context.become(beasyWorkerReceive(jobs.tail))
    case GimmeJob                   =>
      worker ! WorkerActor.NoJobActually
      context.become(freeWorkerReceive(Queue.empty[Job]))
  }

}

object MasterActor {

  def props = Props[MasterActor]
  def name = "master-name"

  sealed trait MasterMsg
  case class NewJob(job: Job) extends MasterMsg
  case object GimmeJob extends MasterMsg
  case class WorkerBusy(job: Job) extends MasterMsg
  case object Ack extends MasterMsg
}
package com.example

import akka.actor.{Actor, ActorRef, ActorLogging, Props}
import scala.concurrent.Future
import akka.pattern.pipe
import scala.concurrent.ExecutionContext.Implicits.global

class WorkerActor extends Actor with ActorLogging {
  import WorkerActor._

  def receive = idleWorker

  def idleWorker: Receive = {
    case NewJob(job)   =>
      processLongRunningJob(job).map(_ => SuccessedJob).pipeTo(self)
      context.become(waitForRunningJob(sender()))
    case NoJobActually =>
      log.info("Ok, but do not forget about me!")
      sender ! MasterActor.Ack
  }

  def waitForRunningJob(source: ActorRef): Receive = {
    case SuccessedJob =>
      context.become(idleWorker)
      source ! MasterActor.GimmeJob
    case NewJob(job)  =>
      log.warning("[Worker] I am busy...")
      source ! MasterActor.WorkerBusy(job)
  }

  def processLongRunningJob(job: Job): Future[JobResult] = {
    log.info(s"Long running job = ${job.id}")
    Thread.sleep(3000) // simulation of "heavy" time computaiton work - 1 sec is good enough for our purpose
    log.info(s"Finished job id = ${job.id}")
    Future.successful(SuccessJob)
  }

}

object WorkerActor {

  def props = Props[WorkerActor]
  def name = "worker-name"

  sealed trait WorkerMsg
  case class NewJob(job: Job) extends WorkerMsg
  case object NoJobActually extends WorkerMsg
  case object SuccessedJob extends WorkerMsg

  sealed trait JobResult
  case object SuccessJob extends JobResult
  case object FailedJob extends JobResult
}
package com.example

import akka.actor.{Actor, ActorLogging, Props}

class WorkerActor extends Actor with ActorLogging {
  import WorkerActor._

  var busy: Boolean = _

  def receive = {
    case NewJob(job) if busy == false =>
      processedLongRunningJob(job)
      sender ! MasterActor.GimmeJob
    case NewJob(job) if busy == true  =>
      log.warning("Sth goes wrong as hell...") // this shouldn't never happens
    case NoJobActually                 =>
      log.info("Ok, but do not forget about me!")
      sender ! MasterActor.Ack
  }

  // todo: make it returning Future[JobResult]
  def processedLongRunningJob(job: Job) = {
    log.info(s"Long running job = ${job.id}")
    busy = true
    Thread.sleep(1000) // simulation of "heavy" time computaiton work - 1 sec is good enough for our purpose
    busy = false
    log.info(s"Finished job id = ${job.id}")
  }

}

object WorkerActor {

  def props = Props[WorkerActor]
  def name = "worker-name"

  sealed trait WorkerMsg
  case class NewJob(job: Job) extends WorkerMsg
  case object NoJobActually extends WorkerMsg
}
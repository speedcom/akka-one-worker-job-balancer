package com.example

object JobProducer {
  import scala.collection.immutable.Range

  def produceJobs(ids: Range): Seq[Job] = {
    ids map {
      id =>
        Job(id = id, name = s"job-nr-$id")
    }
  }

}
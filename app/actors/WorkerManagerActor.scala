package actors

import akka.actor._

class WorkerManagerActor(workerProps: Props, maxWorkers: Int) extends Actor {

  override val receive = active(0)

  private def active(numOfWorkers: Int): Receive = {
    case Terminated(worker) =>
      active(numOfWorkers - 1)
    case msg if numOfWorkers < maxWorkers =>
      val worker = newWorker
      worker ! msg
      onNewWorker(numOfWorkers + 1)
  }

  private val overloaded: Receive = {
    case Terminated(worker) =>
      context.become(active(maxWorkers - 1))
    case _ =>
      sender ! Status.Failure(new TooManyRequestsException)
  }

  private def onNewWorker(activeWorkers: Int) = {
    if (activeWorkers == maxWorkers) {
      context.become(overloaded)
    } else {
      context.become(active(activeWorkers))
    }
  }
  
  private def newWorker = {
    val worker = context.actorOf(workerProps)
    context.watch(worker)
    worker
  }
}

object WorkerManagerActor {
  def props(workerProps: Props, maxWorkers: Int) = Props(new WorkerManagerActor(workerProps, maxWorkers))
}
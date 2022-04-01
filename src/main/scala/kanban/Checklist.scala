package kanban

import scalafx.scene.paint.Color

import scala.collection.mutable.Buffer

class Checklist(private var tasks: Buffer[(Boolean, String)] = Buffer[(Boolean, String)]()) {
  def addTask(task: String) = tasks.append((false, task))

  def removeTask(index: Int) = tasks.remove(index)

  def toggleStatus(task: String) = {
    val index = tasks.map(_._2).indexOf(task)
    if (tasks(index)._1) {
      tasks(index) = (false, tasks(index)._2)
    } else {
      tasks(index) = (true, tasks(index)._2)
    }
  }

  def getProgress: Double = tasks.count(_._1).toDouble / tasks.size

  def getTasksNames = tasks.map(_._2)

  def getTasks = tasks

  def resetTasks() = tasks.clear()

  def allComplete = tasks.count(_._1) == tasks.size

  def setTasks(newTasks: Buffer[(Boolean, String)]) = tasks = newTasks

  def hasTasks = tasks.nonEmpty

  def getCorrectColor(isLate: Boolean) = {
    if (allComplete) {
      Color.Green
    } else if (isLate) {
      Color.Red
    } else {
      Color.Black
    }

  }

  override def toString: String = s"${tasks.count(_._1)} / ${tasks.size} completed"

}

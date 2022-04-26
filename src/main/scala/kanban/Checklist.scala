package kanban

import scalafx.scene.paint.Color
import scala.collection.mutable.Buffer


/** Models a checklist consisting of tasks that can either be completed or not.
 *
 * @param tasks tasks of the checklist, have a boolean state and text detailing the task
 */
class Checklist(private var tasks: Buffer[(Boolean, String)] = Buffer[(Boolean, String)]()) {

  // Simple get-methods for getting information about the Checklist
  def getTasks: Buffer[(Boolean, String)] = tasks

  def getTasksNames: List[String] = tasks.map(_._2).toList

  def getProgress: Double = tasks.count(_._1).toDouble / tasks.size

  /** Adds a task to the checklist
   *
   * @param task text describing task */
  def addTask(task: String): Unit = tasks.append((false, task)) // new task is false at the beginning

  /** Removes a task from the checklist
   *
   * @param index index of task to be deleted */
  def removeTask(index: Int): Unit = tasks.remove(index)

  /** Returns whether the Checklist has any tasks */
  def hasTasks: Boolean = tasks.nonEmpty

  /** Changes all tasks to a new set of tasks
   *
   * @param newTasks set of new tasks for the checklist */
  def setTasks(newTasks: Buffer[(Boolean, String)]): Unit = tasks = newTasks

  /** Removes all tasks from the checklist */
  def resetTasks(): Unit = tasks.clear()

  /** Toggles the boolean done state of a task
   *
   * @param task text describing task */
  def toggleStatus(task: String): Unit = {
    val index = tasks.map(_._2).indexOf(task)
    tasks(index) = (!tasks(index)._1, tasks(index)._2)
  }

  /** Returns whether all tasks on the checklist are complete */
  private def allComplete: Boolean = tasks.count(_._1) == tasks.size

  /** Returns a color the deadline will be shown as
   *
   * @param isLate whether current time is past card deadline
   * @return correct color depending on task state */
  def getCorrectColor(isLate: Boolean): Color = {
    if (allComplete) { // green if all tasks are complete
      Color.Green
    } else if (isLate) { // red if all tasks are not complete and past deadline
      Color.Red
    } else { // else neutral black
      Color.Black
    }
  }

  /** Checklist in String form, describes how tasks have been completed
   *
   * @return string representing checklist progress */
  override def toString: String = s"${tasks.count(_._1)} / ${tasks.size} completed"

}

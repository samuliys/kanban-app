package kanban

import scalafx.scene.paint.Color
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


/** Models a deadline, a card feature. Deadline has a due date and has a boolean state.
 *
 * @param date   due date of the deadline
 * @param status state whether the deadline has been completed
 */
class Deadline(private val date: LocalDate, private var status: Boolean = false) {

  // Simple get-methods for getting information about the Deadline
  def getRawDate: LocalDate = date

  def getDate: String = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

  def getStatus: Boolean = status

  /** Toggles the done state of the checklist */
  def toggleStatus(): Unit = status = !status

  /** Returns the number of days until the deadline */
  private def daysUntil: Long = ChronoUnit.DAYS.between(LocalDate.now(), date)

  /** Returns whether current time is past deadline */
  private def isLate: Boolean = daysUntil < 0

  /** Returns a color the deadline will be shown as
   *
   * @param tasks tasks related to the card, if has, the tasks will decide the color
   * @return correct color depending on task state (if has) or deadline done state */
  def getCorrectColor(tasks: Checklist): Color = {
    if (tasks.hasTasks) { // if card has tasks, the state of them will set the color
      tasks.getCorrectColor(isLate)
    } else { // else deadline state decides
      if (status) { // green if done
        Color.Green
      } else if (isLate) { // red if not done and past due date
        Color.Red
      } else { // else neutral black
        Color.Black
      }
    }
  }

  /** Deadline in String form, describes how long till deadline / how much past it
   *
   * @return string representing deadline state */
  override def toString: String = {
    if (daysUntil == 1) {
      s"Due tomorrow ($getDate)"
    } else if (daysUntil > 0) {
      s"Due in ${daysUntil.toString} days ($getDate)"
    } else if (daysUntil == 0) {
      "Due today"
    } else { // for past dates
      s"Due ${daysUntil.abs.toInt.toString} days ago"
    }
  }
}

package kanban

import scalafx.scene.paint.Color

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class Deadline(private val date: LocalDate, private var status: Boolean = false) {
  def getRawDate = date

  def getDate = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

  def daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), date)

  def daysUntilString = daysUntil.toString

  def toggleStatus() = {
    if (status) status = false else status = true
  }

  def isLate = daysUntil < 0

  def getCorrectColor(tasks: Checklist) = {
    if (tasks.hasTasks) {
      tasks.getCorrectColor(isLate)
    } else {
      if (status) {
        Color.Green
      } else if (isLate) {
        Color.Red
      } else {
        Color.Black
      }
    }
  }

  def getStatus = status

  def getString = {
    if (daysUntil == 1) {
      s"Due tomorrow ($getDate)"
    } else if (daysUntil > 0) {
      s"Due in $daysUntilString days ($getDate)"
    } else if (daysUntil == 0) {
      "Due today"
    } else {
      s"Due ${daysUntil.abs.toInt.toString} days ago"
    }
  }
}

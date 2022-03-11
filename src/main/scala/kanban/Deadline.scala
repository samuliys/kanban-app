package kanban

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class Deadline(private val date: LocalDate) {
  def getRawDate = date

  def getDate = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

  def daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), date)

  def daysUntilString = daysUntil.toString

  def getString = {
    if (daysUntil == 1) {
      s"Due tomorrow ($getDate)"
    } else if (daysUntil > 0) {
      s"Due in $daysUntilString days ($getDate)"
    } else if (daysUntil == 0) {
      "Due today"
    } else {
      "Due day ago"
    }
  }
}

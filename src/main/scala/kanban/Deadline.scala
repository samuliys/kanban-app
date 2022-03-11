package kanban

import java.time.LocalDate
import java.time.temporal.ChronoUnit

class Deadline(private val date: LocalDate) {
  def getRawDate = date

  def getDate = date.toString

  def daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), date)
}

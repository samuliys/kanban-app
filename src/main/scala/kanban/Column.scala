package kanban

import scala.collection.mutable.Buffer


class Column(private var name: String) {
  private val cards = Buffer[Card]()
  this.addCard("card1")
  this.addCard("card2")
  this.addCard("card3")

  def getCards = this.cards
  def getName = name

  def rename(newName: String) = name = newName

  def addCard(name: String = "untitled") = {
    val card = new Card(name)
    cards += card
  }
}
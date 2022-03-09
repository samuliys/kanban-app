package kanban

import scalafx.scene.paint.Color

import scala.collection.mutable.Buffer


class Column(private var name: String) {
  private val cards = Buffer[Card]()
  this.addCard("card1", Color.Blue)
  this.addCard("card2", Color.GreenYellow)
  this.addCard("card3", Color.OrangeRed)

  def getCards = this.cards
  def getName = name

  def rename(newName: String) = name = newName

  def addCard(name: String = "untitled", color: Color = Color.Blue) = {
    val card = new Card(name, color)
    cards += card
  }
}
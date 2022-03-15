package kanban

import scalafx.scene.paint.Color
import scala.collection.mutable.Buffer


class Column(private var name: String,
             private var color: Color,
             private val cards: Buffer[Card] = Buffer[Card]()) {

  def getCards = this.cards

  def getName = name

  def getColor = color

  def rename(newName: String) = name = newName

  def addCard(name: String, color: Color, tags: Buffer[String] = Buffer[String](), deadline: Option[Deadline] = None, index: Int = cards.size): Card = {
    val card = new Card(name, color, tags, deadline)
    cards.insert(index, card)
    card
  }

  def addCard(card: Card) = {
    cards.insert(0, card)
  }

  def deleteCard(card: Card) = {
    cards.remove(cards.indexOf(card))
  }

  def editColumn(newName: String, newColor: Color) = {
    name = newName
    color = newColor
  }
}

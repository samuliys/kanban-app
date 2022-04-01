package kanban

import scalafx.scene.paint.Color

import java.io.File
import scala.collection.mutable.Buffer


class Column(private var name: String = "",
             private var color: Color = Color.Black,
             private val cards: Buffer[Card] = Buffer[Card]()) {

  def getName = name

  def getColor = color

  def getCards = cards

  def rename(newName: String) = name = newName

  def addNewCard(text: String, color: Color, tags: Buffer[String], checklist: Checklist, deadline: Option[Deadline], file: Option[File], subCard: Option[SubCard] = None) = {
    cards += new Card(text, color, tags, checklist, deadline, file, subCard)
  }

  def addCard(card: Card, index: Int = 0) = {
    cards.insert(index, card)
  }

  def deleteCard(card: Card) = {
    cards.remove(cards.indexOf(card))
  }

  def editColumn(newName: String, newColor: Color) = {
    name = newName
    color = newColor
  }
}

package kanban

import scala.collection.mutable.Buffer

class Tag(private val name: String) {
  private val cards = Buffer[Card]()

  def addCard(card: Card) = cards += card
  def removeCard(card: Card) = cards.remove(cards.indexOf(card))

  def getTagCards = cards
}
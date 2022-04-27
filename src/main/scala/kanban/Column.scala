package kanban

import scalafx.scene.paint.Color
import java.io.File
import scala.collection.mutable.Buffer


/** Models a list, column on the board. Columns have cards inside them.
 *
 * @param name  name of the list
 * @param color border color
 * @param cards cards that the list contains
 */
class Column(private var name: String = "",
             private var color: Color = Color.Black,
             private val cards: Buffer[Card] = Buffer[Card]()) {

  // Simple get-methods for getting information about the Column
  def getName: String = name

  def getColor: Color = color

  def getCards: Buffer[Card] = cards

  /** Creates a new card and adds it to the column cards
   *
   * @param text        text on the card
   * @param textColor   color of text
   * @param borderColor color of border
   * @param tags        tags that the card has
   * @param checklist   card's checklist
   * @param deadline    card's deadline
   * @param file        possible file attachment
   * @param subCard     possible card attachment
   * @param url         possible url attachment */
  def addNewCard(text: String, textColor: Color, borderColor: Color, tags: Buffer[String],
                 checklist: Checklist, deadline: Option[Deadline], file: Option[File],
                 subCard: Option[SubCard] = None, url: Option[String] = None): Unit = {
    cards += new Card(text, textColor, borderColor, tags, checklist, deadline, file, subCard, url)
  }

  /** Adds an existing card to specific location on the Column
   *
   * @param card  card to be added to the column
   * @param index index of target location */
  def addCard(card: Card, index: Int = 0): Unit = {
    cards.insert(index, card)
  }

  /** Deletes a card from the column
   *
   * @param card card to be deleted */
  def deleteCard(card: Card): Unit = cards -= card

  /** Edits column information based on given parameters
   *
   * @param newName  new name of the column
   * @param newColor new border color of the column */
  def editColumn(newName: String, newColor: Color): Unit = {
    name = newName
    color = newColor
  }
}

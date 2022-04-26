package kanban

import scalafx.scene.paint.Color
import java.io.File
import scala.collection.mutable.Buffer


/** Models a kanban board. A board has lists that have cards inside them.
 * Boards can also have background images and each of them has their own archive.
 *
 * @param text        text on the card
 * @param textColor   color of text
 * @param borderColor color of border
 * @param tags        tags that the card has
 * @param checklist   card's checklist
 * @param deadline    card's deadline
 * @param file        possible file attachment
 * @param subCard     possible card attachment
 * @param url         possible url attachment
 */
class Card(private var text: String = "",
           private var textColor: Color = Color.Black,
           private var borderColor: Color = Color.Black,
           private var tags: Buffer[String] = Buffer[String](),
           private var checklist: Checklist = new Checklist,
           private var deadline: Option[Deadline] = None,
           private var file: Option[File] = None,
           private var subCard: Option[SubCard] = None,
           private var url: Option[String] = None) {

  // Simple get-methods for all aspects of the Card class
  def getText: String = text

  def getTextColor: Color = textColor

  def getBorderColor: Color = borderColor

  def getTags: Buffer[String] = tags

  def getChecklist: Checklist = checklist

  def getDeadline: Option[Deadline] = deadline

  def getFile: Option[File] = file

  def getSubcard: Option[SubCard] = subCard

  def getUrl: Option[String] = url

  /** Adds a new tag to the card
   *
   * @param tag tag to be added */
  def addTag(tag: String): Unit = tags += tag

  /** Removes a tag from the card, granted it has it
   *
   * @param tag tag to be deleted */
  def removeTag(tag: String): Unit = {
    // if a tag is deleted it needs to be removed from all cards,
    // check that it has a certain tag before attempting to remove
    if (tags.contains(tag)) tags -= tag
  }

  /** Edit all card information based on given parameters
   *
   * @param newText        new text on the card
   * @param newTextColor   new color of text
   * @param newBorderColor new color of border
   * @param newTags        new tags of the card
   * @param newChecklist   new card checklist
   * @param newDeadline    new card deadline
   * @param newFile        new possible file attachment
   * @param newSubcard     new possible card attachment
   * @param newUrl         new possible url attachment */
  def editCard(newText: String, newTextColor: Color, newBorderColor: Color,
               newTags: Buffer[String], newChecklist: Checklist, newDeadline: Option[Deadline],
               newFile: Option[File], newSubcard: Option[SubCard], newUrl: Option[String]): Unit = {

    text = newText
    textColor = newTextColor
    borderColor = newBorderColor
    tags = newTags
    deadline = newDeadline
    file = newFile
    checklist = newChecklist
    url = newUrl
    subCard = newSubcard
  }

  /** Returns whether the card has a deadline set */
  def hasDeadline: Boolean = deadline.isDefined

  /** Turns a card to a subcard so it can be used as a card attachment */
  def toSub: SubCard = new SubCard(this)

  /** Removes file attachment from the card. Done if file has been moved so it no longer can be accessed. */
  def resetFile(): Unit = file = None
}

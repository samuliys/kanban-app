package kanban

import scalafx.scene.paint.Color
import java.io.File
import scala.collection.mutable.Buffer

class Card(private var text: String = "",
           private var textColor: Color = Color.Black,
           private var borderColor: Color = Color.Black,
           private var tags: Buffer[String] = Buffer[String](),
           private var checklist: Checklist = new Checklist,
           private var deadline: Option[Deadline] = None,
           private var file: Option[File] = None,
           private var subCard: Option[SubCard] = None,
           private var url: Option[String] = None) {

  def getText = text

  def getTextColor = textColor

  def getBorderColor = borderColor

  def getTags = tags

  def getChecklist = checklist

  def addTag(tag: String) = tags += tag

  def removeTag(tag: String) = {
    if (tags.contains(tag)) {
      tags.remove(tags.indexOf(tag))
    }
  }

  def editCard(newText: String, newTextColor: Color, newBorderColor: Color,
               newTags: Buffer[String], newChecklist: Checklist, newDeadline: Option[Deadline],
               newFile: Option[File], newSubcard: Option[SubCard], newUrl: Option[String]) = {
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

  def hasDeadline = deadline.isDefined

  def getDeadline = deadline

  def getFile = file

  def addSubCard(card: SubCard) = subCard = Some(card)

  def getSubcard = subCard

  def removeSubcard(): Unit = subCard = None

  def getUrl = url

  def toSub: SubCard = new SubCard(this)

  def resetFile() = file = None
}

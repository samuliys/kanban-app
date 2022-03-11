package kanban

import scalafx.scene.paint.Color

import scala.collection.mutable.Buffer

class Card(private var text: String, private var color: Color, private var tags: Buffer[Tag], private var deadline: Option[Deadline]) {

  def getText = text

  def getColor = color

  def getTags = tags

  def getTagNames = tags.map(_.getName)

  def addTag(tag: Tag) = tags += tag

  def removeTag(tag: Tag) = tags.remove(tags.indexOf(tag))

  def editCard(newText: String, newColor: Color, newTags: Buffer[Tag], newDeadline: Option[Deadline]) = {
    text = newText
    color = newColor
    tags = newTags
    deadline = newDeadline
  }

  def hasDeadline = deadline.isDefined

  def getDeadline = deadline
}

package kanban

import scalafx.scene.paint.Color

import scala.collection.mutable.Buffer

class Card(private var text: String,
           private var color: Color,
           private var tags: Buffer[String],
           private var deadline: Option[Deadline]) {

  def getText = text

  def getColor = color

  def getTags = tags

  def addTag(tag: String) = tags += tag

  def removeTag(tag: String) = {
    if (tags.contains(tag)) {
      tags.remove(tags.indexOf(tag))
    }
  }

  def editCard(newText: String, newColor: Color, newTags: Buffer[String], newDeadline: Option[Deadline]) = {
    text = newText
    color = newColor
    tags = newTags
    deadline = newDeadline
  }

  def hasDeadline = deadline.isDefined

  def getDeadline = deadline
}

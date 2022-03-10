package kanban

import scalafx.scene.paint.Color

import scala.collection.mutable.Buffer

class Card(private var text: String, private var color: Color, private var tags: Buffer[Tag]) {

  def getText = text

  def getColor = color

  def getTags = tags

  def getTagNames = tags.map(_.getName)

  def addTag(tag: Tag) = tags += tag

  def removeTag(tag: Tag) = tags.remove(tags.indexOf(tag))

  def editCard(newText: String, newColor: Color, newTags: Buffer[Tag]) = {
    text = newText
    color = newColor
    tags = newTags
  }
}

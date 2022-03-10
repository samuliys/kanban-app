package kanban

import scalafx.scene.paint.Color

import scala.collection.mutable.Buffer

class Card(private var text: String, private var color: Color) {

  private val tags = Buffer[Tag]()
  def getText = text
  def getColor = color

  def getTags = tags

  def editCard(newText: String, newColor: Color) = {
    text = newText
    color = newColor
  }

  def addTag(tag: Tag) = tags += tag
}

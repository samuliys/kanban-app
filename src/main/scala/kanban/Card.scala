package kanban

import scalafx.scene.paint.Color

class Card(private var text: String, private var color: Color) {

  def getText = text
  def getColor = color

  def changeText(newText: String) = text = newText

}
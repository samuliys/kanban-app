package kanban

class Card(private var text: String) {

  def getText = text

  def changeText(newText: String) = text = newText

}
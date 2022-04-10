package kanban

class SubCard(card: Card) extends Card(card.getText, card.getColor, card.getTags, card.getChecklist, card.getDeadline, card.getFile, card.getSubcard) {

  private var original = card

  def getOriginal = original

}

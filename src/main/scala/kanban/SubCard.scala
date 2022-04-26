package kanban


/** Models a card attachment on a card. Class was created to circumvent a circe-library json-encode restriction.
 *
 * @param card the card the subcard models
 */
class SubCard(card: Card) extends Card {

  // Save the original card
  private val asCard = new Card(card.getText, card.getTextColor, card.getBorderColor, card.getTags, card.getChecklist, card.getDeadline, card.getFile)

  /** Returns the subcard as an instance of the original Card class
   *
   * @return Original card version of the subcard */
  def getCard = asCard

}

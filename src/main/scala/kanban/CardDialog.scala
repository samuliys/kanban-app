package kanban

import kanban.Main.{activeCard, cardEditActive, kanbanApp, stage}
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.geometry.Insets
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.scene.paint.Color


object CardDialog {

  val dialog = new Dialog[Card]() {
    initOwner(stage)
    title = "Create new card"

    headerText = "Create new card using options below"
  }

  val okButtonType = new ButtonType("OK", ButtonData.OKDone)
  dialog.dialogPane().buttonTypes = Seq(okButtonType, ButtonType.Cancel)

  val cardText = new TextField() {
    promptText = "Card Text"
    minWidth = 200
  }
  val cardColor = new ColorPicker(Color.Black) {
    promptText = "Color"

  }

  val grid = new GridPane() {
    hgap = 10
    vgap = 10
    padding = Insets(20, 200, 10, 10)

    add(new Label("Text:"), 0, 0)
    add(cardText, 1, 0)
    add(new Label("Color:"), 0, 1)
    add(cardColor, 1, 1)
  }

  val okButton = dialog.dialogPane().lookupButton(okButtonType)
  okButton.disable = true

  cardText.text.onChange { (_, _, newValue) =>
    okButton.disable = newValue.trim().isEmpty
  }

  dialog.dialogPane().content = grid

  Platform.runLater(cardText.requestFocus())

  def reset() = {
    dialog.title = "Kanban - new card"
    dialog.headerText = "Add new card"
    cardText.text = ""
    cardColor.value = Color.Black
  }

  def setCardEdit(card: Card) = {
    reset()
    dialog.title = "Kanban - card edit"
    dialog.headerText = "Edit card"
    cardText.text = card.getText
    cardColor.value = card.getColor
  }

  dialog.resultConverter = dialogButton =>
    if (dialogButton == okButtonType) {
      if (cardEditActive) {
        activeCard.editCard(cardText.text(), cardColor.getValue)
        new Card(cardText.text(), cardColor.getValue)
      } else {
        kanbanApp.getActiveColumn.addCard(cardText.text(), cardColor.getValue)
        new Card(cardText.text(), cardColor.getValue)
      }

    } else
      null
}
package kanban

import javafx.scene.paint.Color
import kanban.Main.{kanbanApp, stage}
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.geometry.Insets
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.layout._
import scalafx.scene.control._


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
  val cardColor = new ColorPicker(Color.BLUE) {
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
    cardText.text = ""
    cardColor.value = Color.BLUE
  }

  dialog.resultConverter = dialogButton =>
    if (dialogButton == okButtonType) {
      kanbanApp.getActiveColumn.addCard(cardText.text(), cardColor.getValue)
      new Card(cardText.text(), cardColor.getValue)
    } else
      null
}
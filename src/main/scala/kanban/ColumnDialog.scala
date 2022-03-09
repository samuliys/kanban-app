package kanban

import javafx.scene.paint.Color
import kanban.Main.{columnEditActive, kanbanApp, stage}
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.geometry.Insets
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.layout._
import scalafx.scene.control._


object ColumnDialog {

  val dialog = new Dialog[Column]() {
    initOwner(stage)
    title = "Kanban - new column"
    headerText = "Add new column"
  }

  val okButtonType = new ButtonType("OK", ButtonData.OKDone)
  dialog.dialogPane().buttonTypes = Seq(okButtonType, ButtonType.Cancel)

  val columnName = new TextField() {
    promptText = "Card Text"
    minWidth = 200
  }

  val columnColor = new ColorPicker(Color.BLUE) {
    promptText = "Color"
  }

  val grid = new GridPane() {
    hgap = 10
    vgap = 10
    padding = Insets(20, 200, 10, 10)

    add(new Label("Name:"), 0, 0)
    add(columnName, 1, 0)
    add(new Label("Color:"), 0, 1)
    add(columnColor, 1, 1)
  }

  val okButton = dialog.dialogPane().lookupButton(okButtonType)
  okButton.disable = true

  columnName.text.onChange { (_, _, newValue) =>
    okButton.disable = newValue.trim().isEmpty
  }

  dialog.dialogPane().content = grid

  Platform.runLater(columnName.requestFocus())

  def reset() = {
    dialog.title = "Kanban - new column"
    dialog.headerText = "Add new column"
    columnName.text = ""
    columnColor.value = Color.BLUE
  }

  def setColumnEdit(column: Column) = {
    reset()
    dialog.title = "Kanban - column edit"
    dialog.headerText = "Edit column"
    columnName.text = column.getName
    columnColor.value = column.getColor
  }

  dialog.resultConverter = dialogButton =>
    if (dialogButton == okButtonType) {
      if (columnEditActive) {
        kanbanApp.getActiveColumn.editColumn(columnName.text(), columnColor.getValue)
        new Column(columnName.text(), columnColor.getValue)
      } else {
        kanbanApp.getBoards.addColumn(columnName.text(), columnColor.getValue)
        new Column(columnName.text(), columnColor.getValue)
      }

    } else {
      null
    }
}
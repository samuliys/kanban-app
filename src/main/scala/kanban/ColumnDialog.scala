package kanban

import scalafx.scene.paint.Color
import kanban.Main.{activeBoard, activeColumn, columnEditActive, kanbanApp, stage}
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.geometry.Insets
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.layout._
import scalafx.scene.control._


object ColumnDialog {

  val dialog = new Dialog[Column]() {
    initOwner(stage)
    title = "Kanban - new list"
    headerText = "Add new list"
  }

  val okButtonType = new ButtonType("OK", ButtonData.OKDone)
  dialog.dialogPane().buttonTypes = Seq(okButtonType, ButtonType.Cancel)

  val columnName = new TextField() {
    promptText = "List name"
    minWidth = 200
  }

  val columnColor = new ColorPicker(Color.Black) {
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
    dialog.title = "Kanban - new list"
    dialog.headerText = "Add new list"
    columnName.text = ""
    columnColor.value = Color.Black
  }

  def setColumnEdit(column: Column) = {
    reset()
    dialog.title = "Kanban - list edit"
    dialog.headerText = "Edit list"
    columnName.text = column.getName
    columnColor.value = column.getColor
  }

  dialog.resultConverter = dialogButton =>
    if (dialogButton == okButtonType) {
      if (columnEditActive) {
        activeColumn.editColumn(columnName.text(), columnColor.getValue)
        new Column(columnName.text(), columnColor.getValue)
      } else {
        activeBoard.addColumn(columnName.text(), columnColor.getValue)
        new Column(columnName.text(), columnColor.getValue)
      }

    } else {
      null
    }
}

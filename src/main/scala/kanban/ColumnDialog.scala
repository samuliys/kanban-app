package kanban

import scalafx.scene.paint.Color
import kanban.Main.{activeBoard, activeColumn, columnEditActive, stage}
import scalafx.Includes._
import scalafx.application.Platform
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

  val errorLabel = new Label {
    textFill = Color.Red
  }

  def drawContents = new VBox(10) {
    minWidth = 500
    children += new HBox(10) {
      children += new Label("Name:")
      children += columnName
      children += errorLabel
    }
    children += new HBox(10) {
      children += new Label("Color:")
      children += columnColor
    }
  }

  val okButton = dialog.dialogPane().lookupButton(okButtonType)
  okButton.disable = true

  columnName.text.onChange { (_, _, newValue) =>

    if (activeBoard.getColumns.map(_.getName).contains(newValue)) {
      okButton.disable = true
      errorLabel.text = "List with that name already exists"
    } else if (newValue.trim().isEmpty) {
      okButton.disable = true
    } else {
      errorLabel.text = ""
      okButton.disable = false
    }


  }

  dialog.dialogPane().content = drawContents

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

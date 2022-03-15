package kanban

import scalafx.scene.paint.Color
import kanban.Main.stage
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.layout._
import scalafx.scene.control._


object ColumnDialog {

  def showDialog() = dialog.showAndWait()

  private var selectedBoard = new Board("")
  private var selectedColumn = new Column("", Color.Black)
  private var newColumn = false

  private val dialog = new Dialog[Column] {
    initOwner(stage)
    title = "Kanban - new list"
    headerText = "Add new list"
  }

  private val okButtonType = new ButtonType("OK", ButtonData.OKDone)
  dialog.dialogPane().buttonTypes = Seq(okButtonType, ButtonType.Cancel)

  private val columnName = new TextField() {
    promptText = "List name"
    minWidth = 200
  }

  private val columnColor = new ColorPicker(Color.Black) {
    promptText = "Color"
  }

  private val errorLabel = new Label {
    textFill = Color.Red
  }

  private def drawContents = new VBox(10) {
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

  private val okButton = dialog.dialogPane().lookupButton(okButtonType)

  columnName.text.onChange { (_, _, newValue) =>

    if (newValue == "") {
      errorLabel.text = "List name can't be empty"
      okButton.disable = true
    } else if (newColumn && selectedBoard.getColumnNames.contains(newValue)) {
      errorLabel.text = "List " + newValue + " already exists"
      okButton.disable = true
    } else if (!newColumn && selectedBoard.getColumnNames.filterNot(_ == selectedColumn.getName).contains(newValue)) {
      errorLabel.text = "List " + newValue + " already exists"
      okButton.disable = true
    } else if (newValue.length > 10) {
      errorLabel.text = "List name too long"
      okButton.disable = true
    } else {
      errorLabel.text = ""
      okButton.disable = false
    }
  }

  dialog.dialogPane().content = drawContents

  Platform.runLater(columnName.requestFocus())

  def reset(board: Board, column: Column, isNew: Boolean) = {
    selectedBoard = board
    selectedColumn = column
    newColumn = isNew

    errorLabel.text = ""
    if (isNew) {
      dialog.title = "Kanban - New List"
      dialog.headerText = "Add New List"
      columnName.text = ""
      columnColor.value = Color.Black
      okButton.disable = true
    } else {
      dialog.title = "Kanban - List Edit"
      dialog.headerText = "Edit List: " + column.getName
      columnName.text = column.getName
      columnColor.value = column.getColor
      okButton.disable = false
    }
  }

  dialog.resultConverter = dialogButton => {
    if (dialogButton == okButtonType) {
      if (newColumn) {
        selectedBoard.addColumn(columnName.text(), columnColor.getValue)
      } else {
        selectedColumn.editColumn(columnName.text(), columnColor.getValue)
      }
    }
    selectedColumn
  }
}

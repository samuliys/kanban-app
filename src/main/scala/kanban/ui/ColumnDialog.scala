package kanban.ui

import kanban._
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.scene.paint.Color

/** Dialog object for creating and editing columns. */
object ColumnDialog {

  /** Opens dialog window */
  def showDialog() = dialog.showAndWait()

  private val dialog = new Dialog[Column] { // Create main dialog
    initOwner(Main.getStage)
    title = "Kanban - New list"
    headerText = "Add New List"
  }

  // Variables used to store instances of classes
  private var selectedBoard = new Board
  private var selectedColumn = new Column
  private var newColumn = false

  private val okButtonType = new ButtonType("OK", ButtonData.OKDone)

  dialog.dialogPane().buttonTypes = Seq(okButtonType, ButtonType.Cancel) // add buttons to dialog

  private val columnName = new TextField { // text field for entering name
    promptText = "List name"
    minWidth = 200
  }

  private val columnColor = new ColorPicker(Color.Black) { // coloc picker for choosing border color
    promptText = "Color"
  }

  private val errorLabel = new Label { // label used for displaying error messages
    textFill = Color.Red
  }

  /** Forms VBox component used as the root of all dialog components
   *
   * @return VBox component with all dialog window components */
  private def drawContents: VBox = new VBox(10) {
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

  // Check Column name as the user types it
  columnName.text.onChange { (_, _, newValue) =>

    if (newValue == "") { // don't allow empty names
      errorLabel.text = "List name can't be empty"
      okButton.disable = true
    } else if (newColumn && selectedBoard.getColumnNames.contains(newValue)) { // column name must be unique
      errorLabel.text = "List '" + newValue + "' already exists"
      okButton.disable = true
    } else if (!newColumn && selectedBoard.getColumnNames.filterNot(_ == selectedColumn.getName).contains(newValue)) {
      // for old column, exclude current name so it can be kept unchanged
      errorLabel.text = "List '" + newValue + "' already exists"
      okButton.disable = true
    } else if (newValue.length > 15) { // don't allow too long column names
      errorLabel.text = "List name is too long"
      okButton.disable = true
    } else { // else no error message and everything is ok
      errorLabel.text = ""
      okButton.disable = false
    }
  }

  private val okButton = dialog.dialogPane().lookupButton(okButtonType)

  dialog.dialogPane().content = drawContents // set dialog content to view

  Platform.runLater(columnName.requestFocus()) // set focus on text field

  /** Resets the dialog in order to create a new column or edit an old one
   *
   * @param board  board the column belong to
   * @param column column to be created or edited
   * @param isNew  whether a new column will be made or old one edited */
  def reset(board: Board, column: Column, isNew: Boolean): Unit = {

    // Set given parameters to correct variables
    selectedBoard = board
    selectedColumn = column
    newColumn = isNew

    if (isNew) { // reset many aspects of dialog when creating new board
      dialog.title = "Kanban - New List"
      dialog.headerText = "Add New List"
      columnName.text = ""
      columnColor.value = Color.Black
      okButton.disable = true
    } else { // set dialog details with existing column info
      dialog.title = "Kanban - List Edit"
      dialog.headerText = "Edit List: " + column.getName
      columnName.text = column.getName
      columnColor.value = column.getColor
      okButton.disable = false
    }
    errorLabel.text = "" // no error message
  }

  dialog.resultConverter = dialogButton => { // handle column creation or edit when ok button is clicked
    if (dialogButton == okButtonType) {
      if (newColumn) { // create new
        selectedBoard.addColumn(columnName.text(), columnColor.getValue)
      } else { // edit old one
        selectedColumn.editColumn(columnName.text(), columnColor.getValue)
      }
    }
    selectedColumn
  }
}

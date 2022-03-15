package kanban

import kanban.Main.{drawAlert, stage}
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.scene.paint.Color


object BoardDialog {

  val dialog = new Dialog[Board] {
    initOwner(stage)
    title = "Kanban - Board"
    headerText = "Board"
  }

  var kanbanapp = new Kanban
  var selectedBoard = new Board("")
  var newBoard = false

  val okButtonType = new ButtonType("OK", ButtonData.OKDone)
  //val deleteButtonType = new ButtonType("Delete", ButtonData.Other)

  dialog.dialogPane().buttonTypes = Seq(okButtonType, ButtonType.Cancel)

  val promptLabel = new Label("Name: ")

  val boardName = new TextField() {
    promptText = "Board Name"
  }

  val errorLabel = new Label {
    textFill = Color.Red
  }
  val deleteBoardButton = new Button("Delete Board") {
    onAction = (event) => {
      val result = drawAlert("Delete", "Are you sure you want delete board?").showAndWait()
      result match {
        case Some(ButtonType.OK) => {
          kanbanapp.deleteBoard(selectedBoard)
          dialog.close()
        }
        case _ =>
      }
    }
  }

  val deletePane = new Pane {
    children = deleteBoardButton
  }

  boardName.text.onChange { (_, _, newValue) =>
    if (newValue == "") {
      errorLabel.text = "Board name can't be empty"
      okButton.disable = true
    } else if (newBoard && kanbanapp.getBoardNames.contains(newValue)) {
      errorLabel.text = "Board " + newValue + " already exists"
      okButton.disable = true
    } else if (!newBoard && kanbanapp.getBoardNames.filterNot(_ == selectedBoard.getName).contains(newValue)) {
      errorLabel.text = "Board " + newValue + " already exists"
      okButton.disable = true
    } else if (newValue.length > 10) {
      errorLabel.text = "Board name too long"
      okButton.disable = true
    } else {
      errorLabel.text = ""
      okButton.disable = false
    }
  }


  val drawContents = new VBox(10) {
    minWidth = 400
    children += new HBox(10) {
      children += promptLabel
      children += boardName
      children += errorLabel
    }
    children += deletePane
  }

  val okButton = dialog.dialogPane().lookupButton(okButtonType)

  dialog.dialogPane().content = drawContents

  Platform.runLater(boardName.requestFocus())

  def reset(kanban: Kanban, board: Board, isNew: Boolean) = {
    kanbanapp = kanban
    selectedBoard = board
    newBoard = isNew

    errorLabel.text = ""
    boardName.text = ""

    if (isNew) {
      dialog.title = "Kanban - New Board"
      dialog.headerText = "Create New Board"
      okButton.disable = true
      boardName.text = ""
      deletePane.children = new Pane
      errorLabel.text = ""

    } else {
      dialog.title = "Kanban - Board Edit"
      dialog.headerText = "Edit board: " + board.getName
      okButton.disable = false
      boardName.text = board.getName
      deletePane.children = deleteBoardButton
      errorLabel.text = ""

    }
  }

  dialog.resultConverter = dialogButton => {
    if (dialogButton == okButtonType) {
      if (newBoard) {
        kanbanapp.createBoard(boardName.text())
      } else {
        selectedBoard.rename(boardName.text())
      }
    }
    selectedBoard
  }
}

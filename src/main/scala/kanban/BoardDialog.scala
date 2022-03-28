package kanban

import kanban.Main.{drawAlert, stage}
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.scene.paint.Color
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter

import java.io.File


object BoardDialog {

  def showDialog() = dialog.showAndWait()

  private val dialog = new Dialog[Board] {
    initOwner(stage)
    title = "Kanban - Board"
    headerText = "Board"
  }

  private var kanbanapp = new Kanban
  private var selectedBoard = new Board("")
  private var newBoard = false

  private var selectedFile: Option[File] = None

  private val okButtonType = new ButtonType("OK", ButtonData.OKDone)

  dialog.dialogPane().buttonTypes = Seq(okButtonType, ButtonType.Cancel)

  private val promptLabel = new Label("Name: ")

  private val boardName = new TextField() {
    promptText = "Board Name"
  }

  private val errorLabel = new Label {
    textFill = Color.Red
  }

  private val bgLabel = new Label("Background")

  private val fileLabel = new Label("No Chosen Image")

  private val boardColor = new ColorPicker(Color.White) {
    promptText = "Color"
    minHeight = 25
  }

  private val chooseImageBtn = new Button("Choose Image") {
    onAction = (event) => {
      val fileChooser = new FileChooser {
        extensionFilters.add(new ExtensionFilter("Image Files (*.png, *.jpg)", "*.png"))
        extensionFilters.add(new ExtensionFilter("Image Files (*.png, *.jpg)", "*.jpg"))
      }
      val chooseFile = fileChooser.showOpenDialog(stage)

      if (chooseFile != null) {
        selectedFile = Some(chooseFile)
        fileLabel.text = "Chosen file: " + chooseFile.getName
      }
    }
  }

  private val radio1 = new RadioButton("Solid Color") {
    onAction = (event) => {
      boardColor.disable = false
      chooseImageBtn.disable = true

    }
  }

  private val radio2 = new RadioButton("Image") {
    onAction = (event) => {
      boardColor.disable = true
      chooseImageBtn.disable = false
    }
  }

  private val toggle = new ToggleGroup() {
    toggles = List(radio1, radio2)
  }

  private val deleteBoardButton = new Button("Delete Board") {
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

  private val deletePane = new Pane {
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


  private val drawContents = new VBox(10) {
    minWidth = 400
    children += new HBox(10) {
      children += promptLabel
      children += boardName
      children += errorLabel
    }
    children += new Separator
    children += new VBox(10) {
      children += bgLabel
      children += new HBox(5) {
        children += radio1
        children += boardColor
      }
      children += new HBox(5) {
        children += radio2
        children += chooseImageBtn
        children += fileLabel
      }
    }
    children += deletePane
  }

  private val okButton = dialog.dialogPane().lookupButton(okButtonType)

  dialog.dialogPane().content = drawContents

  Platform.runLater(boardName.requestFocus())

  def reset(kanban: Kanban, board: Board, isNew: Boolean) = {
    kanbanapp = kanban
    selectedBoard = board
    newBoard = isNew

    if (isNew) {
      dialog.title = "Kanban - New Board"
      dialog.headerText = "Create New Board"
      okButton.disable = true
      boardName.text = ""
      deletePane.children = new Pane
      radio1.selected = true
      boardColor.value = Color.White
      boardColor.disable = true

    } else {
      dialog.title = "Kanban - Board Edit"
      dialog.headerText = "Edit board: " + board.getName
      okButton.disable = false
      boardName.text = board.getName
      deletePane.children = deleteBoardButton
      boardColor.value = selectedBoard.getColor

      selectedBoard.getBgImage match {
        case Some(imgFile) => {
          radio2.selected = true
          boardColor.disable = true
          chooseImageBtn.disable = false
          fileLabel.text = imgFile.getName

        }
        case None => {
          radio1.selected = true
          boardColor.disable = false
          chooseImageBtn.disable = true
          fileLabel.text = ""
        }
      }

    }
    errorLabel.text = ""
  }

  dialog.resultConverter = dialogButton => {
    if (dialogButton == okButtonType) {
      if (newBoard) {
        kanbanapp.createBoard(boardName.text(), boardColor.value(), selectedFile)
      } else {
        selectedBoard.rename(boardName.text())
        if (radio1.selected()) {
          selectedBoard.setColor(boardColor.value())
          selectedBoard.setBgImage(None)
        } else {
          selectedBoard.setColor(boardColor.value())
          selectedBoard.setBgImage(selectedFile)
        }
      }
    }
    selectedBoard
  }
}

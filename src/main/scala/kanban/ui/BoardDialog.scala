package kanban.ui

import kanban._
import kanban.Main.drawAlert
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.scene.paint.Color
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter
import settings._

import java.io.File

/** Dialog object for creating and editing boards. */
object BoardDialog {

  /** Opens dialog window */
  def showDialog() = dialog.showAndWait()

  private val dialog = new Dialog[Board] { // Create main dialog
    initOwner(Main.getStage)
    title = "Kanban - Board"
    headerText = "Board"
  }

  // Variables used to store instances of classes
  private var kanbanapp = new Kanban
  private var selectedBoard = new Board
  private var newBoard = false // whether the user is crating a new board or editing old

  private var selectedFile: Option[File] = None

  private val okButtonType = new ButtonType("OK", ButtonData.OKDone)

  dialog.dialogPane().buttonTypes = Seq(okButtonType, ButtonType.Cancel) // add buttons to dialog

  private val promptLabel = new Label("Name: ")

  private val boardName = new TextField() { // text field for entering the board title
    promptText = "Board Name"
  }

  private val errorLabel = new Label {
    textFill = Color.Red // label used for displaying error messages
  }

  private val bgLabel = new Label("Background")

  private val fileLabel = new Label("No Chosen Image")

  private val boardColor = new ColorPicker(Color.White) { // color picker for choosing background color
    promptText = "Color"
    minHeight = 25
  }

  private val chooseImageBtn = new Button("Choose Image") { // button for choosing bg image
    minWidth = 125
    onAction = (event) => {
      val fileChooser = new FileChooser {
        extensionFilters.add(new ExtensionFilter("Image Files (*.png, *.jpg)", Seq("*.png", "*.jpg")))
      }
      val chooseFile = fileChooser.showOpenDialog(Main.getStage)

      if (chooseFile != null) { // make sure a file was actually selected
        selectedFile = Some(chooseFile)
        fileLabel.text = "Chosen file: " + chooseFile.getName
      }
    }
  }
  // two radio buttons for choosing between background color and image
  private val radio1 = new RadioButton("Solid Color") {
    minWidth = 90
    onAction = (event) => {
      boardColor.disable = false
      chooseImageBtn.disable = true
    }
  }

  private val radio2 = new RadioButton("Image") {
    minWidth = 90
    onAction = (event) => {
      boardColor.disable = true
      chooseImageBtn.disable = false
    }
  }

  private val toggle = new ToggleGroup { // create a toggle group for them so only 1 can be selected at a time
    toggles = List(radio1, radio2)
  }

  private val deleteBoardButton = new Button("Delete Board") {
    onAction = (event) => { // ask for confirmation
      val result = drawAlert("Delete", "Are you sure you want delete board?").showAndWait()
      result match {
        case Some(ButtonType.OK) => {
          kanbanapp.deleteBoard(selectedBoard)
          dialog.close() // close dialog after deletion
        }
        case _ =>
      }
    }
  }

  private val separator = new Separator // add separator as variable so it can be accessed later

  private val deletePane = new Pane {
    children += deleteBoardButton // pane for delete button so it can be hidden
  }

  private val drawContents = new VBox(10) {
    minWidth = 500
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
    children += separator
    children += deletePane
  }

  private val okButton = dialog.dialogPane().lookupButton(okButtonType)

  dialog.dialogPane().content = drawContents // set dialog content to view

  // Check board name as the user types it
  boardName.text.onChange { (_, _, newValue) =>
    if (newValue == "") { // don't allow empty names
      errorLabel.text = "Board name can't be empty"
      okButton.disable = true
    } else if (newBoard && kanbanapp.getBoardNames.contains(newValue)) { // board name must be unique
      errorLabel.text = "Board '" + newValue + "' already exists"
      okButton.disable = true
    } else if (!newBoard && kanbanapp.getBoardNames.filterNot(_ == selectedBoard.getName).contains(newValue)) {
      // for old boards, exclude current name so it can be kept unchanged
      errorLabel.text = "Board '" + newValue + "' already exists"
      okButton.disable = true
    } else if (newValue.length > 15) { // don't allow too long board names
      errorLabel.text = "Board name is too long"
      okButton.disable = true
    } else { // else no error message and all is ok
      errorLabel.text = ""
      okButton.disable = false
    }
  }

  Platform.runLater(boardName.requestFocus()) // set focus on text field

  /** Resets the dialog in order to create a new board or edit an old one
   *
   * @param kanban Kanban session the board belong to
   * @param board  board to be created or edited
   * @param isNew  whether a new board will be made or old one edited
   */
  def reset(kanban: Kanban, board: Board, isNew: Boolean): Unit = {
    // Set given parameters to correct variables
    kanbanapp = kanban
    selectedBoard = board
    newBoard = isNew

    if (isNew) { // reset many aspects of dialog when creating new board
      dialog.title = "Kanban - New Board"
      dialog.headerText = "Create New Board"
      okButton.disable = true
      boardName.text = ""
      separator.visible = false
      deletePane.children = new Pane // when crating new board hide delete button
      radio1.selected = true // color mode by defaul
      chooseImageBtn.disable = true
      boardColor.value = DefaultBoardColor // white by default
      boardColor.disable = false
      fileLabel.text = ""
      selectedFile = None
    } else { // set dialog details with board info
      dialog.title = "Kanban - Board Edit"
      dialog.headerText = "Edit board: " + board.getName
      okButton.disable = false
      boardName.text = board.getName
      separator.visible = true

      if (kanban.getBoards.size == 1) { // prevent board deletion if only 1 board
        deletePane.children = new Pane
      } else {
        deletePane.children = deleteBoardButton // else allow it
      }

      boardColor.value = selectedBoard.getColor

      selectedFile = board.getBgImage
      selectedBoard.getBgImage match {
        case Some(imgFile) => { // if file found, set img mode
          if (imgFile.canRead) { // make sure file can still be found
            radio2.selected = true
            boardColor.disable = true
            chooseImageBtn.disable = false
            fileLabel.text = imgFile.getName
          } else {
            radio1.selected = true
            boardColor.disable = false
            chooseImageBtn.disable = true
            fileLabel.text = ""
            selectedFile = None
          }

        }
        case None => { // if no img file, set color mode
          radio1.selected = true
          boardColor.disable = false
          chooseImageBtn.disable = true
          fileLabel.text = ""
        }
      }

    }
    errorLabel.text = "" // reset error text
  }

  dialog.resultConverter = dialogButton => { // handle board creation or edit when ok button is clicked
    if (dialogButton == okButtonType) {
      if (newBoard) { // create new board using user chosen values
        kanbanapp.createBoard(boardName.text(), boardColor.value(), selectedFile)
      } else {
        selectedBoard.setName(boardName.text()) // edit the name to a new one
        selectedBoard.setColor(boardColor.value()) // set new color
        if (radio2.selected()) { // if file mode selected
          selectedFile match {
            case Some(img) => if (img.canRead) { // make sure file can still be found
              selectedBoard.setBgImage(selectedFile) // if so set it as bg img
            } else {
              selectedBoard.setBgImage(None) // else none
            }
            case None =>
          }

        } else { // if color mode is selected, se None as bg img
          selectedBoard.setBgImage(None)
        }
      }
    }
    selectedBoard
  }
}

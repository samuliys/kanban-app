package kanban.ui

import kanban._
import scalafx.Includes._
import scalafx.geometry.Pos._
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.scene.paint.Color
import scalafx.scene.text._
import scalafx.stage.Stage


/** Stage object for startup window that is displayed when program is opened. */
object StartupWindow {

  // Create stage object
  private val startupWindow: Stage = new Stage {
    title = "Kanban App"
    width = 400
    height = 300
  }

  // Constants for the startup window
  private val buttonWidth = 130
  private val titleFont = Font.font("arial", 18)
  private val buttonFont = Font.font("arial", 14)

  private var kanban = new Kanban // create kanban variable that will be requested by Main
  // add basic elements in case user closes window without making any decisions
  kanban.createBoard("untitled", Color.White, None)
  kanban.getBoards.head.addColumn("List 1")

  private val filemanager = new FileHandler

  /** Creates a small filler pane used for padding
   *
   * @return pane used for button padding */
  private def pane = new Pane {
    minWidth = 20
  }

  private val root = new VBox(15) {
    alignment = Center
    children += new Label("Welcome to Kanban App") {
      font = titleFont
    }
    children += new Label("Choose whether to create a new project from scratch\nor open a session from file") {
      textAlignment = TextAlignment.Center
    }

    children += new HBox(5) {
      alignment = CenterLeft
      children += pane
      children += new Button("New Project") { // button used to create a new project
        font = buttonFont
        minWidth = buttonWidth
        onAction = (event) => {
          val newKanban = new Kanban
          BoardDialog.reset(newKanban, new Board, true) // promt user to create a new first board
          BoardDialog.showDialog()
          if (newKanban.getBoards.isEmpty) { // if no board was created, make default
            newKanban.createBoard("untitled", Color.White, None)
          }
          ColumnDialog.reset(newKanban.getBoards.head, new Column, true) // prompt user to create the first list
          ColumnDialog.showDialog()
          if (newKanban.getBoards.head.getColumns.isEmpty) { // if user chose not to create it, make default
            newKanban.getBoards.head.addColumn("List 1")
          }
          kanban = newKanban // set this as the kanban that will be requested by Main after window closes
          startupWindow.close()
        }
      }
    }

    children += new HBox(5) {
      alignment = CenterLeft
      children += pane
      children += new Button("Open from File") { // button used to open a project from file
        font = buttonFont
        minWidth = buttonWidth
        onAction = (event) => {
          val result = filemanager.load(new Kanban) // use FileHandler class to load a file
          if (result._2) { // if a kanban file was loaded succesfully, use match to get it
            result._1 match {
              case Some(kanbanapp) => {
                kanban = kanbanapp // set this as the kanban that will be requested by Main after window closes
                startupWindow.close()
              }
              case None =>
            }
          }
        }
      }
    }

    children += new HBox(5) {
      alignment = CenterLeft
      children += pane
      children += new Button("Exit") { // user can exit the app from the startup window
        font = buttonFont
        minWidth = buttonWidth
        onAction = (event) => sys.exit(0)
      }
    }
  }

  startupWindow.scene = new Scene(root)

  /** Shows startup window on screen */
  def showWindow() = startupWindow.showAndWait()

  /** Returns Kanban-object that will be displayed on screen after startup window cloes
   *
   * @return Kanban based on user choises */
  def getKanban = kanban
}
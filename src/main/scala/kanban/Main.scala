package kanban

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.geometry.Pos._
import scalafx.scene.Scene
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.scene.text._
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.paint.Color
import scala.collection.mutable.{Buffer, Map}


object Main extends JFXApp {
  stage = new JFXApp.PrimaryStage {
    title.value = "KanbanApp - SY"
    width = 1000
    height = 700
  }
  var cardEditActive = false
  var columnEditActive = false

  val panes = Map[Column, Buffer[String]]()

  val kanbanApp = new Kanban
  val fileManager = new FileHandler
  val noCard = new Card("", Color.Black)
  var activeCard = noCard
  var cardActiveStatus = true

  var activeBoard = kanbanApp.getBoards

  var cardMoveActive = false


  val fontChoice = Font.font("arial", 16)

  def drawCard(column: Column, card: Card): VBox = new VBox(4) {
    if (activeCard == card) {
      border = new Border(new BorderStroke(card.getColor, BorderStrokeStyle.Dashed, new CornerRadii(2), new BorderWidths(6)))
    } else {
      border = new Border(new BorderStroke(card.getColor, BorderStrokeStyle.Solid, new CornerRadii(2), new BorderWidths(6)))
    }

    minWidth = 250
    maxWidth = 250
    minHeight = 150
    alignment = Center
    children += new Label(card.getText) {
      wrapText = true
      textAlignment = TextAlignment.Center
      font = fontChoice
    }

    if (activeCard == card) {
      children += new HBox(4) {
        alignment = Center
        children += new Button("Edit") {
          onAction = (event) => {
            cardEditActive = true
            CardDialog.setCardEdit(card)
            val result = CardDialog.dialog.showAndWait()
            stage.scene = new Scene(root)
            cardEditActive = false
          }
        }

        children += new Button("Delete") {
          onAction = (event) => {
            val alert = new Alert(AlertType.Confirmation) {
              initOwner(stage)
              title = "Delete Card"
              contentText = "Are you sure you want to delete the card?"
            }

            val result = alert.showAndWait()
            result match {
              case Some(ButtonType.OK) => {
                column.deleteCard(card)
                stage.scene = new Scene(root)
              }
              case _ =>
            }
          }

        }
        children += new Button("Archive")
      }

    }
    onMouseClicked = (event) => {
      if (activeCard == card) {
        activeCard = noCard
        cardMoveActive = false
      } else {
        activeCard = card
        kanbanApp.setActiveColumn(column)
        cardMoveActive = true
      }

      stage.scene = new Scene(root)
    }
    onDragDetected = (event) => {
      println(event)
    }
  }

  def getPane(column: Column, minheight: Int) = {
    new Pane() {
      minHeight = minheight
      onMouseReleased = (event) => {
        println(panes)
        println(event.getPickResult.getIntersectedNode.toString)
        println(panes(column).indexOf("[SFX]" + event.getPickResult.getIntersectedNode.toString))
        val index = panes(column).indexOf("[SFX]" + event.getPickResult.getIntersectedNode.toString)
        if (cardMoveActive) {
          kanbanApp.getActiveColumn.deleteCard(activeCard)
          column.addCard(activeCard.getText, activeCard.getColor, index)
          stage.scene = new Scene(root)
          cardMoveActive = false
        }
      }
    }
  }

  def drawColumn(board: Board, column: Column): VBox = new VBox() {
    alignment = TopCenter
    minHeight = stage.height.value
    minWidth = 280
    border = new Border(new BorderStroke(column.getColor, BorderStrokeStyle.Solid, new CornerRadii(2), new BorderWidths(6)))
    children += new Label(column.getName) {
      minHeight = 40
      font = Font.font("arial", 20)
    }
    children += new HBox(10) {
      alignment = Center
      children += new Button("New Card") {
        font = fontChoice

        onAction = (event) => {
          kanbanApp.setActiveColumn(column)
          CardDialog.reset()
          val result = CardDialog.dialog.showAndWait()
          stage.scene = new Scene(root)
        }
      }
      children += new Button("Edit") {
        font = fontChoice

        onAction = (event) => {
          kanbanApp.setActiveColumn(column)
          columnEditActive = true
          ColumnDialog.setColumnEdit(column)
          val result = ColumnDialog.dialog.showAndWait()
          stage.scene = new Scene(root)
          columnEditActive = false
        }
      }
      children += new Button("Delete") {
        font = fontChoice
        onAction = (event) => {
          val alert = new Alert(AlertType.Confirmation) {
            initOwner(stage)
            title = "Delete Column"
            contentText = "Are you sure you want to delete the column?"
          }

          val result = alert.showAndWait()
          result match {
            case Some(ButtonType.OK) => {
              board.deleteColumn(column)
              stage.scene = new Scene(root)
            }
            case _ =>
          }
        }
      }
    }
    children += new Separator
    panes(column) = Buffer[String]()
    for (card <- column.getCards) {
      val pane = getPane(column, 20)
      panes(column) += pane.toString()
      children += pane
      children += drawCard(column, card)

    }
    val pane = getPane(column, 50)
    panes(column) += pane.toString()
    children += pane
  }

  val menubar = new MenuBar {
    menus += new Menu("File") {
      items += new MenuItem("Open")
      items += new MenuItem("Save")
      items += new SeparatorMenuItem
      items += new MenuItem("Exit") {
        onAction = (event) => sys.exit(0)
      }
    }
  }

  def root: VBox = new VBox(8) {

    children += menubar


    children += new HBox(14) {
      alignment = CenterLeft
      for (column <- kanbanApp.getBoards.getColumns) {
        children += drawColumn(activeBoard, column)
      }
      children += new Button("New Column") {
        font = fontChoice
        onAction = (event) => {
          ColumnDialog.reset()
          val result = ColumnDialog.dialog.showAndWait()
          stage.scene = new Scene(root)
        }
      }
    }
  }

  val scene = new Scene(root)
  stage.scene = scene

}
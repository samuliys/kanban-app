package kanban

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.geometry.Pos._
import scalafx.scene.Scene
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.scene.text._
import scalafx.scene.control.Alert.AlertType


object Main extends JFXApp {
  stage = new JFXApp.PrimaryStage {
    title.value = "KanbanApp - SY"
    width = 1000
    height = 700
  }
  var cardEditActive = false
  var columnEditActive = false

  val kanbanApp = new Kanban()
  val fileManager = new FileHandler

  var activeCard = kanbanApp.getBoards.getColumns.head.getCards.head
  var cardActiveStatus = true

  var activeBoard = kanbanApp.getBoards

  val fontChoice = Font.font("arial", 16)

  def drawCard(column: Column, card: Card): VBox = new VBox(4) {
    border = new Border(new BorderStroke(card.getColor, BorderStrokeStyle.Solid, new CornerRadii(2), new BorderWidths(6)))
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
      activeCard = card
      stage.scene = new Scene(root)
    }
  }

  def drawColumn(board: Board, column: Column): VBox = new VBox(8) {
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

    for (card <- column.getCards) {
      children += drawCard(column, card)
    }
  }

  def root: VBox = new VBox(8) {

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
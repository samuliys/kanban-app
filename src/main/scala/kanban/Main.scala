package kanban

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.geometry.Pos._
import scalafx.scene.Scene
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.scene.text._


object Main extends JFXApp {
  stage = new JFXApp.PrimaryStage {
    title.value = "KanbanApp"
    width = 600
    height = 450
  }
  var editActive = false

  val kanbanApp = new Kanban()
  val fileManager = new FileHandler

  var activeCard = kanbanApp.getBoards.getColumns.head.getCards.head
  var cardActiveStatus = true

  val fontChoice = Font.font("arial", 16)

  def drawCard(card: Card): VBox = new VBox(4) {
    border = new Border(new BorderStroke(card.getColor, BorderStrokeStyle.Solid, new CornerRadii(2), new BorderWidths(6)))
    minWidth = 250
    maxWidth = 250
    alignment = Center
    children += new Label(card.getText) {
      wrapText = true
      textAlignment = TextAlignment.Center
      font = fontChoice
    }

    if (activeCard == card) {
      children += new Button("Edit card") {
        onAction = (event) => {
          editActive = true
          CardDialog.reset()
          CardDialog.setCardEdit(card)
          val result = CardDialog.dialog.showAndWait()
          stage.scene = new Scene(root)
          editActive = false
        }
      }

    }
    onMouseClicked = (event) => {
      activeCard = card
      stage.scene = new Scene(root)
    }
  }

  def drawColumn(column: Column): VBox = new VBox(8) {
    alignment = Center
    children += new Button("Add new card") {
      font = fontChoice

      onAction = (event) => {
        kanbanApp.setActiveColumn(column)
        CardDialog.reset()
        val result = CardDialog.dialog.showAndWait()
        stage.scene = new Scene(root)
      }
    }

    for (card <- column.getCards) {
      children += drawCard(card)
    }
  }

  def root: VBox = new VBox(8) {

    children += new HBox(14) {
      for (column <- kanbanApp.getBoards.getColumns) {
        children += drawColumn(column)
      }
    }
  }

  val scene = new Scene(root)
  stage.scene = scene

}
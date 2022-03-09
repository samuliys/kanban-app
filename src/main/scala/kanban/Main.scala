package kanban

import javafx.scene.paint.Color
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.geometry.Pos.Center
import scalafx.scene.Scene
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.scene.text.{Font, TextAlignment}

import scala.collection.mutable.Buffer


object Main extends JFXApp {
  stage = new JFXApp.PrimaryStage {
    title.value = "KanbanApp"
    width = 600
    height = 450
  }
  var number = 3

  val kanbanApp = new Kanban()
  val fileManager = new FileHandler

  var activeCard = kanbanApp.getBoards.getColumns.head.getCards.head
  var cardActiveStatus = true

  def drawCard(card: Card): VBox = new VBox(4) {
    border = new Border(new BorderStroke(Color.SKYBLUE, BorderStrokeStyle.Solid, new CornerRadii(2), new BorderWidths(6)))
    maxWidth = 250
    alignment = Center
    children += new Label(card.getText) {
      wrapText = true
      textAlignment = TextAlignment.Center
      font = Font.font("arial", 16)
    }

    if (activeCard == card) {
      children += new Label("This card is active")
    }
    onMouseClicked = (event) => {
      activeCard = card
      stage.scene = new Scene(root)
    }
  }

  def root: VBox = new VBox(8) {

    for (card <- kanbanApp.getBoards.getColumns.head.getCards) {
      children += drawCard(card)
    }
  }

  val scene = new Scene(root)
  stage.scene = scene
}
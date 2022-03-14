package kanban

import kanban.Main.stage
import scalafx.Includes._
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.scene.paint.Color
import scalafx.scene.text.TextAlignment
import scala.collection.mutable.Buffer


object FromArchiveDialog {

  def getDialog = dialog

  private val dialog = new Dialog[Card] {
    initOwner(stage)
    title = "Kanban - Return From Archive"
    headerText = "Return Card to List"
  }
  private var noCard = new Card("", Color.Black, Buffer[String](), None)
  private var selectedCard = noCard
  private var archive = new Column("", Color.Black)
  private var selectedBoard = new Board("")
  private var selectedColumn = new Column("", Color.Black)

  private val okButtonType = new ButtonType("Return Card", ButtonData.OKDone)
  dialog.dialogPane().buttonTypes = Seq(okButtonType, ButtonType.Cancel)

  private def drawCard(card: Card): VBox = {
    new VBox(5) {
      if (selectedCard == card) {
        border = new Border(new BorderStroke(card.getColor, BorderStrokeStyle.Dotted, new CornerRadii(2), new BorderWidths(6)))
      } else {
        border = new Border(new BorderStroke(card.getColor, BorderStrokeStyle.Solid, new CornerRadii(2), new BorderWidths(6)))
      }

      children += new Label(card.getText) {
        wrapText = true
        textAlignment = TextAlignment.Center
      }
      card.getDeadline match {
        case Some(deadline) => {
          children += new Label(deadline.getString)
        }
        case None =>
      }
      onMouseClicked = (event) => {
        if (selectedCard == card) {
          selectedCard = noCard
        } else {
          selectedCard = card
        }
        update()
      }
    }
  }

  private def archiveCards: VBox = {
    new VBox(5) {
      for (card <- archive.getCards) {
        children += drawCard(card)
      }
    }
  }

  private val scroll = new ScrollPane {
    content = archiveCards
  }

  private def drawContents = new VBox(10) {
    minWidth = 300
    children += new Button("Manage Archive")
    children += scroll
  }

  private val okButton = dialog.dialogPane().lookupButton(okButtonType)

  dialog.dialogPane().content = drawContents

  def reset(board: Board, column: Column) = {
    selectedBoard = board
    selectedColumn = column
    selectedCard = noCard
    dialog.title = "Kanban - Archive"
    dialog.headerText = "Return Card to List: " + column.getName
    archive = board.getArchive
    okButton.disable = true

    if (archive.getCards.isEmpty) {
      scroll.content = new Label("Archive is empty")

    } else {
      scroll.content = archiveCards
    }

  }

  private def update(): Unit = {
    if (selectedCard == noCard) {
      okButton.disable = true
    } else {
      okButton.disable = false
    }
    if (archive.getCards.isEmpty) {
      scroll.content = new Label("Archive is empty")
      okButton.disable = true
    } else {
      scroll.content = archiveCards
      okButton.disable = false
    }
  }

  dialog.resultConverter = dialogButton => {
    if (dialogButton == okButtonType) {
      archive.deleteCard(selectedCard)
      selectedColumn.addCard(selectedCard)
    }
    selectedCard
  }
}

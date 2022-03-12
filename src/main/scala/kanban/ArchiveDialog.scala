package kanban

import kanban.Main.stage
import scalafx.Includes._
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.scene.paint.Color
import scalafx.scene.text.TextAlignment


object ArchiveDialog {

  val dialog = new Dialog {
    initOwner(stage)
    title = "Kanban - Archive"
    headerText = "Archive"
  }
  var archive = new Column("", Color.Black)

  val okButtonType = new ButtonType("OK", ButtonData.OKDone)
  dialog.dialogPane().buttonTypes = Seq(ButtonType.OK)

  val selectAllButton = new Button("Select All")

  val unSelectButton = new Button("Remove Selection")

  val deleteSelectedButton = new Button("Delete Selected")

  def drawCard(card: Card): VBox = {
    new VBox() {
      border = new Border(new BorderStroke(card.getColor, BorderStrokeStyle.Solid, new CornerRadii(2), new BorderWidths(6)))
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
    }
  }

  def archiveCards: VBox = {
    new VBox(5) {
      for (card <- archive.getCards) {
        children += drawCard(card)
      }
    }
  }

  val scroll = new ScrollPane {
    content = archiveCards
  }

  val listSelection = new ComboBox()

  val returnCardButton = new Button("Return Card") {

  }

  def drawContents = new VBox(10) {
    minWidth = 300
    children += new HBox(10) {
      children += selectAllButton
      children += unSelectButton
      children += deleteSelectedButton
    }
    children += scroll
    children += new HBox(5) {
      children += new Label("Select Target List:")
      children += listSelection
      children += returnCardButton
    }
  }

  val okButton = dialog.dialogPane().lookupButton(okButtonType)

  dialog.dialogPane().content = drawContents

  def reset(board: Board) = {
    dialog.title = "Kanban - Archive"
    dialog.headerText = "Archive: " + board.getName
    archive = board.getArchive
    scroll.content = archiveCards
  }
}

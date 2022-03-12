package kanban

import kanban.Main.stage
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.scene.paint.Color
import scalafx.scene.text.TextAlignment
import scala.collection.mutable.Buffer


object ArchiveDialog {

  val dialog = new Dialog {
    initOwner(stage)
    title = "Kanban - Archive"
    headerText = "Archive"
  }

  val selectedCards = Buffer[Card]()
  var archive = new Column("", Color.Black)
  var selectedBoard = new Board("")

  val okButtonType = new ButtonType("OK", ButtonData.OKDone)
  dialog.dialogPane().buttonTypes = Seq(ButtonType.OK)

  val selectAllButton = new Button("Select All") {
    onAction = (event) => {
      selectedCards.clear()
      archive.getCards.foreach(selectedCards.append(_))
      update()

    }
  }

  val unSelectButton = new Button("Remove Selection") {
    onAction = (event) => {
      selectedCards.clear()
      update()
    }
  }

  val deleteSelectedButton = new Button("Delete Selected") {
    onAction = (event) => {
      selectedCards.foreach(archive.deleteCard(_))
      selectedCards.clear()
      update()
    }
  }

  def drawCard(card: Card): VBox = {
    new VBox(5) {
      if (selectedCards.contains(card)) {
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
        if (selectedCards.contains(card)) {
          selectedCards.remove(selectedCards.indexOf(card))
        } else {
          selectedCards += card
        }
        update()
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

  val listSelection = new ComboBox(List(""))

  val returnCardButton = new Button("Return Selected Cards") {
    onAction = (event) => {
      val targetColumn = selectedBoard.getColumn(listSelection.value())
      selectedCards.foreach(targetColumn.addCard(_))
      selectedCards.foreach(archive.deleteCard(_))
      selectedCards.clear()
      update()
    }
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
      children += new Label("Target List:")
      children += listSelection
      children += returnCardButton
    }
  }

  val okButton = dialog.dialogPane().lookupButton(okButtonType)

  dialog.dialogPane().content = drawContents

  def reset(board: Board) = {
    selectedBoard = board
    dialog.title = "Kanban - Archive"
    dialog.headerText = "Archive: " + board.getName
    archive = board.getArchive
    selectedCards.clear()
    if (archive.getCards.isEmpty) {
      scroll.content = new Label("Archive is empty")
      selectAllButton.disable = true
      listSelection.disable = true
    } else {
      scroll.content = archiveCards
      selectAllButton.disable = false
      listSelection.disable = false
    }
    unSelectButton.disable = true
    deleteSelectedButton.disable = true
    returnCardButton.disable = true

    listSelection.items = ObservableBuffer(board.getColumns.map(_.getName).toList)
    listSelection.getSelectionModel.selectFirst()


  }

  def update(): Unit = {
    if (selectedCards.isEmpty) {
      unSelectButton.disable = true
      selectAllButton.disable = false
      deleteSelectedButton.disable = true
      returnCardButton.disable = true
    } else if (selectedCards.size == archive.getCards.size) {
      selectAllButton.disable = true
      unSelectButton.disable = false
      deleteSelectedButton.disable = false
      returnCardButton.disable = false
    } else {
      deleteSelectedButton.disable = false
      unSelectButton.disable = false
      returnCardButton.disable = false
    }
    if (archive.getCards.isEmpty) {
      selectAllButton.disable = true
      listSelection.disable = true
    }
    scroll.content = archiveCards
    println(listSelection.value())
  }
}

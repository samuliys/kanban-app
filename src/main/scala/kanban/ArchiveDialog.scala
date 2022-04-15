package kanban

import kanban.Main.{drawAlert, stage}
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.geometry.Pos._
import scalafx.scene.paint.Color
import scalafx.scene.text.TextAlignment

import scala.collection.mutable.Buffer


object ArchiveDialog {

  def showDialog() = dialog.showAndWait()

  private val dialog = new Dialog {
    initOwner(stage)
    title = "Kanban - Archive"
    headerText = "Archive"
  }
  private var kanbanapp = new Kanban
  private val selectedCards = Buffer[Card]()
  private var archive = new Column
  private var selectedBoard = new Board
  private var archiveMode = true

  private val okButtonType = new ButtonType("OK", ButtonData.OKDone)
  dialog.dialogPane().buttonTypes = Seq(ButtonType.OK)

  private val selectAllButton = new Button("Select All") {
    onAction = (event) => {
      selectedCards.clear()
      if (archiveMode) {
        archive.getCards.foreach(selectedCards.append(_))
      } else {
        kanbanapp.getTemplates.foreach(selectedCards.append(_))
      }
      update()

    }
  }

  private val unSelectButton = new Button("Remove Selection") {
    onAction = (event) => {
      selectedCards.clear()
      update()
    }
  }

  private val deleteSelectedButton = new Button("Delete Selected") {
    onAction = (event) => {
      if (archiveMode) {
        selectedCards.foreach(archive.deleteCard(_))
      } else {
        selectedCards.foreach(kanbanapp.removeTemplate(_))
      }

      selectedCards.clear()
      update()
    }
  }

  private def drawCard(card: Card): VBox = {
    new VBox(4) {
      if (selectedCards.contains(card)) {
        border = new Border(new BorderStroke(card.getBorderColor, BorderStrokeStyle.Dotted, new CornerRadii(2), new BorderWidths(6)))
      } else {
        border = new Border(new BorderStroke(card.getBorderColor, BorderStrokeStyle.Solid, new CornerRadii(2), new BorderWidths(6)))
      }
      minHeight = 120
      minWidth = 300

      alignment = Center
      children += new Label(card.getText) {
        wrapText = true
        textAlignment = TextAlignment.Center
        textFill = card.getTextColor
      }
      card.getDeadline match {
        case Some(deadline) => {
          children += new Label(deadline.getString)
        }
        case None =>
      }

      if (card.getChecklist.hasTasks) {
        children += new HBox(10) {
          alignment = Center
          children += new Label(card.getChecklist.toString)
          children += new ProgressBar {
            progress = card.getChecklist.getProgress
            minHeight = 20
            minWidth = 120
          }
        }
        if (selectedCards.contains(card)) {
          children += new HBox {
            children += new Pane {
              minWidth = 20
            }
            children += new VBox(2) {
              for (task <- card.getChecklist.getTasks) {
                children += new CheckBox {
                  text = task._2
                  selected = task._1
                  disable = true
                }
              }
            }
          }
        }
      }

      card.getFile match {
        case Some(file) => {
          children += new Label("File Attachment: " + file.getName)
        }
        case None =>
      }

      if (selectedCards.contains(card)) {
        children += new HBox(4) {
          alignment = Center
          children += new Button("Edit") {
            onAction = (event) => {
              CardDialog.reset(kanbanapp, archive, card, false)
              CardDialog.showDialog()
              update()
            }
          }
          children += new Button("Delete") {
            onAction = (event) => {
              val result = drawAlert("Delete Card", "Are you sure you want to delete the card?").showAndWait()
              result match {
                case Some(ButtonType.OK) => {
                  archive.deleteCard(card)
                  update()
                }
                case _ =>
              }
            }
          }
        }
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

  private def getListCards(cards: Buffer[Card]): VBox = {
    new VBox(5) {
      for (card <- cards) {
        children += drawCard(card)
      }
    }
  }

  private val scroll = new ScrollPane {

    minHeight = 400
    minWidth = 320
    content = getListCards(archive.getCards)
  }

  private val listSelection = new ComboBox(List(""))

  private val returnCardButton = new Button("Return Selected Cards") {
    onAction = (event) => {
      val targetColumn = selectedBoard.getColumn(listSelection.value())
      selectedCards.foreach(targetColumn.addCard(_))
      selectedCards.foreach(archive.deleteCard(_))
      selectedCards.clear()
      update()
    }
  }

  private val archiveControls = new HBox(5) {
    children += new Label("Target List:")
    children += listSelection
    children += returnCardButton
  }

  private def drawContents = new VBox(10) {
    alignment = Center
    minWidth = 320
    children += new HBox(10) {
      children += selectAllButton
      children += unSelectButton
      children += deleteSelectedButton
    }
    children += scroll
    children += archiveControls
  }

  private val okButton = dialog.dialogPane().lookupButton(okButtonType)

  dialog.dialogPane().content = drawContents

  def reset(kanban: Kanban, board: Board, isArchive: Boolean = true) = {
    kanbanapp = kanban
    selectedBoard = board
    archiveMode = isArchive

    if (archiveMode) {
      dialog.title = "Kanban - Archive"
      dialog.headerText = "Archive: " + board.getName

      archiveControls.visible = true

      archive = board.getArchive
      selectedCards.clear()

      if (archive.getCards.isEmpty) {
        scroll.content = new Label("Archive is empty")
        selectAllButton.disable = true
        listSelection.disable = true
      } else {
        scroll.content = getListCards(archive.getCards)
        selectAllButton.disable = false
        listSelection.disable = false
      }

      listSelection.items = ObservableBuffer(board.getColumns.map(_.getName).toList)
      listSelection.getSelectionModel.selectFirst()

    } else {
      dialog.title = "Kanban - Templates"
      dialog.headerText = "Manage Templates"

      archiveControls.visible = false

      if (kanbanapp.getTemplates.isEmpty) {
        scroll.content = new Label("No Templates")
        selectAllButton.disable = true
        listSelection.disable = true
      } else {
        scroll.content = getListCards(kanbanapp.getTemplates)
        selectAllButton.disable = false
        listSelection.disable = false
      }
    }
    unSelectButton.disable = true
    deleteSelectedButton.disable = true
    returnCardButton.disable = true


  }

  private def update(): Unit = {
    if (archiveMode) {
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
      scroll.content = getListCards(archive.getCards)
    } else {
      if (selectedCards.isEmpty) {
        unSelectButton.disable = true
        selectAllButton.disable = false
        deleteSelectedButton.disable = true
        returnCardButton.disable = true
      } else if (selectedCards.size == kanbanapp.getTemplates.size) {
        selectAllButton.disable = true
        unSelectButton.disable = false
        deleteSelectedButton.disable = false
        returnCardButton.disable = false
      } else {
        deleteSelectedButton.disable = false
        unSelectButton.disable = false
        returnCardButton.disable = false
      }
      if (kanbanapp.getTemplates.isEmpty) {
        selectAllButton.disable = true
        listSelection.disable = true
      }
      scroll.content = getListCards(kanbanapp.getTemplates)
    }

  }
}

package kanban

import kanban.Main.drawAlert
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.geometry.Pos._
import scalafx.scene.text.{Font, TextAlignment}
import scala.collection.mutable.Buffer

/** Dialog object for managing archive and templates. */
object ArchiveDialog {

  private val dialog = new Dialog { // Create main dialog
    initOwner(Main.getStage)
  }

  /** Opens dialog window */
  def showDialog() = dialog.showAndWait()

  // Variables used to store instances of classes
  private var kanbanapp = new Kanban
  private val selectedCards = Buffer[Card]() // keeps track of user selected cards
  private var archive = new Column
  private var selectedBoard = new Board
  private var archiveMode = true // same dialog is used for both archive and template management

  private val okButtonType = new ButtonType("OK", ButtonData.OKDone)
  private val okButton = dialog.dialogPane().lookupButton(okButtonType)

  dialog.dialogPane().buttonTypes = Seq(ButtonType.OK) // add button to dialog

  private val selectAllButton = new Button("Select All") {
    onAction = (event) => { // sets all cards as selcted
      selectedCards.clear()
      if (archiveMode) { // depending on current mode dialog deals with either archive or templates
        archive.getCards.foreach(selectedCards.append(_))
      } else {
        kanbanapp.getTemplates.foreach(selectedCards.append(_))
      }
      update()
    }
  }

  private val unSelectButton = new Button("Remove Selection") {
    onAction = (event) => { // unselecs all possibly selected cards
      selectedCards.clear()
      update()
    }
  }

  private val deleteSelectedButton = new Button("Delete Selected") {
    onAction = (event) => { // removes all selected cads
      if (archiveMode) { // depends on currently active mode
        selectedCards.foreach(archive.deleteCard(_))
      } else {
        selectedCards.foreach(kanbanapp.removeTemplate(_))
      }
      selectedCards.clear() // those cards will also have to unselected
      update()
    }
  }

  /** Forms VBox component used to display a card in archive/template management setting
   *
   * @param card card to be displayed
   * @return VBox component with all components needed to display a card */
  private def drawCard(card: Card): VBox = {
    new VBox(4) {
      if (selectedCards.contains(card)) { // if card is selcted special border
        border = new Border(new BorderStroke(card.getBorderColor, BorderStrokeStyle.Dotted, new CornerRadii(2), new BorderWidths(6)))
      } else {
        border = new Border(new BorderStroke(card.getBorderColor, BorderStrokeStyle.Solid, new CornerRadii(2), new BorderWidths(6)))
      }
      minHeight = 120
      minWidth = 300
      alignment = Center

      children += new Label(card.getText) { // display card text
        wrapText = true
        textAlignment = TextAlignment.Center
        textFill = card.getTextColor
      }
      card.getDeadline match { // display card deadline (if has one)
        case Some(deadline) => {
          children += new Label(deadline.toString)
        }
        case None =>
      }

      if (card.getChecklist.hasTasks) { // display tasks (if has them)
        children += new HBox(10) {
          alignment = Center
          children += new Label(card.getChecklist.toString)
          children += new ProgressBar {
            progress = card.getChecklist.getProgress
            minHeight = 20
            minWidth = 120
          }
        }
        if (selectedCards.contains(card)) { // if card is selected, show more information about tasks
          children += new HBox {
            children += new Pane {
              minWidth = 20
            }
            children += new VBox(2) {
              for (task <- card.getChecklist.getTasks) {
                children += new CheckBox {
                  text = task._2
                  selected = task._1
                  disable = true // in archive/templates, card state cannot be altered
                }
              }
            }
          }
        }
      }

      card.getFile match { // display possible file attachment
        case Some(file) => {
          children += new Label("File: " + file.getName)
        }
        case None =>
      }

      card.getUrl match { // display possible url attachment
        case Some(url) => {
          children += new Label("URL: " + url)
        }
        case None =>
      }

      if (selectedCards.contains(card)) { // display edit and delele buttons for selected cards
        children += new HBox(4) {
          alignment = Center
          children += new Button("Edit") {
            onAction = (event) => { // start edit process
              CardDialog.reset(kanbanapp, archive, card, false)
              CardDialog.showDialog()
              update()
            }
          }
          children += new Button("Delete") {
            onAction = (event) => { // ask for delete confirmation
              val result = drawAlert("Delete Card", "Are you sure you want to delete the card?").showAndWait()
              result match {
                case Some(ButtonType.OK) => { // if ok, delete card
                  archive.deleteCard(card)
                  update()
                }
                case _ =>
              }
            }
          }
        }
      }

      onMouseClicked = (event) => { // when cliked, toggle selection
        if (selectedCards.contains(card)) {
          selectedCards -= card
        } else {
          selectedCards += card
        }
        update()
      }
    }
  }

  /** Forms VBox component used to display a card
   *
   * @param cards cards to be shown
   * @return VBox component with components needed to show cards */
  private def getListCards(cards: Buffer[Card]): VBox = {
    new VBox(5) {
      for (card <- cards) {
        children += drawCard(card) // use method to form one card components
      }
    }
  }

  private val scroll = new ScrollPane { // main scroll component for displaying all cards
    minHeight = 400
    minWidth = 320
    content = getListCards(archive.getCards)
  }

  private val listSelection = new ComboBox(List("")) // create basic combo box, content will be added by reset method

  private val returnCardButton = new Button("Return Selected Cards") {
    onAction = (event) => { // selected cards will be deleted from archive and added to the user chosen column
      val targetColumn = selectedBoard.getColumn(listSelection.value())
      selectedCards.foreach(targetColumn.addCard(_))
      selectedCards.foreach(archive.deleteCard(_))
      selectedCards.clear()
      update()
    }
  }

  private val archiveControls = new HBox(5) { // buttons for returning cards
    children += new Label("Target List:")
    children += listSelection
    children += returnCardButton
  }

  /** Forms VBox component used as the root of all dialog components
   *
   * @return VBox component with all dialog window components */
  private def drawContents: VBox = new VBox(10) {
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

  dialog.dialogPane().content = drawContents // set dialog pane content

  /** Resets the dialog in order to view correct archive or templates
   *
   * @param kanban    current Kanban session
   * @param board     board the archive belongs to
   * @param isArchive whether the dialog will be used for archive or template management
   */
  def reset(kanban: Kanban, board: Board, isArchive: Boolean = true): Unit = {
    // Set given parameters to correct variables
    kanbanapp = kanban
    selectedBoard = board
    archiveMode = isArchive

    if (archiveMode) { // prepare dialog for archive management
      dialog.title = "Kanban - Archive"
      dialog.headerText = "Archive: " + board.getName

      archiveControls.visible = true // only visible in archive mode

      archive = board.getArchive // get archive from give board
      selectedCards.clear() // no cards are selected at the beginning

      if (archive.getCards.isEmpty) { // in case no cards are in the archive
        scroll.content = new Label("              Archive is empty") {
          font = Font.font("arial", 20)
        }
        selectAllButton.disable = true // disable buttons related to
        listSelection.disable = true
      } else {
        scroll.content = getListCards(archive.getCards) // set correct cards to be shown
        selectAllButton.disable = false
        listSelection.disable = false
      }

      listSelection.items = ObservableBuffer(board.getColumns.map(_.getName).toList)
      listSelection.getSelectionModel.selectFirst()

    } else { // prepare dialog for template management
      dialog.title = "Kanban - Templates"
      dialog.headerText = "Manage Templates"

      archiveControls.visible = false // only shown in archive mode

      if (kanbanapp.getTemplates.isEmpty) {
        scroll.content = new Label("             No Templates") {
          font = Font.font("arial", 20)
        }
        selectAllButton.disable = true
        listSelection.disable = true
      } else {
        scroll.content = getListCards(kanbanapp.getTemplates)
        selectAllButton.disable = false
        listSelection.disable = false
      }
    }
    // These buttons are disabled by default
    unSelectButton.disable = true
    deleteSelectedButton.disable = true
    returnCardButton.disable = true
  }
  /** Updates the view and state of dialog buttons after user action */
  private def update(): Unit = {
    if (archiveMode) { // handle both dialog modes separately
      if (selectedCards.isEmpty) { // disable most buttons if no cards are selected
        unSelectButton.disable = true
        selectAllButton.disable = false
        deleteSelectedButton.disable = true
        returnCardButton.disable = true
      } else if (selectedCards.size == archive.getCards.size) {
        selectAllButton.disable = true // if all cards are selected disable select all button
        unSelectButton.disable = false
        deleteSelectedButton.disable = false
        returnCardButton.disable = false
      } else {
        deleteSelectedButton.disable = false
        unSelectButton.disable = false
        returnCardButton.disable = false
      }
      if (archive.getCards.isEmpty) { // if no archive cards, disable buttons
        selectAllButton.disable = true
        listSelection.disable = true
      }
      scroll.content = getListCards(archive.getCards) // set updated cards to  view
    } else { // same thing with template mode
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

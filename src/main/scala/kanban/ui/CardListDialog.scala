package kanban.ui

import kanban._
import scalafx.Includes._
import scalafx.geometry.Pos._
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.scene.text.{Font, TextAlignment}
import scala.collection.mutable.Buffer


case class Result(card: Card) // case class for returning selected card

/** Dialog object for listing cards when adding a card from archive or template
 * or choosing a card attachment. */
object CardListDialog {

  /** Opens dialog window */
  def showDialog() = dialog.showAndWait()

  private val dialog = new Dialog[Result] {
    initOwner(Main.getStage)
    height = 500
  }

  // Variables used to store current instances of classes
  private var kanban = new Kanban
  private var noCard = new Card
  private var targetCard = new Card
  private var selectedCard = noCard
  private var archive = new Column
  private var cardList = Buffer[Card]()
  private var selectedBoard = new Board
  private var selectedColumn = new Column

  private var mode = 1 // keep track of mode, default 1

  private var okButtonType = new ButtonType("Return Card", ButtonData.OKDone)
  private var okButton = dialog.dialogPane().lookupButton(okButtonType)

  dialog.dialogPane().buttonTypes = Seq(okButtonType, ButtonType.Cancel) // add buttons to dialog

  /** Forms VBox component used to display a card
   *
   * @param card card to be displayed
   * @return VBox component with all components needed to display a card */
  private def drawCard(card: Card): VBox = {
    new VBox(4) {
      if (selectedCard == card) { // use special border if card is selected
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
      card.getDeadline match { // display card deadline
        case Some(deadline) => {
          children += new Label(deadline.toString) {
            textFill = deadline.getCorrectColor(card.getChecklist)
          }
        }
        case None =>
      }

      if (card.getChecklist.hasTasks) { // display card tasks
        children += new HBox(10) {
          alignment = Center
          children += new Label(card.getChecklist.toString)
          children += new ProgressBar {
            progress = card.getChecklist.getProgress
            minHeight = 20
            minWidth = 120
          }
        }
      }

      card.getFile match { // display card file attachment
        case Some(file) => {
          children += new Label("File: " + file.getName)
        }
        case None =>
      }

      card.getUrl match { // display card url attachment
        case Some(url) => {
          children += new Label("URL: " + url)
        }
        case None =>
      }

      onMouseClicked = (event) => { // toggle card selection state
        if (selectedCard == card) {
          selectedCard = noCard
        } else {
          selectedCard = card
        }
        update()
      }
    }
  }

  /** Forms VBox component containing all cards that will be shown
   *
   * @return VBox component with all cards */
  private def cardListCards: VBox = {
    new VBox(5) {
      alignment = Center
      if (cardList.nonEmpty) {
        for (card <- cardList) {
          children += drawCard(card) // create components for each card
        }
      } else { // if no cards, show text
        children += new Label("                       No Cards") {
          font = Font.font("arial", 20)
        }
      }
    }
  }

  private val scroll = new ScrollPane { // main scroll component for displaying all cards
    minHeight = 400
    maxHeight = 400
    minWidth = 350
    maxWidth = 350
    content = cardListCards
  }

  /** Forms VBox component used as the root of all dialog components
   *
   * @return VBox component with all dialog window components */
  private def drawContents(mode: Int = 1): VBox = new VBox(10) {
    if (mode == 1) { // depending on mode show archive or...
      children += new Button("Manage Archive") {
        onAction = (event) => {
          ArchiveDialog.reset(kanban, selectedBoard)
          ArchiveDialog.showDialog()
          scroll.content = cardListCards // update cards
        }
      }
    } else if (mode == 2) { // ...template management button
      children += new Button("Manage Templates") {
        onAction = (event) => {
          ArchiveDialog.reset(kanban, selectedBoard, false)
          ArchiveDialog.showDialog()
          scroll.content = cardListCards // update cards
        }
      }
    }

    children += scroll
  }

  dialog.dialogPane().content = drawContents() // set content to view

  /** Resets the dialog in order to display cards for selections
   *
   * @param kanbanapp current Kanban session
   * @param board     currently active board
   * @param column    currently active column
   * @param card      card the attachment will be to
   * @param listMode  dialog window mode: archive 1, template 2, attachment card 3
   */
  def reset(kanbanapp: Kanban, board: Board, column: Column, card: Card = new Card, listMode: Int = 1): Unit = {
    // Set variables based on parameters
    kanban = kanbanapp
    selectedBoard = board
    selectedColumn = column
    targetCard = card
    selectedCard = noCard
    mode = listMode
    archive = board.getArchive

    // for each mode, set the correct list of cards, window title and header text and ok button text
    if (mode == 1) { // archive mode

      cardList = board.getArchive.getCards
      dialog.title = "Kanban - Archive"
      dialog.headerText = "Return Card to List: " + column.getName
      okButtonType = new ButtonType("Return Card", ButtonData.OKDone)
      dialog.dialogPane().buttonTypes = Seq(okButtonType, ButtonType.Cancel)

    } else if (mode == 2) { // template mode

      cardList = kanbanapp.getTemplates
      dialog.title = "Kanban - Templates"
      dialog.headerText = "New Card From Template"
      okButtonType = new ButtonType("Choose Card", ButtonData.OKDone)
      dialog.dialogPane().buttonTypes = Seq(okButtonType, ButtonType.Cancel)

    } else { // card mode

      cardList = kanbanapp.getAllCards.filterNot(_ == targetCard).toBuffer
      dialog.title = "Kanban - Attachment Card"
      dialog.headerText = "Choose Card as Attachment"
      okButtonType = new ButtonType("Select Card", ButtonData.OKDone)
      dialog.dialogPane().buttonTypes = Seq(okButtonType, ButtonType.Cancel)

    }

    okButton = dialog.dialogPane().lookupButton(okButtonType) //
    okButton.disable = true // disable by default

    scroll.content = cardListCards // set current cards

    dialog.dialogPane().content = drawContents(listMode) // update view

  }

  /** Updates dialog view */
  private def update(): Unit = {
    // only allow ok button if a card has been selected
    okButton.disable = (selectedCard == noCard || cardList.isEmpty)
    scroll.content = cardListCards
  }

  dialog.resultConverter = dialogButton => { // handle card selection when user clicks ok
    if (dialogButton == okButtonType) {
      if (mode == 1) { // delete from archive and add to target column
        archive.deleteCard(selectedCard)
        selectedColumn.addCard(selectedCard)
      } else if (mode == 2) { // when selecting a template, prompt user to customize the template
        CardDialog.reset(kanban, selectedColumn, selectedCard, false, true)
        CardDialog.showDialog()
      }
      Result(selectedCard) // return selected card using Result case class when user selects card as attachment
    } else {
      null
    }

  }
}

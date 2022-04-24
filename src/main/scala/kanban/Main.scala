package kanban

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.scene.text._
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.input._
import scalafx.scene.paint.Color
import scalafx.scene.image.Image
import scalafx.scene.shape.StrokeType
import scalafx.geometry.Pos._
import scalafx.geometry.Insets
import scalafx.Includes._
import javafx.beans.value.ObservableValue
import java.awt.Desktop
import java.net.URI
import scala.collection.mutable.{Buffer, Map}
import scala.util.{Failure, Success, Try}
import settings._

import java.io.File

/** Main object handling the ScalaFX GUI. */
object Main extends JFXApp {
  stage = new PrimaryStage { // create stage with width and height from the settings file
    width = StartingWidth
    height = StartingHeight

    val icon = Try {
      new Image("icon.png")
    } // make sure the program doesn't crash
    icon match { // in case loading icon image fails
      case Failure(exception) =>
      case Success(image) => icons += image
    }
  }

  // Create instances of the main kanban app and file handler used to save and load files
  private var kanbanApp = new Kanban
  private val fileManager = new FileHandler

  // Create various buffers that keep track of the gaps between columns and between card
  private val panes = Map[Column, Buffer[String]]()
  private val columnPanes = Buffer[String]()

  private val currentFilter = Buffer[String]() // Buffer for tracking the tags that are currently used for filtering



  // Create first board and list on startup
  kanbanApp.createBoard("board1")
  kanbanApp.getBoards.head.addColumn("list 1", Color.Black)
  kanbanApp.getBoards.head.getColumns.head.addCard(new Card("card1", Color.LightBlue))

  // Options for keeping track of activve and/or selected card/column
  private var activeCard: Option[Card] = None
  private var activeColumn: Option[Column] = None
  private var columnMove: Option[Column] = None

  //private var activeBoard = kanbanApp.getBoards.head // not option as one will always have to be selected
  private var activeBoard = kanbanApp.getBoards.head // not option as one will always have to be selected

  private var boardBackground: Background = getBoardBackground(activeBoard) // store the current background so no need to load in every update

  /** Returns the stage used in the GUI
   *
   * @return currently active stage */
  def getStage: PrimaryStage = stage

  /** Creates a confirmation alert to ask for confirmation before doing a task
   *
   * @param alertTitle title of the alert window
   * @param content    text for the content part of the alert window
   * @return alert with chosen title and content asking for confirmation */
  def drawAlert(alertTitle: String, content: String): Alert = {
    new Alert(AlertType.Confirmation) {
      initOwner(stage)
      title = alertTitle
      contentText = content
    }
  }

  /** Creates a background with chosen color using the GUI's background classes
   *
   * @param color desired color for the background
   * @return background using color */
  private def getColorBg(color: Color): Background = new Background(Array(new BackgroundFill(color, CornerRadii.Empty, Insets.Empty)))

  /** Creates a border using card's color and active state
   *
   * @param card     card that the border is for
   * @param isActive whether the card is selected on the GUI
   * @return border based on card and its state */
  private def getCardBorder(card: Card, isActive: Boolean): Border = {
    if (isActive) {
      new Border(new BorderStroke(card.getBorderColor, BorderStrokeStyle.Dotted, new CornerRadii(2), new BorderWidths(6)))
    } else {
      new Border(new BorderStroke(card.getBorderColor, BorderStrokeStyle.Solid, new CornerRadii(2), new BorderWidths(6)))
    }
  }

  /** Forms Button component used to delete a card
   *
   * @param column column where the card is located
   * @param card   the card to be deleted
   * @return Button component with logic needed to delete a card */
  private def drawCardDelete(column: Column, card: Card): Button = {
    new Button("Delete") {
      onAction = (event) => { // ask for confirmation
        val result = drawAlert("Delete Card", "Are you sure you want to delete the card?").showAndWait()
        result match {
          case Some(ButtonType.OK) => {
            column.deleteCard(card)
            update()
          }
          case _ =>
        }
      }
    }
  }

  /** Forms Button component used to edit a card
   *
   * @param column column where the card is located
   * @param card   the card to be edited
   * @return Button component with logic needed to edit a card */
  private def drawCardEdit(column: Column, card: Card): Button = {
    new Button("Edit") {
      onAction = (event) => { // open dialog window used to edit cards
        CardDialog.reset(kanbanApp, column, card, false)
        CardDialog.showDialog()
        activeCard = None
        activeColumn = None
        update()
      }
    }
  }

  /** Forms Button component used to archive a card
   *
   * @param column column where the card is located
   * @param card   the card to be archive
   * @return Button component with logic needed to archive a card */
  private def drawCardArchive(board: Board, column: Column, card: Card): Button = {
    new Button("Archive") {
      onAction = (event) => { // save card to archive and delele it from board
        board.getArchive.addCard(card)
        column.deleteCard(card)
        activeCard = None
        activeColumn = None
        update()
      }
    }
  }

  /** Forms Label component used to display card text
   *
   * @param card the card whose deadline will be displayed
   * @return GUI component used to display text of the card */
  private def cardTextLabel(card: Card) = new Label(card.getText) {
    wrapText = true
    textAlignment = TextAlignment.Center
    font = CardTextFont
    textFill = card.getTextColor
  }

  /** Forms HBox component used to display card deadline on an active card
   *
   * @param card     the card whose deadline will be displayed
   * @param deadline the deadline object used for the information
   * @return HBox component used to display information about the deadline */
  private def drawActiveCardDeadline(card: Card, deadline: Deadline): HBox = new HBox(12) {
    alignment = Center
    children += new Label(deadline.toString) {
      textFill = deadline.getCorrectColor(card.getChecklist) // black is neutral, red if late, green if done
    }
    children += new CheckBox("Done") {
      selected = deadline.getStatus
      onAction = (event) => {
        deadline.toggleStatus()
        update()
      }
    }
  }

  /** Forms GUI components used to display card checklist
   *
   * @param card     the card whose checklist will be displayed
   * @param isActive whether the card is set to active
   * @return HBox component for basic checklist status and a possible second HBox component
   *         with more delaied view of the checklist task that is shown when card is active */
  private def drawCardChecklist(card: Card, isActive: Boolean): (HBox, Option[HBox]) = {
    val box1 = new HBox(10) { // first HBox used to display overall checklist progress status
      alignment = Center
      children += new Label(card.getChecklist.toString)
      children += new ProgressBar {
        progress = card.getChecklist.getProgress
        minHeight = 20
        minWidth = 120
      }
    }

    val box2: Option[HBox] = { // second HBox if card is active wrapped in an option
      if (isActive) {
        Some(new HBox {
          children += new Pane {
            minWidth = 20
          }
          children += new VBox(2) {
            for (task <- card.getChecklist.getTasks) { // display each task with a checkbox
              children += new CheckBox() {
                text = task._2
                selected = task._1
                onAction = (event) => { // clicking the checkbox toggles the status of the task
                  card.getChecklist.toggleStatus(task._2)
                  update()
                }
              }
            }
          }
        })
      } else None
    }
    (box1, box2)
  }

  /** Forms HBox component used to display file attachment on an active card
   *
   * @param file file attachment which info will be displayed
   * @return HBox component used to display information about the file */
  private def drawActiveCardFile(card: Card, file: File): HBox = new HBox(5) {
    alignment = Center
    children += new Label("File: " + file.getName)
    children += new Button("Open") {
      onAction = (event) => {
        // make sure file is available and computer supports Desktop to prevent exceptions
        if (file.canRead && Desktop.isDesktopSupported) {
          Desktop.getDesktop.open(file) // open file with file type default program
        } else {
          card.resetFile()
          new Alert(AlertType.Warning, "There was an error opening the file.").showAndWait()
        }
      }
    }
  }

  /** Forms HBox component used to display url attachment on an active card
   *
   * @param url url attachment which info will be displayed
   * @return HBox component used to display information about the url */
  private def drawActiveCardUrl(url: String): HBox = new HBox(5) {
    alignment = Center
    children += new Label("URL: " + url)
    children += new Button("Open") {
      onAction = (event) => {
        if (Desktop.isDesktopSupported) {
          Desktop.getDesktop.browse(new URI(url))
        }
      }
    }
  }

  /** Forms HBox component used to display card attachment on an active card
   *
   * @param sub card attachment which info will be displayed
   * @return HBox component used to display information about the subcard */
  private def drawActiveCardSub(sub: SubCard) = new HBox(5) {
    alignment = Center
    children += new Label("Card Attachment: ")
    children += new Button("View") {
      onAction = (event) => {
        CardViewDialog.reset(sub.getCard)
        CardViewDialog.showDialog()
      }
    }
  }

  /** Forms VBox component used to display a card
   *
   * @param board  active board that is displayed on the GUI
   * @param column column the card belongs to
   * @param card   card to be displayed
   * @return VBox component with all components needed to display a card */
  private def drawCard(board: Board, column: Column, card: Card): VBox = new VBox(4) {

    // each card has white background so it can be seen despite board background
    background = new Background(Array(new BackgroundFill(Color.White, new CornerRadii(6), Insets.Empty)))

    val isActive = activeCard.getOrElse(new Card) == card // check active state
    alignment = Center
    border = getCardBorder(card, isActive) // special border if active

    // Card dimensions based on settings
    minWidth = CardWidth
    maxWidth = CardWidth
    minHeight = CardHeight

    if (!isActive) maxHeight = CardHeight // max height not limited when card is active to show all details

    children += cardTextLabel(card) // main text component of the card

    card.getDeadline match { // if card has deadline, display it depending card active state
      case Some(deadline) => {
        if (!card.getChecklist.hasTasks && activeCard.getOrElse(new Card) == card) {
          children += drawActiveCardDeadline(card, deadline)
        } else {
          children += new Label(deadline.toString) {
            textFill = deadline.getCorrectColor(card.getChecklist) // black is neutral, green if complete, red if late
          }
        }
      }
      case None =>
    }

    if (card.getChecklist.hasTasks) {
      val result = drawCardChecklist(card, activeCard.getOrElse(new Card) == card)
      children += result._1
      result._2 match {
        case Some(box) => children += box
        case None =>
      }
    }

    card.getFile match { // display potential card file attachment information
      case Some(file) => {
        if (activeCard.getOrElse(new Card) == card) { // if active, add button to open file
          children += drawActiveCardFile(card, file)
        } else {
          children += new Label("File: " + file.getName)
        }
      }
      case None =>
    }

    card.getUrl match { // display potential card url attachment information
      case Some(url) => {
        if (activeCard.getOrElse(new Card) == card) { // if active add button to open url
          children += drawActiveCardUrl(url)
        } else {
          children += new Label("URL: " + url)
        }
      }
      case None =>
    }

    card.getSubcard match {
      case Some(sub) => {
        if (activeCard.getOrElse(new Card) == card) { // if active add button to view card attachment
          children += drawActiveCardSub(sub)
        }
      }
      case None =>
    }

    if (activeCard.getOrElse(new Card) == card) {
      if (card.getTags.nonEmpty) { // display tags on card
        children += new Label("Tags: " + card.getTags.mkString(", "))
      }
      children += new HBox(4) { // add buttons to card when active
        alignment = Center
        children += drawCardEdit(column, card)
        children += drawCardDelete(column, card)
        children += drawCardArchive(board, column, card)
      }
    }

    onMouseClicked = (event) => { // Flip the active state of the card when clicked
      if (activeCard.getOrElse(new Card) == card) {
        activeCard = None
        activeColumn = None
      } else {
        activeCard = Some(card)
        activeColumn = Some(column)
      }
      columnMove = None
      update() // update GUI
    }
  }

  /** Creates a pane used as gap between columns
   *
   * @param board    currently active board displayed on the GUI
   * @param minwidth wanted width for the gap between columns
   * @return pane acting as gap between columns that knows its location */
  private def getColumnPane(board: Board, minwidth: Int): Pane = {
    new Pane {
      minWidth = minwidth
      onMouseReleased = (event) => {
        // after moused is released on a pane, using the unique ID of the pane,
        // get its index (= location) using its place in the buffer
        var index = columnPanes.indexOf("[SFX]" + event.getPickResult.getIntersectedNode.toString)

        // offset the effect that removing and adding back has on the indexes by subtracting 1
        // if the column is located so that it will be moved to the left of it
        if (index != -1 && columnMove.isDefined) {
          if (board.getColumns.indexOf(columnMove.getOrElse(new Column)) < index) {
            index -= 1
          }
          board.moveColumn(columnMove.getOrElse(new Column), index) // move column to new location
          columnMove = None
          update() // update GUI
        }
      }
    }
  }

  /** Creates a pane used as gap between cards
   *
   * @param board     currently active board displayed on the GUI
   * @param column    card list in question
   * @param minheight wanted height for the gap between cards
   * @return pane acting as gap between cards that knows its location */
  private def getPane(board: Board, column: Column, minheight: Int): Pane = {
    new Pane {
      minHeight = minheight

      onMouseReleased = (event) => {
        // after moused is released on a pane, using column get a buffer with all panes in that column
        // using the unique ID of the pane, get its index (= location) using its place in the buffer
        var index = panes(column).indexOf("[SFX]" + event.getPickResult.getIntersectedNode.toString)
        if (index != -1 && activeCard.isDefined) { // make sure index was found (i.e. not 1) and that a card is active so that there is a card to be moved
          val cardMove = activeCard.getOrElse(new Card) // get the card that will be moved

          // if the card is moved within its own column and its location is so that after the card is removed from the buffer
          // the original index would then be incorrect
          // in this case, 1 will be subtracted from the index to offset the effect removing and adding has
          if (activeColumn.getOrElse(new Column) == column && column.getCards.indexOf(cardMove) < index) {
            index = (index - 1) max 0
          }
          board.moveCard(cardMove, activeColumn.getOrElse(new Column), column, index) // move the card to new location
          activeCard = None
          update()
        }
      }
    }
  }

  /** Forms Button component used to add new card to list
   *
   * @param column column displayed on the GUI
   * @return Button component with logic needed to add new card to column */
  private def drawColumnNewCard(board: Board, column: Column): SplitMenuButton = {
    new SplitMenuButton {
      text = "New Card"
      font = DefaultFont
      minWidth = 110

      items += new MenuItem("From Archive") { // option for bringing a card back from archive
        onAction = (event) => {
          CardListDialog.reset(kanbanApp, board, column)
          CardListDialog.showDialog()
          update()
        }
      }

      items += new MenuItem("From Template") { // or create card using template
        onAction = (event) => {
          CardListDialog.reset(kanbanApp, board, column, new Card, 2)
          CardListDialog.showDialog()
          update()
        }
      }

      onAction = (event) => { // main action of button is creating a new card
        activeColumn = Some(column)
        activeCard = None
        CardDialog.reset(kanbanApp, column, new Card, true)
        CardDialog.showDialog()
        activeColumn = None
        update()
      }
    }
  }

  /** Forms Button component used to edit the column
   *
   * @param column column displayed on the GUI
   * @return Button component with logic needed to edit the column */
  private def drawColumnEdit(board: Board, column: Column): Button = {
    new Button("Edit") {
      font = DefaultFont
      minWidth = 70

      onAction = (event) => {
        activeColumn = Some(column)
        activeCard = None
        ColumnDialog.reset(board, column, false) // reset dialog
        ColumnDialog.showDialog()
        update() // update after changes were made
      }
    }
  }

  /** Forms Button component used to delete the column
   *
   * @param column column displayed on the GUI
   * @return Button component with logic needed to delete the column */
  private def drawColumnDelete(board: Board, column: Column): Button = {
    new Button("Delete") {
      font = DefaultFont
      minWidth = 70
      disable = (activeBoard.getColumns.size == 1) // if only 1, remove ability to delete

      onAction = (event) => {
        val result = drawAlert("Delete List", "Are you sure you want to delete the list?").showAndWait() // ask for confirmation
        result match {
          case Some(ButtonType.OK) => { // if confirmed, delete list
            board.deleteColumn(column)
            update()
          }
          case _ =>
        }
      }
    }
  }

  /** Forms Button component used to move the column
   *
   * @param column column displayed on the GUI
   * @return Button component with logic needed to set the column active to move it */
  private def drawColumnMove(column: Column): Button = new Button("Move") {
    font = DefaultFont
    minWidth = 70

    disable = (activeBoard.getColumns.size == 1) // if only 1 list, disable moving it

    onAction = (event) => {
      if (columnMove.getOrElse(new Column) == column) { // if already active, remove active status
        columnMove = None
      } else { // if not active, set to active
        columnMove = Some(column)
        activeCard = None
      }
      update()
    }
  }

  /** Forms VBox component used to display a column
   *
   * @param board  active board that is displayed on the GUI
   * @param column column to be displayed on the GUI
   * @return VBox component with all components needed to display a column */
  private def drawColumn(board: Board, column: Column): VBox = new VBox {
    if (columnMove.getOrElse(new Column) == column) { // if active, special borders
      border = new Border(new BorderStroke(column.getColor, BorderStrokeStyle.Dotted, new CornerRadii(2), new BorderWidths(6)))
    } else {
      border = new Border(new BorderStroke(column.getColor, BorderStrokeStyle.Solid, new CornerRadii(2), new BorderWidths(6)))
    }
    alignment = TopCenter
    minHeight = stage.height.value - 95
    minWidth = ColumnWidth

    children += new HBox { // add buttons used to manage the column
      alignment = Center
      children += drawColumnNewCard(board, column)
      children += drawColumnEdit(board, column)
      children += drawColumnDelete(board, column)
      children += drawColumnMove(column)
    }
    children += new Pane {
      minHeight = 10
    }
    children += new Text {
      text = column.getName
      font = Font.font("arial", 26)
      fill = Color.Black
      stroke = Color.White
      strokeType = StrokeType.Outside
      strokeWidth = 1
    }

    panes(column) = Buffer[String]()
    for (card <- column.getCards) { // for each card, there is a pane used as a gap between the cards
      val pane = getPane(board, column, 20)
      panes(column) += pane.toString()
      children += pane

      if (currentFilter.forall(card.getTags.contains(_))) { // only cards that have the filter tags are displayed
        children += drawCard(board, column, card)
      }

    }
    val pane = getPane(board, column, 50) // bigger pane at the end of the card list
    panes(column) += pane.toString()
    children += pane
  }

  /** Creates menu items for all kanban session boards that is used to switch between them
   *
   * @return Buffer of menu items for each board */
  private def boardSelectMenuItems: Buffer[MenuItem] = {
    val keyCodes = Vector(KeyCode.Digit1, KeyCode.Digit2, KeyCode.Digit3, KeyCode.Digit4, KeyCode.Digit5,
      KeyCode.Digit6, KeyCode.Digit7, KeyCode.Digit8, KeyCode.Digit9)
    var index = 0 // keep track of loop so correct accelerator can be picker from above vector

    val items = Buffer[MenuItem]()
    for (board <- kanbanApp.getBoardNames) {
      items += new MenuItem(board) { // menu item for each board that is used to select the board
        accelerator = new KeyCodeCombination(keyCodes(index), KeyCombination.ControlDown) // Ctrl+Num
        onAction = (event) => {
          activeBoard = kanbanApp.getBoard(board)
          boardBackground = getBoardBackground(activeBoard) // update background
          update() // update GUI
        }
      }
      index += 1
    }
    items
  }

  private val selectBoardMenu = new MenuButton {
    text = activeBoard.getName
    items = boardSelectMenuItems
  }

  private val newBoardButton = new Button("New Board") {
    font = DefaultFont

    onAction = (event) => {
      val boardNum = kanbanApp.getBoards.size
      BoardDialog.reset(kanbanApp, activeBoard, true)
      BoardDialog.showDialog()

      if (boardNum < kanbanApp.getBoardNames.size) {
        activeBoard = kanbanApp.getBoards.takeRight(1).head
      }
      if (activeBoard.getColumns.isEmpty) { // if a new board was created, ask user to add list
        ColumnDialog.reset(activeBoard, activeColumn.getOrElse(new Column), true)
        ColumnDialog.showDialog()

        if (activeBoard.getColumns.isEmpty) { // If the user chose not to add new list
          activeBoard.addColumn("List 1") // add a default list so board is not empty
        }
      }

      boardBackground = getBoardBackground(activeBoard)
      update()
    }
  }

  private val fileMenuButton = new MenuButton("File") {
    items += new MenuItem("Open") {
      accelerator = new KeyCodeCombination(KeyCode.O, KeyCombination.ControlDown) // CTRL + O shortcut
      onAction = (event) => {
        val result = fileManager.load(kanbanApp)
        result._1 match {
          case Some(kanban) => {
            kanbanApp = kanban
            fullUpdate()
          }
          case None =>
        }
      }
    }
    items += new MenuItem("Save") {
      accelerator = new KeyCodeCombination(KeyCode.S, KeyCombination.ControlDown)
      onAction = (event) => {
        fileManager.save(kanbanApp)
      }
    }
    items += new SeparatorMenuItem
    items += new MenuItem("Quit") {
      accelerator = new KeyCodeCombination(KeyCode.Q, KeyCombination.ControlDown)
      onAction = (event) => {
        val result = drawAlert("Quit", "Are you sure you want to quit?").showAndWait()
        result match {
          case Some(ButtonType.OK) => {
            sys.exit(0)
          }
          case _ =>
        }
      }
    }
  }

  private val editBoardButton = new Button("Edit Board") {
    font = DefaultFont
    onAction = (event) => {
      val boardCount = kanbanApp.getBoards.size
      BoardDialog.reset(kanbanApp, activeBoard, false)
      BoardDialog.showDialog()
      if (kanbanApp.getBoards.size < boardCount) {
        fullUpdate()
      } else {
        boardBackground = getBoardBackground(activeBoard)
        update()
      }

    }
  }

  private val newListButton = new Button("New List") {
    font = DefaultFont
    onAction = (event) => {
      ColumnDialog.reset(activeBoard, activeColumn.getOrElse(new Column), true)
      ColumnDialog.showDialog()
      update()
    }
  }

  private val archiveButton = new Button("Archive") {
    font = DefaultFont
    onAction = (event) => {
      ArchiveDialog.reset(kanbanApp, activeBoard)
      ArchiveDialog.showDialog()
      update()
    }
  }

  private val templateButton = new Button("Templates") {
    font = DefaultFont
    onAction = (event) => {
      ArchiveDialog.reset(kanbanApp, activeBoard, false)
      ArchiveDialog.showDialog()
      update()
    }
  }

  private val manageTagsButton = new Button("Manage Tags") {
    font = DefaultFont
    onAction = (event) => {
      TagDialog.reset(kanbanApp)
      TagDialog.showDialog()
      update()
    }
  }

  private val filterButton = new MenuButton("Filter") {
    font = DefaultFont
    items = getFilterItems
  }

  /** Returns menu item for filter menu when no tags have been created by user
   *
   * @return Menu item for a button prompting user to create a tag to use the filter feature */
  private def getEmptyFilterItems: Buffer[MenuItem] = {
    val items = Buffer[MenuItem]()
    items += new MenuItem("No Tags - Create New") {
      onAction = (event) => {
        activeCard = None
        activeColumn = None
        TagDialog.reset(kanbanApp)
        TagDialog.showDialog()
        update()
      }
    }
    items
  }

  /** Returns menu items based on tags created by user and handles setting them active
   *
   * @return Buffer of menu items used in the filter menu button */
  private def getFilterItems: Buffer[MenuItem] = {
    val items = Buffer[MenuItem]()
    items += new MenuItem("Reset") {
      onAction = (event) => {
        currentFilter.clear()
        activeCard = None
        activeColumn = None
        update()
      }
    }
    items += new SeparatorMenuItem

    for (tag <- kanbanApp.getTags) {
      items += new MenuItem(tag) {
        onAction = (event) => {
          activeCard = None
          activeColumn = None
          if (currentFilter.contains(tag)) {
            currentFilter.remove(currentFilter.indexOf(tag))
          } else {
            currentFilter += tag
          }
          update()
        }
      }
    }
    if (kanbanApp.getTags.isEmpty) getEmptyFilterItems else items
  }

  /** Returns text displayig active filters
   *
   * @return string describing active filters */
  private def getFilterText: String = {
    if (currentFilter.isEmpty) {
      ""
    } else {
      "   Current filter: " + currentFilter.mkString(", ")
    }
  }

  private val filterLabel = new Label {
    font = DefaultFont
    text = getFilterText
  }

  private val toolbar = new ToolBar {
    // File segment for loading, saving and exiting
    items += fileMenuButton

    items += new Separator
    // Board management segment
    items += selectBoardMenu
    items += newBoardButton
    items += editBoardButton

    items += new Separator
    // New list segment
    items += newListButton

    items += new Separator
    // Archive and template segment
    items += archiveButton
    items += templateButton

    items += new Separator
    // Filter segment
    items += manageTagsButton
    items += filterButton
    items += filterLabel
  }

  /** Returns correct background for active board
   *
   * @param board active board that is displayed on the GUI
   * @return background based on whether an image has been selected, color if not */
  private def getBoardBackground(board: Board): Background = {
    board.getBgImage match {
      case Some(file) => {
        val imgTry = Try {
          new Image(file.toURI.toString)
        } // get image based on file
        imgTry match {
          case Failure(exception) => getColorBg(board.getColor)
          case Success(img) => {
            var image = img
            if (image.height.value > stage.height.value) { // if image is large
              val aspectRatio = image.width() / image.height() // use aspect ratio to display image based on current window size
              image = new Image(file.toURI.toString, stage.width(), stage.width() / aspectRatio, false, true)
            }
            val bgImg = new BackgroundImage(image, BackgroundRepeat.Repeat, BackgroundRepeat.Repeat,
              BackgroundPosition.Default, BackgroundSize.Default) // create background using image
            new Background(Array(bgImg))
          }
        }
      }
      case None => getColorBg(board.getColor) // if no image, use color background
    }
  }

  /** Calculates correct width for the last filler pane so that whole screen is filled
   *
   * @param board active board that is displayed on the GUI
   * @return width so that it fills the rest of the screen after existing lists */
  private def calculateWidth(board: Board): Int = {
    val amount = board.getColumns.size
    // based on currently existing lists, the more there are, the smaller it needs to be
    (stage.width() - amount * ColumnWidth - amount * ColumnGapSize - 20).toInt max 200
    // 200 as even if the screen is filled there still needs to be a pane needed to move the columsn around
  }

  /** Forms HBox component used to display a board
   *
   * @param board active board that is displayed on the GUI
   * @return HBox component with all components needed to display the board */
  private def drawBoard(board: Board): HBox = {
    new HBox {
      background = boardBackground
      alignment = CenterLeft

      columnPanes.clear() // when new board is drawn, new panes need to be calculated
      for (column <- board.getColumns) {
        val pane = getColumnPane(board, ColumnGapSize) // between each column is a gap
        columnPanes += pane.toString() // store the unique ID of that gap pane
        children += pane
        children += drawColumn(board, column) // add column
      }
      // after all columns is a larger gap that fills the rest of the window
      val pane = getColumnPane(board, calculateWidth(board))
      columnPanes += pane.toString()
      children += pane

    }
  }


  private val boardPane = new ScrollPane {
    minHeight = stage.height.value - 75
    content = drawBoard(activeBoard)
  }

  /** Returns the root GUI component, base of the whole GUI
   *
   * @return VBox component that acts as the root of the whole GUI */
  private def root: VBox = new VBox {
    stage.title = "KanbanApp - " + activeBoard.getName
    children += toolbar
    children += boardPane
  }

  /** Updates the screen after user makes changes by drawing the active board again */
  private def update(): Unit = {
    boardPane.content = drawBoard(activeBoard) // set content again, updating possible changes
    stage.title = "KanbanApp - " + activeBoard.getName // set correct window title

    // Handle changes to the filters and board selection list
    filterButton.items = getFilterItems
    filterLabel.text = getFilterText
    selectBoardMenu.text = activeBoard.getName
    selectBoardMenu.items = boardSelectMenuItems


    // Check if max number of board or columns have been reached
    // If so, disable the ability to create new ones
    if (kanbanApp.getBoards.size == MaxBoards) {
      newBoardButton.disable = true
      newBoardButton.setTooltip(new Tooltip("Max Boards Reached"))
    } else {
      newBoardButton.disable = false
      newBoardButton.tooltip = new Tooltip()
    }

    if (activeBoard.getColumns.size == MaxColumns) {
      newListButton.disable = true
      newListButton.setTooltip(new Tooltip("Max Lists Reached"))
    } else {
      newListButton.disable = false
      newListButton.tooltip = new Tooltip()
    }
  }

  /** After Kanban is loaded from file, reset all aspects of the GUI */
  private def fullUpdate(): Unit = {
    activeBoard = kanbanApp.getBoards.head
    boardBackground = getBoardBackground(activeBoard) // get new correct background

    // Reset all variables tracking active GUI components
    activeCard = None
    activeColumn = None
    columnMove = None

    currentFilter.clear()
    kanbanApp.checkFiles() // check that file paths still point to valid files
    update()
  }

  private val scene = new Scene(root) // create scene from root
  stage.scene = scene // set the scene of the main stage
  scene.setFill(Color.Transparent) // set to transparent so background can be seen from behind
  stage.centerOnScreen() // center window

  // Show menu window on startup
  /*  StartupWindow.showWindow()

    // Get the kanban session user chose and update GUI
    kanbanApp = StartupWindow.getKanban
    fullUpdate()*/

  // Listen to changes made to the height and width of the window
  // so that GUI components that are based on the size of the window are updated accordingly
  stage.heightProperty.addListener { (obs: ObservableValue[_ <: Number], oldVal: Number, newVal: Number) =>
    update()
  }

  stage.widthProperty.addListener { (obs: ObservableValue[_ <: Number], oldVal: Number, newVal: Number) =>
    update()
  }

}

package kanban

import scalafx.application.JFXApp
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
import settings._
import java.awt.Desktop
import scala.collection.mutable.{Buffer, Map}
import scalafx.application.JFXApp.PrimaryStage


object Main extends JFXApp {
  stage = new PrimaryStage {
    title.value = "KanbanApp"
    width = 1100
    height = 800
  }

  private val panes = Map[Column, Buffer[String]]()
  private val columnPanes = Buffer[String]()
  private val currentFilter = Buffer[String]()

  private var kanbanApp = new Kanban
  private val fileManager = new FileHandler

  kanbanApp.setStage(stage)
  kanbanApp.createBoard("board1")
  kanbanApp.getBoards.head.addColumn("list1", Color.Black)
  kanbanApp.getBoards.head.getColumns.head.addCard(new Card("card1", Color.LightBlue))

  private var activeBoard = kanbanApp.getBoards.head

  private var activeCard: Option[Card]      = None
  private var activeColumn: Option[Column]  = None
  private var columnMove: Option[Column]    = None

  private var boardBackground: Background = getBoardBackground(activeBoard)

  def getColorBg(color: Color): Background = new Background(Array(new BackgroundFill(color, CornerRadii.Empty, Insets.Empty)))

  def drawAlert(alertTitle: String, content: String): Alert = {
    new Alert(AlertType.Confirmation) {
      initOwner(stage)
      title = alertTitle
      contentText = content
    }
  }

  def drawCardDelete(column: Column, card: Card): Button = {
    new Button("Delete") {
      onAction = (event) => {
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

  def drawCardEdit(column: Column, card: Card): Button = {
    new Button("Edit") {
      onAction = (event) => {
        CardDialog.reset(kanbanApp, column, card, false)
        CardDialog.showDialog()
        activeCard = None
        activeColumn = None
        update()
      }
    }
  }

  def drawCardArchive(board: Board, column: Column, card: Card): Button = {
    new Button("Archive") {
      onAction = (event) => {
        board.getArchive.addCard(card)
        column.deleteCard(card)
        activeCard = None
        activeColumn = None
        update()
      }
    }
  }

  def drawCard(board: Board, column: Column, card: Card): VBox = new VBox(4) {
    background = new Background(Array(new BackgroundFill(Color.White, new CornerRadii(6), Insets.Empty)))
    if (activeCard.getOrElse(new Card) == card) {
      border = new Border(new BorderStroke(card.getBorderColor, BorderStrokeStyle.Dotted, new CornerRadii(2), new BorderWidths(6)))
    } else {
      maxHeight = CardHeight
      border = new Border(new BorderStroke(card.getBorderColor, BorderStrokeStyle.Solid, new CornerRadii(2), new BorderWidths(6)))
    }

    minWidth = CardWidth
    maxWidth = CardWidth
    minHeight = CardHeight

    alignment = Center
    children += new Label(card.getText) {
      wrapText = true
      textAlignment = TextAlignment.Center
      font = CardTextFont
      textFill = card.getTextColor
    }

    card.getDeadline match {
      case Some(deadline) => {
        if (!card.getChecklist.hasTasks && activeCard.getOrElse(new Card) == card) {
          children += new HBox(12) {
            alignment = Center
            children += new Label(deadline.getString) {
              textFill = deadline.getCorrectColor(card.getChecklist)
            }
            children += new CheckBox("Done") {
              selected = deadline.getStatus
              onAction = (event) => {
                deadline.toggleStatus()
                update()
              }
            }
          }
        } else {
          children += new Label(deadline.getString) {
            textFill = deadline.getCorrectColor(card.getChecklist)
          }
        }
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

      if (activeCard.getOrElse(new Card) == card) {
        children += new HBox {
          children += new Pane {
            minWidth = 20
          }
          children += new VBox(2) {
            for (task <- card.getChecklist.getTasks) {
              children += new CheckBox() {
                text = task._2
                selected = task._1
                onAction = (event) => {
                  card.getChecklist.toggleStatus(task._2)
                  update()
                }
              }
            }
          }
        }
      }
    }

    card.getFile match {
      case Some(file) => {
        if (activeCard.getOrElse(new Card) == card) {
          children += new HBox(5) {
            alignment = Center
            children += new Label("File: " + file.getName)
            children += new Button("Open File") {
              onAction = (event) => {
                if (file.canRead && Desktop.isDesktopSupported) {
                  Desktop.getDesktop.open(file)
                }
              }
            }
          }
        } else {
          children += new Label("File Attachment: " + file.getName)
        }
      }
      case None =>
    }

    card.getSubcard match {
      case Some(sub) => {
        if (activeCard.getOrElse(new Card) == card) {
          children += new HBox(5) {
            alignment = Center
            children += new Label("SubCard: ")
            children += new Button("View") {
              onAction = (event) => {
                CardViewDialog.reset(sub.getOriginal)
                CardViewDialog.showDialog()
              }
            }
          }
        }
      }
      case None =>
    }

    if (activeCard.getOrElse(new Card) == card) {
      if (card.getTags.nonEmpty) {
        children += new Label("Tags: " + card.getTags.mkString(", "))
      }
      children += new HBox(4) {
        alignment = Center
        children += drawCardEdit(column, card)
        children += drawCardDelete(column, card)
        children += drawCardArchive(board, column, card)
      }
    }

    onMouseClicked = (event) => {
      if (activeCard.getOrElse(new Card) == card) {
        activeCard = None
        activeColumn = None
      } else {
        activeCard = Some(card)
        activeColumn = Some(column)
      }
      columnMove = None
      update()
    }
  }

  def getColumnPane(board: Board, minwidth: Int): Pane = {
    new Pane {
      minWidth = minwidth
      onMouseReleased = (event) => {
        var index = columnPanes.indexOf("[SFX]" + event.getPickResult.getIntersectedNode.toString)
        if (index != -1 && columnMove.isDefined) {
          if (board.getColumns.indexOf(columnMove.getOrElse(new Column)) < index) {
            index -= 1
          }
          board.moveColumn(columnMove.getOrElse(new Column), index)
          columnMove = None
          update()
        }
      }
    }
  }

  def getPane(board: Board, column: Column, minheight: Int): Pane = {
    new Pane {
      minHeight = minheight
      onMouseReleased = (event) => {
        var index = panes(column).indexOf("[SFX]" + event.getPickResult.getIntersectedNode.toString)
        if (index != -1 && activeCard.isDefined) {
          val theCard = activeCard.getOrElse(new Card)
          if (activeColumn.getOrElse(new Column) == column && column.getCards.indexOf(theCard) < index) {
            index = (index - 1) max 0
          }
          board.moveCard(theCard, activeColumn.getOrElse(new Column), column, index)
          activeCard = None
          update()
        }
      }
    }
  }

  def drawColumnNewCard(board: Board, column: Column): SplitMenuButton = {
    new SplitMenuButton {
      text = "New Card"
      font = DefaultFont
      minWidth = 110

      items += new MenuItem("From Archive") {
        onAction = (event) => {
          CardListDialog.reset(kanbanApp, board, column)
          CardListDialog.showDialog()
          update()
        }
      }

      items += new MenuItem("From Template") {
        onAction = (event) => {
          CardListDialog.reset(kanbanApp, board, column, new Card, 2)
          CardListDialog.showDialog()
          update()
        }
      }

      onAction = (event) => {
        activeColumn = Some(column)
        activeCard = None
        CardDialog.reset(kanbanApp, column, new Card, true)
        CardDialog.showDialog()
        activeColumn = None
        update()
      }
    }
  }

  def drawColumnEdit(board: Board, column: Column): Button = {
    new Button("Edit") {
      font = DefaultFont
      minWidth = 70
      onAction = (event) => {
        activeColumn = Some(column)
        activeCard = None
        ColumnDialog.reset(board, column, false)
        ColumnDialog.showDialog()
        update()
      }
    }
  }

  def drawColumnDelete(board: Board, column: Column): Button = {
    new Button("Delete") {
      font = DefaultFont
      minWidth = 70
      onAction = (event) => {

        val result = drawAlert("Delete List", "Are you sure you want to delete the list?").showAndWait()
        result match {
          case Some(ButtonType.OK) => {
            board.deleteColumn(column)
            update()
          }
          case _ =>
        }
      }
    }
  }

  def drawColumnMove(column: Column): Button = new Button("Move") {
    font = DefaultFont
    minWidth = 70
    onAction = (event) => {
      if (columnMove.getOrElse(new Column) == column) {
        columnMove = None
      } else {
        columnMove = Some(column)
        activeCard = None
      }
      update()
    }
  }

  def drawColumn(board: Board, column: Column): VBox = new VBox {
    if (columnMove.getOrElse(new Column) == column) {
      border = new Border(new BorderStroke(column.getColor, BorderStrokeStyle.Dotted, new CornerRadii(2), new BorderWidths(6)))
    } else {
      border = new Border(new BorderStroke(column.getColor, BorderStrokeStyle.Solid, new CornerRadii(2), new BorderWidths(6)))
    }
    alignment = TopCenter
    minHeight = stage.height.value - 95
    minWidth = ColumnWidth

    children += new HBox {
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
    for (card <- column.getCards) {
      val pane = getPane(board, column, 20)
      panes(column) += pane.toString()
      children += pane

      if (currentFilter.forall(card.getTags.contains(_))) {
        children += drawCard(board, column, card)
      }

    }
    val pane = getPane(board, column, 50)
    panes(column) += pane.toString()
    children += pane
  }

  val filterButton = new MenuButton("Filter") {
    font = DefaultFont
    items = getFilterItems
  }

  def getFilterItems = {
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

  def getEmptyFilterItems = {
    val items = Buffer[MenuItem]()
    items += new MenuItem("No Filters - Add New") {
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

  def getFilterText = {
    if (currentFilter.isEmpty) {
      ""
    } else {
      "   Current filter: " + currentFilter.mkString(", ")
    }
  }

  val filterLabel = new Label {
    font = DefaultFont
    text = getFilterText
  }

  def boardSelectMenuItems: Buffer[MenuItem] = {
    val keyCodes = Vector(KeyCode.Digit1, KeyCode.Digit2, KeyCode.Digit3, KeyCode.Digit4, KeyCode.Digit5)
    var index = 0
    val items = Buffer[MenuItem]()
    for (board <- kanbanApp.getBoardNames) {
      items += new MenuItem(board) {
        accelerator = new KeyCodeCombination(keyCodes(index), KeyCombination.ControlDown)
        onAction = (event) => {
          activeBoard = kanbanApp.getBoard(board)
          boardBackground = getBoardBackground(activeBoard)
          update()
        }
      }
      index += 1
    }
    items
  }

  val selectBoardMenu = new MenuButton {
    text = activeBoard.getName
    items = boardSelectMenuItems
  }

  val newBoardButton = new Button("New Board") {
    font = DefaultFont

    onAction = (event) => {
      val boardNum = kanbanApp.getBoards.size
      BoardDialog.reset(kanbanApp, activeBoard, true)
      BoardDialog.showDialog()

      if (boardNum < kanbanApp.getBoardNames.size) {
        activeBoard = kanbanApp.getBoards.takeRight(1).head
      }
      if (activeBoard.getColumns.isEmpty) {
        ColumnDialog.reset(activeBoard, activeColumn.getOrElse(new Column), true)
        ColumnDialog.showDialog()
      }

      if (activeBoard.getColumns.isEmpty) {
        activeBoard.addColumn("List 1")
      }
      boardBackground = getBoardBackground(activeBoard)
      update()
    }
  }

  val fileMenuButton = new MenuButton("File") {
    items += new MenuItem("Open") {
      accelerator = new KeyCodeCombination(KeyCode.O, KeyCombination.ControlDown)
      onAction = (event) => {
        val result = fileManager.load(kanbanApp)
        result match {
          case Some(kanban) => {
            kanbanApp = kanban
            kanbanApp.setStage(stage)
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

  val editBoardButton = new Button("Edit Board") {
    font = DefaultFont
    onAction = (event) => {
      BoardDialog.reset(kanbanApp, activeBoard, false)
      BoardDialog.showDialog()
      boardBackground = getBoardBackground(activeBoard)
      update()
    }
  }

  val newListButton = new Button("New List") {
    font = DefaultFont
    onAction = (event) => {
      ColumnDialog.reset(activeBoard, activeColumn.getOrElse(new Column), true)
      ColumnDialog.showDialog()
      update()
    }
  }

  val toolbar = new ToolBar {
    items += fileMenuButton
    items += new Separator
    items += selectBoardMenu
    items += newBoardButton
    items += editBoardButton
    items += new Separator
    items += newListButton
    items += new Separator
    items += new Button("Archive") {
      font = DefaultFont
      onAction = (event) => {
        ArchiveDialog.reset(kanbanApp, activeBoard)
        ArchiveDialog.showDialog()
        update()
      }
    }
    items += new Button("Templates") {
      font = DefaultFont
      onAction = (event) => {
        ArchiveDialog.reset(kanbanApp, activeBoard, false)
        ArchiveDialog.showDialog()
        update()
      }
    }
    items += new Separator
    items += new Button("Manage Tags") {
      font = DefaultFont
      onAction = (event) => {
        TagDialog.reset(kanbanApp)
        TagDialog.showDialog()
        update()
      }
    }
    items += filterButton
    items += filterLabel
  }

  def getBoardBackground(board: Board): Background = {
    board.getBgImage match {
      case Some(file) => {
        var img = new Image(file.toURI.toString)
        if (img.height.value > stage.height.value) {
          val aspectRatio = stage.width() / stage.height()
          img = new Image(file.toURI.toString, stage.height() * aspectRatio, stage.height(), false, true)
        }

        val bgImg = new BackgroundImage(img, BackgroundRepeat.Repeat, BackgroundRepeat.Repeat,
          BackgroundPosition.Default, BackgroundSize.Default)
        new Background(Array(bgImg))
      }
      case None => getColorBg(board.getColor)
    }
  }

  def drawBoard(board: Board): HBox = {
    new HBox() {

      background = boardBackground
      alignment = CenterLeft
      columnPanes.clear()
      for (column <- board.getColumns) {
        val pane = getColumnPane(board, 20)
        columnPanes += pane.toString()
        children += pane
        children += drawColumn(board, column)
      }

      val pane = getColumnPane(board, calculateWidth(board))
      columnPanes += pane.toString()
      children += pane

    }
  }

  def calculateWidth(board: Board) = {
    val amount = board.getColumns.size
    (stage.width() - amount * ColumnWidth - amount * 20 - 20).toInt max 200
  }

  val boardPane = new ScrollPane {
    minHeight = stage.height.value - 75
    content = drawBoard(activeBoard)
  }

  def root: VBox = new VBox {
    stage.title = "KanbanApp - " + activeBoard.getName
    children += toolbar
    children += boardPane
  }


  def update(): Unit = {
    boardPane.content = drawBoard(activeBoard)
    stage.title = "KanbanApp - " + activeBoard.getName
    filterButton.items = getFilterItems
    filterLabel.text = getFilterText
    selectBoardMenu.text = activeBoard.getName
    selectBoardMenu.items = boardSelectMenuItems

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

  def fullUpdate(): Unit = {
    activeBoard = kanbanApp.getBoards.head
    boardBackground = getBoardBackground(activeBoard)
    activeCard = None
    activeColumn = None
    columnMove = None
    currentFilter.clear()
    kanbanApp.checkFiles()
    update()
  }

  val scene = new Scene(root)
  stage.scene = scene
  scene.setFill(Color.Transparent)
  stage.centerOnScreen()

  stage.heightProperty.addListener { (obs: ObservableValue[_ <: Number], oldVal: Number, newVal: Number) =>
    update()
  }

  stage.widthProperty.addListener { (obs: ObservableValue[_ <: Number], oldVal: Number, newVal: Number) =>
    update()
  }
}

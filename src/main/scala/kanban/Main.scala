package kanban

import scalafx.application.JFXApp
import scalafx.geometry.Pos._
import scalafx.Includes._
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.scene.text._
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.input._
import scalafx.scene.paint.Color
import javafx.beans.value.ObservableValue
import scalafx.scene.image.Image
import scalafx.scene.shape.StrokeType

import java.awt.Desktop
import scala.collection.mutable.{Buffer, Map}


object Main extends JFXApp {
  stage = new JFXApp.PrimaryStage {
    title.value = "KanbanApp"
    width = 1100
    height = 800
  }

  var cardEditActive = false
  var columnEditActive = false
  var tagEditActive = false

  val panes = Map[Column, Buffer[String]]()
  val columnPanes = Buffer[String]()

  var kanbanApp = new Kanban

  kanbanApp.createBoard("board1")
  kanbanApp.getBoards.head.addColumn("list1", Color.Black)
  kanbanApp.getBoards.head.getColumns.head.addCard("card1", Color.Green)

  val fileManager = new FileHandler

  val noCard = new Card("", Color.Black, Buffer[String](), new Checklist, None, None)
  val noColumn = new Column("", Color.Black)

  var activeCard = noCard

  var activeBoard = kanbanApp.getBoards.head

  var activeColumn = noColumn
  var columnMove = noColumn

  val fontChoice = Font.font("arial", 13)

  val currentFilter = Buffer[String]()

  private val CARD_WIDTH = 310
  private val CARD_HEIGHT = 120
  private val COLUMN_WIDTH = 330

  val bgFill = new BackgroundFill(Color.Orange, CornerRadii.Empty, Insets.Empty)
  val bgFill2 = new BackgroundFill(Color.White, new CornerRadii(6), Insets.Empty)

  val bg = new Background(Array(bgFill))
  val bg2 = new Background(Array(bgFill2))

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
        activeCard = noCard
        activeColumn = noColumn
        update()

      }
    }
  }

  def drawCardArchive(board: Board, column: Column, card: Card): Button = {
    new Button("Archive") {
      onAction = (event) => {
        board.getArchive.addCard(card)
        column.deleteCard(card)
        activeCard = noCard
        activeColumn = noColumn
        update()

      }
    }
  }

  def drawCard(board: Board, column: Column, card: Card): VBox = new VBox(4) {
    background = bg2
    if (activeCard == card) {
      border = new Border(new BorderStroke(card.getColor, BorderStrokeStyle.Dotted, new CornerRadii(2), new BorderWidths(6)))
    } else {
      maxHeight = CARD_HEIGHT
      border = new Border(new BorderStroke(card.getColor, BorderStrokeStyle.Solid, new CornerRadii(2), new BorderWidths(6)))
    }

    minWidth = CARD_WIDTH
    maxWidth = CARD_WIDTH
    minHeight = CARD_HEIGHT

    alignment = Center
    children += new Label(card.getText) {
      wrapText = true
      textAlignment = TextAlignment.Center
      font = fontChoice
    }
    card.getDeadline match {
      case Some(deadline) => {
        children += new Label(deadline.getString)
      }
      case None =>
    }

    if (card.getChecklist.hasTasks) {
      children += new Label(card.getChecklist.toString)
      children += new ProgressBar {
        progress = card.getChecklist.getProgress
        minHeight = 20
        minWidth = 150

      }
      if (activeCard == card) {
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
        if (activeCard == card) {
          children += new HBox(3) {
            alignment = Center
            children += new Label("File: " + file.getName)
            children += new Button("Open File") {
              onAction = (event) => {
                if (file.canRead) {
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

    if (activeCard == card) {
      children += new HBox(4) {
        alignment = Center
        children += drawCardEdit(column, card)
        children += drawCardDelete(column, card)
        children += drawCardArchive(board, column, card)
      }

    }
    onMouseClicked = (event) => {
      if (activeCard == card) {
        activeCard = noCard
        activeColumn = noColumn
        columnMove = noColumn
      } else {
        activeCard = card
        activeColumn = column
        columnMove = noColumn
      }
      update()
    }
  }

  def getColumnPane(board: Board, minwidth: Int): Pane = {
    new Pane {
      minWidth = minwidth
      onMouseReleased = (event) => {
        var index = columnPanes.indexOf("[SFX]" + event.getPickResult.getIntersectedNode.toString)
        if (index != -1 && columnMove != noColumn) {
          if (board.getColumns.indexOf(columnMove) < index) {
            index -= 1
          }
          board.deleteColumn(columnMove)
          board.addColumn(columnMove, index)
          columnMove = noColumn
          update()
        }
      }
    }
  }

  def getPane(column: Column, minheight: Int): Pane = {
    new Pane {
      minHeight = minheight
      onMouseReleased = (event) => {
        var index = panes(column).indexOf("[SFX]" + event.getPickResult.getIntersectedNode.toString)
        if (index != -1 && activeCard != noCard) {
          if (activeColumn == column && column.getCards.indexOf(activeCard) < index) {
            index = (index - 1) max 0
          }
          println(index)
          activeColumn.deleteCard(activeCard)
          column.addCard(activeCard.getText, activeCard.getColor, activeCard.getTags, activeCard.getChecklist, activeCard.getDeadline, index)
          update()
        }
      }
    }
  }

  def drawColumnNewCard(board: Board, column: Column): SplitMenuButton = {
    new SplitMenuButton {
      text = "New Card"
      font = fontChoice

      items += new MenuItem("From Archive") {
        onAction = (event) => {
          FromArchiveDialog.reset(board, column)
          FromArchiveDialog.showDialog()
          update()
        }
      }

      onAction = (event) => {
        activeColumn = column
        activeCard = noCard
        CardDialog.reset(kanbanApp, column, noCard, true)
        CardDialog.showDialog()
        activeColumn = noColumn
        update()
      }
    }
  }

  def drawColumnEdit(board: Board, column: Column): Button = {
    new Button("Edit") {
      font = fontChoice

      onAction = (event) => {
        activeColumn = column
        activeCard = noCard
        ColumnDialog.reset(board, column, false)
        ColumnDialog.showDialog()
        update()
      }
    }
  }

  def drawColumnDelete(board: Board, column: Column): Button = {
    new Button("Delete") {
      font = fontChoice
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

  def drawColumn(board: Board, column: Column): VBox = new VBox {
    if (columnMove == column) {
      border = new Border(new BorderStroke(column.getColor, BorderStrokeStyle.Dotted, new CornerRadii(2), new BorderWidths(6)))
    } else {
      border = new Border(new BorderStroke(column.getColor, BorderStrokeStyle.Solid, new CornerRadii(2), new BorderWidths(6)))
    }
    alignment = TopCenter
    minHeight = stage.height.value - 120
    minWidth = COLUMN_WIDTH

    children += new HBox(12) {
      alignment = Center
      children += drawColumnNewCard(board, column)
      children += drawColumnEdit(board, column)
      children += drawColumnDelete(board, column)
      children += new Button("Move") {
        font = fontChoice
        onAction = (event) => {
          if (columnMove == column) {
            columnMove = noColumn
          } else {
            columnMove = column
            activeCard = noCard
          }
          update()
        }
      }
    }
    children += new Pane {
       minHeight = 10
    }
    children += new Text {
      text = column.getName
      font = Font.font("arial", 26)
      //minHeight = 35
      fill = Color.Black
      stroke = Color.White
      strokeType = StrokeType.Outside
      strokeWidth = 1
    }

    panes(column) = Buffer[String]()
    for (card <- column.getCards) {
      val pane = getPane(column, 20)
      panes(column) += pane.toString()
      children += pane

      if (currentFilter.forall(card.getTags.contains(_))) {
        children += drawCard(board, column, card)
      }

    }
    val pane = getPane(column, 50)
    panes(column) += pane.toString()
    children += pane
  }

  val filterButton = new MenuButton("Filter") {
    font = fontChoice
    items = getFilterItems
  }

  def getFilterItems = {
    val items = Buffer[MenuItem]()
    items += new MenuItem("Reset") {
      onAction = (event) => {
        currentFilter.clear()
        activeCard = noCard
        activeColumn = noColumn
        update()
      }
    }
    items += new SeparatorMenuItem

    for (tag <- kanbanApp.getTags) {
      items += new MenuItem(tag) {
        onAction = (event) => {
          activeCard = noCard
          activeColumn = noColumn
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
        activeCard = noCard
        activeColumn = noColumn
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
    font = fontChoice
    text = getFilterText
  }

  def boardSelectMenuItems: Buffer[MenuItem] = {
    val items = Buffer[MenuItem]()
    for (board <- kanbanApp.getBoardNames) {
      items += new MenuItem(board) {
        onAction = (event) => {
          println(board)
          activeBoard = kanbanApp.getBoard(board)
          update()
        }
      }
    }
    items
  }

  val selectBoardMenu = new MenuButton {
    text = activeBoard.getName
    items = boardSelectMenuItems
  }

  val toolbar = new ToolBar {
    items += selectBoardMenu
    items += new Button("New Board") {
      font = fontChoice
      onAction = (event) => {
        val boardNum = kanbanApp.getBoards.size
        BoardDialog.reset(kanbanApp, activeBoard, true)
        BoardDialog.showDialog()
        if (boardNum < kanbanApp.getBoardNames.size) {
          activeBoard = kanbanApp.getBoards.takeRight(1).head
        }
        update()
      }
    }
    items += new Button("Edit Board") {
      font = fontChoice
      onAction = (event) => {
        BoardDialog.reset(kanbanApp, activeBoard, false)
        BoardDialog.showDialog()
        update()
      }
    }
    items += new Separator
    items += new Button("New List") {
      font = fontChoice
      onAction = (event) => {
        ColumnDialog.reset(activeBoard, activeColumn, true)
        ColumnDialog.showDialog()
        update()
      }
    }
    items += new Separator
    items += new Button("Archive") {
      font = fontChoice
      onAction = (event) => {
        ArchiveDialog.reset(activeBoard)
        ArchiveDialog.showDialog()
        update()
      }
    }
    items += new Separator
    items += new Button("Manage Tags") {
      font = fontChoice
      onAction = (event) => {
        TagDialog.reset(kanbanApp)
        TagDialog.showDialog()
        update()
      }
    }
    items += filterButton
    items += filterLabel
  }

  val menubar = new MenuBar {
    menus += new Menu("File") {
      items += new MenuItem("New") {
        accelerator = new KeyCodeCombination(KeyCode.N, KeyCombination.ControlDown)
      }
      items += new MenuItem("Open") {
        accelerator = new KeyCodeCombination(KeyCode.O, KeyCombination.ControlDown)
        onAction = (event) => {
          val result = fileManager.load(kanbanApp)
          result match {
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
  }

  def drawBoard(board: Board): HBox = {
    new HBox() {
      board.getBgImage match {
        case Some(file) => {
          val img = new Image(file.toURI.toString) //, stage.width(), stage.height(), true, true)

          val bgImg = new BackgroundImage(img, BackgroundRepeat.Repeat, BackgroundRepeat.Repeat,
            BackgroundPosition.Default, BackgroundSize.Default)
          background = new Background(Array(bgImg))
        }
        case None => background = getColorBg(board.getColor)
      }

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
    (stage.width() - amount * COLUMN_WIDTH - amount * 20 - 20).toInt
  }

  val boardPane = new ScrollPane {
    content = drawBoard(activeBoard)
  }

  def root: VBox = new VBox(8) {
    stage.title = "KanbanApp - " + activeBoard.getName
    children += menubar
    children += toolbar
    children += boardPane
  }

  def checkFiles() = {
    for (card <- kanbanApp.getAllCards.filter(_.getFile.isDefined)) {
      card.getFile match {
        case Some(file) => {
          println("fadssdf")
          if (!file.canRead) {
            card.resetFile()
          }
        }
        case None =>
      }
    }
  }

  def update(): Unit = {
    boardPane.content = drawBoard(activeBoard)
    stage.title = "KanbanApp - " + activeBoard.getName
    filterButton.items = getFilterItems
    filterLabel.text = getFilterText
    selectBoardMenu.text = activeBoard.getName
    selectBoardMenu.items = boardSelectMenuItems
  }

  def fullUpdate(): Unit = {
    activeCard = noCard
    activeBoard = kanbanApp.getBoards.head
    activeColumn = noColumn
    columnMove = noColumn
    currentFilter.clear()
    checkFiles()
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

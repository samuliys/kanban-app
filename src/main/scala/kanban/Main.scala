package kanban

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.geometry.Pos._
import scalafx.scene.Scene
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.scene.text._
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.input._
import scalafx.scene.paint.Color
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

  val kanbanApp = new Kanban
  val fileManager = new FileHandler
  val noCard = new Card("", Color.Black, Buffer[String](), None)
  val noColumn = new Column("", Color.Black)

  var activeCard = noCard
  var cardActiveStatus = true

  var activeBoard = kanbanApp.getBoards.head

  var activeColumn = noColumn
  var columnMove = noColumn

  var cardMoveActive = false

  val fontChoice = Font.font("arial", 14)
  val cardSizeWidth = 250
  val cardSizeHeight = 100

  val currentFilter = Buffer[String]()


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

  def drawCardEdit(card: Card): Button = {
    new Button("Edit") {
      onAction = (event) => {
        cardEditActive = true
        CardDialog.setCardEdit(card)
        val result = CardDialog.dialog.showAndWait()
        cardEditActive = false
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

    if (activeCard == card) {
      border = new Border(new BorderStroke(card.getColor, BorderStrokeStyle.Dotted, new CornerRadii(2), new BorderWidths(6)))
    } else {
      border = new Border(new BorderStroke(card.getColor, BorderStrokeStyle.Solid, new CornerRadii(2), new BorderWidths(6)))
    }

    minWidth = cardSizeWidth
    maxWidth = cardSizeWidth
    minHeight = cardSizeHeight
    maxHeight = cardSizeHeight

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

    if (activeCard == card) {
      children += new HBox(4) {
        alignment = Center
        children += drawCardEdit(card)
        children += drawCardDelete(column, card)
        children += drawCardArchive(board, column, card)
      }

    }
    onMouseClicked = (event) => {
      if (activeCard == card) {
        activeCard = noCard
        activeColumn = noColumn
        columnMove = noColumn
        cardMoveActive = false
      } else {
        activeCard = card
        activeColumn = column
        columnMove = noColumn
        cardMoveActive = true
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
        if (cardMoveActive && index != -1) {
          if (activeColumn == column && column.getCards.indexOf(activeCard) < index) {
            index = index - 1
          }
          activeColumn.deleteCard(activeCard)
          column.addCard(activeCard.getText, activeCard.getColor, activeCard.getTags, activeCard.getDeadline, index)
          update()
          cardMoveActive = false
        }
      }
    }
  }

  def drawColumnNewCard(column: Column): SplitMenuButton = {
    new SplitMenuButton {
      text = "New Card"
      font = fontChoice

      items += new MenuItem("From Archive") {

      }

      onAction = (event) => {
        activeColumn = column
        activeCard = noCard
        cardActiveStatus = false
        CardDialog.reset()
        val result = CardDialog.dialog.showAndWait()
        activeColumn = noColumn
        update()
      }
    }
  }

  def drawColumnEdit(column: Column): Button = {
    new Button("Edit") {
      font = fontChoice

      onAction = (event) => {
        activeColumn = column
        activeCard = noCard
        cardActiveStatus = false
        columnEditActive = true
        ColumnDialog.setColumnEdit(column)
        val result = ColumnDialog.dialog.showAndWait()
        update()
        columnEditActive = false
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
    minHeight = stage.height.value - 116
    minWidth = 280

    children += new Label(column.getName) {
      minHeight = 40
      font = Font.font("arial", 20)
    }
    children += new HBox(2) {
      alignment = Center
      children += drawColumnNewCard(column)
      children += drawColumnEdit(column)
      children += drawColumnDelete(board, column)
      children += new Button("Move") {
        font = fontChoice
        onAction = (event) => {
          if (columnMove == column) {
            columnMove = noColumn
          } else {
            columnMove = column
            cardMoveActive = false
            activeCard = noCard
          }
          update()
        }
      }
    }
    children += new Separator

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

  def toolbar = new ToolBar {
    items += new ComboBox(kanbanApp.getBoardNames) {
      promptText = activeBoard.getName
      minHeight = 20
      onAction = (event) => {
        activeCard = noCard
        activeColumn = noColumn
        cardMoveActive = false
        activeBoard = kanbanApp.getBoards(kanbanApp.getBoardNames.indexOf(value()))
        println(value())
        update()
      }
    }
    items += new Button("New Board") {
      font = fontChoice
    }
    items += new Button("Edit Board") {
      font = fontChoice
    }
    items += new Separator
    items += new Button("Archive") {
      font = fontChoice
      onAction = (event) => {
        ArchiveDialog.reset(activeBoard)
        val result = ArchiveDialog.dialog.showAndWait()
        update()
      }
    }
    items += new Separator
    items += new MenuButton("Filter") {
      font = fontChoice
      items += new MenuItem("Reset") {
        onAction = (event) => {
          currentFilter.clear()
          activeCard = noCard
          activeColumn = noColumn
          cardMoveActive = false
          update()
        }
      }
      items += new SeparatorMenuItem

      for (tag <- kanbanApp.getTags) {
        items += new MenuItem(tag) {
          onAction = (event) => {
            activeCard = noCard
            activeColumn = noColumn
            cardMoveActive = false
            if (currentFilter.contains(tag)) {
              currentFilter.remove(currentFilter.indexOf(tag))
            } else {
              currentFilter += tag
            }
            update()
          }
        }
      }
    }
    items += new Button("Manage Tags") {
      font = fontChoice
      onAction = (event) => {
        TagDialog.reset()
        TagDialog.dialog.showAndWait()
        update()
      }
    }

  }

  val menubar = new MenuBar {
    menus += new Menu("File") {
      items += new MenuItem("New") {
        accelerator = new KeyCodeCombination(KeyCode.N, KeyCombination.ControlDown)
      }
      items += new MenuItem("Open") {
        accelerator = new KeyCodeCombination(KeyCode.O, KeyCombination.ControlDown)
      }
      items += new MenuItem("Save") {
        accelerator = new KeyCodeCombination(KeyCode.S, KeyCombination.ControlDown)
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
      alignment = CenterLeft
      columnPanes.clear()
      for (column <- board.getColumns) {
        val pane = getColumnPane(board, 20)
        columnPanes += pane.toString()
        children += pane
        children += drawColumn(board, column)
      }

      val pane = getColumnPane(board, 100)
      columnPanes += pane.toString()
      children += pane

      children += new Button("New List") {
        font = fontChoice
        onAction = (event) => {
          activeBoard = board
          ColumnDialog.reset()
          val result = ColumnDialog.dialog.showAndWait()
          update()
        }
      }
    }
  }

  def root: VBox = new VBox(8) {
    stage.title = "KanbanApp - " + activeBoard.getName
    children += menubar
    children += toolbar
    children += drawBoard(activeBoard)
  }

  def update(): Unit = stage.scene = new Scene(root)

  val scene = new Scene(root)
  stage.scene = scene

}

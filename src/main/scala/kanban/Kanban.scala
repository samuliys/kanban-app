package kanban

import scala.collection.mutable.Buffer


class Kanban(var name: String = "untitled") {

  private val boards = Buffer[Board]()
  this.createBoard()
  private var activeColumn = this.boards.head.getColumns.head

  def rename(newName: String) = name = newName

  def getName = name
  def getBoards = this.boards.head

  def setActiveColumn(column: Column) = activeColumn = column
  def getActiveColumn = activeColumn

  def createBoard(name: String = "untitled") = {
    val board = new Board(name)
    boards += board
  }
}
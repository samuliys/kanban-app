package kanban

import scala.collection.mutable.Buffer

class Kanban(private var name: String = "untitled") {

  private val tags = Buffer[String]()
  private val boards = Buffer[Board]()

  createBoard("board1")
  createBoard("board2")

  addTag("tag1")
  addTag("tag2")

  def rename(newName: String) = name = newName

  def getName = name

  def getBoards = boards

  def getBoardNames = boards.map(_.getName).toList

  def getTags = tags

  def createBoard(name: String = "untitled"): Board = {
    val board = new Board(name)
    boards += board
    board
  }

  def addTag(name: String) = {
    tags += name
  }

  def removeTag(name: String) = {
    boards.foreach(_.getColumns.foreach(_.getCards.foreach(_.removeTag(name))))
    tags -= name
  }
}

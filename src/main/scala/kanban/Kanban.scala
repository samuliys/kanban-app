package kanban

import scala.collection.mutable.Buffer

class Kanban(private var name: String = "untitled",
             private val boards: Buffer[Board] = Buffer[Board](),
             private val tags: Buffer[String] = Buffer[String]()) {

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

  def getBoard(name: String): Board = boards(getBoardNames.indexOf(name))

  def deleteBoard(board: Board) = boards -= board

  def addTag(name: String) = {
    tags += name
  }

  def removeTag(name: String) = {
    boards.foreach(_.getColumns.foreach(_.getCards.foreach(_.removeTag(name))))
    tags -= name
  }
}

package kanban

import scala.collection.mutable.{Buffer, Map}


class Kanban(var name: String = "untitled") {
  private val tags = Map[String, Tag]()
  private val boards = Buffer[Board]()

  this.createBoard()
  addTag("tag1")
  addTag("tag2")

  def rename(newName: String) = name = newName

  def getName = name
  def getBoards = this.boards.head

  def getTags = tags.values
  def getTagNames = tags.keys
  def getTag(name: String) = tags(name)

  def createBoard(name: String = "untitled") = {
    val board = new Board(name)
    boards += board
  }

  def addTag(name: String) = tags(name) = new Tag(name)
  def removeTag(name: String) = tags -= name
}

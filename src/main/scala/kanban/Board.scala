package kanban

import scala.collection.mutable.Buffer


class Board(private var name: String) {
  private val columns = Buffer[Column]()
  this.addColumn("Test1")
  this.addColumn("Test2")

  def getColumns = this.columns
  def getName = name

  def rename(newName: String) = name = newName

  def addColumn(name: String = "untitled") = {
    val column = new Column(name)
    //column.addCard(name)
    columns += column
  }
}
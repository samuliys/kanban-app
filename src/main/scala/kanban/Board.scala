package kanban

import scalafx.scene.paint.Color

import scala.collection.mutable.Buffer


class Board(private var name: String) {
  private val columns = Buffer[Column]()
  this.addColumn("List1", Color.Black)
  this.addColumn("List2", Color.Black)

  private val archive = new Column("Archive", Color.Black)

  def getArchive = archive

  def getColumns = this.columns
  def getName = name

  def rename(newName: String) = name = newName

  def addColumn(name: String = "untitled", color: Color = Color.Blue) = {
    val column = new Column(name, color)
    columns += column
  }

  def deleteColumn(column: Column) = {
    columns.remove(columns.indexOf(column))
  }
}

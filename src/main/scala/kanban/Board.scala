package kanban

import scalafx.scene.paint.Color
import scala.collection.mutable.Buffer


class Board(private var name: String,
            private val columns: Buffer[Column] = Buffer[Column](),
            private val archive: Column = new Column("Archive", Color.Black)) {

  def getArchive = archive

  def getColumns = this.columns

  def getName = name

  def getColumn(name: String) = {
    val columnNames = columns.map(_.getName)
    val index = columnNames.indexOf(name)
    columns(index)
  }

  def rename(newName: String) = name = newName

  def addColumn(name: String = "untitled", color: Color = Color.Black): Column = {
    val column = new Column(name, color)
    columns += column
    column
  }

  def addColumn(column: Column, index: Int) = {
    columns.insert(index, column)
  }

  def deleteColumn(column: Column) = {
    columns.remove(columns.indexOf(column))
  }

  def getColumnNames = columns.map(_.getName).toList
}

package kanban

import scalafx.scene.paint.Color

import scala.collection.mutable.Buffer


class Board(private var name: String) {
  private val columns = Buffer[Column]()
  this.addColumn("Column1", Color.OrangeRed)
  this.addColumn("Test2", Color.SeaGreen)

  def getColumns = this.columns
  def getName = name

  def rename(newName: String) = name = newName

  def addColumn(name: String = "untitled", color: Color = Color.Blue) = {
    val column = new Column(name, color)
    columns += column
  }
}
package kanban

import scalafx.scene.paint.Color
import java.io.File
import scala.collection.mutable.Buffer


/** Models a kanban board. A board has lists that have cards inside them.
 * Boards can also have background images and each of them has their own archive.
 *
 * @param name    name of the board
 * @param color   background color (used if no image)
 * @param bgImage background image
 * @param columns columns that the board has
 * @param archive the board's archive
 */
class Board(private var name: String = "",
            private var color: Color = Color.White,
            private var bgImage: Option[File] = None,
            private val columns: Buffer[Column] = Buffer[Column](),
            private val archive: Column = new Column("Archive")) {

  // Simple get-methods for getting information about the Board
  def getName: String = name

  def getColor: Color = color

  def getBgImage: Option[File] = bgImage

  def getColumns: Buffer[Column] = columns

  def getArchive: Column = archive

  def getColumnNames: List[String] = columns.map(_.getName).toList

  def getColumn(name: String): Column = columns(getColumnNames.indexOf(name)) // used by the archive to select correct column based on name

  // Simple set-methods for setting board variables
  def setName(newName: String): Unit = name = newName

  def setBgImage(newBg: Option[File]): Unit = bgImage = newBg

  def setColor(newColor: Color): Unit = color = newColor

  /** Moves a column from one position to another
   *
   * @param column column to be moved
   * @param index  target location index */
  def moveColumn(column: Column, index: Int): Unit = {
    columns -= column
    columns.insert(index, column)
  }

  /** Moves a column from one position to another
   *
   * @param card         card to be moved
   * @param startColumn  original column of the card
   * @param targetColumn column the card will be moved
   * @param index        target column location index */
  def moveCard(card: Card, startColumn: Column, targetColumn: Column, index: Int): Unit = {
    startColumn.deleteCard(card)
    targetColumn.addCard(card, index)
  }

  /** Creates a new column and adds it to the board columns
   *
   * @param name  name of the column to be created
   * @param color border color of the column */
  def addColumn(name: String = "untitled", color: Color = Color.Black): Unit = columns += new Column(name, color)

  /** Creates a new column and adds it to the board columns
   *
   * @param column column to be deleted */
  def deleteColumn(column: Column): Unit = columns -= column
}

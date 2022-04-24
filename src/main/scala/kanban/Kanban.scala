package kanban

import scala.collection.mutable.Buffer
import scalafx.scene.paint.Color
import java.io.File


/** Models the main Kanban app. Keeps track of all the kanban boards and tags used in the session.
 *
 * @param boards    boards of the Kanban app
 * @param tags      tags created and used in the app
 * @param templates premade and/or user made templates that cards cards can be created using
 */
class Kanban(private val boards: Buffer[Board] = Buffer[Board](),
             private val tags: Buffer[String] = Buffer[String](),
             private val templates: Buffer[Card] = Buffer(Template1, Template2)) {

  // Simple get-methods for getting data about the Kanban session
  def getBoards: Buffer[Board] = boards

  def getBoardNames: List[String] = boards.map(_.getName).toList // used for switching between boards based on selected name

  def getBoard(name: String): Board = boards(getBoardNames.indexOf(name)) // when selecting a board

  def getTags: Buffer[String] = tags

  def getTemplates: Buffer[Card] = templates

  /** Returns all cards including cards from boards and their arhives and templates
   *
   * @return All possible cards used in the session */
  def getAllCards: List[Card] = {
    val boardCards = boards.flatMap(_.getColumns.flatMap(_.getCards)).toList
    val archiveCards = boards.map(_.getArchive).flatMap(_.getCards).toList
    boardCards ++ archiveCards ++ templates
  }

  /** Adds a card to the list of available templates
   *
   * @param card new card template */
  def addTemplate(card: Card): Unit = templates += card

  /** Deletes a card from the list of available templates
   *
   * @param card template to be deleted */
  def removeTemplate(card: Card): Unit = templates -= card

  /** Creates a new board and adds it to the Kanban boards
   *
   * @param name    name of the board
   * @param color   background color of the board
   * @param bgImage possible background image for the board */
  def createBoard(name: String = "untitled", color: Color = Color.White, bgImage: Option[File] = None): Unit = {
    boards += new Board(name, color, bgImage)
  }

  /** Deleted a board from Kanban boards
   *
   * @param board board to be deleted */
  def deleteBoard(board: Board): Unit = boards -= board

  /** Adds a new tag to Kanban tags
   *
   * @param tag tag to be added */
  def addTag(tag: String): Unit = tags += tag

  /** Removes a tag from both the Kanban session and all cards that have it
   *
   * @param tag tag to be deleted */
  def removeTag(tag: String): Unit = {
    getAllCards.foreach(_.removeTag(tag)) // remove tag from all cards that have it
    tags -= tag // and remove it from sesion tags
  }

  /** Checks all cards that have files that the files can still be read
   * and have not been moved or deleted */
  def checkFiles(): Unit = {
    for (card <- getAllCards.filter(_.getFile.isDefined)) {
      card.getFile match {
        case Some(file) => {
          if (!file.canRead) {
            card.resetFile()
          }
        }
        case None =>
      }
    }
  }
}

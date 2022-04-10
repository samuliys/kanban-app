package kanban

import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.paint.Color

import java.io.File
import scala.collection.mutable.Buffer

class Kanban(private var name: String = "untitled",
             private val boards: Buffer[Board] = Buffer[Board](),
             private val tags: Buffer[String] = Buffer[String](),
             private val templates: Buffer[Card] = Buffer(Template1)) {

  private var stage = new PrimaryStage

  def getStage = stage
  def setStage(newStage: PrimaryStage) = stage = newStage

  def rename(newName: String) = name = newName

  def getName = name

  def getBoards = boards

  def getBoardNames = boards.map(_.getName).toList

  def getTags = tags

  def getTemplates = templates

  def addTemplate(card: Card) = templates += card

  def removeTemplate(card: Card) = templates.remove(templates.indexOf(card))

  def createBoard(name: String = "untitled", color: Color = Color.White, bgImage: Option[File] = None): Board = {
    val board = new Board(name, color, bgImage)
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

  def getAllCards = boards.flatMap(_.getColumns.flatMap(_.getCards))

  def checkFiles() = {
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

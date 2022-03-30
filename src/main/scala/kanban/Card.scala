package kanban

import scalafx.scene.paint.Color

import java.io.File
import scala.collection.mutable.Buffer

class Card(private var text: String,
           private var color: Color,
           private var tags: Buffer[String] = Buffer[String](),
           private var checklist: Checklist = new Checklist,
           private var deadline: Option[Deadline] = None,
           private var file: Option[File] = None) {

  def getText = text

  def getColor = color

  def getTags = tags

  def getChecklist = checklist

  def addTag(tag: String) = tags += tag

  def removeTag(tag: String) = {
    if (tags.contains(tag)) {
      tags.remove(tags.indexOf(tag))
    }
  }

  def editCard(newText: String, newColor: Color, newTags: Buffer[String], newChecklist: Checklist, newDeadline: Option[Deadline], newFile: Option[File]) = {
    text = newText
    color = newColor
    tags = newTags
    deadline = newDeadline
    file = newFile
    checklist = newChecklist
  }

  def hasDeadline = deadline.isDefined

  def getDeadline = deadline

  def getFile = file

  def resetFile() = file = None
}

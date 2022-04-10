package kanban

import kanban.Main.stage

import scala.collection.mutable.Buffer
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter
import scalafx.scene.paint.Color

import java.io.{File, FileWriter, PrintWriter}
import java.time.LocalDate
import scala.io.Source
import scala.util.Using
import io.circe._
import io.circe.parser._
import io.circe.syntax._
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType


class FileHandler {

  implicit val encodeKanban: Encoder[Kanban] = (a: Kanban) => Json.obj(
    ("name", a.getName.asJson),
    ("boards", a.getBoards.asJson),
    ("tags", a.getTags.asJson),
    ("templates", a.getTemplates.asJson)
  )

  implicit val decodeKanban: Decoder[Kanban] = (c: HCursor) => for {
    name <- c.downField("name").as[String]
    boards <- c.downField("boards").as[Buffer[Board]]
    tags <- c.downField("tags").as[Buffer[String]]
    templates <- c.downField("templates").as[Buffer[Card]]
  } yield {
    new Kanban(name, boards, tags, templates)
  }

  implicit val encodeFile: Encoder[File] = (a: File) => Json.obj(
    ("file", a.getAbsolutePath.asJson),
  )

  implicit val decodeFile: Decoder[File] = (c: HCursor) => for {
    path <- c.downField("file").as[String]
  } yield {
    new File(path)
  }

  implicit val encodeBoard: Encoder[Board] = (a: Board) => Json.obj(
    ("name", a.getName.asJson),
    ("color", a.getColor.asJson),
    ("image", a.getBgImage.asJson),
    ("columns", a.getColumns.asJson),
    ("archive", a.getArchive.asJson)
  )

  implicit val decodeBoard: Decoder[Board] = (c: HCursor) => for {
    name <- c.downField("name").as[String]
    color <- c.downField("color").as[Color]
    image <- c.downField("image").as[Option[File]]
    columns <- c.downField("columns").as[Buffer[Column]]
    archive <- c.downField("archive").as[Column]
  } yield {
    new Board(name, color, image, columns, archive)
  }

  implicit val encodeColumn: Encoder[Column] = (a: Column) => Json.obj(
    ("name", a.getName.asJson),
    ("color", a.getColor.asJson),
    ("cards", a.getCards.asJson)
  )

  implicit val decodeColumn: Decoder[Column] = (c: HCursor) => for {
    name <- c.downField("name").as[String]
    color <- c.downField("color").as[Color]
    cards <- c.downField("cards").as[Buffer[Card]]
  } yield {
    new Column(name, color, cards)
  }

  implicit val encodeDeadline: Encoder[Deadline] = (a: Deadline) => Json.obj(
    ("date", a.getRawDate.asJson),
    ("status", a.getStatus.asJson)
  )

  implicit val decodeDeadline: Decoder[Deadline] = (c: HCursor) => for {
    deadline <- c.downField("date").as[String]
    status <- c.downField("status").as[Boolean]
  } yield {
    new Deadline(LocalDate.parse(deadline), status)
  }

  implicit val encodeColor: Encoder[Color] = (a: Color) => Json.obj(
    ("R", a.red.asJson),
    ("G", a.green.asJson),
    ("B", a.blue.asJson)
  )

  implicit val decodeColor: Decoder[Color] = (c: HCursor) => for {
    red <- c.downField("R").as[Double]
    green <- c.downField("G").as[Double]
    blue <- c.downField("B").as[Double]
  } yield {
    Color.color(red, green, blue)
  }

  implicit val encodeChecklist: Encoder[Checklist] = (a: Checklist) => Json.obj(
    ("tasks", a.getTasks.asJson)
  )

  implicit val decodeChecklist: Decoder[Checklist] = (c: HCursor) => for {
    tasks <- c.downField("tasks").as[Buffer[(Boolean, String)]]
  } yield {
    new Checklist(tasks)
  }

  implicit val encodeSubCard: Encoder[SubCard] = (a: SubCard) => Json.obj(
    ("card", a.getOriginal.asJson)
  )
  implicit val decodeSubCard: Decoder[SubCard] = (c: HCursor) => for {
    card <- c.downField("card").as[Card]
  } yield {
    new SubCard(card)
  }

  implicit val encodeCard: Encoder[Card] = (a: Card) => Json.obj(
    ("text", a.getText.asJson),
    ("color", a.getColor.asJson),
    ("tags", a.getTags.asJson),
    ("checklist", a.getChecklist.asJson),
    ("deadline", a.getDeadline.asJson),
    ("file", a.getFile.asJson),
    ("subcard", a.getSubcard.asJson)
  )

  implicit val decodeCard: Decoder[Card] = (c: HCursor) => for {
    text <- c.downField("text").as[String]
    color <- c.downField("color").as[Color]
    tags <- c.downField("tags").as[Buffer[String]]
    checklist <- c.downField("checklist").as[Checklist]
    deadline <- c.downField("deadline").as[Option[Deadline]]
    file <- c.downField("file").as[Option[File]]
    subcard <- c.downField("subcard").as[Option[SubCard]]
  } yield {
    new Card(text, color, tags, checklist, deadline, file, subcard)
  }

  def save(kanbanapp: Kanban): Boolean = {
    val fileChooser = new FileChooser {
      extensionFilters.add(new ExtensionFilter("JSON Files (*.json)", "*.json"))
    }
    val selectedFile = fileChooser.showSaveDialog(stage)
    if (selectedFile != null) {
      val kanbanJson = kanbanapp.asJson.noSpaces
      val fileWriter = new FileWriter(selectedFile)
      val printWriter = new PrintWriter(fileWriter)
      val save = Using(printWriter) {
        writer => writer.println(kanbanJson)
      }
      true
    } else {
      false
    }
  }

  def load(oldKanban: Kanban): Option[Kanban] = {
    val fileChooser = new FileChooser {
      extensionFilters.add(new ExtensionFilter("JSON Files (*.json)", "*.json"))
    }
    val selectedFile = fileChooser.showOpenDialog(stage)
    if (selectedFile != null) {
      val sourceData = Source.fromFile(selectedFile)
      val kanbanFromFile = Using(sourceData) {
        source => decode[Kanban](source.getLines().mkString(""))
      }.toEither.flatten
      val result = kanbanFromFile.getOrElse(oldKanban)
      if (result == oldKanban) {
        new Alert(AlertType.Warning, "Selected File has incorrect json format").showAndWait()
      }
      Some(result)
    } else {
      None
    }
  }
}

package kanban

import kanban.Main.stage
import scala.collection.mutable.Buffer
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter
import scalafx.scene.paint.Color
import java.io.{FileWriter, PrintWriter}
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
    ("tags", a.getTags.asJson)
  )

  implicit val decodeKanban: Decoder[Kanban] = (c: HCursor) => for {
    name <- c.downField("name").as[String]
    boards <- c.downField("boards").as[Buffer[Board]]
    tags <- c.downField("tags").as[Buffer[String]]
  } yield {
    new Kanban(name, boards, tags)
  }

  implicit val encodeBoard: Encoder[Board] = (a: Board) => Json.obj(
    ("name", a.getName.asJson),
    ("columns", a.getColumns.asJson),
    ("archive", a.getArchive.asJson)
  )

  implicit val decodeBoard: Decoder[Board] = (c: HCursor) => for {
    name <- c.downField("name").as[String]
    columns <- c.downField("columns").as[Buffer[Column]]
    archive <- c.downField("archive").as[Column]
  } yield {
    new Board(name, columns, archive)
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
    ("date", a.getRawDate.asJson)
  )

  implicit val decodeDeadline: Decoder[Deadline] = (c: HCursor) => for {
    deadline <- c.downField("date").as[String]
  } yield {
    new Deadline(LocalDate.parse(deadline))
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

  implicit val encodeCard: Encoder[Card] = (a: Card) => Json.obj(
    ("text", a.getText.asJson),
    ("color", a.getColor.asJson),
    ("tags", a.getTags.asJson),
    ("deadline", a.getDeadline.asJson)
  )

  implicit val decodeCard: Decoder[Card] = (c: HCursor) => for {
    text <- c.downField("text").as[String]
    color <- c.downField("color").as[Color]
    tags <- c.downField("tags").as[Buffer[String]]
    deadline <- c.downField("deadline").as[Option[Deadline]]
  } yield {
    new Card(text, color, tags, deadline)
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

package kanban

import scala.collection.mutable.Buffer
import scalafx.scene.paint.Color
import java.time.LocalDate
import io.circe._
import io.circe.syntax._


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
    ???
  }

  def load(oldKanban: Kanban): Option[Kanban] = {
    ???
  }
}

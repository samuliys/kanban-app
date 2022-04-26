package kanban

import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter
import scalafx.scene.paint.Color
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import scala.io.Source
import scala.collection.mutable.Buffer
import scala.util.{Failure, Success, Try, Using}
import java.time.LocalDate
import java.io.{File, FileWriter, PrintWriter}
import io.circe._
import io.circe.parser._
import io.circe.syntax._


/** File handler class that takes care of saving a Kanban instance to file and loading from file. */
class FileHandler {

  // Manually created json encoders and decoders for all classes that will be saved to file

  implicit val encodeKanban: Encoder[Kanban] = (a: Kanban) => Json.obj(
    ("boards", a.getBoards.asJson),
    ("tags", a.getTags.asJson),
    ("templates", a.getTemplates.asJson)
  )

  implicit val decodeKanban: Decoder[Kanban] = (c: HCursor) => for {
    boards <- c.downField("boards").as[Buffer[Board]]
    tags <- c.downField("tags").as[Buffer[String]]
    templates <- c.downField("templates").as[Buffer[Card]]
  } yield {
    new Kanban(boards, tags, templates)
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
    ("card", a.getCard.asJson)
  )
  implicit val decodeSubCard: Decoder[SubCard] = (c: HCursor) => for {
    card <- c.downField("card").as[Card]
  } yield {
    new SubCard(card)
  }

  implicit val encodeCard: Encoder[Card] = (a: Card) => Json.obj(
    ("text", a.getText.asJson),
    ("textColor", a.getTextColor.asJson),
    ("borderColor", a.getBorderColor.asJson),
    ("tags", a.getTags.asJson),
    ("checklist", a.getChecklist.asJson),
    ("deadline", a.getDeadline.asJson),
    ("file", a.getFile.asJson),
    ("subcard", a.getSubcard.asJson),
    ("url", a.getUrl.asJson)
  )

  implicit val decodeCard: Decoder[Card] = (c: HCursor) => for {
    text <- c.downField("text").as[String]
    textColor <- c.downField("textColor").as[Color]
    borderColor <- c.downField("borderColor").as[Color]
    tags <- c.downField("tags").as[Buffer[String]]
    checklist <- c.downField("checklist").as[Checklist]
    deadline <- c.downField("deadline").as[Option[Deadline]]
    file <- c.downField("file").as[Option[File]]
    subcard <- c.downField("subcard").as[Option[SubCard]]
    url <- c.downField("url").as[Option[String]]
  } yield {
    new Card(text, textColor, borderColor, tags, checklist, deadline, file, subcard, url)
  }

  /** Saves a kanban session to file using json
   *
   * @param kanbanapp instance of Kanban class that will be saved to file
   */
  def save(kanbanapp: Kanban): Unit = {
    val fileChooser = new FileChooser {
      extensionFilters.add(new ExtensionFilter("JSON Files (*.json)", "*.json")) // only json files will be created
    }
    var selectedFile = fileChooser.showSaveDialog(Main.getStage)
    if (selectedFile != null) { // make sure a save file was selected before using it
      val save = Try {
        // on some devices .json will not be automatically added to the file name despite extension filter
        if (!selectedFile.getName.endsWith(".json")) {
          selectedFile = new File(selectedFile.getAbsolutePath + ".json") // in those cases, add it manually
        }
        val kanbanJson = kanbanapp.asJson.noSpaces // encode entire kanban session
        val fileWriter = new FileWriter(selectedFile)
        val printWriter = new PrintWriter(fileWriter)
        Using(printWriter) {
          writer => writer.println(kanbanJson) // write to file
        }
      }
      save match { // display error message if something went wrong while saving
        case Failure(exception) => new Alert(AlertType.Warning, "There Was an Error Saving to File").showAndWait()
        case Success(value) => {
          val alert = new Alert(AlertType.Information, "File Saved Succesfully") {
            title = "Save to File"
            headerText = "File Saved Succesfully"
          }
          alert.showAndWait()
        }
      }
    }
  }

  /** Loads and decodes a json save file and returns the new kanban session wrapped in option
   *
   * @param oldKanban current kanban session, will be reverted back to if file decode fails
   * @return None and false if no file selected or error reading file,
   *         or instance of Kanban class wrapped in an option;
   *         new kanban from file and true or old if decode fails and false */
  def load(oldKanban: Kanban): (Option[Kanban], Boolean) = {
    val fileChooser = new FileChooser {
      extensionFilters.add(new ExtensionFilter("JSON Files (*.json)", "*.json")) // allow only selecting json files
    }

    val selectedFile = fileChooser.showOpenDialog(Main.getStage)
    if (selectedFile != null) { // make sure a file was selected
      val sourceData = Try { // handle case where file can't be read
        Source.fromFile(selectedFile)
      }
      sourceData match {
        case Failure(exception) => { // show error message if fails
          new Alert(AlertType.Warning, "There Was an Error Reading the File.").showAndWait()
          (None, false)
        }
        case Success(data) => {
          var success = true
          val kanbanFromFile = Using(data) { // read the file and handle possible exceptions
            source => decode[Kanban](source.getLines().mkString(""))
          }.toEither.flatten
          val result = kanbanFromFile.getOrElse(oldKanban) // get either the new one or if failed, revert back to old
          if (result == oldKanban) { // if file could not be decoded, show user error message alert
            success = false
            new Alert(AlertType.Warning, "Selected File Uses an Incorrect JSON Format.\nPlease Select Another File.").showAndWait()
          }
          (Some(result), success)
        }
      }
    } else {
      (None, false)
    }
  }
}

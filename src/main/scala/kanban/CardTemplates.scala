package kanban

import scalafx.scene.paint.Color

import java.time.LocalDate
import scala.collection.mutable.Buffer

object Template1 extends Card("Joku otsikko", Color.LightBlue, Buffer[String](), new Checklist, Some(new Deadline(LocalDate.of(2022, 4, 27)))) {
  getChecklist.addTask("Do Something")
  getChecklist.addTask("Do Smthng Else")
}

object Template2 extends Card {

}
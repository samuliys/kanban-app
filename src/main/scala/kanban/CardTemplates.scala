package kanban

import scalafx.scene.paint.Color

import java.time.LocalDate
import scala.collection.mutable.Buffer

// Premade templates for showcase, the user can create their own
object Template1 extends Card("Example 1", Color.Black, Color.LightBlue, Buffer[String](), new Checklist, Some(new Deadline(LocalDate.of(2022, 4, 27)))) {
  getChecklist.addTask("Create a new list")
  getChecklist.addTask("Add new cards")
  getChecklist.addTask("Create a new board")
}

object Template2 extends Card("Example 2", Color.Red, Color.Orange, Buffer[String](), new Checklist, Some(new Deadline(LocalDate.now())), None, None, Some("https://www.aalto.fi/fi"))

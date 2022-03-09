package kanban

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.scene.layout.Pane
import scalafx.scene.control.Button


object Main extends JFXApp {
  stage = new JFXApp.PrimaryStage {
    title.value = "KanbanApp"
    width = 600
    height = 450
  }

  val root = new Pane

  val button = new Button("I'm a button!")

  button.onAction = (event) => {
      println("Kanban!")
  }

  root.children += button
  val scene = new Scene(root)
  stage.scene = scene
}
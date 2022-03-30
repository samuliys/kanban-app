package kanban

import kanban.Main.stage
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.scene.paint.Color
import scalafx.stage.FileChooser

import java.awt.Desktop
import java.io.File
import java.time.LocalDate
import scala.collection.mutable.Buffer

object CardDialog {

  def showDialog() = dialog.showAndWait()

  private var kanbanapp = new Kanban
  private var selectedColumn = new Column("", Color.Black)
  private var selectedCard = new Card("", Color.Black, Buffer[String](), new Checklist, None, None)
  private var newCard = false
  private var selectedFile: Option[File] = None
  private var checklist = new Checklist
  private var tasks = Buffer[(Boolean, String)]()

  private val dialog = new Dialog[Card]() {
    initOwner(stage)
    title = "Kanban - New Card"
    headerText = "Add New Card"
  }

  private val cardTags = Buffer[String]()

  private val okButtonType = new ButtonType("OK", ButtonData.OKDone)
  dialog.dialogPane().buttonTypes = Seq(okButtonType, ButtonType.Cancel)

  private val cardText = new TextField {
    promptText = "Card Text"
  }
  private val cardColor = new ColorPicker(Color.Black) {
    promptText = "Color"
    minHeight = 25
  }

  private def drawRemoveTagMenuItems: Buffer[MenuItem] = {
    val items = Buffer[MenuItem]()
    for (tag <- cardTags) {
      items += new MenuItem(tag) {
        onAction = (event) => {
          cardTags.remove(cardTags.indexOf(tag))
          resetTagEdit()
        }
      }
    }
    items
  }

  private val drawRemoveTag = new MenuButton("Select Tag to Remove") {
    items = drawRemoveTagMenuItems
  }

  private def drawAddTagMenuItems: Buffer[MenuItem] = {
    val items = Buffer[MenuItem]()
    for (tag <- kanbanapp.getTags.filterNot(cardTags.contains(_))) {
      items += new MenuItem(tag) {
        onAction = (event) => {
          println(tag)
          cardTags += tag
          resetTagEdit()
        }
      }
    }
    items
  }

  private val drawAddTag = new MenuButton("Select Tag to Add") {
    items = drawAddTagMenuItems
  }

  private val drawCurrentTags = new Label {
    if (cardTags.isEmpty) {
      text = "No tags. Add tag below."
    } else {
      text = cardTags.mkString(", ")
    }

  }

  private val drawDatePicker = new DatePicker(LocalDate.now)

  private val deadlineCheckbox = new CheckBox("Include Deadline") {
    minWidth = 200
    onAction = (event) => checkCheckbox()
  }

  private val taskText = new TextField {
    promptText = "Task"
  }

  private val addTaskButton = new Button("Add Task") {
    onAction = (event) => {
      checklist.addTask(taskText.text())
      taskText.text = ""
      errorLabel.text = ""
      taskRemoveMenu.items = ObservableBuffer(getTaskList)
    }
  }

  private def getTaskList = checklist.getTasksNames.toList

  //private def getTaskList = tasks.map(_._2).toList

  private val taskRemoveMenu: ComboBox[String] = new ComboBox(getTaskList) {
    promptText = "Select Task to Remove"
    onAction = (event) => {
      deleteTaskButton.disable = false
    }
  }

  private val deleteTaskButton = new Button("Delete Task") {
    onAction = (event) => {
      checklist.removeTask(checklist.getTasksNames.indexOf(taskRemoveMenu.value()))
      taskRemoveMenu.items = ObservableBuffer(getTaskList)
      taskRemoveMenu.promptText = "Select Task to Remove"
      disable = true
      if (!getTaskList.contains(taskText.text())) {
        errorLabel.text = ""
        addTaskButton.disable = false
      }
    }
  }

  private val errorLabel = new Label {
    textFill = Color.Red
  }

  taskText.text.onChange { (_, _, newValue) =>
    if (newValue == "") {
      errorLabel.text = "Tag name can't be empty"
      addTaskButton.disable = true
    } else if (newValue.length > 10) {
      errorLabel.text = "Tag name too long"
      addTaskButton.disable = true
    } else if (getTaskList.contains(newValue)) {
      errorLabel.text = "Tag \"" + taskText.text() + "\" already exits"
      addTaskButton.disable = true
    } else {
      errorLabel.text = ""
      addTaskButton.disable = false
    }
  }

  private val fileButton = new Button("Choose File") {
    onAction = (event) => {
      val fileChooser = new FileChooser
      val chooseFile = fileChooser.showOpenDialog(stage)

      if (chooseFile != null) {
        selectedFile = Some(chooseFile)
        fileLabel.text = "Chosen file: " + chooseFile.getName
        openFile.disable = false
        removeFileButton.disable = false
      } else {
        removeFileButton.disable = true
      }
    }
  }

  private val removeFileButton = new Button("Remove File") {
    onAction = (event) => {
      selectedFile match {
        case Some(file) => {
          openFile.disable = true
          selectedFile = None
          fileLabel.text = "No Selected File"
          disableRemoveBtn()
        }
        case None =>
      }
    }
  }

  private def disableRemoveBtn(): Unit = removeFileButton.disable = true


  private val openFile = new Button("Open File") {
    onAction = (event) => {
      selectedFile match {
        case Some(file) => Desktop.getDesktop.open(file)
        case None =>
      }
    }
  }

  private val fileLabel = new Label("Chosen File")

  private val chooseCardButton = new Button("Choose Card") {

  }

  private val removeCardButton = new Button("Remove Card") {

  }

  private val cardView = new Label("[Card displayed here]")

  private def checkCheckbox(): Unit = {
    drawDatePicker.disable = !deadlineCheckbox.selected()
  }

  private def drawContents: VBox = new VBox(10) {
    minWidth = 500
    minHeight = 400
    children += new HBox(10) {
      children += new Label("Text:")
      children += cardText
    }
    children += new HBox(10) {
      children += new Label("Color:")
      children += cardColor
    }
    children += new Separator
    children += new HBox(10) {
      children += new Label("Deadline:")
      children += deadlineCheckbox
      children += drawDatePicker
    }
    children += new Separator
    children += new HBox(10) {
      children += new Label("New Task:")
      children += taskText
      children += addTaskButton
      children += errorLabel
    }
    children += new HBox(5) {
      children += new Label("Remove Task:")
      children += taskRemoveMenu
      children += deleteTaskButton
    }
    children += new Separator
    children += new HBox(10) {
      children += new Label("File Attachment: ")
      children += fileButton
      children += openFile
      children += removeFileButton
    }
    children += fileLabel
    children += new Separator
    children += new HBox(10) {
      children += new Label("Card Attachment: ")
      children += chooseCardButton
      children += removeCardButton
      children += cardView
    }

    children += new Separator
    children += new HBox(10) {
      children += new Label("Tags: ")
      children += drawCurrentTags
    }
    children += new HBox(10) {
      children += new Label("Edit tags: ")
      children += drawAddTag
      children += drawRemoveTag
      children += new Button("Manage Tags") {
        onAction = (event) => {
          TagDialog.reset(kanbanapp)
          TagDialog.showDialog()
          if (newCard) {
            val correctTags = cardTags.filter(kanbanapp.getTags.contains(_))
            cardTags.clear()
            correctTags.foreach(cardTags.append(_))
          } else {
            cardTags.clear()
            selectedCard.getTags.foreach(cardTags.append(_))
          }
          resetTagEdit()
        }
      }
    }
  }

  private val okButton = dialog.dialogPane().lookupButton(okButtonType)
  okButton.disable = true

  cardText.text.onChange { (_, _, newValue) =>
    okButton.disable = newValue.trim().isEmpty
  }

  dialog.dialogPane().content = drawContents

  Platform.runLater(cardText.requestFocus())

  def reset(kanban: Kanban, column: Column, card: Card, isNew: Boolean) = {
    kanbanapp = kanban
    selectedColumn = column
    selectedCard = card
    newCard = isNew
    checklist = new Checklist
    checklist.resetTasks()


    cardTags.clear()

    if (isNew) {
      dialog.title = "Kanban - New Card"
      dialog.headerText = "Add New Card"
      cardText.text = ""
      cardColor.value = Color.Black
      selectedFile = None
    } else {
      dialog.title = "Kanban - Card Edit"
      dialog.headerText = "Edit Card"
      cardText.text = card.getText
      cardColor.value = card.getColor
      card.getTags.foreach(cardTags.append(_))
      selectedFile = card.getFile
      tasks = card.getChecklist.getTasks
      checklist.setTasks(card.getChecklist.getTasks)
    }
    taskRemoveMenu.items = ObservableBuffer(getTaskList)

    selectedFile match {
      case Some(file) => {
        openFile.disable = false
        removeFileButton.disable = false
        fileLabel.text = "Selected File: " + file.getName
      }
      case None => {
        openFile.disable = true
        removeFileButton.disable = true
        fileLabel.text = "No Selected File"
      }
    }

    card.getDeadline match {
      case Some(deadline) => {
        drawDatePicker.disable = false
        drawDatePicker.value = deadline.getRawDate
        deadlineCheckbox.selected = true
      }
      case None => {
        drawDatePicker.disable = true
        drawDatePicker.value = LocalDate.now()
        deadlineCheckbox.selected = false
      }
    }
    resetTagEdit()
    resetTaskEdit()
  }


  private def resetTagEdit() = {
    drawAddTag.items = drawAddTagMenuItems
    drawRemoveTag.items = drawRemoveTagMenuItems
    if (cardTags.isEmpty) {
      drawCurrentTags.text = "No tags"
    } else {
      drawCurrentTags.text = cardTags.mkString(", ")
    }
  }

  private def resetTaskEdit() = {
    drawAddTag.items = drawAddTagMenuItems
    drawRemoveTag.items = drawRemoveTagMenuItems
    if (cardTags.isEmpty) {
      drawCurrentTags.text = "No tags"
    } else {
      drawCurrentTags.text = cardTags.mkString(", ")
    }
  }

  private def getDeadline = {
    if (deadlineCheckbox.selected()) {
      Some(new Deadline(drawDatePicker.value()))
    } else {
      None
    }
  }

  private def update() = dialog.dialogPane().content = drawContents

  dialog.resultConverter = dialogButton => {
    if (dialogButton == okButtonType) {
      if (newCard) {
        selectedColumn.addCard(cardText.text(), cardColor.getValue, cardTags, checklist, getDeadline)
      } else {
        selectedCard.editCard(cardText.text(), cardColor.getValue, cardTags, checklist, getDeadline, None)
      }
    }
    selectedCard
  }
}

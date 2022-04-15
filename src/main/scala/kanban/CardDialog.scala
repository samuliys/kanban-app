package kanban

import kanban.Main.stage
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.geometry.Pos._
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.scene.paint.Color
import scalafx.stage.FileChooser
import settings._

import java.awt.Desktop
import java.io.File
import java.net.{URI, URL}
import java.time.LocalDate
import scala.collection.mutable.Buffer

object CardDialog {

  def showDialog() = dialog.showAndWait()

  private var kanbanapp = new Kanban
  private var selectedBoard = new Board
  private var selectedColumn = new Column
  private var selectedCard = new Card
  private var newCard = false
  private var template = false
  private var selectedFile: Option[File] = None
  private var selectedUrl: Option[String] = None
  private var selectedSubcard: Option[SubCard] = None
  private var checklist = new Checklist
  private var tasks = Buffer[(Boolean, String)]()

  private val dialog = new Dialog[Card] {
    initOwner(stage)
  }

  private val headlineWidth = 90
  private val attachmentButtonWidth = 90
  private val removeButtonWidth = 70
  private val openButtonWidth = 50

  private var cardTags = Buffer[String]()

  private val okButtonType = new ButtonType("OK", ButtonData.OKDone)

  dialog.dialogPane().buttonTypes = Seq(okButtonType, ButtonType.Cancel)

  private val cardText = new TextField {
    promptText = "Card Text"
    minWidth = 420
  }

  private val borderColor = new ColorPicker(Color.Black) {
    promptText = "Border Color"
    minHeight = 25

  }

  private val textColor = new ColorPicker(Color.Black) {
    promptText = "Text Color"
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

  private val deadlineCheckbox = new CheckBox("Include") {
    minWidth = 70 // width so that whole text is visible
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
      errorLabel.text = "Task name can't be empty"
      addTaskButton.disable = true
    } else if (newValue.length > 10) {
      errorLabel.text = "Task name too long"
      addTaskButton.disable = true
    } else if (getTaskList.contains(newValue)) {
      errorLabel.text = "Task \"" + taskText.text() + "\" already exits"
      addTaskButton.disable = true
    } else {
      errorLabel.text = ""
      addTaskButton.disable = false
    }
  }

  private val fileButton = new Button("Choose File") {
    minWidth = attachmentButtonWidth
    onAction = (event) => {
      val fileChooser = new FileChooser
      val chooseFile = fileChooser.showOpenDialog(stage)

      if (chooseFile != null) {
        selectedFile = Some(chooseFile)
        fileLabel.text = "File: " + chooseFile.getName
        openFile.disable = false
        removeFileButton.disable = false
      } else {
        removeFileButton.disable = true
        openFile.disable = true
        fileLabel.text = "No Selected File"
      }
    }
  }

  private val removeFileButton = new Button("Remove") {
    minWidth = removeButtonWidth
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

  private val openFile = new Button("Open") {
    minWidth = openButtonWidth
    onAction = (event) => {
      selectedFile match {
        case Some(file) => {
          if (file.canRead && Desktop.isDesktopSupported) {
            Desktop.getDesktop.open(file)
          }
        }
        case None =>
      }
    }
  }

  private val fileLabel = new Label("Chosen File")

  private val chooseCardButton = new Button("Choose Card") {
    minWidth = attachmentButtonWidth
    onAction = (event) => {
      CardListDialog.reset(kanbanapp, selectedBoard, selectedColumn, selectedCard, 3)
      val result = CardListDialog.showDialog()
      result match {
        case Some(Result(card)) => {
          selectedSubcard = Some(card.toSub)
          viewCardButton.disable = false
          cardView.text = "Card Selected"
          removeCardButton.disable = false
        }
        case None => {
          viewCardButton.disable = true
          cardView.text = "No Selected Card"
        }
      }
    }
  }

  private val removeCardButton = new Button("Remove") {
    minWidth = removeButtonWidth
    onAction = (event) => {
      selectedSubcard = None
      viewCardButton.disable = true
      cardView.text = "No Selected Card"
      disableRemoveCard()
    }
  }

  private def disableRemoveCard(): Unit = removeCardButton.disable = true

  private val viewCardButton = new Button("View") {
    minWidth = openButtonWidth
    onAction = (event) => {
      selectedSubcard match {
        case Some(card) => {
          CardViewDialog.reset(card.getOriginal)
          CardViewDialog.showDialog()
        }
        case None =>
      }
    }
  }

  private val cardView = new Label("No Subcard Selected")

  private val enterUrlButton = new Button("Enter URL") {
    minWidth = attachmentButtonWidth
    onAction = (event) => {
      val dialog = new TextInputDialog() {
        initOwner(stage)
        title = "Enter URL"
        headerText = "Card URL"
        contentText = "Enter URL:"
      }
      val result = dialog.showAndWait()
      result match {
        case Some(enteredUrl) =>
          val joo = new URL(enteredUrl)
          selectedUrl = Some(enteredUrl)
          removeUrlButton.disable = false
          openUrlButton.disable = false
          urlLabel.text = "URL: " + enteredUrl
        case None =>
      }
    }
  }

  private val removeUrlButton: Button = new Button("Remove") {
    minWidth = removeButtonWidth
    onAction = (event) => {
      selectedUrl = None
      openUrlButton.disable = true
      urlLabel.text = "No URL Set"
      disableRemoveUrlButton()
    }
  }

  def disableRemoveUrlButton() = removeUrlButton.disable = true

  private val openUrlButton = new Button("Open") {
    minWidth = openButtonWidth
    onAction = (event) => {
      selectedUrl match {
        case Some(url) => {

          if (Desktop.isDesktopSupported) {
            Desktop.getDesktop.browse(new URI(url))
          }
        }
        case None =>
      }

    }
  }

  private val urlLabel = new Label("URL: ")

  private val toTemplateButton = new Button("Make into a template") {
    onAction = (event) => {
      kanbanapp.addTemplate(selectedCard)
      new Alert(AlertType.Information) {
        initOwner(stage)
        title = "Template"
        headerText = "Template Created Succesfully"
        //contentText = ""
      }.showAndWait()
    }
  }

  private val manageTagsButton = new Button("Manage Tags") {
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

  private def checkCheckbox(): Unit = {
    drawDatePicker.disable = !deadlineCheckbox.selected()
  }

  private val resetBorderColorButton = new Button("Reset to Default") {
    onAction = (event) => {
      borderColor.value = DefaultColor
    }
  }

  private val resetTextColorButton = new Button("Reset to Default") {
    onAction = (event) => {
      textColor.value = DefaultColor
    }
  }

  private val resetAllButton = new Button("Reset All") {
    onAction = (event) => {
      val wasNew = newCard
      reset(kanbanapp, selectedColumn, new Card, true)
      if (!wasNew) {
        dialog.title = "Kanban - Card Edit"
        dialog.headerText = "Edit Card"
      }
    }
  }

  private def drawContents: VBox = new VBox(10) {
    minWidth = 600
    minHeight = 400
    alignment = CenterLeft

    children += new HBox(10) {
      children += new Label("Text") {
        minHeight = 20
        font = CardTextFont
        minWidth = headlineWidth
      }
      children += cardText
    }
    children += new Separator
    children += new HBox(10) {
      alignment = CenterLeft
      children += new Label("Appereance") {
        font = CardTextFont
        minWidth = headlineWidth
      }
      children += new VBox(10) {
        children += new HBox(10) {
          children += new Label("Border Color") {
            minWidth = 80
          }
          children += borderColor
          children += resetBorderColorButton
        }
        children += new HBox(10) {
          children += new Label("Text Color") {
            minWidth = 80
          }
          children += textColor
          children += resetTextColorButton
        }
      }
    }

    children += new Separator

    children += new HBox(10) {
      alignment = CenterLeft
      children += new Label("Tasks") {
        font = CardTextFont
        minWidth = headlineWidth
      }
      children += new VBox(10) {
        children += new HBox(10) {
          children += new Label("Deadline")
          children += deadlineCheckbox
          children += drawDatePicker
        }
        children += new HBox(10) {
          children += new Label("Add New")
          children += taskText
          children += addTaskButton
          children += errorLabel
        }
        children += new HBox(5) {
          children += new Label("Remove")
          children += taskRemoveMenu
          children += deleteTaskButton
        }
      }
    }


    children += new Separator
    children += new HBox(10) {
      alignment = CenterLeft
      children += new Label("Attachments") {
        font = CardTextFont
        minWidth = headlineWidth
      }
      children += new VBox(10) {
        children += new HBox(10) {
          children += fileButton
          children += removeFileButton
          children += openFile
          children += fileLabel
        }
        children += new HBox(10) {
          children += chooseCardButton
          children += removeCardButton
          children += viewCardButton
          children += cardView
        }
        children += new HBox(10) {
          children += enterUrlButton
          children += removeUrlButton
          children += openUrlButton
          children += urlLabel
        }
      }
    }


    children += new Separator
    children += new HBox(10) {
      alignment = CenterLeft
      children += new Label("Tags") {
        font = CardTextFont
        minWidth = headlineWidth
      }
      children += new VBox(10) {
        children += new HBox(10) {
          children += new Label("Current")
          children += drawCurrentTags
        }
        children += new HBox(10) {
          children += new Label("Edit")
          children += manageTagsButton
          children += drawAddTag
          children += drawRemoveTag
        }
      }
    }

    children += new Separator
    children += new HBox(15) {
      children += resetAllButton
      children += toTemplateButton
    }
  }

  private val okButton = dialog.dialogPane().lookupButton(okButtonType)

  okButton.disable = true

  cardText.text.onChange { (_, _, newValue) =>
    okButton.disable = newValue.trim().isEmpty
    toTemplateButton.disable = newValue.trim().isEmpty
  }

  dialog.dialogPane().content = drawContents

  Platform.runLater(cardText.requestFocus())

  def reset(kanban: Kanban, column: Column, card: Card, isNew: Boolean, isTemplate: Boolean = false) = {
    kanbanapp = kanban
    selectedColumn = column
    selectedCard = card
    newCard = isNew
    template = isTemplate
    checklist = new Checklist
    checklist.resetTasks()
    taskText.text = ""
    errorLabel.text = ""

    selectedFile = card.getFile
    selectedUrl = card.getUrl
    selectedSubcard = card.getSubcard

    if (isNew) {
      dialog.title = "Kanban - New Card"
      dialog.headerText = "Add New Card"
      cardText.text = ""
      borderColor.value = DefaultColor
      textColor.value = DefaultColor


    } else {
      dialog.title = "Kanban - Card Edit"
      dialog.headerText = "Edit Card"
      cardText.text = card.getText
      borderColor.value = card.getBorderColor
      textColor.value = card.getTextColor

      cardTags = card.getTags
      tasks = card.getChecklist.getTasks
      checklist.setTasks(card.getChecklist.getTasks)

      card.getFile match {
        case Some(file) => {
          if (file.canRead) {
            selectedFile = Some(file)
          } else {
            selectedFile = None
          }
        }
        case None => selectedFile = None
      }
    }
    taskRemoveMenu.items = ObservableBuffer(getTaskList)

    selectedFile match {
      case Some(file) => {
        openFile.disable = false
        removeFileButton.disable = false
        fileLabel.text = "File: " + file.getName
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

    card.getSubcard match {
      case Some(subcard) => {
        removeCardButton.disable = false
        viewCardButton.disable = false
        cardView.text = "Card Selected"
      }
      case None => {
        removeCardButton.disable = true
        viewCardButton.disable = true
        cardView.text = "No Selected Card"
      }
    }

    card.getUrl match {
      case Some(url) => {
        removeUrlButton.disable = false
        openUrlButton.disable = false
        urlLabel.text = "URL: " + url
      }
      case None => {
        removeUrlButton.disable = true
        openUrlButton.disable = true
        urlLabel.text = "No URL Set"
      }
    }
    resetTagEdit()
  }

  private def resetTagEdit() = {

    drawAddTag.items = drawAddTagMenuItems
    drawRemoveTag.items = drawRemoveTagMenuItems

    if (cardTags.isEmpty) {
      drawCurrentTags.text = "No Tags"
    } else {
      drawCurrentTags.text = cardTags.mkString(", ")
    }

    if (kanbanapp.getTags.isEmpty) {
      drawAddTag.visible = false
      drawRemoveTag.visible = false
    } else {
      drawAddTag.visible = true
      drawRemoveTag.visible = true
    }
  }

  private def resetTaskEdit() = {
    drawAddTag.items = drawAddTagMenuItems
    drawRemoveTag.items = drawRemoveTagMenuItems
    if (cardTags.isEmpty) {
      drawCurrentTags.text = "No tags - Click 'Manage Tags' to Add New Tags"
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
      if (newCard || template) {
        selectedColumn.addNewCard(cardText.text(), textColor.getValue, borderColor.getValue, cardTags, checklist, getDeadline, selectedFile, selectedSubcard, selectedUrl)
      } else {
        selectedCard.editCard(cardText.text(), textColor.getValue, borderColor.getValue, cardTags, checklist, getDeadline, selectedFile, selectedSubcard, selectedUrl)
      }
    }
    selectedCard
  }
}

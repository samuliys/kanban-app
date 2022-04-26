package kanban.ui

import kanban._
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Pos._
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.scene.paint.Color
import scalafx.stage.FileChooser
import scala.collection.mutable.Buffer
import scala.util.{Failure, Success, Try}
import java.awt.Desktop
import java.io.File
import java.net.{URI, URL}
import java.time.LocalDate


/** Dialog object for creating and editing cards. */
object CardDialog {

  private val dialog = new Dialog[Card] {
    initOwner(Main.getStage)
  }

  /** Opens dialog window */
  def showDialog() = dialog.showAndWait()

  // Variables to keep track of all aspects of card edit/creation
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
  private var cardTags = Buffer[String]()

  // Widths for various UI elements so that they have the same width despite different texts
  private val colorHeadlineWidth = 80
  private val taskHeadlineWidth = 65
  private val taskButtonWidth = 80
  private val headlineWidth = 90
  private val attachmentButtonWidth = 90
  private val removeButtonWidth = 70
  private val openButtonWidth = 50
  private val taskEntryWidth = 200

  private val okButtonType = new ButtonType("OK", ButtonData.OKDone)

  dialog.dialogPane().buttonTypes = Seq(okButtonType, ButtonType.Cancel) // add buttons to dialog

  private val cardText = new TextField { // text field for entering card text
    promptText = "Card Text"
    minWidth = 420
  }

  private val borderColor = new ColorPicker(Color.Black) { // color picker for border color
    promptText = "Border Color"
  }

  private val textColor = new ColorPicker(Color.Black) { // color picker for text color
    promptText = "Text Color"
  }

  /** Creates MenuItem components used for listing tags for removal
   *
   * @return MenuItem omponents needed to list tags for removal */
  private def drawRemoveTagMenuItems: Buffer[MenuItem] = {
    val items = Buffer[MenuItem]()
    for (tag <- cardTags) {
      items += new MenuItem(tag) {
        onAction = (event) => {
          cardTags -= tag
          resetTagEdit()
        }
      }
    }
    items
  }

  private val drawRemoveTag = new MenuButton("Select Tag to Remove") { // menu button for tag removal from card
    items = drawRemoveTagMenuItems
  }

  /** Creates MenuItem components used for listing tags to add them to cards
   *
   * @return MenuItem omponents needed to list tags for adding to card */
  private def drawAddTagMenuItems: Buffer[MenuItem] = {
    val items = Buffer[MenuItem]()
    for (tag <- kanbanapp.getTags.filterNot(cardTags.contains(_))) { // show only tags the card does not have
      items += new MenuItem(tag) {
        onAction = (event) => {
          cardTags += tag
          resetTagEdit()
        }
      }
    }
    items
  }

  private val drawAddTag = new MenuButton("Select Tag to Add") { // menu button for adding tag to card
    items = drawAddTagMenuItems
  }

  private val drawCurrentTags = new Label { // display current tags
    if (cardTags.isEmpty) {
      text = "No tags. Add tag below."
    } else {
      text = cardTags.mkString(", ")
    }
  }

  private val drawDatePicker = new DatePicker(LocalDate.now) // date picker for choosing deadline

  private val deadlineCheckbox = new CheckBox("Include") { // checkbox for selecting whether to have a deadline on the card
    minWidth = 70 // width so that whole text is visible
    onAction = (event) => checkCheckbox()
  }

  private def checkCheckbox(): Unit = { // toggle deadline checkbox
    drawDatePicker.disable = !deadlineCheckbox.selected()
  }

  private val taskText = new TextField { // text field for entering task
    promptText = "Task"
    minWidth = taskEntryWidth
  }

  private val addTaskButton = new Button("Add Task") { // button for adding task
    minWidth = taskButtonWidth
    onAction = (event) => {
      checklist.addTask(taskText.text())
      taskText.text = ""
      errorLabel.text = ""
      taskRemoveMenu.items = ObservableBuffer(getTaskList) // update list of tasks
    }
  }

  /** Returns list of task names
   *
   * @return Checklist task names as a List */
  private def getTaskList: List[String] = checklist.getTasksNames.toList

  private val taskRemoveMenu: ComboBox[String] = new ComboBox(getTaskList) { // combobox for choosing a task to be removed
    minWidth = taskEntryWidth
    promptText = "Select Task to Remove"
    onAction = (event) => {
      deleteTaskButton.disable = false
    }
  }

  private val deleteTaskButton = new Button("Delete Task") { // button for deleting task
    minWidth = taskButtonWidth
    onAction = (event) => {
      checklist.removeTask(checklist.getTasksNames.indexOf(taskRemoveMenu.value()))
      taskRemoveMenu.items = ObservableBuffer(getTaskList) // update tasks
      taskRemoveMenu.promptText = "Select Task to Remove"
      disable = true
      if (!getTaskList.contains(taskText.text())) { // enable creating task with same name as the one just removed
        errorLabel.text = ""
        addTaskButton.disable = false
      }
    }
  }

  private val errorLabel = new Label { // label for showing user error messages
    textFill = Color.Red
  }

  taskText.text.onChange { (_, _, newValue) => // make sure task name is allowed
    if (newValue == "") {
      errorLabel.text = "Task can't be empty"
      addTaskButton.disable = true
    } else if (newValue.length > 30) { // only allow tasks of certain length
      errorLabel.text = "Task too long"
      addTaskButton.disable = true
    } else if (getTaskList.contains(newValue)) { // task name must be unique
      errorLabel.text = "Task '" + taskText.text() + "' already exits"
      addTaskButton.disable = true
    } else { // task name ok
      errorLabel.text = ""
      addTaskButton.disable = false
    }
  }

  private val fileButton = new Button("Choose File") { // button for choosing file
    minWidth = attachmentButtonWidth
    onAction = (event) => {
      val fileChooser = new FileChooser
      val chooseFile = fileChooser.showOpenDialog(Main.getStage)

      if (chooseFile != null) { // make sure a file was actually selected
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

  private val removeFileButton = new Button("Remove") { // button for removing file attachment
    minWidth = removeButtonWidth
    onAction = (event) => {
      selectedFile match {
        case Some(file) => {
          openFile.disable = true
          selectedFile = None
          fileLabel.text = "No Selected File"
          disableRemoveFileBtn()
        }
        case None =>
      }
    }
  }

  /** Disables remove file button. Needed so that button can be disabled within. */
  private def disableRemoveFileBtn(): Unit = removeFileButton.disable = true

  private val openFile = new Button("Open") { // button for opening file attachment
    minWidth = openButtonWidth
    onAction = (event) => {
      selectedFile match {
        case Some(file) => {
          if (file.canRead && Desktop.isDesktopSupported) {
            Desktop.getDesktop.open(file)
          } else {
            selectedFile = None
            new Alert(AlertType.Warning, "There was an error opening the file.").showAndWait()
          }
        }
        case None =>
      }
    }
  }

  private val fileLabel = new Label("Chosen File") // display chosen file name

  private val chooseCardButton = new Button("Choose Card") { // button for choosing card attachment
    minWidth = attachmentButtonWidth
    onAction = (event) => {
      CardListDialog.reset(kanbanapp, selectedBoard, selectedColumn, selectedCard, 3)
      val result = CardListDialog.showDialog()
      result match { // dialog window returns a Result object that needs to be matched
        case Some(Result(card)) => {
          selectedSubcard = Some(card.toSub)
          viewCardButton.disable = false
          cardView.text = "Card Selected"
          removeCardButton.disable = false
        }
        case None => {
          selectedSubcard = None
          viewCardButton.disable = true
          removeCardButton.disable = true
          cardView.text = "No Selected Card"
        }
      }
    }
  }

  private val removeCardButton = new Button("Remove") { // button for removing card attachment
    minWidth = removeButtonWidth
    onAction = (event) => {
      selectedSubcard = None
      viewCardButton.disable = true
      cardView.text = "No Selected Card"
      disableRemoveCard()
    }
  }

  /** Disables remove card button. Needed so that button can be disabled within. */
  private def disableRemoveCard(): Unit = removeCardButton.disable = true

  private val viewCardButton = new Button("View") { // button for viewing card attachment
    minWidth = openButtonWidth
    onAction = (event) => {
      selectedSubcard match {
        case Some(card) => {
          CardViewDialog.reset(card.getCard)
          CardViewDialog.showDialog()
        }
        case None =>
      }
    }
  }

  private val cardView = new Label("No Selected Card") { // label for card attachment information
    minWidth = 200
  }

  private val enterUrlButton = new Button("Enter URL") { // button for adding url attachment
    minWidth = attachmentButtonWidth
    onAction = (event) => {
      val dialog = new TextInputDialog { // open dialog asking for url
        initOwner(Main.getStage)
        title = "Enter URL"
        headerText = "Card URL"
        contentText = "Enter URL:"
      }
      val result = dialog.showAndWait()
      result match { // match result of the dialog entry
        case Some(enteredUrl) =>
          val validateUrl = Try { // check that url entered is a valid url
            new URL(enteredUrl)
          }
          validateUrl match {
            case Failure(exception) => { // if not valid, infor user
              new Alert(AlertType.Information) {
                initOwner(Main.getStage)
                title = "Invalid URL"
                headerText = "Invalid URL"
                contentText = "Please Enter a Valid URL Starting with http(s)://"
              }.showAndWait()
            }
            case Success(url) => { // url was ok
              selectedUrl = Some(enteredUrl)
              removeUrlButton.disable = false
              openUrlButton.disable = false
              urlLabel.text = "URL: " + enteredUrl
            }
          }
        case None =>
      }
    }
  }

  private val removeUrlButton: Button = new Button("Remove") { // button for removing url attachment
    minWidth = removeButtonWidth
    onAction = (event) => {
      selectedUrl = None
      openUrlButton.disable = true
      urlLabel.text = "No URL Set"
      disableRemoveUrlButton()
    }
  }

  /** Disables remove url button. Needed so that button can be disabled within. */
  def disableRemoveUrlButton(): Unit = removeUrlButton.disable = true

  private val openUrlButton = new Button("Open") { // button for opening url attachment
    minWidth = openButtonWidth
    onAction = (event) => {
      selectedUrl match {
        case Some(url) => {
          if (Desktop.isDesktopSupported) { // make sure device supports java.awt.Desktop
            Desktop.getDesktop.browse(new URI(url)) // this url is always valid as that was checked when url was given
          }
        }
        case None =>
      }

    }
  }

  private val urlLabel = new Label("URL: ") // label for dispalying url informatin

  private val toTemplateButton = new Button("Make into a template") { // button for turning a card into a template
    onAction = (event) => {
      kanbanapp.addTemplate(selectedCard)
      new Alert(AlertType.Information) { // show success message
        initOwner(Main.getStage)
        title = "Template"
        headerText = "Template Created Succesfully"
        contentText = "Click 'Templates' on Toolbar to Manage"
      }.showAndWait()
    }
  }

  private val manageTagsButton = new Button("Manage Tags") { // button for opening tag management
    onAction = (event) => {
      TagDialog.reset(kanbanapp)
      TagDialog.showDialog()
      if (newCard) { // make sure tags are still among the kanban session tags
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

  private val resetBorderColorButton = new Button("Reset to Default") { // set color to default
    onAction = (event) => {
      borderColor.value = DefaultColor
    }
  }

  private val resetTextColorButton = new Button("Reset to Default") { // set color to default
    onAction = (event) => {
      textColor.value = DefaultColor
    }
  }

  /** Resets all aspects of card edit/creation to their default values */
  private def cardReset(): Unit = {
    // This is done using the dialog's own reset method
    // so some information might be lost if not saved to variables beforehand
    val wasNew = newCard
    val wasTemplate = template
    val oldCard = selectedCard

    reset(kanbanapp, selectedColumn, new Card, true)
    if (!wasNew) { // make sure title is still correct
      dialog.title = "Kanban - Card Edit"
      dialog.headerText = "Edit Card"
    }
    newCard = wasNew // set old values to these variables
    template = wasTemplate
    selectedCard = oldCard
  }

  private val resetAllButton = new Button("Reset All") { // button for reseting card to default values
    onAction = (event) => {
      cardReset()
    }
  }

  /** Forms VBox component used as the root of all dialog components
   *
   * @return VBox component with all dialog window components */
  private def drawContents: VBox = new VBox(10) {
    minWidth = 700
    minHeight = 400
    alignment = CenterLeft

    children += new HBox(10) { // text segment
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
      children += new Label("Appereance") { // card colors segment
        font = CardTextFont
        minWidth = headlineWidth
      }
      children += new VBox(10) {
        children += new HBox(10) {
          children += new Label("Border Color") {
            minWidth = colorHeadlineWidth
          }
          children += borderColor
          children += resetBorderColorButton
        }
        children += new HBox(10) {
          children += new Label("Text Color") {
            minWidth = colorHeadlineWidth
          }
          children += textColor
          children += resetTextColorButton
        }
      }
    }

    children += new Separator

    children += new HBox(10) {
      alignment = CenterLeft
      children += new Label("Tasks") { // task segment
        font = CardTextFont
        minWidth = headlineWidth
      }
      children += new VBox(10) {
        alignment = CenterLeft
        children += new HBox(10) {
          children += new Label("Deadline") { // deadline is linked to tasks
            minWidth = taskHeadlineWidth
          }
          children += deadlineCheckbox
          children += drawDatePicker
        }
        children += new HBox(10) {
          children += new Label("Add New") {
            minWidth = taskHeadlineWidth
          }
          children += taskText
          children += addTaskButton
          children += errorLabel
        }
        children += new HBox(10) {
          children += new Label("Remove") {
            minWidth = taskHeadlineWidth
          }
          children += taskRemoveMenu
          children += deleteTaskButton
        }
      }
    }

    children += new Separator
    children += new HBox(10) {
      alignment = CenterLeft
      children += new Label("Attachments") { // card attachment segment
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
      children += new Label("Tags") { // tag segment
        font = CardTextFont
        minWidth = headlineWidth
      }
      children += new VBox(10) {
        children += new HBox(10) {
          children += new Label("Current") {
            minWidth = 50
          }
          children += drawCurrentTags
        }
        children += new HBox(10) {
          children += new Label("Edit") {
            minWidth = 50
          }
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

  cardText.text.onChange { (_, _, newValue) => // card text can't be empty
    okButton.disable = newValue.trim().isEmpty
    toTemplateButton.disable = newValue.trim().isEmpty
  }

  dialog.dialogPane().content = drawContents // set content to view

  Platform.runLater(cardText.requestFocus()) // focus on text entry field

  /** Resets the dialog in order to create new card or edit an old one
   *
   * @param kanban     current Kanban session
   * @param column     column the card belongs to
   * @param card       card to be created or edited
   * @param isNew      whether the dialog will be used to create a new card or edit existing
   * @param isTemplate whether the dialog will be used edit a template
   */
  def reset(kanban: Kanban, column: Column, card: Card, isNew: Boolean, isTemplate: Boolean = false): Unit = {
    // Set parameteres to variables
    kanbanapp = kanban
    selectedColumn = column
    selectedCard = card
    newCard = isNew
    template = isTemplate

    checklist = new Checklist // blank new checkist
    checklist.resetTasks()

    taskText.text = "" // reset text fields
    errorLabel.text = ""
    okButton.disable = isNew // ok button will be disabled as new cards don't have any text

    selectedFile = card.getFile // get information about card attachnents
    selectedUrl = card.getUrl
    selectedSubcard = card.getSubcard

    if (isNew) { // reset dialog based on if new card or not
      dialog.title = "Kanban - New Card"
      dialog.headerText = "Add New Card"
      cardText.text = ""
      borderColor.value = DefaultColor
      textColor.value = DefaultColor

      cardTags = Buffer[String]()

    } else {
      dialog.title = "Kanban - Card Edit"
      dialog.headerText = "Edit Card"
      cardText.text = card.getText // get data from the existing card
      borderColor.value = card.getBorderColor
      textColor.value = card.getTextColor

      cardTags = card.getTags
      tasks = card.getChecklist.getTasks
      checklist.setTasks(card.getChecklist.getTasks)

      card.getFile match {
        case Some(file) => {
          if (file.canRead) { // nake sure file can still be found
            selectedFile = Some(file)
          } else {
            selectedFile = None
          }
        }
        case None => selectedFile = None
      }
    }
    taskRemoveMenu.items = ObservableBuffer(getTaskList) // update task list

    selectedFile match { // hamdle file attachment
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

    card.getDeadline match { // reset deadline view based on card
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

    card.getSubcard match { // reset subcard view based on card
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

    card.getUrl match { // reset url view based on card
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
    resetTagEdit() // reset tag segment
  }

  /** Resets tag portion of dialog with concurrent information */
  private def resetTagEdit(): Unit = {
    // Add current tags to view
    drawAddTag.items = drawAddTagMenuItems
    drawRemoveTag.items = drawRemoveTagMenuItems

    if (cardTags.isEmpty) { // display current card tags
      drawCurrentTags.text = "No Tags"
    } else {
      drawCurrentTags.text = cardTags.mkString(", ")
    }

    if (kanbanapp.getTags.isEmpty) { // handle buttons based on whether there are any tags
      drawAddTag.visible = false
      drawRemoveTag.visible = false
    } else {
      drawAddTag.visible = true
      drawRemoveTag.visible = true
    }
  }

  /** Returns a possible deadline depending if user has chosen to include one on the card
   *
   * @return Deadline wrapped in an option */
  private def getDeadline: Option[Deadline] = {
    if (deadlineCheckbox.selected()) {
      Some(new Deadline(drawDatePicker.value()))
    } else {
      None
    }
  }
  /** Updates view and dialog compoenents */
  private def update(): Unit = dialog.dialogPane().content = drawContents

  dialog.resultConverter = dialogButton => { // create new card or edit old one when user clicks ok button
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

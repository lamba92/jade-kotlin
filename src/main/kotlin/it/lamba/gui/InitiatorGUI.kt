package it.lamba.gui

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXComboBox
import com.jfoenix.controls.JFXTextField
import it.lamba.events.OnGuiClosedEvent
import javafx.application.Application
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.stage.Stage
import javafx.scene.Scene
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.layout.AnchorPane
import javafx.stage.FileChooser
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.net.URL
import java.util.*
import it.lamba.data.Job


class InitiatorGUI: Application(), Initializable {

    @FXML lateinit var selectFileButton: JFXButton
    @FXML lateinit var selectedFilePathTextField: JFXTextField
    @FXML lateinit var videoComboBox: JFXComboBox<String>
    @FXML lateinit var audioComboBox: JFXComboBox<String>
    @FXML lateinit var convertButton: JFXButton
    //@FXML lateinit var progressTreeView: JFXTreeTableView

    lateinit var converterGUIStage: Stage

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        selectFileButton.setOnMouseClicked {
            selectedFilePathTextField.text = FileChooser().apply {
                title = "Seleziona il video"
                initialDirectory = File(System.getProperty("user.home"))
                extensionFilters.addAll(
                        FileChooser.ExtensionFilter("Tutti i files", "*"),
                        FileChooser.ExtensionFilter("MP4", "*.mp4"),
                        FileChooser.ExtensionFilter("WEBM", "*.webm"),
                        FileChooser.ExtensionFilter("MPG", "*.MPG")
                )
            }.showOpenDialog(Stage().apply {
                //TODO mettere il file chooser in primo piano sempre
                //initOwner(selectFileButton.scene.window)
            })?.absolutePath ?: ""
        }
        videoComboBox.items = FXCollections.observableArrayList("h.264", "h.265")
        videoComboBox.value = "h.264"
        audioComboBox.items = FXCollections.observableArrayList("aac", "mp3")
        audioComboBox.value = "aac"
        convertButton.setOnMouseClicked {
            if(!selectedFilePathTextField.text.isEmpty())
                EventBus.getDefault().post(Job(
                        selectedFilePathTextField.text,
                        videoComboBox.value,
                        audioComboBox.value
                ))
        }
        selectedFilePathTextField.clear()
    }

    override fun start(primaryStage: Stage) {
        this.converterGUIStage = primaryStage
        val root = FXMLLoader.load<AnchorPane>(javaClass.classLoader.getResource("InitiatorGUI.fxml"))
        primaryStage.title = "InitiatorGUI"
        primaryStage.scene = Scene(root)
        primaryStage.show()
    }

    override fun stop() {
        EventBus.getDefault().post(OnGuiClosedEvent())
        Platform.exit()
    }

}
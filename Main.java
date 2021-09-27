package com.company;

import com.sun.jmx.snmp.internal.SnmpAccessControlModel;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import org.fxmisc.richtext.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Main extends Application{

    public int line;

    public static void main(String[] args) {

        Application.launch(args);
    }
    @FXML
    private CodeArea specialArea;
    @FXML
    VBox editorPane;
    @FXML
    TableView<String> lineNumberArea;
    @FXML
    TableColumn<String, String> lineNumberColumn;
    @FXML
    TableView<Token> tokensTable;
    @FXML
    TableColumn<Token, String> columnName;
    @FXML
    TableColumn<Token, String> lengthColumn;
    @FXML
    TableColumn<Token, String> commonPlaceColumn;
    @FXML
    TableColumn<Token,String> classColumn;
    @FXML
    TableColumn<Token, String> lineColumn;
    @Override
    public void start(Stage stage) {

        // установка надписи
        Text text = new Text("Hello from JavaFX!");
        text.setLayoutY(80);    // установка положения надписи по оси Y
        text.setLayoutX(100);   // установка положения надписи по оси X

        Parent root=null;
        try {
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("main.fxml")));
        } catch (IOException e) {
            e.printStackTrace();
        }


        assert root != null;
        Scene scene = new Scene(root);


        stage.setScene(scene);
        stage.setTitle("First Application");
        line=1;
        stage.show();


    }

    String numerics = "150";

    @FXML
    void initialize() {

        lineNumberArea.setFixedCellSize(19);
        lineNumberColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue()));

        ArrayList<String> tableData = new ArrayList<>();
        for (int i = 0; i < 150; i++) {
            tableData.add((i+1)+""); // добавить строку
        }
        lineNumberArea.getItems().addAll(tableData);
        specialArea.addEventFilter(ScrollEvent.ANY, e-> {
            if (e.getDeltaY() > 0) {
                lineNumberArea.scrollTo(line-1);
                line-=1;
            } else if (e.getDeltaY() < 0) {
                lineNumberArea.scrollTo(line+1);
                line+=1;
            }
        });
    }


    @FXML
    void isScrolled() {
        line = specialArea.getParagraphs().size();
        System.err.println(line);
        if (line > 25) {
            if (line >= Integer.parseInt(numerics)) {
                numerics = (Integer.parseInt(numerics) + 1) + "";
                lineNumberArea.getItems().add(numerics);
            }
            lineNumberArea.scrollTo(line-25);
        } else if (line < 25) {
            lineNumberArea.scrollTo(0);
        }
    }
    @FXML
     void isScrolledCodeArea() {
        System.err.println("Scroool");
    }

    ArrayList<Token> tokens = new ArrayList<>();
    public void build(ActionEvent actionEvent) {
        Parser parser = new Parser(specialArea.getText());
        tokens=parser.getAllTokens();


        columnName.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getItem()));
        lengthColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getTokenLength()));
        commonPlaceColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCommonStartPoint()));
        classColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getItemClass()));
        lineColumn.setCellValueFactory(param-> new SimpleStringProperty(param.getValue().getLinePoint()));
        tokensTable.getItems().addAll(tokens);

    }
}
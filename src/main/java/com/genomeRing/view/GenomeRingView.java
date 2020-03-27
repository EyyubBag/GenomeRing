package com.genomeRing.view;

import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.stage.Popup;
import com.genomeRing.model.structure.*;
import com.genomeRing.model.structure.RingDimensions;
import com.genomeRing.view.genomeRingWindow.GenomeRingWindow;

import java.util.Optional;


/**
 * contains every Group
 */
public class GenomeRingView extends Group implements SuperGenomeListener {
    private SuperGenome superGenome;
    private RingDimensions ringDimensions;
    //Adding the Window so we can use Bindings
    private GenomeRingWindow window;

    public GenomeRingView() {
    }


    public void setupView(SuperGenome superGenome, RingDimensions ringDimensions, GenomeRingWindow window) {
        this.getChildren().clear();

        this.superGenome = superGenome;
        this.ringDimensions = ringDimensions;
        this.window = window;

        //setup SuperGenome TODO remove this
        superGenome.addListener(this);

        //setting up the boundaries for the Legend
        double prefHeight = window.getController().getLegendVBox().getPrefHeight();
        double bestSpacing = prefHeight / superGenome.getGenomes().size();
        window.getController().getLegendVBox().setSpacing(bestSpacing);

        drawSuperGenome();
        drawGenomes();

    }

    private void drawGenomes() {

        for (Genome g : this.superGenome.getGenomes()) {
            GenomeView genomeView = new GenomeView(g, this.ringDimensions, this.window);
            this.getChildren().add(genomeView);

            createLegendItem(g, genomeView);

        }

    }

    private void drawSuperGenome() {
        // SuperGenomeView superGenomeView = new SuperGenomeView(superGenome,ringDimensions, window);
        SuperGenomeView superGenomeView = new SuperGenomeView(superGenome, ringDimensions, window);
        superGenomeView.setStyle(Color.BLACK, StrokeLineCap.BUTT, StrokeLineJoin.ROUND, 1d);
        this.getChildren().add(superGenomeView);
    }

    /**
     * For each genome g a LegendItem is created and bound to the Color Property of the genome
     * this can be changed by a colorPicker
     * @param g Genome
     */
    private void createLegendItem(Genome g, GenomeView genomeView){
        HBox legendItem = new HBox();

        Label label = new Label();
        label.textProperty().bind(g.nameProperty());
        Rectangle rectangle = new Rectangle();
        rectangle.setStroke(Color.BLACK);
        rectangle.setWidth(20);
        rectangle.setHeight(20);
        rectangle.fillProperty().bind(g.colorProperty());

        legendItem.getChildren().addAll(rectangle, label);
        window.getController().getLegendVBox().getChildren().add(legendItem);

        GenomeColors genomeColors = new GenomeColors();

        rectangle.setOnMousePressed((e)->{
            if(e.isPrimaryButtonDown()) {
                if (genomeView.isVisible()) {
                    genomeView.setVisible(false);
                    rectangle.fillProperty().unbind();
                    rectangle.setFill(Color.TRANSPARENT);
                } else {
                    genomeView.setVisible(true);
                    rectangle.fillProperty().bind(g.colorProperty());
                }
            }
        });

        legendItem.setOnContextMenuRequested((event -> {
            ContextMenu contextMenu = new ContextMenu();
            MenuItem pickColor = new MenuItem("Change Color");
            MenuItem changeName = new MenuItem("Change Name");

            changeName.setOnAction((e)->{
                createChangeNameDialog(g);
            });

            pickColor.setOnAction((e) -> {
                Popup popup = new Popup();

                ColorPicker colorPicker = new ColorPicker(g.getColor());
                colorPicker.setOnAction((actionEvent) -> {
                    Color c = colorPicker.getValue();
                    g.setColor(c);
                    popup.hide();
                });

                colorPicker.getCustomColors().addAll(genomeColors.colors);

                popup.getContent().add(colorPicker);
                popup.show(window.getStage(), window.getStage().getWidth() / 3, window.getStage().getHeight() / 3);
                popup.setAutoHide(true);
            });

            contextMenu.getItems().addAll(pickColor, changeName);
            contextMenu.show(legendItem, event.getScreenX(), event.getScreenY());
        }));
    }

    private void createChangeNameDialog(Genome g){
        Dialog<String> dialog = new Dialog<>();

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType resetButton = new ButtonType("Reset Name");
        dialog.getDialogPane().getButtonTypes().addAll(saveButton,resetButton, ButtonType.CANCEL);

        TextField textfield = new TextField();

        dialog.getDialogPane().setContent(textfield);

        dialog.setResultConverter((button)->{
            String result = "";
            if(button == saveButton){
                result = textfield.getText();
                return result;
            }
            else if(button == resetButton){
                textfield.clear();
                String initialName = g.getInitialName();
                textfield.setText(initialName);
                result = initialName;
                return result;
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        result.ifPresent((string)->{
            g.setName(string);
        });
    }

    @Override
    public void superGenomeChanged(SuperGenomeEvent evt) {
       // this.getChildren().clear();
       // drawSuperGenome();
        // drawGenomes();

    }

}

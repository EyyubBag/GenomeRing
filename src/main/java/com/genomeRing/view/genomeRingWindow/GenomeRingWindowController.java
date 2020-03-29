package com.genomeRing.view.genomeRingWindow;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import com.genomeRing.model.structure.Block;

import java.net.URL;
import java.util.ResourceBundle;

public class GenomeRingWindowController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private javafx.scene.layout.BorderPane BorderPane;

    @FXML
    private MenuItem loadMenuItem;

    @FXML
    private MenuItem saveMenuItem;

    @FXML
    private MenuItem exportMenuItem;


    @FXML
    private MenuItem exitMenuItem;


    @FXML
    private CheckBox showPathsCheckBox;

    @FXML
    private CheckBox showSegmentsCheckbox;

    @FXML
    private CheckBox showRingDimensionsCheckBox;

    @FXML
    private CheckBox showLegendCheck;

    @FXML
    private CheckBox showBlockLabelsCheck;

    @FXML
    private CheckBox scaleCheckBox;

    @FXML
    private VBox ringInfoVBOX;

    @FXML
    private Label genomeWidthLabel;

    @FXML
    private Label blockGapLabel;

    @FXML
    private Label circleSpacingLabel;

    @FXML
    private Label rotationLabel;

    @FXML
    private MenuButton sortBlocksMenuButton;

    @FXML
    private RadioMenuItem restoreOrderMenuItem;

    @FXML
    private ToggleGroup OptimizerToggleGroup;

    @FXML
    private RadioMenuItem nOfJumpsRadioItem;

    @FXML
    private RadioMenuItem nOfBlocksRadioItem;

    @FXML
    private RadioMenuItem jumpLengthRadioItem;

    @FXML
    private RadioMenuItem manualItem;

    @FXML
    private ListView<Block> BlockListView;

    @FXML
    private VBox legendVBox;

    @FXML
    private Label updateLabel;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    void initialize() {
        assert BorderPane != null : "fx:id=\"BorderPane\" was not injected: check your FXML file 'GenomeRingWindow.fxml'.";
        assert loadMenuItem != null : "fx:id=\"loadMenuItem\" was not injected: check your FXML file 'GenomeRingWindow.fxml'.";
        assert saveMenuItem != null : "fx:id=\"saveMenuItem\" was not injected: check your FXML file 'GenomeRingWindow.fxml'.";
        assert exportMenuItem != null : "fx:id=\"exportMenuItem\" was not injected: check your FXML file 'GenomeRingWindow.fxml'.";
        assert exitMenuItem != null : "fx:id=\"exitMenuItem\" was not injected: check your FXML file 'GenomeRingWindow.fxml'.";
        assert showPathsCheckBox != null : "fx:id=\"showPathsCheckBox\" was not injected: check your FXML file 'GenomeRingWindow.fxml'.";
        assert showSegmentsCheckbox != null : "fx:id=\"showSegmentsCheckbox\" was not injected: check your FXML file 'GenomeRingWindow.fxml'.";
        assert showRingDimensionsCheckBox != null : "fx:id=\"showRingDimensionsCheckBox\" was not injected: check your FXML file 'GenomeRingWindow.fxml'.";
        assert showLegendCheck != null : "fx:id=\"showLegendCheck\" was not injected: check your FXML file 'GenomeRingWindow.fxml'.";
        assert showBlockLabelsCheck != null : "fx:id=\"showBlockLabelsCheck\" was not injected: check your FXML file 'GenomeRingWindow.fxml'.";
        assert scaleCheckBox != null : "fx:id=\"scaleCheckBox\" was not injected: check your FXML file 'GenomeRingWindow.fxml'.";
        assert ringInfoVBOX != null : "fx:id=\"ringInfoVBOX\" was not injected: check your FXML file 'GenomeRingWindow.fxml'.";
        assert genomeWidthLabel != null : "fx:id=\"genomeWidthLabel\" was not injected: check your FXML file 'GenomeRingWindow.fxml'.";
        assert blockGapLabel != null : "fx:id=\"blockGapLabel\" was not injected: check your FXML file 'GenomeRingWindow.fxml'.";
        assert circleSpacingLabel != null : "fx:id=\"circleSpacingLabel\" was not injected: check your FXML file 'GenomeRingWindow.fxml'.";
        assert rotationLabel != null : "fx:id=\"rotationLabel\" was not injected: check your FXML file 'GenomeRingWindow.fxml'.";
        assert sortBlocksMenuButton != null : "fx:id=\"sortBlocksMenuButton\" was not injected: check your FXML file 'GenomeRingWindow.fxml'.";
        assert restoreOrderMenuItem != null : "fx:id=\"restoreOrderMenuItem\" was not injected: check your FXML file 'GenomeRingWindow.fxml'.";
        assert OptimizerToggleGroup != null : "fx:id=\"OptimizerToggleGroup\" was not injected: check your FXML file 'GenomeRingWindow.fxml'.";
        assert nOfJumpsRadioItem != null : "fx:id=\"nOfJumpsRadioItem\" was not injected: check your FXML file 'GenomeRingWindow.fxml'.";
        assert nOfBlocksRadioItem != null : "fx:id=\"nOfBlocksRadioItem\" was not injected: check your FXML file 'GenomeRingWindow.fxml'.";
        assert jumpLengthRadioItem != null : "fx:id=\"jumpLengthRadioItem\" was not injected: check your FXML file 'GenomeRingWindow.fxml'.";
        assert manualItem != null : "fx:id=\"manualItem\" was not injected: check your FXML file 'GenomeRingWindow.fxml'.";
        assert BlockListView != null : "fx:id=\"BlockListView\" was not injected: check your FXML file 'GenomeRingWindow.fxml'.";
        assert legendVBox != null : "fx:id=\"legendVBox\" was not injected: check your FXML file 'GenomeRingWindow.fxml'.";
        assert updateLabel != null : "fx:id=\"updateLabel\" was not injected: check your FXML file 'GenomeRingWindow.fxml'.";
        assert scrollPane != null : "fx:id=\"scrollPane\" was not injected: check your FXML file 'GenomeRingWindow.fxml'.";

    }

    public ResourceBundle getResources() {
        return resources;
    }

    public URL getLocation() {
        return location;
    }

    public javafx.scene.layout.BorderPane getBorderPane() {
        return BorderPane;
    }

    public MenuItem getLoadMenuItem() {
        return loadMenuItem;
    }

    public MenuItem getSaveMenuItem() {
        return saveMenuItem;
    }

    public MenuItem getExportMenuItem() {
        return exportMenuItem;
    }


    public MenuItem getExitMenuItem() {
        return exitMenuItem;
    }


    public CheckBox getShowPathsCheckBox() {
        return showPathsCheckBox;
    }

    public CheckBox getShowSegmentsCheckbox() {
        return showSegmentsCheckbox;
    }

    public CheckBox getShowRingDimensionsCheckBox() {
        return showRingDimensionsCheckBox;
    }

    public CheckBox getShowLegendCheck() {
        return showLegendCheck;
    }

    public CheckBox getShowBlockLabelsCheck() {
        return showBlockLabelsCheck;
    }

    public CheckBox getScaleCheckBox() {
        return scaleCheckBox;
    }

    public VBox getRingInfoVBOX() {
        return ringInfoVBOX;
    }

    public Label getGenomeWidthLabel() {
        return genomeWidthLabel;
    }

    public Label getBlockGapLabel() {
        return blockGapLabel;
    }

    public Label getCircleSpacingLabel() {
        return circleSpacingLabel;
    }

    public Label getRotationLabel() {
        return rotationLabel;
    }

    public MenuButton getSortBlocksMenuButton() {
        return sortBlocksMenuButton;
    }

    public RadioMenuItem getRestoreOrderMenuItem() {
        return restoreOrderMenuItem;
    }

    public ToggleGroup getOptimizerToggleGroup() {
        return OptimizerToggleGroup;
    }

    public RadioMenuItem getnOfJumpsRadioItem() {
        return nOfJumpsRadioItem;
    }

    public RadioMenuItem getnOfBlocksRadioItem() {
        return nOfBlocksRadioItem;
    }

    public RadioMenuItem getJumpLengthRadioItem() {
        return jumpLengthRadioItem;
    }

    public RadioMenuItem getManualItem() {
        return manualItem;
    }

    public ListView<Block> getBlockListView() {
        return BlockListView;
    }

    public VBox getLegendVBox() {
        return legendVBox;
    }

    public Label getUpdateLabel() {
        return updateLabel;
    }

    public ScrollPane getScrollPane() {
        return scrollPane;
    }

}

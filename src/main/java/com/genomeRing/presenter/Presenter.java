package com.genomeRing.presenter;

import com.genomeRing.view.exportViewWindow.ExportViewWindow;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import com.genomeRing.model.structure.*;
import com.genomeRing.model.structure.tasks.LoadSuperGenomeService;
import com.genomeRing.model.structure.tasks.SaveSuperGenomeService;
import com.genomeRing.presenter.optimize.OptimizeService;
import com.genomeRing.presenter.optimize.SuperGenomeOptimizer;
import com.genomeRing.view.dialogWindow.DialogWindow;
import com.genomeRing.view.genomeRingWindow.GenomeRingWindow;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Presenter {
    private GenomeRingWindow window;
    private Stage stage;
    private SuperGenome superGenome;
    private RingDimensions ringDimensions;
    private ObservableList<Block> blocks = FXCollections.observableArrayList(new ArrayList<>());

    //we need this for the Drag and Drop feature of the ListView
    private DataFormat blockFormat = new DataFormat("Block");

    public Presenter(GenomeRingWindow window, Stage stage) throws Exception {
        this.window = window;
        this.stage = stage;

        displayExample();

        loadFile();
        saveFile();
        setupRingInfoBox();
        setupExportWindow();

        window.getController().getExitMenuItem().setOnAction((e)->{
            stage.hide();
        });
    }


    /**
     * Setting up the LoadMenuItem Action.
     * Choose a single File and call the LoadSuperGenomeService on it.
     * The resulting SuperGenome is used to create a GenomeRingView and fill in the ListView.
     */
    private void loadFile(){
        FileChooser fileChooser = new FileChooser();
        File initFile = new File(".");

        FileChooser.ExtensionFilter xmfaFilter = new FileChooser.ExtensionFilter("Mauve XMFA Alignment File","*.xmfa", ".XMFA");
        FileChooser.ExtensionFilter blocksFilter = new FileChooser.ExtensionFilter("SuperGenome Blocks File (*.blocks)", "*.blocks", ".BLOCKS");
        FileChooser.ExtensionFilter wipFilter = new FileChooser.ExtensionFilter("WIP", "*.blocks", "*.xmfa", ".XMFA", "*.BLOCKS");

        fileChooser.getExtensionFilters().addAll(wipFilter,xmfaFilter,blocksFilter);

        fileChooser.setInitialDirectory(initFile);
        this.window.getController().getLoadMenuItem().setOnAction(
                (e)->{

                    File file = fileChooser.showOpenDialog(this.stage);
                    if(file != null){
                        window.resetView();
                        superGenome = new SuperGenome();

                        try {
                            setupParameters(superGenome,file);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        LoadSuperGenomeService service = new LoadSuperGenomeService(superGenome,file.getAbsolutePath());
                        service.setOnRunning((e1)->{
                           window.getController().getUpdateLabel().textProperty().bind(service.messageProperty());
                        });


                        service.setOnSucceeded((event)->{
                            int radius = 100 * superGenome.getNumberOfGenomes();
                            ringDimensions = new RingDimensions(20, radius, 1, 5, superGenome);

                            window.setupView(superGenome,ringDimensions);


                            window.getController().getUpdateLabel().textProperty().unbind();
                            setupBlockList();
                            setupOptimizer();
                        });

                        service.restart();
                    }
                }
        );

    }

    /**
     * Sets up the SaveMenuItem action and calls the SaveSuperGenomeService on the current SuperGenome.
     * Saves the SuperGenome in a  given or newly created .blocks file.
     */
    private void saveFile(){

        FileChooser fileChooser = new FileChooser();

        File initFile = new File(".");
        fileChooser.setInitialDirectory(initFile);

        FileChooser.ExtensionFilter blocksFilter = new FileChooser.ExtensionFilter("SuperGenome Blocks File (*.blocks)", "*.blocks", ".BLOCKS");
        fileChooser.getExtensionFilters().add(blocksFilter);

        window.getController().getSaveMenuItem().setOnAction((e)->{
            File file = fileChooser.showSaveDialog(stage);

            if(file != null){
                SaveSuperGenomeService service = new SaveSuperGenomeService(superGenome,file.getAbsolutePath());
                service.setOnRunning((e1)->{
                    window.getController().getUpdateLabel().textProperty().bind(service.messageProperty());
                });
                service.setOnSucceeded((e1)->{
                    window.getController().getUpdateLabel().textProperty().unbind();
                });

                service.restart();
            }

        });

    }

    /**
     * Sets up the Sort by: Button of the ListView. First sorts the List of Blocks using the Optimizer
     * and then redrawing the View.
     */
    private void setupOptimizer(){
        //TODO maybe do all that in the constructor of the Service
        window.getController().getOptimizerToggleGroup().selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                if(newValue.equals(window.getController().getJumpLengthRadioItem())){

                    OptimizeService optimizeService = new OptimizeService(superGenome,ringDimensions, SuperGenomeOptimizer.SWITCH_FIRST_INSERT_LATER,SuperGenomeOptimizer.OPTIMIZE_ANGLES);

                    optimizeService.setOnSucceeded(event1 -> {
                        superGenome.setBlocks(optimizeService.getValue());
                        blocks.clear();
                        blocks.addAll(superGenome.getBlocks());
                        int radius = 100 * superGenome.getNumberOfGenomes();
                        ringDimensions = new RingDimensions(20, radius, 1, 5, superGenome);
                        window.setupView(superGenome,ringDimensions);
                    });
                    optimizeService.restart();
                }else if(newValue.equals(window.getController().getnOfBlocksRadioItem())){

                    OptimizeService optimizeService = new OptimizeService(superGenome,ringDimensions, SuperGenomeOptimizer.SWITCH_FIRST_INSERT_LATER,SuperGenomeOptimizer.OPTIMIZE_BLOCKS);
                    optimizeService.setOnSucceeded(event1 -> {
                        superGenome.setBlocks(optimizeService.getValue());
                        blocks.clear();
                        blocks.addAll(superGenome.getBlocks());

                        int radius = 100 * superGenome.getNumberOfGenomes();
                        ringDimensions = new RingDimensions(20, radius, 1, 5, superGenome);

                        window.setupView(superGenome,ringDimensions);
                    });
                    optimizeService.restart();
                }
                else if(newValue.equals(window.getController().getnOfJumpsRadioItem())){

                    OptimizeService optimizeService = new OptimizeService(superGenome,ringDimensions, SuperGenomeOptimizer.SWITCH_FIRST_INSERT_LATER,SuperGenomeOptimizer.OPTIMIZE_JUMPS);
                    optimizeService.setOnSucceeded(event1 -> {
                        superGenome.setBlocks(optimizeService.getValue());
                        blocks.clear();
                        blocks.addAll(superGenome.getBlocks());

                        int radius = 100 * superGenome.getNumberOfGenomes();
                        ringDimensions = new RingDimensions(20, radius, 1, 5, superGenome);

                        window.setupView(superGenome,ringDimensions);
                    });
                    optimizeService.restart();
                }

                else if(newValue.equals(window.getController().getRestoreOrderMenuItem())){
                    superGenome.setBlocks(superGenome.getInitialBlockOrder());
                    blocks.clear();
                    blocks.addAll(superGenome.getBlocks());

                    int radius = 100 * superGenome.getNumberOfGenomes();
                    ringDimensions = new RingDimensions(20, radius, 1, 5, superGenome);

                    window.setupView(superGenome,ringDimensions);
                }
            }
        });


    }


    private void setupBlockList(){
        blocks.clear();
        blocks = FXCollections.observableArrayList(superGenome.getBlocks());

        window.getController().getBlockListView().setItems(blocks);
        window.getController().getBlockListView().setCellFactory(param -> {
            return new BlockCell();
        });

    }

    /**
     * Makes the Cells of the ListView Drag & Droppable.
     * Source: https://gist.github.com/jewelsea/7821196
     */
    private class BlockCell extends ListCell<Block> {

        public BlockCell() {
            ListCell thisCell = this;

            setupContextMenu(this);


            setOnDragDetected((event -> {

                System.out.println("Detected");
                window.getController().getOptimizerToggleGroup().selectToggle(window.getController().getManualItem());

                if(getItem() == null){
                    return;
                }

                Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
                ClipboardContent clipboardContent = new ClipboardContent();

                clipboardContent.put(blockFormat,getItem());
                Image image = thisCell.snapshot(null,null);
                dragboard.setDragView(image);
                dragboard.setContent(clipboardContent);
                event.consume();
            }));

            setOnDragOver(event -> {
                System.out.println("Drag OVer");
                if(event.getGestureSource() != thisCell && event.getDragboard().hasContent(blockFormat)){
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            setOnDragEntered(event -> {
                System.out.println("Entered");
                if (event.getGestureSource() != thisCell &&
                        event.getDragboard().hasContent(blockFormat)) {
                    setOpacity(0.3);
                }
            });

            setOnDragExited(event -> {
                System.out.println("Exited");
                if (event.getGestureSource() != thisCell &&
                        event.getDragboard().hasContent(blockFormat)) {
                    setOpacity(1);
                }
                System.out.println(blocks);
            });

            setOnDragDropped(event -> {
                System.out.println("Dropped");
                if (getItem() == null) {
                    System.out.println("Drop empty");
                    return;
                }

                Dragboard db = event.getDragboard();
                boolean success = false;

                if (db.hasContent(blockFormat)) {

                   // ObservableList<Block> items = blocks;
                    Block draggedBlock = (Block) db.getContent(blockFormat);

                    ObservableList<Block> items = window.getController().getBlockListView().getItems();
                    int fromID = items.indexOf(draggedBlock);
                    int toID = items.indexOf(getItem());

                    Block tempBlock = blocks.get(fromID);
                    blocks.set(fromID,getItem());
                    blocks.set(toID,tempBlock);

//
                   if(!blocks.equals(superGenome.getBlocks())){
                        superGenome.getBlocks().setAll(blocks);
                       window.setupView(superGenome,ringDimensions);
                    }


 //                items.set(fromID, getItem());
  //               items.set(toID, draggedBlock);
//
   //                List<Block> itemscopy = new ArrayList<>(window.getController().getBlockListView().getItems());
    //                System.out.println(itemscopy);
    //               getListView().getItems().setAll(itemscopy);

                    success = true;
                }
                event.setDropCompleted(success);
                 event.consume();
            });

            setOnDragDone(DragEvent::consume);

        }

        @Override
        protected void updateItem(Block item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                this.textProperty().unbind();
                this.setText(null);
            } else {
                this.textProperty().bind(item.nameProperty());
            }
        }

        private void setupContextMenu(BlockCell cell){
            cell.setOnContextMenuRequested((e)->{
                ContextMenu contextMenu = new ContextMenu();
                MenuItem changeNameMenuItem = new MenuItem("Change Name");
                contextMenu.getItems().add(changeNameMenuItem);

                changeNameMenuItem.setOnAction((e1)->{
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
                            String initialName = cell.getItem().getInitialName();
                            textfield.setText(initialName);
                            result = initialName;
                            return result;
                        }
                        return null;
                    });

                    Optional<String> result = dialog.showAndWait();

                    result.ifPresent((string)->{
                        cell.getItem().setName(string);
                    });
                });

                contextMenu.show(cell,e.getScreenX(),e.getScreenY());
                });



        }

    }

    /**
     * When loading in a .XMFA file we first have to set the minBlockLength and SubBlocks settings of the SuperGenome.
     * @param superGenome
     * @param file
     * @throws Exception
     */
    private void setupParameters(SuperGenome superGenome, File file) throws Exception{

        if(file.isFile() && file.getName().toLowerCase().endsWith(".xmfa")){
            DialogWindow dialogWindow = new DialogWindow();
            Dialog<Pair<Integer,Boolean>> dialog = new Dialog<>();

            ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

            dialog.getDialogPane().setContent(dialogWindow.getRoot());

            dialog.setResultConverter((button)->{
                if(button == saveButton) {
                    int minBlockSize = 10000;
                    try {
                         minBlockSize = Integer.parseInt(dialogWindow.getController().getBlockSizeInput().getText());
                    }catch (NumberFormatException ex){
                        minBlockSize = 10000;
                        dialogWindow.getController().getBlockSizeInput().setText("10000");
                        System.out.println("Please enter a valid integer");
                    }
                    boolean subBlocks = dialogWindow.getController().getSubBlockCheck().isSelected();
                    return new Pair<>(minBlockSize, subBlocks);
                }
                return null;
            });

            Optional<Pair<Integer,Boolean>> result = dialog.showAndWait();
            result.ifPresent((setting)->{
                superGenome.setMinBlockLength(setting.getKey());
                superGenome.setSubBlocks(setting.getValue());
            });

        }

    }

    /**
     * Exports a screenshot of the view elements in an image file.
     */
    private void setupExportWindow(){
        getWindow().getController().getExportMenuItem().setOnAction((e)->{
            ExportViewWindow exportViewWindow = null;
            Image img = window.getViewAndLegendGroup().snapshot(new SnapshotParameters(),null);

            try {
                exportViewWindow = new ExportViewWindow();
                //exportViewWindow.getController().getImageView().setPreserveRatio(true);
               // exportViewWindow.getController().getImageView().setFitWidth(exportViewWindow.getController().getExportStackPane().getWidth());
               // exportViewWindow.getController().getImageView().setFitHeight(exportViewWindow.getController().getExportStackPane().getHeight());
                exportViewWindow.getController().getImageView().fitHeightProperty().bind(exportViewWindow.getController().getExportBorderPane().heightProperty());
                exportViewWindow.getController().getImageView().fitWidthProperty().bind(exportViewWindow.getController().getExportBorderPane().widthProperty());


                exportViewWindow.getController().getImageView().setImage(img);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            Stage exportStage = new Stage();
            exportStage.setScene(new Scene(exportViewWindow.getRoot(),600,400));


            exportViewWindow.getController().getExportCancelButton().setOnAction((e1)->{
                exportStage.hide();
            });

            exportViewWindow.getController().getExportSaveButton().setOnAction((e2)->{
                FileChooser fileChooser = new FileChooser();

                File initFile = new File(".");
                fileChooser.setInitialDirectory(initFile);

                FileChooser.ExtensionFilter blocksFilter = new FileChooser.ExtensionFilter("Image", "*.png", ".jpeg", "*.PNG","*.JPEG");
                fileChooser.getExtensionFilters().add(blocksFilter);

                File file = fileChooser.showSaveDialog(stage);

                    //to avoid any dependencies with Swing, i used the code given here: https://stackoverflow.com/questions/27054672/writing-javafx-scene-image-image-to-file
                    if(file != null){
                        int width = (int) img.getWidth();
                        int height = (int) img.getHeight();
                        PixelReader reader = img.getPixelReader();
                        byte[] buffer = new byte[width * height * 4];
                        WritablePixelFormat<ByteBuffer> format = PixelFormat.getByteBgraInstance();
                        reader.getPixels(0, 0, width, height, format, buffer, 0, width * 4);
                        try {
                            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                            for(int count = 0; count < buffer.length; count += 4) {
                                out.write(buffer[count + 2]);
                                out.write(buffer[count + 1]);
                                out.write(buffer[count]);
                                out.write(buffer[count + 3]);
                            }
                            out.flush();
                            out.close();
                        } catch(IOException ex) {
                            ex.printStackTrace();
                        }

                    }
                    img.cancel();
                    exportStage.hide();

            });

            exportStage.show();

        });
    }

    /**
     * creates an example SuperGenome without a file.
     * @return example SuperGenome
     */
    private SuperGenome createExample(){
        SuperGenome superGenome = new SuperGenome();

        Block blockA = new Block(superGenome, "A", 100);
        Block blockB = new Block(superGenome, "B", 150);
        Block blockC = new Block(superGenome, "C", 200);
        Block blockD = new Block(superGenome, "D", 80);

        superGenome.addBlock( blockA );
        superGenome.addBlock( blockB );
        superGenome.addBlock( blockC );
        superGenome.addBlock( blockD );

        Genome g1 = new Genome(superGenome, false, "Red");
        g1.addCoveredBlock(new CoveredBlock(blockD, true));
        g1.addCoveredBlock(new CoveredBlock(blockB, false));
        Genome g2 = new Genome(superGenome, false, "Blue");
        g2.addCoveredBlock(new CoveredBlock(blockB, true));
        g2.addCoveredBlock(new CoveredBlock(blockA, false));
        g2.addCoveredBlock(new CoveredBlock(blockC, false));
        Genome g3 = new Genome(superGenome, false, "Green");
        g3.addCoveredBlock(new CoveredBlock(blockA, true));
        g3.addCoveredBlock(new CoveredBlock(blockC, true));
        g3.addCoveredBlock(new CoveredBlock(blockD, true));
        superGenome.addGenome(g1);
        superGenome.addGenome(g2);
        superGenome.addGenome(g3);


        return superGenome;
    }

    /**
     * Used to create and show the example.
     */
    private void displayExample(){
        superGenome = createExample();
        int radius = 100 * superGenome.getNumberOfGenomes();
        setupBlockList();
        setupOptimizer();
        ringDimensions = new RingDimensions(20, radius, 1, 5, superGenome);
        window.setupView(superGenome,ringDimensions);

    }

    /**
     * Sets up the Ring Information Labels easily with Bindings and Listeners.
     * Currently uses decimal commas.
     */
    private void setupRingInfoBox() {

        window.getController().getRingInfoVBOX().visibleProperty().bind(window.getController().getShowRingDimensionsCheckBox().selectedProperty());

        window.getController().getGenomeWidthLabel().textProperty().bind(Bindings.format("%.2f", ringDimensions.genomeWidthProperty()));

        window.getController().getBlockGapLabel().textProperty().bind(Bindings.format("%.2f", ringDimensions.blockGapProperty()));

        window.getController().getCircleSpacingLabel().textProperty().bind(Bindings.format("%.2f", ringDimensions.ringDistanceProperty()));

        window.getController().getRotationLabel().setText(Double.toString(window.getGenomeRingView().getRotate()));

        window.getGenomeRingView().rotateProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                double rotation = (double) t1;
                window.getController().getRotationLabel().setText(Double.toString((rotation % 360)));
            }
        });

    }

    public GenomeRingWindow getWindow() {
        return window;
    }


}

package com.genomeRing.view.genomeRingWindow;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import com.genomeRing.model.structure.RingDimensions;
import com.genomeRing.model.structure.SuperGenome;
import com.genomeRing.view.GenomeRingView;
import com.genomeRing.view.ScaleView;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

public class GenomeRingWindow extends BorderPane {

    private GenomeRingWindowController controller;
    private Parent root;
    private Stage stage;
    private GenomeRingView genomeRingView = new GenomeRingView();
    private StackPane stackPane = new StackPane();
    private StackPane canvasPane = new StackPane();

    private Group scrollContent = new Group();
    private Group zoomContent = new Group();
    private Group completeGroup = new Group();
    private Group viewAndLegendGroup = new Group();



    public GenomeRingWindow(Stage stage) throws Exception {

        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = this.getClass().getResource("GenomeRingWindow.fxml");

       if (url == null)
            throw new Exception("URL not found");
        try  (InputStream ins = url.openStream()) {
            fxmlLoader.load(ins);
        }

        this.root = fxmlLoader.getRoot();
        this.controller = fxmlLoader.getController();
        this.stage = stage;
    }

    /**
     * Creates a new GenomeRingView from the given SuperGenome and places it in the Pane in the Center of the BorderPane.
     * Can be used to redraw everything again.
     * @param superGenome
     * @param ringDimensions
     */
    public void setupView(SuperGenome superGenome, RingDimensions ringDimensions) {
        zoomContent.getChildren().clear();
        stackPane.getChildren().clear();
        controller.getLegendVBox().getChildren().clear();
        completeGroup.getChildren().clear();
        scrollContent.getChildren().clear();
        canvasPane.getChildren().clear();
        viewAndLegendGroup.getChildren().clear();

        genomeRingView.setupView(superGenome, ringDimensions, this);

        completeGroup.getChildren().addAll(drawScale(superGenome,ringDimensions),genomeRingView);

        //show Legend Checkbox
        controller.getLegendVBox().visibleProperty().bind(controller.getShowLegendCheck().selectedProperty());

        setupZoom(superGenome,ringDimensions);

    }

    /**
     * Sets up zooming,rotating and panning with the mouse.
     * Most of the following code is taken from this StackOverflow question: https://stackoverflow.com/questions/16680295/javafx-correct-scaling?noredirect=1&lq=1
     * @param superGenome
     * @param ringDimensions
     */
    private void setupZoom(SuperGenome superGenome, RingDimensions ringDimensions){
        //TODO make the Group be completely visible in the StackPane;
        //FIXME Issue with scrolling while holding shift -> Bug in JavaFX when the shift key is pressed it swapps the DeltaX and DeltaY Values

        stackPane.getChildren().add(completeGroup);
        zoomContent.getChildren().add(stackPane);

        canvasPane.getChildren().add(zoomContent);

        scrollContent.getChildren().add(canvasPane);



        ScrollPane scroller = controller.getScrollPane();
        scroller.setContent(scrollContent);

        scroller.viewportBoundsProperty().addListener(new ChangeListener<Bounds>() {
            @Override
            public void changed(ObservableValue<? extends Bounds> observable,
                                Bounds oldValue, Bounds newValue) {
                canvasPane.setMinSize(newValue.getWidth(), newValue.getHeight());
            }
        });

        scroller.setPrefViewportWidth(256);
        scroller.setPrefViewportHeight(256);

        stackPane.setOnScroll((event -> {
            event.consume();
            //changing Distance between the Rings: CTRL + Shift
             if(event.isShiftDown() && event.isControlDown() && !event.isAltDown()){
                ringDimensions.changeRingDistance(ringDimensions.getRingDistance()+event.getDeltaX()/100);
                this.setupView(superGenome,ringDimensions);
            }
            //changing the GenomeWidth: Shift
             if(event.isShiftDown() && !event.isAltDown() && !event.isControlDown()){
                ringDimensions.changeGenomeWidth(ringDimensions.getGenomeWidth()+event.getDeltaX()/100);
                this.setupView(superGenome,ringDimensions);
            }

            //changing the Block Gap: Alt
            else if(event.isAltDown() && !event.isShiftDown()){
                ringDimensions.changeBlockGap(ringDimensions.getBlockGap() + event.getDeltaY()/100);
                this.setupView(superGenome,ringDimensions);
            }
            //For Zooming. Scales the GenomeRing up: CTRL
            else if (event.isControlDown() && !event.isAltDown()) {
                 if(event.getDeltaY() == 0){
                     return;
                 }

                 Point2D scrollOffset = figureScrollOffset(scrollContent, scroller);

                 final double SCALE_DELTA = 1.1;
                 double scaleFactor = (event.getDeltaY()>0) ? SCALE_DELTA : 1/SCALE_DELTA;
                 stackPane.setScaleX(stackPane.getScaleX() * scaleFactor);
                 stackPane.setScaleY(stackPane.getScaleY() * scaleFactor);

                 repositionScroller(scrollContent, scroller, scaleFactor, scrollOffset);
             }
            else {
                //We only want to rotate the GenomeRing not the scale
                genomeRingView.setRotate(genomeRingView.getRotate() + event.getDeltaY() / 10);
            }
        }));



        // Panning via drag.... //TODO might use this to also determine GenomePosition
        final ObjectProperty<Point2D> lastMouseCoordinates = new SimpleObjectProperty<Point2D>();
        scrollContent.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                lastMouseCoordinates.set(new Point2D(event.getX(), event.getY()));
            }
        });

        scrollContent.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double deltaX = event.getX() - lastMouseCoordinates.get().getX();
                double extraWidth = scrollContent.getLayoutBounds().getWidth() - scroller.getViewportBounds().getWidth();
                double deltaH = deltaX * (scroller.getHmax() - scroller.getHmin()) / extraWidth;
                double desiredH = scroller.getHvalue() - deltaH;
                scroller.setHvalue(Math.max(0, Math.min(scroller.getHmax(), desiredH)));

                double deltaY = event.getY() - lastMouseCoordinates.get().getY();
                double extraHeight = scrollContent.getLayoutBounds().getHeight() - scroller.getViewportBounds().getHeight();
                double deltaV = deltaY * (scroller.getHmax() - scroller.getHmin()) / extraHeight;
                double desiredV = scroller.getVvalue() - deltaV;
                scroller.setVvalue(Math.max(0, Math.min(scroller.getVmax(), desiredV)));
            }
        });



        //makes it so the GenomeRing never leaves the bound of the centerPane in the BorderPane
//        stackPane.layoutBoundsProperty().addListener(new ChangeListener<Bounds>() {
//            @Override
//            public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
//                stackPane.setClip(new Rectangle(newValue.getMinX(),newValue.getMinY(),newValue.getWidth(),newValue.getHeight()));
//            }
//        });
    }

    private Point2D figureScrollOffset(Node scrollContent, ScrollPane scroller) {
        double extraWidth = scrollContent.getLayoutBounds().getWidth() - scroller.getViewportBounds().getWidth();
        double hScrollProportion = (scroller.getHvalue() - scroller.getHmin()) / (scroller.getHmax() - scroller.getHmin());
        double scrollXOffset = hScrollProportion * Math.max(0, extraWidth);
        double extraHeight = scrollContent.getLayoutBounds().getHeight() - scroller.getViewportBounds().getHeight();
        double vScrollProportion = (scroller.getVvalue() - scroller.getVmin()) / (scroller.getVmax() - scroller.getVmin());
        double scrollYOffset = vScrollProportion * Math.max(0, extraHeight);
        return new Point2D(scrollXOffset, scrollYOffset);
    }

    private void repositionScroller(Node scrollContent, ScrollPane scroller, double scaleFactor, Point2D scrollOffset) {
        double scrollXOffset = scrollOffset.getX();
        double scrollYOffset = scrollOffset.getY();
        double extraWidth = scrollContent.getLayoutBounds().getWidth() - scroller.getViewportBounds().getWidth();
        if (extraWidth > 0) {
            double halfWidth = scroller.getViewportBounds().getWidth() / 2 ;
            double newScrollXOffset = (scaleFactor - 1) *  halfWidth + scaleFactor * scrollXOffset;
            scroller.setHvalue(scroller.getHmin() + newScrollXOffset * (scroller.getHmax() - scroller.getHmin()) / extraWidth);
        } else {
            scroller.setHvalue(scroller.getHmin());
        }
        double extraHeight = scrollContent.getLayoutBounds().getHeight() - scroller.getViewportBounds().getHeight();
        if (extraHeight > 0) {
            double halfHeight = scroller.getViewportBounds().getHeight() / 2 ;
            double newScrollYOffset = (scaleFactor - 1) * halfHeight + scaleFactor * scrollYOffset;
            scroller.setVvalue(scroller.getVmin() + newScrollYOffset * (scroller.getVmax() - scroller.getVmin()) / extraHeight);
        } else {
            scroller.setHvalue(scroller.getHmin());
        }
    }



    /**
     * remove every view element created by the SuperGenome and reset the Checkboxes
     */
    public void resetView(){
        genomeRingView.getChildren().clear();
        completeGroup.getChildren().clear();
        controller.getLegendVBox().getChildren().clear();

        controller.getRestoreOrderMenuItem().setSelected(true);

        controller.getShowPathsCheckBox().setSelected(true);
       // controller.getShowRingDimensionsCheckBox().setSelected(false);
        controller.getShowLegendCheck().setSelected(true);

    }

    /**
     * Draws the scale for the GenomeRing.
     */
    private ScaleView drawScale(SuperGenome superGenome, RingDimensions ringDimensions){

        ScaleView scaleView = new ScaleView(superGenome,ringDimensions,45,3);
        scaleView.visibleProperty().bind(controller.getScaleCheckBox().selectedProperty());
        return scaleView;
    }



    public GenomeRingWindowController getController() {
        return controller;
    }

    public Parent getRoot() {
        return root;
    }

    public Stage getStage() {
        return stage;
    }

    public GenomeRingView getGenomeRingView() {
        return genomeRingView;
    }

    public Group getCompleteGroup() {
        return completeGroup;
    }

    public Group getViewAndLegendGroup() {
        return viewAndLegendGroup;
    }
}

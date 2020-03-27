package com.genomeRing.view;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

//TODO change Name and clean up
public class PathView extends Group {

    private SimpleObjectProperty<Color> color = new SimpleObjectProperty<>();
    private StrokeLineJoin strokeLineJoin;
    private StrokeLineCap strokeLineCap;
    private SimpleDoubleProperty width = new SimpleDoubleProperty();

    public PathView(){

    }
    public void setStyle(Color color, StrokeLineCap strokeLineCap, StrokeLineJoin strokeLineJoin, Double width) {
        if(!this.color.isBound()){
            this.setColor(color);
        }

        this.strokeLineJoin = strokeLineJoin;
        this.strokeLineCap = strokeLineCap;
        this.setWidth(width);
    }


    public void setShapeStyle(Shape shape) {
        shape.strokeProperty().bind(color);
        shape.setFill(null);
        shape.setStrokeLineCap(this.strokeLineCap);
        shape.setStrokeLineJoin(this.strokeLineJoin);
        shape.strokeWidthProperty().bind(width);
    }


    public void addSegment(double radius, double alpha_start, double alpha_end, boolean connect) {

    }


    public static double polarToX(double radius, double alpha) {
        return Math.sin(Math.toRadians(90+alpha))*radius;
    }

    public static double polarToY(double radius, double alpha) {
        return Math.cos(Math.toRadians(90+alpha))*radius;
    }

    public static double XYtoPolarAlpha(double x, double y) {
        return 90+Math.toDegrees(Math.atan( y / x ))+(x<0?180:0);
    }

    public static double XYtoPolarRadius(double x, double y) {
        return Math.sqrt(x*x + y*y);
    }


    public Color getColor() {
        return color.get();
    }

    public SimpleObjectProperty<Color> colorProperty() {
        return color;
    }

    public void setColor(Color color) {
        this.color.set(color);
    }

    public StrokeLineJoin getStrokeLineJoin() {
        return strokeLineJoin;
    }

    public void setStrokeLineJoin(StrokeLineJoin strokeLineJoin) {
        this.strokeLineJoin = strokeLineJoin;
    }

    public StrokeLineCap getStrokeLineCap() {
        return strokeLineCap;
    }

    public void setStrokeLineCap(StrokeLineCap strokeLineCap) {
        this.strokeLineCap = strokeLineCap;
    }

    public double getWidth() {
        return width.get();
    }

    public SimpleDoubleProperty widthProperty() {
        return width;
    }

    public void setWidth(double width) {
        this.width.set(width);
    }
}

module genomeRing {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.genomeRing to javafx.fxml;
    exports com.genomeRing;
    opens com.genomeRing.view.genomeRingWindow to javafx.fxml;
    exports com.genomeRing.view.genomeRingWindow;
    opens com.genomeRing.view.dialogWindow to javafx.fxml;
    exports com.genomeRing.view.dialogWindow;
}
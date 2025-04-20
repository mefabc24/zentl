module org.example.demo3 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires annotations;

    exports org.example.demo3.view;
    opens org.example.demo3.view to javafx.fxml;
    exports org.example.demo3.model.board;
    exports org.example.demo3.model.cards;
    exports org.example.demo3.model.player;
    exports org.example.demo3.model.enums;

}
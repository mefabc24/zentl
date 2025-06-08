module org.example.demo3 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires annotations;
    requires com.google.gson;
    requires org.slf4j;
    requires javafx.media;

    opens org.example.demo3.view to com.google.gson, javafx.fxml;
    opens org.example.demo3.model.cards to com.google.gson;

    exports org.example.demo3.model.board;
    exports org.example.demo3.model.cards;
    exports org.example.demo3.model.player;
    exports org.example.demo3.model.enums;
    exports org.example.demo3.model.constants;
    exports org.example.demo3.model.logic;
    exports org.example.demo3.model.service;
    exports org.example.demo3.view;

}
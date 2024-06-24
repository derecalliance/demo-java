module org.derecalliance.derec.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.zxing.javase;
    requires com.google.zxing;
    requires javafx.swing;
//    requires org.derecalliance.derec.lib;
    requires org.derecalliance.derec.lib.api;
    requires org.derecalliance.derec.lib.impl;
    requires org.controlsfx.controls;
    requires org.slf4j;

    uses org.derecalliance.derec.lib.api.SharerFactory;
    uses org.derecalliance.derec.lib.api.HelperFactory;
    uses org.derecalliance.derec.lib.api.ContactFactory;



    opens org.derecalliance.derec.demo to javafx.fxml;
    exports org.derecalliance.derec.demo;
}
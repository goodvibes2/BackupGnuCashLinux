/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

module backupgnucash {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.logging;
    requires java.desktop;

    opens org.openjfx to javafx.fxml;
    exports org.openjfx;
}

/*
 * Copyright (C) 2020 Chris Good
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * 25/05/2016 cgood Remove unused imports.
 */

package org.openjfx;

import javafx.application.Application;
//import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
//import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
//import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * This project backs up and encrypts GnuCash data to Dropbox using 7-Zip.
 * Platform: Windows 7/10
 * 
 * 30/04/2016 CRG Created
 * 
 * @author cgood
 */
public class BackupGnuCash extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("BackupGnuCash.fxml"));
        Scene scene = new Scene(root);
        
        stage.setTitle("Backup GnuCash");        
        stage.setScene(scene);
        
        scene.getStylesheets().add(BackupGnuCash.class.getResource("BackupGnuCash.css").toExternalForm());

        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
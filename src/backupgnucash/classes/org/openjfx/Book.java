/*
 * Copyright (C) 2020 cgood
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
package org.openjfx;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author cgood
 */
public class Book {
    
    /// instance variables
    private final SimpleStringProperty bookName;
    private final SimpleStringProperty gcDat;
    private final SimpleStringProperty gcVer;
    private final SimpleBooleanProperty gcV2Cfg;
    private final SimpleBooleanProperty gcV3Cfg;
    private final SimpleStringProperty dropBox;
    
    // class variables
    private static String defaultBook = "";
    static final StringProperty defaultProp = new SimpleStringProperty();

    // constructor
    public Book(String startBookName, String startGcDat, String startGcVer,
            Boolean startGcV2Cfg, Boolean startGcV3Cfg, String startDropBox) {
        this.bookName = new SimpleStringProperty(startBookName);
        this.gcDat = new SimpleStringProperty(startGcDat);
        this.gcVer = new SimpleStringProperty(startGcVer);
        this.gcV2Cfg = new SimpleBooleanProperty(startGcV2Cfg);
        this.gcV3Cfg = new SimpleBooleanProperty(startGcV3Cfg);
        this.dropBox = new SimpleStringProperty(startDropBox);
    }
 
    // class methods
    public static String getDefaultBook() {
        return defaultBook;
    }
    public static void setDefaultBook(String newDefaultBook) {
        Book.defaultBook = newDefaultBook;
        Book.defaultProp.set(newDefaultBook);
}
    
    // instance methods
    public String getBookName() {
        return bookName.get();
    }
    public void setBookName(String bName) {
        bookName.set(bName);
    }
        
    public String getGcDat() {
        return gcDat.get();
    }
    public void setGcDat(String gcDatStr) {
        gcDat.set(gcDatStr);
    }
    
    public String getGcVer() {
        return gcVer.get();
    }
    public void setGcVer(String gcVerStr) {
        gcVer.set(gcVerStr);
    }
    
    public Boolean getGcV2Cfg() {
//        System.out.println("book.getGcV2Cfg returns: " + gcV2Cfg.get() );
        return gcV2Cfg.get();
    }
    public void setGcV2Cfg(Boolean gcV2CfgBool) {
        gcV2Cfg.set(gcV2CfgBool);
    }

    public Boolean getGcV3Cfg() {
//        System.out.println("book.getGcV3Cfg returns: " + gcV3Cfg.get() );
        return gcV3Cfg.get();
    }
    public void setGcV3Cfg(Boolean gcV3CfgBool) {
        gcV3Cfg.set(gcV3CfgBool);
    }

    public String getDropBox() {
        return dropBox.get();
    }
    public void setDropBox(String dropBoxStr) {
        dropBox.set(dropBoxStr);
    }
           
}

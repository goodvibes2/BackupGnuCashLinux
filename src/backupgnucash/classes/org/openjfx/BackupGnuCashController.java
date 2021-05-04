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

/* Date       Vers Comment
   30/04/2016 1.00 Created.
   15/05/2016 1.01 Backup straight to Dropbox/GnuCash i.e. no need to keep
                    another copy in the Backup subdirectory of the GnuCash data
                    directory.
   16/05/2016 1.02 Fix FileName.filename index out of bounds when passing a
                    file name without an extension.
   25/05/2016 1.10 Port to Linux
                   a. Remove unused import.
                   b. If not Windows, use getenv("USER") instead of USERNAME to
                      fix NullPointerException in Ubuntu.
                   c. DropBox -> Dropbox.
   28/06/2016 1.20 1. Also check for Windows 7-Zip in Program Files (x86).
                   2. Allow configurations for multiple books.
                   3. Make ToolTips font bigger (in BackupGnuCash.fxml).
   27/05/2018 1.3.0 Mods for GnuCash 3, add options for backing up V2 + V3
                    configuration and add Help button.
   26/02/2019 1.3.1 Linux Java 8 Help button causes hang: wrap the call to any
                    AWT APIs (Desktop.getDesktop().browse) in a runnable and
                    submit it for execution via java.awt.EventQueue.invokeLater().
   28/07/2019 2.0.0 Convert project to a modular java 11 project.

   See src/backupgnucash/ChangeLog.txt
*/

package org.openjfx;

import java.awt.Desktop;
import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import static java.lang.Thread.sleep;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
//import java.util.Set;
import java.util.logging.*;
//import java.util.logging.Level;
//import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
//import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Callback;

/**
 *
 * @author cgood
 */
public class BackupGnuCashController implements Initializable {

    /* class variables (static) */

    @FXML
    private GridPane grid;
    @FXML
    private Text sceneTitle;
    @FXML
    private Text versionNo;
    @FXML
    private Label bookLbl;
    @FXML
//  private ComboBox<Book> bookComboBox;
    private ComboBox bookComboBox;
    @FXML
    private Button btnDelete;
    @FXML
    private Button btnHelp;
    @FXML
    private CheckBox defaultBookChb;
    @FXML
    private Label lblGCDatFilStr;
    @FXML
    private TextField txtGcDatFilStr;
    @FXML
    private Button btnChooseGCDatFil;
    @FXML
    private Label lblGCModDate;
    @FXML
    private Label lblGCVer;
    @FXML
    private TextField txtGcVer;
    @FXML
    private Label lblGcCfgData;
    @FXML
    private CheckBox chbGcV2Cfg;
    @FXML
    private CheckBox chbGcV3Cfg;
    @FXML
    private Label lblDropBox;
    @FXML
    private TextField txtDropBox;
    @FXML
    private Button btnChooseDropBox;
    @FXML
    private Button btnSaveSettings;
    @FXML
    private Label lblPswd;
    @FXML
    private PasswordField txtPswd;
    @FXML
    private TextField txtVisPswd;
    @FXML
    private CheckBox chbShowPswd;
    @FXML
    private Button btnBupGC;
    @FXML
    private Separator sep1;
    @FXML
    private Separator sep2;
    @FXML
    private Separator sep4;
  //@FXML
  //private Label lblShowPswd;
    @FXML
    private Label lblLog;
    @FXML
    private TextArea taLog;

    final private static int MAX_BOOKS = 100;
//  final private ObservableList<Book> bookComboBoxData = FXCollections.observableArrayList();
    final private ObservableList       bookComboBoxData = FXCollections.observableArrayList();
    final private Map bookMap = new HashMap(MAX_BOOKS); // key=bookName, value=ref to Book instance

    final private static String OS_NAME = System.getProperty("os.name" );
    private static String USER_NAME;
    // character that separates folders from files in paths
    //  i.e. Windows = backslash, Linux/OSX = /
    private static final char FILE_SEPARATOR =
        System.getProperty("file.separator").charAt(0);
    private static String gcBook = "MyBook";   // current book name
    private static String gcDatFil; // initial default GnuCash data file
    private static String gcVer = ""; // optional version backup filename suffix
    private static Boolean gcV2Cfg = true; // Backup GnuCash V2 config files ?
    private static Boolean gcV3Cfg = true; // Backup GnuCash V3 config files ?

    private static String dropBox; // inital default Dropbox dir

    private static Path pathGcDatFilStr;
    private static final String HOME_DIR = System.getProperty("user.home");
    private static final String PROPERTIES_DIR = ".BupGc";
    private static final String ERR_FILE = HOME_DIR + FILE_SEPARATOR +
            PROPERTIES_DIR + FILE_SEPARATOR + "BackGnuCash.err";
    private static final String OUT_FILE = HOME_DIR + FILE_SEPARATOR +
            PROPERTIES_DIR + FILE_SEPARATOR + "BackGnuCash.out";
    private static final String OUT_REG_FILE = HOME_DIR + FILE_SEPARATOR +
            PROPERTIES_DIR + FILE_SEPARATOR + "GnuCashGSettings.reg";
    private static final String OUT_DCONF_FILE = HOME_DIR + FILE_SEPARATOR +
            PROPERTIES_DIR + FILE_SEPARATOR + "gnucash.dconf";
    // Saved Settings
    private static final String DEFAULT_PROP = "defaultBook";
    private static final String BOOKNAME_PROP = "gcBook.";
    private static final String DROPBOX_PROP = "dropBox.";
    private static final String GCDATFIL_PROP = "gcDatFil.";
    private static final String GCVER_PROP = "gcVer.";
    private static final String GCV2CFG_PROP = "gcV2Cfg.";
    private static final String GCV3CFG_PROP = "gcV3Cfg.";

    private static final String DEF_PROP = HOME_DIR + FILE_SEPARATOR +
            PROPERTIES_DIR + FILE_SEPARATOR + "defaultProperties";
    //  default properties
    private static final Properties defaultProps = new Properties();

    private static final String HELP_URL = "https://github.com/goodvibes2/BackupGnuCashWin/blob/master/src/backupgnucash/README.md";


    // firstTime: true when enable_or_disable_buttons() is run for first time
    //  (ie from initialize()), which means controls are not ready to accept focus,
    //  so have to delay setting focus to password
    private static boolean firstTime = true;

    // loadingScreen: true when loading screen fields from saved book.
    // Used to stop screen control listeners from doing anything while
    //  loading screen controls
    private static boolean loadingScreen = true;

    private static String bookSelectionTarget = "";

    private static final Font BOLD_FONT = Font.font("System", FontWeight.BOLD, 14);
    private static final Font NORMAL_FONT = Font.font("System", FontWeight.NORMAL, 14);
    private final Desktop desktop = Desktop.getDesktop();

    @FXML
    public void handleBtnActionDelete(Event e) throws IOException {

        // Note: In Java, there is no way to immediately destroy an instance.
        //  Instances are automatically cleaned up by garbage collection
        //   sometime after they are no longer referenced so there is no
        //   need to dispose of the deleted Book instance here.

        // Note: The deleteBtn is disabled when on the default book as
        //  this user interface does not permit deleting the default book

        String tmpBook = (String) bookComboBox.getValue();
        if (((tmpBook != null) &&  (! tmpBook.isEmpty()))) {
            if (bookMap.size() > 1) {
                bookMap.remove(tmpBook);
                bookComboBoxData.remove(tmpBook);
                bookComboBox.setValue(Book.getDefaultBook());
//              bookComboBox.getItems().remove(tmpBook); // Always remove from bookComboBoxData instead!
                bookComboBoxData.remove(tmpBook);
                enable_or_disable_buttons();
            }
        }
        // Remove all entries for the deleted Book Name from defaultProps
        // or they will still be saved to defaultProperties file next time the
        // settings are saved. The deleted name may not have already been saved.

        Enumeration<?> enumPropertyNames = defaultProps.propertyNames();
        String suffix = "";
        while(enumPropertyNames.hasMoreElements())
	{
            String key = (String) enumPropertyNames.nextElement();
            if (key != null && key.contains(BOOKNAME_PROP)) {
                if (defaultProps.getProperty(key).equals(tmpBook)) {
                    suffix = key.substring(key.indexOf(".")+1);
                    break;
                }
            }
        }
        if (!suffix.isEmpty()) {
            defaultProps.remove(BOOKNAME_PROP + suffix);
            defaultProps.remove(DROPBOX_PROP + suffix);
            defaultProps.remove(GCDATFIL_PROP + suffix);
            defaultProps.remove(GCVER_PROP + suffix);
            defaultProps.remove(GCV2CFG_PROP + suffix);
            defaultProps.remove(GCV3CFG_PROP + suffix);
        }
    }

    @FXML
    public void handleBtnActionHelp(Event e) throws IOException {

        if (desktop.isDesktopSupported()) {
            EventQueue.invokeLater(() -> {
                try {
                    desktop.browse(new URI(HELP_URL));
                } catch (IOException | URISyntaxException  ex) {
                    Logger.getLogger(BackupGnuCashController.class.getName())
                         .log(Level.SEVERE, null, ex);
                }
            });
        } else {
            taLog.appendText("Error: Desktop is not supported. Cannot open " +
                HELP_URL + "\n");
        }
    }

    @FXML
    public void handleBtnActionSaveSettings(Event e) throws IOException {

        int i = 0;
        String suffix;
        String tmpBook;

        defaultProps.setProperty(DEFAULT_PROP, Book.getDefaultBook());

        // Until problem in Java 8u92 with adding items to ComboBox which uses SortedList is fixed,
        //  sort the books before saving

//      Set bookSet = bookMap.keySet();
//      Iterator itr = bookSet.iterator();

        SortedList sortedBookList = new SortedList<>(bookComboBoxData, Collator.getInstance());
        Iterator itr = sortedBookList.iterator();

        while (itr.hasNext()) {
            tmpBook = (String) itr.next();
            Book refBook = (Book) bookMap.get(tmpBook);
            suffix = String.valueOf(i++);
            defaultProps.setProperty(BOOKNAME_PROP + suffix, refBook.getBookName());
            defaultProps.setProperty(DROPBOX_PROP + suffix, refBook.getDropBox());
            defaultProps.setProperty(GCDATFIL_PROP + suffix, refBook.getGcDat());
            defaultProps.setProperty(GCVER_PROP + suffix, refBook.getGcVer());
            defaultProps.setProperty(GCV2CFG_PROP + suffix, refBook.getGcV2Cfg().toString());
            defaultProps.setProperty(GCV3CFG_PROP + suffix, refBook.getGcV3Cfg().toString()
            );
        }

        try (FileOutputStream out = new FileOutputStream(DEF_PROP)) {
            defaultProps.store(out, "---Backup GnuCash Settings---");
            taLog.setText("Settings successfully saved to " + DEF_PROP);
        } catch (IOException ex) {
            //System.out.println("My Exception Message " + ex.getMessage());
            //System.out.println("My Exception Class " + ex.getClass());
            Logger.getLogger(BackupGnuCashController.class.getName()).log(Level.SEVERE, null, ex);
            taLog.setText("Error: Cannot Save Settings to : " + DEF_PROP);
        }
    }


    // NOTE: There was a bug which was fixed in Java 1.8.0_72 (or maybe 1.8.0_74 ?)
    //        https://bugs.openjdk.java.net/browse/JDK-8136838
    //      which meant the value of ComboBox.getValue() was not correct.
    //   Therefore, For BackupGnuCash V#1.20 or later, which now uses
    //    an editable combobox for the book settings,
    //   MUST use Java 1.8.0_72 or later!
    //
    //   Ubuntu 16.04 openjfx 8u60-b27-4 seems to work OK except must press
    //    ENTER after typing new book name into bookComboBox to get OnAction
    //    to fire. OnAction does not fire if focus changes away.

    // Handle selections in editable bookComboBox

    // This event occurs whenever a new item is selected.
    // This can be because a new item is clicked in the dropdown list
    //  or a new item was keyed into the combobox (and action key ENTER typed or focus lost)
    //  or a new item is automatically selected because the previous
    //   selected item has been removed from the item list.

    // This event does nothing if:
    //  bookSelectionTarget is not Empty AND != the new selection (selected).
    // This is to avoid loops from when a new book becomes selected
    //  after a book is intentionally removed from the combobox
    //   (in order to force the dropdown list to be re-rendered),
    //  then added again.
    // 14/07/2016: Now that books are not intentionally removed, then added again
    //   to force re-rendering, bookSelectionTarget may no longer be needed but
    //   has been left in here just in case...

    @FXML
    public void handleBookComboBoxOnAction(Event event) {
        String selected;
        if (bookComboBox.getValue() == null) {
            selected = "";
        } else {
            selected = bookComboBox.getValue().toString();
        }
        //String editted = bookComboBox.getEditor().getText();
        System.out.println("handleBookComboBoxOnAction(): selected: " + selected
        //  + " editted=" + editted
        );

        if
        ( ( (selected != null) && (! selected.isEmpty()))
          &&
          ((bookSelectionTarget.isEmpty()) || (bookSelectionTarget.equals(selected)))
        ) {
            // If new book (selected) already exists
            //   change to it and show related fields
            // else
            //   add the new book instance to Book class, bookMap and
            //   bookComboBoxData and make it the
            //   selected combobox item

            loadingScreen = true;   // disable listeners
            if (bookMap.containsKey(selected)) {
                // Get ref to book object from bookMap
                Book book = (Book)bookMap.get(selected);
                //bookComboBox.setValue(selecteded); // set selected Value - do NOT do here - causes loop
//                System.out.println("bookComboBox.setOnAction: txtGcDatFilStr.setText to " + book.getGcDat());
                txtGcDatFilStr.setText(book.getGcDat());
                txtGcVer.setText(book.getGcVer());
                chbGcV2Cfg.setSelected(book.getGcV2Cfg());
                chbGcV3Cfg.setSelected(book.getGcV3Cfg());
                txtDropBox.setText(book.getDropBox());
            } else {
                Book book = new Book(selected, txtGcDatFilStr.getText(), txtGcVer.getText(),
                        chbGcV2Cfg.isSelected(), chbGcV3Cfg.isSelected(), txtDropBox.getText());
                bookMap.put(selected, book);
                //bookComboBox.setValue(selected);     // set selected Value - do NOT do here causes loop
                bookComboBoxData.add(selected);
            }
            if (Book.getDefaultBook().equals(selected)) {
                if (! defaultBookChb.isSelected()) {
                    defaultBookChb.setSelected(true);
                }
            } else {
                if (defaultBookChb.isSelected()) {
                    defaultBookChb.setSelected(false);
                }
            }
            loadingScreen = false;  // enable listeners
        }
        enable_or_disable_buttons();
    }

    public void getUserDefaults() {

        bookComboBox.setEditable(true);
        bookComboBox.setVisibleRowCount(20);

        try (   // with resources
            FileInputStream in = new FileInputStream(DEF_PROP);
        )
        {
            int i = 0;
            String suffix;
            String tmpStr;

            defaultProps.load(in);

            // Load old properties from versions of BackupGnuCash before 1.20
            // which did not have a numeric suffix and only allowed 1 saved
            // setting i.e. gcDatFil, gcVer and dropBox
            // Do not delete the old properties, so user can revert back to
            //  an old verions of BackupGnuCash if needed.
            // Only load the old format properties if the new properties
            // are not defined so we only do the conversion once

            gcV2Cfg = true; // Did not exist in versions before 1.20
            gcV3Cfg = true; // Did not exist in versions before 1.20

            // Check for existence of new property gcBook.0
            tmpStr = defaultProps.getProperty(BOOKNAME_PROP + "0");
            if ((tmpStr == null) || (tmpStr.isEmpty())) {
                // new property does NOT exist so load old properties
                tmpStr = defaultProps.getProperty("gcDatFil");
                if ((tmpStr != null) && (!tmpStr.isEmpty())) {
                    gcDatFil = tmpStr;
                }
                tmpStr = defaultProps.getProperty("gcVer");
                if ((tmpStr != null) && (!tmpStr.isEmpty())) {
                    gcVer = tmpStr;
                }
                tmpStr = defaultProps.getProperty("dropBox");
                if ((tmpStr != null) && (!tmpStr.isEmpty())) {
                    dropBox = tmpStr;
                }

                // Use gcDatFil filename without .guncash extension
                //  as book name gcBook
                FileName fileName = new FileName(gcDatFil, FILE_SEPARATOR, '.');
                gcBook = fileName.filename();
            }
            tmpStr = defaultProps.getProperty(DEFAULT_PROP);
            if (tmpStr == null || tmpStr.isEmpty()) {
                tmpStr = gcBook;
            }
            Book.setDefaultBook(tmpStr);
            // Set the bookComboBox selected item
            // Note: Java 1.8.0_05 : needed to set selected item BEFORE populating the combobox
            //          or the selected item is not displayed initially
            //       Java 1.8.0_92 : NO need to set selected item before populating the combobox
            //  As need to use Java 1.8.0_72 or later anyway due to
            //      https://bugs.openjdk.java.net/browse/JDK-8136838,
            //   don't worry about 1.8.0_05
            bookComboBox.setValue(tmpStr);
//            System.out.println("getUserDefault(): Default book=" + tmpStr);

            // load book settings into collection bookComboBoxData, then bookComboBox
            while (i < MAX_BOOKS) {
                suffix = String.valueOf(i++);
                tmpStr = defaultProps.getProperty(BOOKNAME_PROP + suffix);
                if (tmpStr == null) {
                    break;
                }
                gcBook = tmpStr;
                gcDatFil = defaultProps.getProperty(GCDATFIL_PROP + suffix);
                gcVer = defaultProps.getProperty(GCVER_PROP + suffix);
                gcV2Cfg = Boolean.valueOf(defaultProps.getProperty(GCV2CFG_PROP + suffix));
                gcV3Cfg = Boolean.valueOf(defaultProps.getProperty(GCV3CFG_PROP + suffix));
                dropBox = defaultProps.getProperty(DROPBOX_PROP + suffix);

                Book book = new Book(gcBook, gcDatFil, gcVer, gcV2Cfg, gcV3Cfg, dropBox);
                bookComboBoxData.add(gcBook);
                bookMap.put(gcBook, book);  // save ref to book in hashmap

                if (Book.getDefaultBook().equals(gcBook)) {
                    txtGcDatFilStr.setText(gcDatFil);
//                    System.out.println("getUserDefaults(): txtGcDatFilStr set to " + gcDatFil);
                    txtGcVer.setText(gcVer);
                    chbGcV2Cfg.setSelected(gcV2Cfg);
                    chbGcV3Cfg.setSelected(gcV3Cfg);
                    txtDropBox.setText(dropBox);
                    defaultBookChb.setSelected(true);
                }
                //i++;
            }
            if (bookComboBoxData.isEmpty()) {
                Book book = new Book(gcBook, gcDatFil, gcVer, gcV2Cfg, gcV3Cfg, dropBox);
                bookComboBoxData.add(gcBook);
                bookMap.put(gcBook, book);
            }

            // load initial values into bookComboBox
            // bookComboBox.getItems().addAll(bookComboBoxData);
            // Above line works but it is not clear these are inital values.
            //  Also, not recommended - see https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/ListView.html
            //   Using setItems, changes to the observableList are observed and acted on.
            bookComboBox.setItems(bookComboBoxData);      // items are not sorted using this

            // Note: Due to bug https://bugs.openjdk.java.net/browse/JDK-8087838 in Java 8u92
            //  it is not advisable to use SortedList, so until Java 9 is
            //  generally available (Sep 2016 ?), do not use SortedList, but
            //  sort books before they are saved.
            //  See also http://stackoverflow.com/questions/38342046/how-to-use-a-sortedlist-with-javafx-editable-combobox-strange-onaction-events

            //bookComboBox.setItems(new SortedList<>(bookComboBoxData, Collator.getInstance()));
            // Note: Always add/remove bookComboBox items to/from bookComboBoxData
            //  E.g. using       bookComboBoxData.add
            //       instead of  bookComboBox.getItems().add

            //bookComboBox.setEditable(true);
            //bookComboBox.setVisibleRowCount(20);
            //bookComboBox.setValue(Book.getDefaultBook());

            //in.close();  // done automatically when 'try with resources' ends
        } catch (IOException ex) {
            //System.out.println("My Exception Message " + ex.getMessage());
            //System.out.println("My Exception Class " + ex.getClass());

            if (ex.getClass().toString().equals("class java.io.FileNotFoundException")) {
//              System.out.println("getUserDefaults: " + ex.getMessage());
                Book.setDefaultBook(gcBook);
                Book book = new Book(gcBook, gcDatFil, gcVer, gcV2Cfg, gcV3Cfg, dropBox);
                bookComboBoxData.add(gcBook);
                bookMap.put(gcBook, book);
//              bookComboBox.setItems(new SortedList<>(bookComboBoxData, Collator.getInstance()));  // JDK-8087838
                bookComboBox.setItems(bookComboBoxData);
                bookComboBox.setValue(gcBook);
                defaultBookChb.setSelected(true);
                chbGcV2Cfg.setSelected(gcV2Cfg);
                chbGcV3Cfg.setSelected(gcV3Cfg);
            } else {
                Logger.getLogger(BackupGnuCashController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static String getUser() {
        return USER_NAME;
    }

    public static String getGcDatFil() {
        return gcDatFil;
    }

    public static String getGcVer() {
            return gcVer;
    }

    public static String getDropBoxDir() {
        return dropBox;
    }

    /**
     * Check logged in user is valid
     * @return boolean
     */
    static boolean isValidUser() {
        return !USER_NAME.isEmpty();
    }

    boolean isValidPswd() {
        return txtPswd.getText().length() > 7 ;
    }

    boolean exportRegistry() {

        // Use reg.exe to export GnuCash registry entries to a text file which can be backed up

        int exitVal = 0;
        String[] cmdExport = new String[5];

        // chk C:\Windows\System32\reg.exe exists
        //  %SystemRoot% is usually C:\Windows
        Path pathRegExe = Paths.get(System.getenv("SystemRoot") + "\\System32\\reg.exe");
        if (! Files.isExecutable(pathRegExe)) {
            taLog.appendText("Error: Cannot find or execute " +
                pathRegExe.toString() + " on either C: or E:" );
            return false;
        }

        Runtime rt = Runtime.getRuntime();
        System.out.println("Execing reg.exe");
        taLog.appendText("Exporting GnuCash registry entries...\n");

        // Set up cmdExport to be like
        //  C:\Windows\System32\reg.exe
        //    EXPORT HKCU\Software\GSettings\org\gnucash C:\Users\[Name]\.BupGC\GnuCashGSettings.reg /y
        cmdExport[0] = pathRegExe.toString();
        cmdExport[1] = "EXPORT";
        cmdExport[2] = "HKCU\\Software\\GSettings\\org\\gnucash";
        cmdExport[3] = OUT_REG_FILE;
        cmdExport[4] = "/y";    // Force overwriting an existing output file without prompt

        try {
            Process proc = rt.exec(cmdExport);

            // make sure output is consumed so system buffers do not fill up
            // and cause the process to hang

            /*  Because any updates to the JavaFX gui must be done from the JavaFX
                application thread, it is not possible to update taLog from
                StreamGobbler, so I use StreamGobbler to put stdout &
                stderr to files, and just copy the contents of the files to taLog
                when the 7zip'ing finishes.
             */

            try (   // with resources
                FileOutputStream fosErr = new FileOutputStream(ERR_FILE);
                FileOutputStream fosOut = new FileOutputStream(OUT_FILE)
            )
            {
                // any error messages (stderr) ?
                StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR", fosErr);
                // any output?
                StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT", fosOut);
                // kick them off - creates new threads
                errorGobbler.start();
                outputGobbler.start();
                // any error?
                exitVal = proc.waitFor();
                System.out.println("ExitValue: " + exitVal);
                taLog.appendText("reg.exe ExitValue: " + exitVal + "\n");
                fosErr.flush();
                fosOut.flush();
            }
            //fosErr.close();  // done automatically when try with resources ends
            //fosOut.close();  // done automatically when try with resources ends
        } catch (Throwable t)
        {
            taLog.appendText("reg.exe FAILED - StackTrace Logged");
            if (exitVal == 0) {
                exitVal = 99;
            }
            // NetBeans 11 suggests stack traces should be logged, not shown to users
            //t.printStackTrace();
            Logger.getLogger(BackupGnuCashController.class.getName()).log(Level.SEVERE, null, t);
        }

        // add stderr of reg.exe process to taLog
        Path pthErrFil = Paths.get(ERR_FILE);
        try (InputStream in = Files.newInputStream(pthErrFil);
            BufferedReader reader =
                new BufferedReader(new InputStreamReader(in))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                if (!line.equals("")) {
                    taLog.appendText(line + "\n");
                }
            }
        } catch (IOException x) {
            System.err.println(x);
            taLog.appendText("IOException reading " + pthErrFil);
        }
        // add stdout of 7-zip process to taLog
        Path pthOutFil = Paths.get(OUT_FILE);
        try (InputStream in = Files.newInputStream(pthOutFil);
            BufferedReader reader =
                new BufferedReader(new InputStreamReader(in))) {
//            String line = null;
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                if (! line.equals("")) {
                    taLog.appendText(line + "\n");
                }
            }
        } catch (IOException x) {
            System.err.println(x);
            taLog.appendText("IOException reading " + pthOutFil);
        }

        if (exitVal != 0) {
            taLog.appendText("reg.exe proc.waitFor() returned: " + exitVal + "\n");
            return false;
        }

        return true;
    }


boolean exportDconf() {

        // Use Linux dconf to dump GnuCash dconf entries to a text file which can be backed up
        // E.g dconf dump /org/gnucash/ > $HOME/.BupGc/gnucash.dconf

        int exitVal = 0;
        String[] cmdExport = new String[3];
        String strDconf = "/usr/bin/dconf";
        Path pathDconf = Paths.get(strDconf);
        if (! Files.isExecutable(pathDconf)) {
            taLog.setText("Error: Cannot find or execute " + strDconf );
            return false;
        }

        Runtime rt = Runtime.getRuntime();
        System.out.println("Execing " + strDconf);
        taLog.appendText("Dumping GnuCash dconf entries...\n");

        // Set up cmdExport to be like
        //  dconf dump /org/gnucash/ > $HOME/.BupGc/gnucash.dconf
        cmdExport[0] = pathDconf.toString();
        cmdExport[1] = "dump";
        cmdExport[2] = "/org/gnucash/";

        try {
            Process proc = rt.exec(cmdExport);

            // make sure output is consumed so system buffers do not fill up
            // and cause the process to hang

            /*  Because any updates to the JavaFX gui must be done from the JavaFX
                application thread, it is not possible to update taLog from
                StreamGobbler, so use StreamGobbler to put stdout & stderr to
                files, and copy the contents of the ERR_FILE to taLog
                when dconf finishes.
             */

            try (   // with resources
                FileOutputStream fosErr = new FileOutputStream(ERR_FILE);
                FileOutputStream fosOut = new FileOutputStream(OUT_DCONF_FILE);
            )
            {
                // any error messages (stderr) ?
                StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR", fosErr);
                // any output?
                StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT", fosOut);
                // kick them off - creates new threads
                errorGobbler.start();
                outputGobbler.start();
                // any error?
                exitVal = proc.waitFor();
                sleep(1000);        // wait 1 sec for StreamGobbler Threads to finish reading
                System.out.println("ExitValue: " + exitVal);
                taLog.appendText("dconf ExitValue: " + exitVal + "\n");
                fosErr.flush();
                fosOut.flush();
            }
            //fosErr.close();  // done automatically when try with resources ends
            //fosOut.close();  // done automatically when try with resources ends
        } catch (Throwable t)
        {
            taLog.appendText("dconf FAILED - Stack Trace logged");
            if (exitVal == 0) {
                exitVal = 99;
            }
            //t.printStackTrace();
            Logger.getLogger(BackupGnuCashController.class.getName()).log(Level.SEVERE, null, t);
        }

        // add stderr of dconf process to taLog
        Path pthErrFil = Paths.get(ERR_FILE);
        try (InputStream in = Files.newInputStream(pthErrFil);
            BufferedReader reader =
                new BufferedReader(new InputStreamReader(in))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                if (!line.equals("")) {
                    taLog.appendText(line + "\n");
                }
            }
        } catch (IOException x) {
            System.err.println(x);
            taLog.appendText("IOException reading " + pthErrFil);
        }

        if (exitVal != 0) {
            taLog.appendText("dconf proc.waitFor() returned: " + exitVal + "\n");
            return false;
        }

        return true;
    }

    void setTooltips() {

        // Create Tooltips (mouse-over text)

        bookComboBox.setTooltip(new Tooltip(
            "Book:\nBook name to be used for this book's backup settings.\n" +
            "For example: MyBook Live\n" +
            "To add a new book name:\n" +
            " Type the new book name in this combobox, then press ENTER,\n"  +
            " then change the other fields.\n" +
            "Use the Save Settings button to save the settings for all books."
        ));

        defaultBookChb.setTooltip(new Tooltip(
            "Default:\n" +
            "If ticked, this book is the default shown when this program starts.\n" +
            "To make another book the default:\n" +
            " First select the new default book, then tick this checkbox."
        ));

        btnDelete.setTooltip(new Tooltip(
            "Delete:\nDelete settings for the current book.\n" +
            "The settings for the last remaining book and the default book cannot be deleted.\n" +
            "To delete the default book, first make another book the default."
        ));

        btnHelp.setTooltip(new Tooltip(
            "Help:\nOpen in the default web brower:\n" + HELP_URL
        ));

        btnSaveSettings.setTooltip(new Tooltip(
            "Save Settings:\nSave settings for all books in\n" +
            DEF_PROP + "\n" + "The password is NOT saved."
        ));

        txtGcDatFilStr.setTooltip(new Tooltip(
            "GnuCash data file:\n" +
            "The full directory and file string of the GnuCash data file.\n"
        ));

        txtGcVer.setTooltip(new Tooltip(
            "GnuCash Version:\nOptional suffix added to GnuCash backup file name.\n"
        ));

        chbGcV2Cfg.setTooltip(new Tooltip(
            "Backup GnuCash V2 configuration folders and files?\n"
          + " E.g. Among others,\n"
          + "   Windows: C:\\Users\\[USERNAME]\\.gnucash\n"
          + "   Linux:   /home/[Name]/.gnucash\n"
          + "Use the Help button to see full details."
/*          + "  This includes, among others, saved report options\n"
          + "    E.g. Windows: C:\\Users\\[USERNAME]\\.gnucash\\saved-reports-2.4\n"
          + "         Linux:   $HOME/.gnucash/saved-reports-2.4\n"
          + "  and metadata\n"
          + "    E.g. Windows: C:\\Users\\[USERNAME]\\.gnucash\\books\\[BookName].gnucash.gcm\n"
          + "         Linux    $HOME//.gnucash\\books\\[BookName].gnucash.gcm\n"
          + " GTK2:\n"
          + "   Windows: C:\\Users\\[Name]\\.gtkrc-2.0\n"
          + "        and C:\\Users\\[Name]\\.gtkrc-2.0.gnucash"
          + Registry + aqbanking
*/
        ));

        chbGcV3Cfg.setTooltip(new Tooltip(
            "Backup GnuCash V3 configuration folders and files?\n"
          + " E.g. Among others,\n"
          + "   Windows: C:\\Users\\[Name]\\AppData\\Roaming\\GnuCash\n"
          + "   Linux:   /home/[Name]/.config/gnucash\n"
          + "Use the Help button to see full details."
/*          "This backs up the user configuration directory and sub-folders\n" +
            " E.g. C:\\Users\\[USERNAME]\\AppData\\Gnucash\n" +
            "This includes amoung other things\n" +
            " the saved report options\n" +
            "  E.g. C:\\Users\\[USERNAME]\\AppData\\Gnucash\\saved-reports-2.n\n" +
            " and metadata\n" +
            "  E.g. C:\\Users\\[USERNAME]\\AppData\\GnuCash\\books\\[BookName].gnucash.gcm\n" +
            " and the contents of the registry key\n" +
            "  HKCU\\Software\\GSettings\\org\\gnucash"
*/
        ));

        txtDropBox.setTooltip(new Tooltip(
            "Dropbox Base Directory:\n" +
            "The local directory which is replicated to the cloud by Dropbox\n" +
            "or another cloud storage service.\n" +
            "The encrypted compressed backup file will be saved in a sub-directory\n" +
            "of this directory called 'GnuCash'.\n"
        ));

        // Tooltips (same) for txtPswd & txtVisPswd
        final String strTooltipPswd =
            "Password:\n" +
            "The archived files will be compressed and encrypted with this password using 7-Zip.\n" +
            "Minimum length is 8 characters.";
        txtPswd.setTooltip(new Tooltip(strTooltipPswd));

        txtVisPswd.setTooltip(new Tooltip(strTooltipPswd));

        btnBupGC.setTooltip(new Tooltip(
            "Backup GnuCash:\n" +
            "The selected folders and files will be archived (compressed and encrypted) to the 'GnuCash' sub-directory\n" +
            "of the Dropbox directory. The folders and files themselves remains unaltered."
        ));

    }

    void enable_or_disable_buttons() {
        System.out.println("Start enable_or_disable_buttons"
            + " bookComboBox.getValue()=" + bookComboBox.getValue()
//          + " txtGcDatFilStr.getText()=" + txtGcDatFilStr.getText()
        );
        boolean boolUserOk = false;
        boolean boolPswdOk = false;
        boolean boolGcOk = false;
        boolean boolDropBoxOk = false;
        boolean boolBookOK = false;

//        ObservableList<String> items = bookComboBox.getItems();
//        items.stream().forEach((item) -> {
//            System.out.println("bookComboBox " + item);
//        });

//        for (Iterator it = bookComboBoxData.iterator(); it.hasNext();) {
//            String item = (String) it.next();
//            System.out.println("bookComboBoxData " + item);
//        }

        taLog.clear();

        // Note:    Disable  property : Defines the individual disabled state of this Node.
        //                              'Disable' may be false and 'Disabled true' if a parent node is Disabled.
        //          Disabled propery  : Indicates whether or not this Node is disabled.
        //                              Could be 'Disabled' because parent node is disabled.

        // Test: isDisabled(), Set: setDisable() NOT setDisabled()

        if (isValidUser()) {
            boolUserOk = true;
        } else {
            taLog.setText("Error: Invalid user: " + USER_NAME + "\n");
        }

        if (isValidPswd()) {
            boolPswdOk = true;
        } else {
            taLog.appendText("Please enter Password - minimum length is 8 characters\n");
        }

        pathGcDatFilStr = Paths.get(txtGcDatFilStr.getText());
        if (Files.isReadable(pathGcDatFilStr)) {
            // show the Last Modified date/time
            //  FileTime epoch 1970-01-01T00:00:00Z (FileTime no longer used)
            try {
               SimpleDateFormat sdfFormat = new SimpleDateFormat("EEE, dd/MM/yyyy hh:mm aa");
               //long lngGCMod = Files.getLastModifiedTime(pathGcDatFilStr).toMillis();
               //lblGCModDate.setText("Modified : " + sdfFormat.format(lngGCMod));     //OK

               // following line does same as both commented out lines above
               lblGCModDate.setText("Modified : " + sdfFormat.format(Files.getLastModifiedTime(pathGcDatFilStr).toMillis()));
            } catch (IOException x) {
                System.err.println(x);
                lblGCModDate.setText("IOException getLastModifiedTime" + pathGcDatFilStr.toString());
            }
            // chk GnuCash file is not open (ensure lockfile does NOT exist)
            // i.e If data file is XXXX.gnucash
            //      ensure XXXX.gnucash.LCK does NOT exist in same folder

            Path pathGcLckFilStr = Paths.get(txtGcDatFilStr.getText() + ".LCK");
            if (Files.isReadable(pathGcLckFilStr)) {
                taLog.appendText("Error: GnuCash lock file " +
                    Paths.get(txtGcDatFilStr.getText() + ".LCK") +
                    " exists - GnuCash may be open or may have crashed leaving the lockfile\n");
            } else {
                boolGcOk = true;
                FileName fileName = new FileName(txtGcDatFilStr.getText(),
                    FILE_SEPARATOR, '.');

                Path pathGcSavRpt;
                Path pathGcGcm;

                if (chbGcV2Cfg.isSelected()) {
                    // chk C:\Users\[Name]\.gnucash\saved-reports-2.4 exists
                    pathGcSavRpt = Paths.get(HOME_DIR + FILE_SEPARATOR +
                            ".gnucash" + FILE_SEPARATOR + "saved-reports-2.4");
                    if (Files.isReadable(pathGcSavRpt)) {
                        taLog.appendText("Info: Found GnuCash 2 Saved Reports " + pathGcSavRpt.toString() + "\n");
                    } else {
                        taLog.appendText("Info: GnuCash 2 Saved Reports " + pathGcSavRpt.toString() +
                            " is not readable or does not exist\n");
                    }

                    // chk C:\Users\[Name]\.gnucash\books\MyFile.gnucash.gcm exists
                    //       (Linux: $HOME/.gnucash\books\MyFile.gnucash.gcm)
                    fileName = new FileName(txtGcDatFilStr.getText(),
                        FILE_SEPARATOR, '.');
                    pathGcGcm = Paths.get(HOME_DIR + FILE_SEPARATOR +
                        ".gnucash" + FILE_SEPARATOR + "books" + FILE_SEPARATOR +
                        fileName.filename() + "." + fileName.extension()+ ".gcm");
                    if (Files.isReadable(pathGcGcm)) {
                        taLog.appendText("Info: Found GnuCash 2 Configuration metadata " +
                            pathGcGcm.toString() + "\n");
                    } else {
                        taLog.appendText("Info: GnuCash 2 Configuration metadata " +
                            pathGcGcm.toString() + " is not readable or does not exist\n");
                    }
                }

                if (chbGcV3Cfg.isSelected()) {
                    // GnuCash V3
                    // Chk Saved Reports file exists
                    // GnuCash V3 uses saved-reports-2.4 if no saved-reports-2.8 exists
                    //  but only writes to saved-reports-2.8

                    if (OS_NAME.startsWith("Windows")) {
                        // %AppData%\Roaming\GnuCash\saved-reports-2.8
                        //   Note %APPDATA% is usually C:\Users\%USERNAME%\AppData\Roaming
                        pathGcSavRpt = Paths.get(System.getenv("APPDATA") +
                            FILE_SEPARATOR + "GnuCash" + FILE_SEPARATOR +
                            "saved-reports-2.8");
                    } else {
                        // Linux   $HOME/.local/share/gnucash/saved-reports-2.8
                        pathGcSavRpt = Paths.get(HOME_DIR + FILE_SEPARATOR + ".local" +
                            FILE_SEPARATOR + "share" + FILE_SEPARATOR + "gnucash" +
                            FILE_SEPARATOR + "saved-reports-2.8");
                    }
                    if (Files.isReadable(pathGcSavRpt)) {
                        taLog.appendText("Info: Found GnuCash 3 Saved Reports " +
                            pathGcSavRpt.toString() + "\n");
                    } else {
                        // chk saved-reports-2.4 exists
                        if (OS_NAME.startsWith("Windows")) {
                            pathGcSavRpt = Paths.get(System.getenv("APPDATA") +
                                "\\GnuCash\\saved-reports-2.4");
                        } else {
                            // Linux
                            pathGcSavRpt = Paths.get(HOME_DIR + FILE_SEPARATOR +
                                ".local" + FILE_SEPARATOR + "share" + FILE_SEPARATOR +
                                "gnucash" + FILE_SEPARATOR + "saved-reports-2.4");
                        }
                        if (Files.isReadable(pathGcSavRpt)) {
                            taLog.appendText("Info: Found GnuCash 3 " + pathGcSavRpt.toString() + "\n");
                        } else {
                            taLog.appendText("Info: GnuCash 3 " + pathGcSavRpt.toString() +
                                " is not readable or does not exist\n");
                        }
                    }

                    // Chk metadata
                    if (OS_NAME.startsWith("Windows")) {
                        // chk %APPDATA%\GnuCash\books\[BOOK].gnucash.gcm exists
                        //   Note %APPDATA% is usually C:\Users\%USERNAME%\AppData\Roaming
                        //   GnuCash V#2.7+
                        pathGcGcm = Paths.get(System.getenv("APPDATA") + FILE_SEPARATOR +
                            "GnuCash" + FILE_SEPARATOR + "books" + FILE_SEPARATOR +
                            fileName.filename() + "." + fileName.extension() + ".gcm");
                    } else {
                        // chk $HOME/.local/share/gnucash/books/[book].gnucash.gcm exists
                        pathGcGcm = Paths.get(HOME_DIR + FILE_SEPARATOR + ".local" +
                            FILE_SEPARATOR + "share" + FILE_SEPARATOR + "gnucash" +
                            FILE_SEPARATOR + "books" + FILE_SEPARATOR +
                            fileName.filename() + "." + fileName.extension()+ ".gcm");
                    }
                    if (Files.isReadable(pathGcGcm)) {
                        taLog.appendText("Info: Found GnuCash 3 Configuration metadata " +
                            pathGcGcm.toString() + "\n");
                    } else {
                        taLog.appendText("Info: GnuCash 3 Configuration metadata " +
                            pathGcGcm.toString() + " is not readable or does not exist\n");
                    }
                }
            }
        } else {
            taLog.appendText("Error: GnuCash data is not readable or does not exist\n");
        }

        // Validate Dropbox directory

        if (Files.isWritable(Paths.get(txtDropBox.getText()))) {
            if (Files.isWritable(Paths.get(txtDropBox.getText() + FILE_SEPARATOR
                    + "GnuCash"))) {
                boolDropBoxOk = true;
            } else {
                taLog.appendText("Error: Dropbox directory " +
                    txtDropBox.getText() + FILE_SEPARATOR +
                    "GnuCash is not writable or does not exist\n");
            }
        } else {
            taLog.appendText("Error: Dropbox directory " + txtDropBox.getText()
                    + " is not writable or does not exist\n");
        }

        // Validate bookComboBox
        if ((bookComboBox.getValue() != null)
        &&  (!bookComboBox.getValue().toString().isEmpty())) {
            boolBookOK = true;
        }

        // Note:
        //   To Test: use isDisabled(),
        //     as includes a node being disabled due to a parent being disabled
        //     whereas isDisable() only applies to the current node
        //   To actually enable or disable: setDisable()

        // enable or disable bookDefaultChb
        // You can only change a non-default book to be the default,
        // you cannot make the default book not the default.
        // This way you have to choose the new default.
        if ((boolBookOK) && (bookComboBoxData.size() > 1)
        && (! bookComboBox.getValue().equals(Book.getDefaultBook()))) {
            if (defaultBookChb.isDisabled()) {
                defaultBookChb.setDisable(false);       // Enable
            }
        } else {
            if (! defaultBookChb.isDisabled()) {
                defaultBookChb.setDisable(true);        // Disable
            }
        }

        // enable or disable btnDelete
        // default book cannot be deleted
        if (((boolBookOK) && (bookComboBoxData.size() > 1)
        && (! bookComboBox.getValue().equals(Book.getDefaultBook())))) {
            if (btnDelete.isDisabled()) {
                btnDelete.setDisable(false);       // Enable
            }
        } else {
            if (! btnDelete.isDisabled()) {
                btnDelete.setDisable(true);        // Disable
            }
        }

        // enable or disable btnBupGC
        if (boolUserOk && boolPswdOk && boolGcOk && boolDropBoxOk) {
            if (btnBupGC.isDisabled()) {        // if Disabled
                btnBupGC.setDisable(false);     //     Enable
            }
        } else {
            if (! btnBupGC.isDisabled()) {      // if Enabled
                btnBupGC.setDisable(true);     //      Disable
            }
        }

        // enable or disable btnSaveSettings
        if (boolUserOk && boolBookOK && boolGcOk && boolDropBoxOk) {
            if (btnSaveSettings.isDisabled()) {        // if Disabled
                btnSaveSettings.setDisable(false);     //     Enable
                //System.out.println("btnSaveSettings Enabled");
            }
            // If Book already exists
            //   Update Book instance from screen fields
            // else
            //   Add current settings to Book, bookMap and bookComboBoxData
            if (bookMap.containsKey(bookComboBox.getValue())) {
                Book book = (Book)bookMap.get(bookComboBox.getValue());
                book.setGcDat(txtGcDatFilStr.getText());
                book.setGcVer(txtGcVer.getText());
                book.setGcV2Cfg(chbGcV2Cfg.isSelected());
                book.setGcV3Cfg(chbGcV3Cfg.isSelected());
                book.setDropBox(txtDropBox.getText());
                System.out.println("enable_or_disable:"
                    + "set book=" + bookComboBox.getValue()
//                    + " GcDat=" + txtGcDatFilStr.getText()
//                    + " GcVer=" + txtGcVer.getText()
                      + " GcV2Cfg=" + chbGcV2Cfg.isSelected()
                      + " GcV3Cfg=" + chbGcV3Cfg.isSelected()
//                    + " DropBox=" + txtDropBox.getText()
                );
            } else {
                Book book = new Book(bookComboBox.getValue().toString(),
                                     txtGcDatFilStr.getText(),
                                     txtGcVer.getText(),
                                     chbGcV2Cfg.isSelected(),
                                     chbGcV3Cfg.isSelected(),
                                     txtDropBox.getText());
                bookMap.put(bookComboBox.getValue(), book);
                bookComboBoxData.add(bookComboBox.getValue().toString());
            }
        } else {
            if (! btnSaveSettings.isDisabled()) {      // if Enabled
                btnSaveSettings.setDisable(true);     //      Disable
            }
        }

        // Change Focus to password if all except password OK
        if ((boolUserOk && boolBookOK && boolGcOk && boolDropBoxOk) && (! isValidPswd()) ) {
            //System.out.println("txtPswd.isVisible=" + txtPswd.isVisible() +
            //    " txtVisPswd.isVisible=" + txtVisPswd.isVisible());
            if (txtPswd.isVisible()) {
                if (! txtPswd.isFocused()) {
                    if (firstTime) {
                        firstTime = false;
                        // When run from initialize(), controls are not yet ready to handle focus
                        //  so delay first execution of requestFocus until later
                        //  Refer http://stackoverflow.com/questions/12744542/requestfocus-in-textfield-doesnt-work-javafx-2-1
                        Platform.runLater(() -> {
                            txtPswd.requestFocus();
                        });
                    } else {
                        txtPswd.requestFocus();
                        System.out.println("enable_or_disable_buttons: txtPswd.requestFocus");
                    }
                }
            } else {
                if (! txtVisPswd.isFocused()) {
                    txtVisPswd.requestFocus();
                    System.out.println("enable_or_disable_buttons: txtVisPswd.requestFocus");
                }
            }
        }
    }

    /**
     * Backup GnuCash
     *
     * Note that 7z will backup all files and directories, including sub-folders,
     *  when the arg is a directory with a "\" (Windows), or "/" (Linux) suffix
     *
     * Use commands like
     * Windows:
     * cmd.exe          NOT Used
     * /C               NOT Used
     * "E:\Program Files\7-Zip\7z.exe"
     *   a -spf2
     *   E:\Data\Dropbox\GnuCash\GnuCashXXXX_%yyyymmddhhmm%_267.7z
     *   -p%pswd%
     *   E:\Data\GnuCash\267\XXXX\XXXX.gnucash
     *   V2
     *    C:\Users\[Name]\.gnucash\
     *    which includes (if used)
     *     C:\Users\[Name]\.gnucash\saved-reports-2.4
     *     C:\Users\[Name]\.gnucash\books\XXXX.gnucash.gcm
     *    C:\Users\[Name]\.gtkrc-2.0
     *    C:\Users\[Name]\.gtkrc-2.0.gnucash
     *    C:\Program Files (x86)\gnucash\etc\gtk-2.0\gtkrc
     *   V3
     *    C:\Users\[Name]\AppData\Roaming\GnuCash\
     *    which includes (if used)
     *     C:\Users\[Name]\AppData\Roaming\GnuCash\saved-reports-2.4
     *     C:\Users\[Name]\AppData\Roaming\GnuCash\books\[BookName].gnucash.gcm
     *     C:\Users\[Name]\AppData\Roaming\GnuCash\gtk-3.0.css
     *    %LOCALAPPDATA%\gtk-3.0\
     *    which includes (if used)
     *	   C:\Users\[Name]\AppData\Local\gtk-3.0\settings.ini
     *     C:\Users\[Name]\AppData\Local\gtk-3.0\gtk.css
     *    C:\Program Files (x86)\gnucash\etc\gnucash\environment.local
     *
     *   C:\Users\[Name]\.BupGc\GnuCashGSettings.reg
     *   C:\Users\[Name]\aqbanking\
     *
     * Linux:
     * bash         Needed to strip quotes around args      NOT Used
     * -c                                                   NOT Used
     * /usr/bin/7z
     *   a -spf2
     *   $HOME/Dropbox/GnuCash/GnuCashXXXX_yyyymmddhhmm_267.7z
     *   -p"pswd"
     *   $HOME/GnuCash/267/XXXX/XXXX.gnucash
     *   V2
     *    $HOME/.gnucash/
     *     which includes (if used)
     *      $HOME/.gnucash/saved-reports-2.4
     *      $HOME/.gnucash/books/XXXX.gnucash.gcm
     *    $HOME/.gtkrc-2.0      ### NOT on Linux
     *    $HOME/.gtkrc-2.0.gnucash
     *   V3
     *    $HOME/.config/gnucash/
     *     which includes (if used)
     *      $HOME/.config/gnucash/gtk-3.0.css
     *    $HOME/.config/gtk-3.0/
     *     which includes (if used)
     *      $HOME/.config/gtk-3.0/settings.ini
     *      $HOME/.config/gtk-3.0/gtk-css
     *      $HOME/.config/gtk-3.0/gtk-3.0.css
     *    $HOME/.local/share/gnucash/
     *     which includes
     *      $HOME/.local/share/gnucash/saved-reports-2.[48]
     *      $HOME/.local/share/gnucash/books/[BOOK].gnucash[_n].gcm
     *    /etc/gnucash/environment.local
     *
     *   $HOME/.BupGc/gnucash.dconf
     *   $HOME/.aqbanking/
     *
     * @author cgood
     * @param e
     * @throws java.io.IOException
     */
    @FXML
    public void handleBtnActionBupGC(Event e) throws IOException
    {
        /* NOTE: it does NOT seem possible to include redirection args like
            > or 2>&1 even if using cmd.exe /c
        */
        final int cmdElements = 14;
        String strArchive;
        int exitVal = 0;

        // create archive using 7z.exe
        taLog.clear();
        String str7z;
        Path path7z;
        if (OS_NAME.startsWith("Windows")) {
            str7z = "\\Program Files\\7-Zip\\7z.exe";
            path7z = Paths.get("C:" + str7z);
            if (! Files.isExecutable(path7z)) {
                path7z = Paths.get("E:" + str7z);
                if (! Files.isExecutable(path7z)) {
                    str7z = "\\Program Files (x86)\\7-Zip\\7z.exe";
                    path7z = Paths.get("C:" + str7z);
                    if (! Files.isExecutable(path7z)) {
                        path7z = Paths.get("E:" + str7z);
                        if (! Files.isExecutable(path7z)) {
                            taLog.setText("Error: Cannot find or execute "
                                + "\\Program Files\\7-Zip\\7z.exe or " + str7z
                                + " on either C: or E:" );
                            return;
                        }
                    }
                }
            }
        } else {
            str7z = "/usr/bin/7z";
            path7z = Paths.get(str7z);
            if (! Files.isExecutable(path7z)) {
                taLog.setText("Error: Cannot find or execute " + str7z );
                return;
            }
        }
        try {
            int i = 0;
            String[] cmd = new String[cmdElements];

/*          As not using internal shell commands, cmd.exe is not neeeded

            String osName = System.getProperty("os.name" );
            switch (osName) {
                case "Windows 95":
                    cmd[i++] = "command.com" ;
                    cmd[i++] = "/C" ;
                    break;
                case "Windows NT":
                case "Windows 7":
                    cmd[i++] = "cmd.exe" ;
                    cmd[i++] = "/C" ;
                    break;
                default:
                    if (osName.startsWith("Windows")) {
                        cmd[i++] = "cmd.exe" ;
                        cmd[i++] = "/C" ;
                        break;
                    }
            }
*/
            // 7-zip executable eg
            // "E:\Program Files\7-Zip\7z.exe"
            // not sure if need to quote, but doesn't hurt

            // 26/5/2016 Actually, it is OK to quote args for Windows without
            //      using a shell but for Linux, if not using a shell, nothing
            //      strips the quotes, so do NOT quote args.

            // TEST using a path with embedded space ?
            //  Linux OK, Windows OK

            if (OS_NAME.startsWith("Windows")) {
                // quote "Program Files" - maybe not needed
                cmd[i++] = "\"" + path7z.toString() + "\"";
            } else {
                cmd[i++] = str7z;
            }
            cmd[i++] = "a";     // add to archive
            cmd[i++] = "-spf2"; // -spf2 = use full paths without drive letter
                                //  Needed to avoid "Duplicate filename on disk"
                                //  error on Linux because of dirs
                                //    ~/.config/gnucash
                                //    ~/.local/share/gnucash

            // archive file string eg
            // Windows:
            // C:\Users\[USER_NAME]\Dropbox\GnuCash\GnuCashXXXX_yyyymmddhhmm_267.7z
            // or Linux:
            // /home/[USER_NAME]/Dropbox/GnuCash/GnuCashXXXX_%yyyymmddhhmm%_267.7z
            FileName fileName = new FileName(txtGcDatFilStr.getText(),
                FILE_SEPARATOR, '.');
            LocalDateTime today = LocalDateTime.now();
//          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

            String strVerSuffix = "";
            if (! txtGcVer.getText().isEmpty()) {
                strVerSuffix = "_" + txtGcVer.getText();
            }

            strArchive = txtDropBox.getText() + FILE_SEPARATOR + "GnuCash" +
                FILE_SEPARATOR + "GnuCash" + fileName.filename() + "_" +
             /* today.format(DateTimeFormatter.BASIC_ISO_DATE) + */
                today.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")) +
                strVerSuffix + ".7z";
//          cmd[i++] = "\"" + strArchive + "\"";
            cmd[i++] = strArchive;

            // password
            cmd[i++] = "-p" + txtPswd.getText();

            // GC data file eg E:\Data\GnuCash\267\XXXX\XXXX.gnucash
            cmd[i++] = txtGcDatFilStr.getText();

            // configuration dir(s)
            //  eg Windows V2    $HOME\.gnucash\
            //                   $HOME\.gtkrc-2.0
            //                   $HOME\.gtkrc-2.0.gnucash
            //                   [CE]:\Program Files (x86)\gnucash\etc\gtk-2.0\gtkrc
            //             V3    $HOME\AppData\Roaming\GnuCash\
            //                   %LOCALAPPDATA%\gtk-3.0\
            //             V2+V3 C:\Users\[Name]\.BupGc\GnuCashGSettings.reg
            //                   C:\Users\[Name]\aqbanking\
            //     Linux   V2    $HOME/.gnucash/
            //                   $HOME/.gtkrc-2.0.gnucash
            //             V3    $HOME/.config/gnucash/
            //                   $HOME/.config/gtk-3.0/
            //                   $HOME/.local/share/gnucash/
            //             V2+V3 $HOME/.BupGc/gnucash.dconf
            //                   $HOME/.aqbanking/

            if (chbGcV2Cfg.isSelected()) {
                // Windows  C:\Users\[Name]\.gnucash\
                // Linux    $HOME/.gnucash/
                cmd[i++] = HOME_DIR + FILE_SEPARATOR +
                        ".gnucash" + FILE_SEPARATOR;

                Path pathGcGtk;
                if (OS_NAME.startsWith("Windows")) {
                    // Backup $HOME/.gtkrc-2.0 if exists
                    pathGcGtk = Paths.get(HOME_DIR + FILE_SEPARATOR + ".gtkrc-2.0");
                    if (Files.isReadable(pathGcGtk)) {
                        cmd[i++] = pathGcGtk.toString();
                    } else {
                        taLog.appendText("Info: Skip as does not exist: " +
                                pathGcGtk.toString() + "\n");
                    }
                }

                // Backup $HOME/.gtkrc-2.0.gnucash if exists
                pathGcGtk = Paths.get(HOME_DIR + FILE_SEPARATOR + ".gtkrc-2.0.gnucash");
                if (Files.isReadable(pathGcGtk)) {
                    cmd[i++] = pathGcGtk.toString();
                } else {
                    taLog.appendText("Info: Skip as does not exist: " +
                            pathGcGtk.toString() + "\n");
                }

                // [CE]:\Program Files (x86)\gnucash\etc\gtk-2.0\gtkrc if exists
                String strPath = "\\Program Files (x86)\\gnucash\\etc\\gtk-2.0\\gtkrc";
                pathGcGtk = Paths.get("C:" + strPath);
                if (! Files.isReadable(pathGcGtk)) {
                    pathGcGtk = Paths.get("E:" + strPath);
                }
                if (Files.isReadable(pathGcGtk)) {
                    cmd[i++] = pathGcGtk.toString();
                } else {
                    taLog.appendText("Info: Skip as does not exist on C: or E: " +
                            strPath + "\n");
                }
            }

            if (chbGcV3Cfg.isSelected()) {
                if (OS_NAME.startsWith("Windows")) {
                    // C:\Users\[Name]\AppData\Roaming\GnuCash\
                    cmd[i++] = System.getenv("APPDATA") + FILE_SEPARATOR +
                            "GnuCash" + FILE_SEPARATOR;

                    // %LOCALAPPDATA%\gtk-3.0\
                    Path pathGcGtk = Paths.get(System.getenv("LOCALAPPDATA") +
                            FILE_SEPARATOR + "gtk-3.0");
                    if (Files.isReadable(pathGcGtk)) {
                        cmd[i++] = pathGcGtk.toString() + FILE_SEPARATOR;
                    } else {
                        taLog.appendText("Info: Skip as does not exist: " +
                                pathGcGtk.toString() + "\n");
                    }
                    // [CE]:\Program Files (x86)\gnucash\etc\gnucash\environment.local if exists
                    String strPath = "\\Program Files (x86)\\gnucash\\etc\\gnucash\\environment.local";
                    Path pathGcEnv = Paths.get("C:" + strPath);
                    if (! Files.isReadable(pathGcEnv)) {
                        pathGcEnv = Paths.get("E:" + strPath);
                    }
                    if (Files.isReadable(pathGcEnv)) {
                        cmd[i++] = pathGcGtk.toString();
                    }
                } else { // Linux
                    // $HOME/.config/gnucash/
                    cmd[i++] = HOME_DIR + FILE_SEPARATOR + ".config" +
                            FILE_SEPARATOR + "gnucash" + FILE_SEPARATOR;
                    // $HOME/.config/gtk-3.0/
                    Path pathGcGtk = Paths.get(HOME_DIR + FILE_SEPARATOR +
                            ".config" + FILE_SEPARATOR + "gtk-3.0");
                    if (Files.isReadable(pathGcGtk)) {
                        cmd[i++] = pathGcGtk.toString() + FILE_SEPARATOR;
                    } else {
                        taLog.appendText("Info: Skip as does not exist: " +
                                pathGcGtk.toString() + "\n");
                    }
                    // $HOME/.local/share/gnucash/
                    Path pthGcLocal = Paths.get(HOME_DIR + FILE_SEPARATOR +
                            ".local" + FILE_SEPARATOR + "share" +
                            FILE_SEPARATOR + "gnucash");
                    if (Files.exists(pthGcLocal)) {
                        cmd[i++] = pthGcLocal.toString() + FILE_SEPARATOR;
                    } else {
                        taLog.appendText("Info: Skip as does not exist: " +
                                pthGcLocal.toString() + "\n");
                    }
                    // /etc/gnucash/environment.local
                    Path pthGcEnv = Paths.get(FILE_SEPARATOR +
                        "etc" + FILE_SEPARATOR + "gnucash" +
                        FILE_SEPARATOR + "environment.local");
                    if (Files.exists(pthGcEnv)) {
                        cmd[i++] = pthGcEnv.toString() + FILE_SEPARATOR;
                    }
                }
            }

            // Backup things common to GnuCash V2 + V3
            if (OS_NAME.startsWith("Windows")) {
                // Exported registry file: C:\Users\[Name]\.BupGc\GnuCashGSettings.reg
                if (exportRegistry()) {
                    cmd[i++] = OUT_REG_FILE;
                }

                // AqBanking : C:\Users\[Name]\aqbanking\
                Path pathGcAq = Paths.get(HOME_DIR + FILE_SEPARATOR + "aqbanking");
                if (Files.isReadable(pathGcAq)) {
                    cmd[i++] = pathGcAq.toString() + FILE_SEPARATOR;
                } else {
                    taLog.appendText("Info: Skip as does not exist: " +
                            pathGcAq.toString() + "\n");
                }
            } else {    // Linux
                // Exported (dump) dconf settings file
                if (exportDconf()) {
                    cmd[i++] = OUT_DCONF_FILE;
                }

                // AqBanking: $HOME/.aqbanking/
                Path pathGcAq = Paths.get(HOME_DIR + FILE_SEPARATOR + ".aqbanking");
                if (Files.isReadable(pathGcAq)) {
                    cmd[i++] = pathGcAq.toString() + FILE_SEPARATOR;
                } else {
                    taLog.appendText("Info: Skip as does not exist: " +
                            pathGcAq.toString() + "\n");
                }
            }

            while (i < cmdElements) {
                // stop rt.exec getting NullPointerException
                cmd[i++] = "";
            }

            Runtime rt = Runtime.getRuntime();

            System.out.println("Execing ");
//            debugging
//            for (int j = 0; j < i; j++) {
//                System.out.println(cmd[j]);
//            }
            taLog.appendText("Backing up GnuCash...\n");
            Process proc = rt.exec(cmd);

            // make sure output is consumed so system buffers do not fill up
            // and cause the process to hang

            /*  Because any updates to the JavaFX gui must be done from the JavaFX
                application thread, it is not possible to update taLog from
                StreamGobbler, so use StreamGobbler to put stdout &
                stderr to files, and just copy the contents of the files to taLog
                when the 7zip'ing finishes.
             */

            try (   // with resources
                FileOutputStream fosErr = new FileOutputStream(ERR_FILE);
                FileOutputStream fosOut = new FileOutputStream(OUT_FILE)
            )
            {
                // any error messages (stderr) ?
                StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR", fosErr);
                // any output?
                StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT", fosOut);
                // kick them off - creates new threads
                errorGobbler.start();
                outputGobbler.start();
                // any error?
                exitVal = proc.waitFor();
//                System.out.println("ExitValue: " + exitVal);
                taLog.appendText("7-zip ExitValue: " + exitVal + "\n");
                fosErr.flush();
                fosOut.flush();
            }
            //fosErr.close();  // done automatically when try with resources ends
            //fosOut.close();  // done automatically when try with resources ends
        } catch (Throwable t)
        {
            taLog.appendText("7-Zip FAILED");
            if (exitVal == 0) {
                // force exitVal to be non-zero when exception caught just in case
                exitVal = 99;
            }
            //t.printStackTrace();
            Logger.getLogger(BackupGnuCashController.class.getName()).log(Level.SEVERE, null, t);
        }

        // add stderr of 7-zip process to taLog
        Path pthErrFil = Paths.get(ERR_FILE);
        try (InputStream in = Files.newInputStream(pthErrFil);
            BufferedReader reader =
                new BufferedReader(new InputStreamReader(in))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
//                System.out.println(line);
                if (! line.equals("")) {
                    taLog.appendText(line + "\n");
                }
            }
        } catch (IOException x) {
            System.err.println(x);
            taLog.appendText("IOException reading " + pthErrFil);
        }

        // add stdout of 7-zip process to taLog
        Path pthOutFil = Paths.get(OUT_FILE);
        try (InputStream in = Files.newInputStream(pthOutFil);
            BufferedReader reader =
                new BufferedReader(new InputStreamReader(in))) {
            //String line = null;
            String line;
            while ((line = reader.readLine()) != null) {
//                System.out.println(line);
                if (! line.equals("")) {
                    taLog.appendText(line + "\n");
                }
            }
        } catch (IOException x) {
            System.err.println(x);
            taLog.appendText("IOException reading " + pthOutFil);
        }

        if (exitVal != 0) {
            taLog.appendText("Error creating archive");
        }
    }

    @FXML
    public void handleBtnActionChooseGCDat(Event e) throws IOException {

        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose GnuCash Data file");

        if ((txtGcDatFilStr.getText() == null)
        ||  (txtGcDatFilStr.getText().isEmpty())) {
            txtGcDatFilStr.setText(gcDatFil);
            enable_or_disable_buttons();
        }

        final File file = new File(txtGcDatFilStr.getText());
        final String strDir = file.getParent();
        final Path pathGcDatDir = Paths.get(strDir);
        if (Files.isReadable(pathGcDatDir)) {
            fileChooser.setInitialDirectory(new File(strDir));
        } else {
            fileChooser.setInitialDirectory(new File(HOME_DIR));
        }
        fileChooser.setInitialFileName(file.getName());
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("GnuCash files (*.gnucash)", "*.gnucash");
        fileChooser.getExtensionFilters().add(extFilter);

        // get a reference to the current stage for use with showOpenDialog
        //  so it is modal

        Scene scene = btnChooseGCDatFil.getScene(); // any control would do
        if (scene != null) {
            //System.out.println("scene!=null");
            Window window = scene.getWindow();
            File fileSel = fileChooser.showOpenDialog(window);
            if (fileSel != null) {
                try {
                    txtGcDatFilStr.setText(fileSel.getCanonicalPath());
                } catch (IOException ex) {
                    Logger.getLogger(BackupGnuCashController.class.getName()).log(Level.SEVERE, null, ex);
                }
                enable_or_disable_buttons();
            }
        } else {
            //System.out.println("scene=null");
            taLog.appendText("Error: Cannot open modal fileChooser - scene is null\n");
        }
    }

    @FXML
    public void handleBtnActionChooseDropBox() {

        // Chose the Dropbox base directory

        final DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose Dropbox Base Directory");

        if ((txtDropBox.getText() == null)
        ||  (txtDropBox.getText().isEmpty())) {
            txtDropBox.setText(dropBox);
            enable_or_disable_buttons();
        }
        final File file = new File(txtDropBox.getText());
        final String strDir = file.getPath();
        final Path pathDropDir = Paths.get(strDir);
        if (Files.isReadable(pathDropDir)) {
            directoryChooser.setInitialDirectory(file);
        } else {
            directoryChooser.setInitialDirectory(new File(HOME_DIR));
        }

        Scene scene = btnChooseGCDatFil.getScene(); // any control would do
        if (scene != null) {
            //System.out.println("scene!=null");
            Window window = scene.getWindow();
            final File selectedDirectory = directoryChooser.showDialog(window);
            if (selectedDirectory != null) {
                selectedDirectory.getAbsolutePath();
                try {
                    txtDropBox.setText(selectedDirectory.getCanonicalPath());
                } catch (IOException ex) {
                    Logger.getLogger(BackupGnuCashController.class.getName()).log(Level.SEVERE, null, ex);
                }
                enable_or_disable_buttons();
            }
        } else {
            //System.out.println("scene=null");
            taLog.appendText("Error: Cannot open modal directoryChooser - scene is null\n");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        throw new UnsupportedOperationException("Not supported yet.");
//        To change body of generated methods, choose Tools | Templates.

        setTooltips();

        if (OS_NAME.startsWith("Windows")) {
            USER_NAME = System.getenv("USERNAME").toLowerCase();
            gcDatFil = HOME_DIR + "\\Documents\\GnuCash\\MyFileName.gnucash";
            dropBox = HOME_DIR + "\\Dropbox";

        } else {
            USER_NAME = System.getenv("USER").toLowerCase();
            gcDatFil = HOME_DIR + "/GnuCash/MyFileName.gnucash";
            dropBox = HOME_DIR + "/Dropbox";
        }

        if (! isValidUser()) {
            //System.out.println("Unknown user: " + BackupGnuCashMigor.USER_NAME);

          //taLog.setText("Error: Unknown user: " + BackupGnuCashController.getWinUserName());
            taLog.setText("Error: Unknown user: " + USER_NAME);
            //taLog.setFill(Color.FIREBRICK);
        } else {
            // create dir $HOME/.BupGc if doesn't already exist
            Boolean boolDirOK = true;
            Path pthBupGc = Paths.get(ERR_FILE).getParent();
            if (Files.exists(pthBupGc)) {
                loadingScreen = true;
                getUserDefaults();
                loadingScreen = false;
            } else {
                try {
                    Files.createDirectory(pthBupGc);
                } catch (IOException ex) {
                    //Logger.getLogger(BackupGnuCashController.class.getName()).log(Level.SEVERE, null, ex);
                    boolDirOK = false;
                    taLog.setText("Error: Cannot create folder: " + pthBupGc.toString());
                }
            }

            // bookComboBox CellFactory : Make default book bold

            bookComboBox.setCellFactory(
            new Callback<ListView<String>, ListCell<String>>() {
                @Override public ListCell<String> call(ListView<String> param) {
                    final ListCell<String> cell = new ListCell<String>() {
                        {   // instance initializer
                            super.setPrefWidth(100);
                            fontProperty().bind(Bindings.when(itemProperty().isEqualTo(Book.defaultProp))
                            .then(BOLD_FONT)
                            .otherwise(NORMAL_FONT));
                        }
                        @Override public void updateItem(String item,
                            boolean empty) {
                                super.updateItem(item, empty);
                                if (item != null) {
                                    setText(item);
//                                  if (item.equals(Book.getDefaultBook())) {
//                                      setFont(Font.font("System", FontWeight.BOLD, 14));
//                                      System.out.println("bookComboBox.setCellFactory: set BOLD item=" + item);
//                                  }
//                                  else {
//                                      setFont(Font.font("System", FontWeight.NORMAL, 14));
//                                      System.out.println("bookComboBox.setCellFactory: set NORMAL item=" + item);
//                                  }
                                }
                                else {
                                    setText(null);
                                }
                            }
                        };
                        return cell;
                    }
                });

//            //handle changes to checkbox defaultBookChb
//            defaultBookChb.focusedProperty().addListener((ObservableValue<? extends Boolean> o, Boolean wasFocused, Boolean isNowFocused) -> {
//                if (wasFocused) {
//                    // has just lost focus
//                    enable_or_disable_buttons();
//                }
//            });

            // handle changes to checkbox defaultBookChb
            defaultBookChb.selectedProperty().addListener((ObservableValue<? extends Boolean> o, Boolean wasSelected, Boolean isNowSelected) -> {
//                System.out.println("defaultBookChb.selectedProperty has changed" +
//                    " oldVal=" + wasSelected + " newVal=" + isNowSelected + " o=" + o);

                if (!loadingScreen) {
                    final String oldDefault = (String) Book.getDefaultBook();
                    String newDefault;
                    bookSelectionTarget = (String) bookComboBox.getValue();

                    if (isNowSelected) {
                        // IS ticked so this book will become the new default
                        newDefault = bookSelectionTarget;
                        Book.setDefaultBook((String) newDefault);
    //                    System.out.println("defaultBookChb.selectedProperty: IsTicked: new defaultBook=" + Book.getDefaultBook());
                    } else {
                        // Current book is now NOT to be the default.
                        // Current book is either the default book
                        //   or a new book if default book name was changed to a new book name
                        //
                        // If current book is not the default
                        //   do nothing - default stays the same
                        // else
                        //   If 1st Book is the default
                        //     set default to 2nd book
                        //   else
                        //     set default to 1st book

                        // Following is not needed anymore now bookComboBox dropdown listView font
                        //  is bound to defaultProp and cannot untick the default book
                        //  as defaultChb is disabled when default book is currently selected.
    //                    if (bookSelectionTarget.equals(Book.getDefaultBook())) {
    //                        if (bookComboBoxData.get(0).equals(Book.getDefaultBook())) {
    //                            Book.setDefaultBook((String) bookComboBoxData.get(1));
    //                        } else {
    //                            Book.setDefaultBook((String) bookComboBoxData.get(0));
    //                        }
    //                    }
    //                    newDefault = (String) Book.getDefaultBook();
    //                    System.out.println("defaultBookChb.selectedProperty: IsNotTicked: new defaultBook=" + Book.getDefaultBook());
                    }

                    bookSelectionTarget = "";
                    enable_or_disable_buttons();
                }
            });

            // handle changes to txtGcVer when it loses focus so that a new value
            //  is updated into Book.gcVer
            txtGcVer.focusedProperty().addListener((ObservableValue<? extends Boolean> o, Boolean wasFocused, Boolean isNowFocused) -> {
//                System.out.println("txtGcVer.focusedProperty has changed" +
//                    " oldVal=" + wasFocused + " newVal=" + isNowFocused + " o=" + o);
                if (!loadingScreen) {
                    if (wasFocused) {
                        // has just lost focus
                        enable_or_disable_buttons();
                    }
                }
            });

            // handle changes to chbGcV2Cfg.selectedProperty
            chbGcV2Cfg.selectedProperty().addListener((ObservableValue<? extends Boolean> o,
                    Boolean oldVal, Boolean newVal) -> {
                System.out.println("chbGcV2Cfg.selectedProperty has changed" +
                    " oldVal=" + oldVal + " newVal=" + newVal + " o=" + o);
                if (!loadingScreen) {
                    enable_or_disable_buttons();
                }
            });

            // handle changes to chbGcV3Cfg.selectedProperty
            chbGcV3Cfg.selectedProperty().addListener((ObservableValue<? extends Boolean> o,
                    Boolean oldVal, Boolean newVal) -> {
                System.out.println("chbGcV3Cfg.selectedProperty has changed" +
                    " oldVal=" + oldVal + " newVal=" + newVal + " o=" + o);
                if (!loadingScreen) {
                    enable_or_disable_buttons();
                }
            });

            if (boolDirOK) {
                if ((txtGcDatFilStr.getText() == null) || (txtGcDatFilStr.getText().isEmpty())) {
//                    System.out.println("initialize(): txtGcDatFilStr.setText to " + getGcDatFil());
                    txtGcDatFilStr.setText(getGcDatFil());
                }
                if ((txtGcVer.getText() == null) || (txtGcVer.getText().isEmpty())) {
                    txtGcVer.setText(getGcVer());
                }
                if ((txtDropBox.getText() == null) || (txtDropBox.getText().isEmpty())) {
                    txtDropBox.setText(getDropBoxDir());
                }

                pathGcDatFilStr = Paths.get(txtGcDatFilStr.getText());

                // handle changes to txtGcDatFilStr
                txtGcDatFilStr.focusedProperty().addListener((ObservableValue<? extends Boolean> o, Boolean oldVal, Boolean newVal) -> {
                    if (!loadingScreen) {
                        if (oldVal == true) {
                            // has just lost focus
                            enable_or_disable_buttons();
                        }
                    }
                });

                // handle changes to txtDropBox
                txtDropBox.focusedProperty().addListener((ObservableValue<? extends Boolean> o, Boolean oldVal, Boolean newVal) -> {
                    if (!loadingScreen) {
                        if (oldVal == true) {
                            // has just lost focus
                            enable_or_disable_buttons();
                        }
                    }
                });

                // handle changes to txtPswd
                txtPswd.textProperty().addListener((ObservableValue<? extends String> o, String oldVal, String newVal) -> {
                    //System.out.println("txtPswd.textProperty has changed" +
                    //    " oldVal=" + oldVal + " newVal=" + newVal + " o=" + o);

                    if (!loadingScreen) {
                        enable_or_disable_buttons();
                    }
                });

                // handle changes to chbShowPswd
                chbShowPswd.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov,
                    Boolean old_val, Boolean new_val) -> {
                    if (!loadingScreen) {
                        enable_or_disable_buttons();
                    }
                });

                // txtPswd    is a PasswordField    (masked)
                // txtVisPswd is a TextField        (not masked)
                // Only 1 is visible based on if chbShowPswd is ticked

                // Bind properties. Toggle txtVisPswd and txtPswd
                // visibility and managability properties mutually when chbShowPswd's state is changed.
                // Because we want to display only one component (txtVisPswd or txtPswd)
                // on the scene at a time.
                // Ref http://stackoverflow.com/questions/17014012/how-to-unmask-a-javafx-passwordfield-or-properly-mask-a-textfield
                //
                // managedProperty : Defines whether or not this node's layout will be managed by it's parent.
                //      (doesn't need to change for this program as parent's visibility is not changed)
//              txtVisPswd.managedProperty().bind(chbShowPswd.selectedProperty());
                txtVisPswd.visibleProperty().bind(chbShowPswd.selectedProperty());

//              txtPswd.managedProperty().bind(chbShowPswd.selectedProperty().not());
                txtPswd.visibleProperty().bind(chbShowPswd.selectedProperty().not());

                // Bind the textField and passwordField text values bidirectionally.
                //      ie If 1 changes, the other also changes
                txtVisPswd.textProperty().bindBidirectional(txtPswd.textProperty());

                enable_or_disable_buttons();
            }
        }
    }
}
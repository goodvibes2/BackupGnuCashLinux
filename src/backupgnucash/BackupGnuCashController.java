/*
 * Copyright (C) 2016 Chris Good
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
   
*/

package backupgnucash;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
//import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
//import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
/* import java.time.LocalDate; */
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
//import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
//import javafx.scene.control.Toggle;
//import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 *
 * @author cgood
 */
public class BackupGnuCashController implements Initializable {
    
    /* class variables */
    
    @FXML
    private GridPane grid;
    @FXML
    private Text sceneTitle;
    @FXML
    private Text versionNo;
    @FXML
    private Button btnSaveSettings;
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
    private Label lblDropBox;
    @FXML
    private TextField txtDropBox;
    @FXML
    private Button btnChooseDropBox;
    @FXML
    private Label lblPswd;
    @FXML
    private PasswordField txtPswd;
    @FXML
    private TextField txtVisPswd;
    @FXML
    private CheckBox chbShowPswd;
    @FXML
    private Separator sep1;
    @FXML
    private Separator sep2;
    @FXML
    private Separator sep3;
    @FXML
    private Separator sep4;
  //@FXML
  //private Label lblShowPswd;
    @FXML
    private Button btnBupGC;
    @FXML
    private Label lblLog;
    @FXML
    private TextArea taLog;
    
    final private static String OS_NAME = System.getProperty("os.name" );
        
    private static String USER_NAME;
    // character that separates folders from files in paths
    //  i.e. Windows = backslash, Linux/OSX = /
    private static final char FILE_SEPARATOR = 
        System.getProperty("file.separator").charAt(0);
    private static String gcDatFil; // initial default GnuCash data file
    private static String gcVer = ""; // optional version backup filename suffix
    private static String dropBox; // inital default Dropbox dir
        
    private static Path pathGcDatFilStr;
    private static final String HOME_DIR = System.getProperty("user.home");
    private static final String ERR_FILE = HOME_DIR + FILE_SEPARATOR
            + ".BupGc" + FILE_SEPARATOR + "BackGnuCash.err";
    private static final String OUT_FILE = HOME_DIR + FILE_SEPARATOR
            + ".BupGc" + FILE_SEPARATOR + "BackGnuCash.out";

    // Saved Settings
    private static final String DEF_PROP = HOME_DIR + FILE_SEPARATOR
            + ".BupGc" + FILE_SEPARATOR + "defaultProperties";
    //  default properties
    private static final Properties defaultProps = new Properties();
            
    private static boolean firstTime = true;
    
    @FXML
    public void handleBtnActionSaveSettings(Event e) throws IOException {
        defaultProps.setProperty("dropBox",     txtDropBox.getText());
        defaultProps.setProperty("gcDatFil",    txtGcDatFilStr.getText());
        //if (! txtGcVer.getText().isEmpty()) {
            defaultProps.setProperty("gcVer",   txtGcVer.getText());
        //}
        
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
    
    public static void getUserDefaults() {
        
        try (   // with resources
            FileInputStream in = new FileInputStream(DEF_PROP);
        )
        {
            defaultProps.load(in);
            gcDatFil = defaultProps.getProperty("gcDatFil");
            gcVer = defaultProps.getProperty("gcVer");
            dropBox = defaultProps.getProperty("dropBox");
            
            //in.close();  // done automatically when 'try with resources' ends            
        } catch (IOException ex) {
            //System.out.println("My Exception Message " + ex.getMessage());
            //System.out.println("My Exception Class " + ex.getClass());
            
            if (ex.getClass().toString().equals("class java.io.FileNotFoundException")) {
                System.out.println("getUserDefaults: " + ex.getMessage());
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
    
    private static boolean isFirstTime() {
        return firstTime;
    }
    
/*    void logText(String strText) {
        taLog.appendText(strText);
    }
*/    
    
    void setTooltips() {

        // Create Tooltips (mouse-over text)
        btnSaveSettings.setTooltip(new Tooltip(
            "Save Settings:\nSave current settings in\n" +
            DEF_PROP + "\n" + "The password is NOT saved."
        ));
        
        txtGcDatFilStr.setTooltip(new Tooltip(
            "GnuCash data file:\n" +
            "The full directory and file string of the GnuCash data file.\n"
        ));
        
        txtGcVer.setTooltip(new Tooltip(
            "GnuCash Version:\nOptional suffix added to GnuCash backup file name.\n"
        ));
        
        txtDropBox.setTooltip(new Tooltip(
            "Dropbox Base Directory:\n" +
            "The encrypted compressed backup file will be saved in a sub-directory\n" +
            "of this directory.\n" +
            "The GnuCash backup file will be saved in a sub-directory called 'GnuCash'.\n"
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
            "The data file will be archived (compressed and encrypted) to the 'GnuCash' sub-directory\n" +
            "of the Dropbox directory. The data file itself remains unaltered.\n" +
            "Two additional GnuCash files are included in the archive:\n" +
            "1. The saved reports configuration file:\n" +
            " " + HOME_DIR + FILE_SEPARATOR + ".gnucash" + FILE_SEPARATOR +
            "saved-reports-2.4\n" +
            "2. The preferences file:\n" + 
            " " + HOME_DIR + FILE_SEPARATOR + ".gnucash" + FILE_SEPARATOR +
            "books" + FILE_SEPARATOR + "[BookName].gnucash.gcm"
        ));

    }
    
    void enable_or_disable_buttons() {
        //System.out.println("enable_or_disable_buttons");
        boolean boolUserOk = false;
        boolean boolPswdOk = false;
        boolean boolGcOk = false;
        boolean boolDropBoxOk = false;
        
        taLog.clear();
        
        // Note:    Disable  property : Defines the individual disabled state of this Node.
        //                              'Disable' may be false and 'Disabled true' if a parent node is Disabled.
        //          Disabled propery  : Indicates whether or not this Node is disabled. Could be 'Disabled' because parent node is disabled.
        
        // Test: isDisabled(), Set: setDisable() NOT setDisabled()
        
        // 18/07/2015 Do NOT enable or disable until it is determined this needs 
        //              happen to see if this is causing unwanted Focus change ???
        
/*      btnBupGC.setDisable(true);          //Disable
        btnSaveSettings.setDisable(true);
*/
        //System.out.println("btnSaveSettings Disabled");
                
        if ((isValidUser() == true)) {
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
                // chk C:\Users\[Name]\.gnucash\saved-reports-2.4 exists
                Path pathGcSavRpt = Paths.get(HOME_DIR + FILE_SEPARATOR +
                        ".gnucash" + FILE_SEPARATOR + "saved-reports-2.4");
                if (Files.isReadable(pathGcSavRpt)) {
                    // chk C:\Users\[Name]\.gnucash\books\MyFile.gnucash.gcm exists
                    //       (Linux: $HOME/.gnucash\books\MyFile.gnucash.gcm)
                    FileName fileName = new FileName(txtGcDatFilStr.getText(),
                        FILE_SEPARATOR, '.');
                    Path pathGcGcm = Paths.get(HOME_DIR + FILE_SEPARATOR +
                        ".gnucash" + FILE_SEPARATOR + "books" + FILE_SEPARATOR +
                        fileName.filename() + "." + fileName.extension()+ ".gcm");
                    if (Files.isReadable(pathGcGcm)) {
                        boolGcOk = true;
                    } else {
                        taLog.appendText("Error: " + pathGcGcm.toString() +
                            " is not readable or does not exist");
                    }
                } else {
                    taLog.appendText("Error: " + pathGcSavRpt.toString() +
                        " is not readable or does not exist");
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

        // Note: To Test: use isDisabled(),
        //      but to actually enable or disable: setDisable()
        
        // enable or disable btnBupGC
        if (boolUserOk && boolPswdOk & boolGcOk & boolDropBoxOk) {
            if (btnBupGC.isDisabled()) {        // if Disabled
                btnBupGC.setDisable(false);     //     Enable
            }
        } else {
            if (! btnBupGC.isDisabled()) {      // if Enabled
                btnBupGC.setDisable(true);     //      Disable
            }
        }
                
        // enable or disable btnSaveSettings
        if (boolUserOk && boolGcOk && boolDropBoxOk) {
            if (btnSaveSettings.isDisabled()) {        // if Disabled
                btnSaveSettings.setDisable(false);     //     Enable
                //System.out.println("btnSaveSettings Enabled");
            }
        } else {
            if (! btnSaveSettings.isDisabled()) {      // if Enabled
                btnSaveSettings.setDisable(true);     //      Disable
            }
        }

        // Change Focus to password if all except password OK
        if ((boolUserOk && boolGcOk && boolDropBoxOk) && (! isValidPswd()) ) {
            //System.out.println("txtPswd.isVisible=" + txtPswd.isVisible() + 
            //    " txtVisPswd.isVisible=" + txtVisPswd.isVisible());
            if (txtPswd.isVisible()) {
                if (! txtPswd.isFocused()) {
                    if (isFirstTime()) {
                        firstTime = false;
                        // When run from initialize(), controls are not yet ready to handle focus
                        //  so delay first execution of requestFocus until later
                        //  Refer http://stackoverflow.com/questions/12744542/requestfocus-in-textfield-doesnt-work-javafx-2-1
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                txtPswd.requestFocus();
                            }
                        });
                    } else {
                        txtPswd.requestFocus();
                        //System.out.println("enable_or_disable_buttons: txtPswd.requestFocus");
                    }
                }
            } else {
                if (! txtVisPswd.isFocused()) {
                    txtVisPswd.requestFocus();
                    //System.out.println("enable_or_disable_buttons: txtVisPswd.requestFocus");
                }
            }
        }
    }
        
    /**
     * Backup GnuCash by using commands like
     * Windows:
     * cmd.exe          NOT Used
     * /C               NOT Used
     * "E:\Program Files\7-Zip\7z.exe"
     *   a
     *   E:\Data\Dropbox\GnuCash\GnuCashXXXX_%yyyymmddhhmm%_267.7z
     *   -p%pswd%
     *   E:\Data\GnuCash\267\XXXX\XXXX.gnucash
     *   C:\\users\\[Name]\\.gnucash\\saved-reports-2.4
     *   C:\\users\\[Name]\\.gnucash\\books\\XXXX.gnucash.gcm
     * Linux:
     * bash         Needed to strip quotes around args      NOT Used
     * -c                                                   NOT Used
     * /usr/bin/7z
     *   a
     *   /home/[USER_NAME]/Dropbox\GnuCash\GnuCashXXXX_yyyymmddhhmm_267.7z
     *   -p"pswd"
     *   /home/[USER_NAME]/GnuCash/267/XXXX\XXXX.gnucash
     *   /home/[USER_NAME]/.gnucash/saved-reports-2.4
     *   /home/[USER_NAME]/.gnucash/books/XXXX.gnucash.gcm
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
        final int cmdElements = 7;
        String strArchive = "";
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
                    taLog.setText("Error: Cannot execute " + str7z 
                        + " on either C: or E:" );
                    return;
                }
            }
        } else {
            str7z = "/usr/bin/7z";
            path7z = Paths.get(str7z);
            if (! Files.isExecutable(path7z)) {
                taLog.setText("Error: Cannot execute " + str7z );
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
                    if (osName.startsWith("Windows") == true) {
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
            
            // saved reports configuration file
            //  eg C:\\users\[Name]\\.gnucash\saved-reports-2.4
            cmd[i++] = HOME_DIR + FILE_SEPARATOR +
                    ".gnucash" + FILE_SEPARATOR + "saved-reports-2.4";
                
            // GC options
            //  eg C:\\users\[Name]\.gnucash\books\XXXX.gnucash.gcm
            cmd[i++] = HOME_DIR + FILE_SEPARATOR + ".gnucash" +
                FILE_SEPARATOR + "books" + FILE_SEPARATOR +
                fileName.filename() + "." + fileName.extension()+ ".gcm";
            
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
                System.out.println("ExitValue: " + exitVal);
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
            t.printStackTrace();
        }
        
        // add stderr of 7-zip process to taLog
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
            //String line = null;
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
            taLog.appendText("Error creating archive");
        }
    }

    @FXML
    public void handleBtnActionChooseGCDat(Event e) throws IOException {
              
        final FileChooser fileChooser = new FileChooser();         
        fileChooser.setTitle("Choose GnuCash Data file");   
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


        if (isValidUser() == false) {
            //System.out.println("Unknown user: " + BackupGnuCashMigor.USER_NAME);
                   
          //taLog.setText("Error: Unknown user: " + BackupGnuCashController.getWinUserName());
            taLog.setText("Error: Unknown user: " + USER_NAME);
            //taLog.setFill(Color.FIREBRICK);
        } else {
            // create dir $HOME/.BupGc if doesn't already exist
            Boolean boolDirOK = true;
            Path pthBupGc = Paths.get(ERR_FILE).getParent();
            if (Files.exists(pthBupGc)) {
                getUserDefaults();
            } else {
                try {
                    Files.createDirectory(pthBupGc);
                } catch (IOException ex) {
                    //Logger.getLogger(BackupGnuCashController.class.getName()).log(Level.SEVERE, null, ex);
                    boolDirOK = false;
                    taLog.setText("Error: Cannot create folder: " + pthBupGc.toString());
                }
            }
            
            if (boolDirOK == true) {
                txtGcDatFilStr.setText(getGcDatFil());
                txtGcVer.setText(getGcVer());
                txtDropBox.setText(getDropBoxDir());

                pathGcDatFilStr = Paths.get(txtGcDatFilStr.getText());

                // handle changes to txtGcDatFilStr
                txtGcDatFilStr.focusedProperty().addListener(new ChangeListener<Boolean>(){
                    @Override
                    public void changed(ObservableValue<? extends Boolean> o, Boolean oldVal, Boolean newVal){
                        if (oldVal == true) {
                            // has just lost focus
                            enable_or_disable_buttons();
                        }
                    }
                });

                // handle changes to txtDropBox
                txtDropBox.focusedProperty().addListener(new ChangeListener<Boolean>(){
                    @Override
                    public void changed(ObservableValue<? extends Boolean> o, Boolean oldVal, Boolean newVal){
                        if (oldVal == true) {
                            // has just lost focus
                            enable_or_disable_buttons();
                        }
                    }
                });
                                
                // handle changes to txtPswd
                txtPswd.textProperty().addListener(new ChangeListener<String>(){
                    @Override
                    public void changed(ObservableValue<? extends String> o, String oldVal, String newVal){
                        //System.out.println("txtPswd.textProperty has changed" + 
                        //    " oldVal=" + oldVal + " newVal=" + newVal + " o=" + o);
                        
                            enable_or_disable_buttons();
                    }
                });

                // handle changes to txtVisPswd
                
                //  don't need following listener 
                //  as txtPswd.textProperty and txtVisPswd.textProperty are 
                //  bound bidirectionally
                
/*              txtVisPswd.focusedProperty().addListener(new ChangeListener<Boolean>(){
                    @Override
                    public void changed(ObservableValue<? extends Boolean> o, Boolean oldVal, Boolean newVal){
                        //System.out.println("txtVisPswd.focusedProperty has changed!" + 
                        //    " oldVal=" + oldVal + " newVal=" + newVal + " o=" + o);
                        if (oldVal == true) {
                            // txtVisPswd has just lost focus
                            enable_or_disable_buttons();
                            if ((! btnBupGC.isDisabled()) && isValidPswd()) {
                                btnBupGC.requestFocus();
                            }
                        }
                    }
                });
*/
                // handle changes to chbShowPswd
                chbShowPswd.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) -> {
                //            lblShowPswd.setVisible(new_val);
                //            //icon.setImage(new_val ? image : null);
                              enable_or_disable_buttons();
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
                
              //txtVisPswd.managedProperty().bind(chbShowPswd.selectedProperty());
                txtVisPswd.visibleProperty().bind(chbShowPswd.selectedProperty());

              //txtPswd.managedProperty().bind(chbShowPswd.selectedProperty().not());
                txtPswd.visibleProperty().bind(chbShowPswd.selectedProperty().not());

                // Bind the textField and passwordField text values bidirectionally.
                //      ie If 1 changes, the other also changes
                txtVisPswd.textProperty().bindBidirectional(txtPswd.textProperty());
                
                enable_or_disable_buttons();
            }
        }
    }
}
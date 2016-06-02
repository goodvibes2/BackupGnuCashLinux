#          BackupGnuCash 1.10 README.md file.

This README.md is formatted for github markdown and is most easily read using a web browser
to view https://github.com/goodvibes2/BackupGnuCashWin/blob/master/src/backupgnucash/README.md.

The last known BackupGnuCash stable series is the 1.1x series.

## Table of Contents:
------------------

  - Overview
  - Features
  - Dependencies
  - Running
  - Internationalization
  - Building & Installing
  - Supported Platforms

## Overview
--------

BackupGnuCash is an application for easily creating offsite encrypted backups
of the data files used by the **GnuCash** personal finance manager.
See http://www.gnucash.org for more information about GnuCash.

This application is written in Java version 8 using JavaFX (or openjfx) for
the graphical user interface. Java versions before 8 cannot be used with this
application as they do not support JavaFX.

Free software **7-Zip** is used to perform the encryption.

The BackupGnuCash project is _not_ part of the GnuCash project.

If you need help, please email chris.good@ozemail.com.au and I will help
if I can and have time available.
Please read all of this document before asking for help.

## Features
--------
Features include:

- Available for both GNU/Linux and Microsoft Windows.
BackupGnuCash has been tested in GNU/Linux Ubuntu 16.04 and Windows 10.
GnuCash is also available for Mac OS/X and BackupGnuCash may work
but this has not (yet) been tested.

- An easy-to-use interface.
After finishing a GnuCash session, you just start BackupGnuCash,
optionally select different directories, enter the password for encryption
and click the Backup button. BackupGnuCash then encrypts the GnuCash data,
saved reports and preferences files to a date/time stampded file name in
the local 3rd party cloud storage directory.

- All GnuCash data files needed for system recovery are backed up.
The 3 files backed up into each archive file are:
  1. the **main GnuCash data file** which usually has a .gnucash extension.
     For example:
     ```
       GNU/Linux:              /home/[USER_NAME]/GnuCash/[BOOK].gnucash
       Windows:   C:\Users\[USER_NAME]\Documents\GnuCash\[BOOK].gnucash
     ```
  
  2. the **saved reports file**, for example
     ```
       GNU/Linux:   /home/[USER_NAME]/.gnucash/saved-reports-2.4
       Windows:  C:\Users\[USER_NAME]\.gnucash\saved-reports-2.4
     ```
     **Note**: the 2.4 suffix is used for both GnuCash 2.4 and 2.6.
     As of 29th May 2016 current stable version of GnuCash is
     2.6.12.
  
  3. the **preferences file**, usually
       ```
        GNU/Linux:   /home/[USER_NAME]/.gnucash/books/XXXX.gnucash.gcm
        Windows: C:\Users\[USER_NAME]\.gnucash\books\XXXX.gnucash.gcm
       ```

  These 3 files are all that is usually required when restoring or
  moving to a new computer, apart from the GnuCash program itself
  which can be downloaded from https://www.gnucash.org/download.

- One-time setup of:
  1. **location and name of the GnuCash data file**.

     Once a GnuCash data file has been selected in the BackupGnuCash
     window, the file modification date and time is display below it,
     so the user can check it approximately matches the date and time
     of their last GnuCash session.

     If you find that the GnuCash data file has a file name like
     ```
      [BOOK].gnucash.yyyymmddhhmmss.gnucash
     ```

     where yyyymmddhhmmss is a string of numbers representing a date
     and time, then probably some-one has mistakenly opened a GnuCash
     automatically created backup, and forgotten to re-open the real
     GnuCash data file.

     In this case, to go back to using the intended data file,  open
     the latest (or most correct) data file, then use
     'File', 'Save As' to save in the intended data file.
     The next time GnuCash is opened, it will open the intended data 
     file because GnuCash, unless told to open a specific data file,
     will open the last data file it used.

  2. **GnuCash version** may optionally be specified which
     will add a suffix of _[Version] to the archive file name
     (before the .7z extension).
     While GnuCash developers try hard to make most versions of
     the data files backwards and forwards compatible, some
     versions are not totally backwards and forwards compatible,
     so this information may be useful if a restore is needed.

     I suggest for example if you are using GnuCash 2.6.12, enter version
     ```
       2612
     ```
  3. **location of the base archive directory**, for example:
     ```
          GNU/Linux: /home/[USER_NAME]/Dropbox
          Windows:   C:\Users\[USER_NAME]\Dropbox
     ```
     The archive is created in a sub-directory of the base archive
     directory, called "GnuCash". For example:
     ```
           GNU/Linux: /home/[USER_NAME]/Dropbox/GnuCash
           Windows:   C:\Users\[USER_NAME]\Dropbox\GnuCash
     ```
     The GnuCash sub-directory must be manually created. If the GnuCash
     sub-directory of the archive directory does not exist, BackupGnuCash
     will show an error message in the Log area at the bottom of the
     window and the Backup button will be disabled.

  After valid (existing) entry of:

    - the location and name of the main GnuCash data file
    - the location of the base archive directory

  BackupGnuCash enables the **Save Settings** button, which when clicked,
  will save the 2 above entries, and the Version field (optional), in file:
  ```
    GNU/Linux: /home/[USER_NAME]/.BupGc/defaultProperties
    Windows: C:\Users\USER_NAME]\.BupGc/defaultProperties
  ```
  **Note** the password is NOT saved and must be entered each time
  BackupGnuCash is started. The password must be at least 8 characters.

  The next time BackupGnuCash is started, the 3 saved fields will be
  automatically loaded from the defaultProperties file.

  Saving the locations of multiple GnuCash books is currently not
  supported. In this case, if you wish to backup a book which is
  not the one for which the settings are saved, use the **Browse** button
  to select the required book and optionally select a different
  archive file directory. As the book name is part of the archive
  file name, so long as books have unique names, archive files
  for multiple books may be put in the same archive directory.

- The **archive** file is intended to be created in a local directory
  which is replicated in the _cloud_ by a 3rd party cloud storage
  service such as Dropbox, Google Drive or Microsoft OneDrive.
  This makes the backup an off-site and local backup.
  It is the **user's responsibility** to regularly ensure the 3rd party
  cloud storage service is working correctly, say by connecting to the
  service in a web browser or on another computer and checking
  the expected files exist and the contents match the local copy.

  It is also the **user's responsibility** to periodically remove old
  backups from the archive directory to ensure any cloud storage
  capacity limits are not exceeded.

  BackupGnuCash refers to the base archive directory as the **Dropbox**
  directory, but it could be used with any other cloud storage
  service, like Google Drive or Microsoft OneDrive, which operates
  in a similar fashion.

  The archive file name will be created in the following format:
  ```
  GnuCash[BOOK]_yyyyMMddhhmmss[_Version].7z
  ```
  where
    - [BOOK] is the data file name without the .gnucash externsion
    - yyyyMMddhhmmss is the current date and time 
    - [_Version] is the optional GnuCash version number if entered.

- Each archive file will contain encrypted copies of the 3 data files.
  The files will be encrypted, using the entered password, by free
  software **7-Zip**, using AES-256 encryption.

  BackupGnuCash is not intended to be used to conceal illegal activities.
  The GnuCash data files are encrypted in the archive because, even though
  3rd party cloud storage services usually try hard to ensure the privacy
  of your data, why risk unencrypted data in the cloud if you don't have to?

  BackupGnuCash does not provide any facility for extracting the data files
  from an encrypted archive. 7-Zip must be installed in order for the
  encrypted archives to be created and for the data files to be extracted.

  **Windows**: 7-Zip includes gui tool **7-Zip File Manager** which can be used to
  decrypt the archive files if they need to be restored.
  Alternatively, use the 7-Zip command in a command prompt window, E.g.
  ```
    C:\Program Files\7-Zip\7z.exe e archive.7z
  ```
  extracts all files from archive archive.7z to the current folder.
  ```
    C:\Program Files\7-Zip\7z.exe e archive.7z -oc:\soft *.gnucash
  ```
  extracts *.gnucash files from archive archive.7z to c:\soft folder.

  **GNU/Linux**: **Archive Manager** can be used to manage 7-Zip files if the
  **p7zip-full** package is installed.
  Alternatively, use the 7-Zip command in a terminal window, E.g.
  ```
    7z e archive.7z
  ```
  extracts all files from archive archive.7z to the current directory.
  ```
  7z e archive.7z -o/tmp "*.gnucash"
  ```
  extracts *.gnucash files from archive archive.7z to /tmp directory.

  Of course, the user needs to enter the password carefully, and remember it!
  **If the password is entered incorrectly or forgotten, the encrypted archives
  will be of no use.**

  There is a **Show** checkbox to the right of the password. If this is checked,
  the password will display. If it is unchecked, the characters of the
  password will display as asterixes.
  After ensuring no-one is watching, show and carefully check the password
  each time you enter it.


### Home Page
None

### Precompiled binaries

```
 https:://github.com/goodvibes2/BackupGnuCashWin/blob/master/dist/BackupGnuCash.jar
 or
 https:://github.com/goodvibes2/BackupGnuCashLinux/blob/master/dist/BackupGnuCash.jar
```
Being Java bytecode, either of the above should work in either GNU/Linux or
Windows, so long as dependencies are installed.

To download BackupGnuCash,jar:

Paste either of the above URL's into a web browser,
**Right** click on the **Raw** button, **Save target as**,
select the required location.

I suggest:
```
GNU/Linux:              /home/[USER_NAME]/BackupGnuCash/BackupGnuCash.jar
Windows:   C:\Users\[USER_NAME]\Documents\BackupGnuCash\BackupGnuCash.jar
```

## Dependencies
------------

There are 2 ways to use this application:

  1. Download the prebuilt BackupGnuCash.jar from this project

     This application comes with no warranty and you should think about the security
     implications of using software downloaded from the internet. You are trusting
     my good nature and the codebase from which this is built!
     I have not security audited this code.

  OR

  2. Download the project source from github, check the code for security and
     build your own copy of BackupGnuCash.jar.

### To download the prebuilt BackupGnuCash.jar from github

**Note** There are 2 versions of BackupGnuCash on github:
  ```
    https://github.com/goodvibes2/BackupGnuCashWin
  ```
    which is the project for Microsoft Windows using
    Oracle Java 8, and netbeans IDE 8.0

  AND
  ```
    https://github.com/goodvibes2/BackupGnuCashLinux
  ```
    which is the project for GNU/Linux Ubuntu 16.04 using
    Java OpenJDK 8, OpenJFX, and netbeans IDE 8.1

The java source files in both the above projects should be identical
and the dist/BackupGnuCash.jar files in both, being Java bytecode, should
work in both GNU/Linux and Windows. The differences between these projects
are only in the netbeans project files used for building the project.
This is so as to make it easy to download (or clone) the project, set up the
dependencies, and then be able to open the project in netbeans IDE, and
build it without any further setup.

Paste either of the following URL's into a web browser:
```
https:://github.com/goodvibes2/BackupGnuCashWin/blob/master/dist/BackupGnuCash.jar
 or
https:://github.com/goodvibes2/BackupGnuCashLinux/blob/master/dist/BackupGnuCash.jar
```
**Right** click on the **Raw** button, **Save target as**,
  select the required location.

I suggest:
```
GNU/Linux:   /home/[USER_NAME]/BackupGnuCash/BackupGnuCash.jar
Windows:     C:\Users\[USER_NAME]\Documents\BackupGnuCash\BackupGnuCash.jar
```

### Dependencies for using prebuilt backupGnuCash.jar

(See **Building & Installing** below if you wish to build from source)

If you wish to download and use the prebuilt BackupGnuCash.jar from
this github project, the following packages are required to be installed:

#### GNU/Linux
These instructions are for Ubuntu 16.04 but should be similar for other
Gnu/Linux flavours/versions.

##### Java
BackupGnuCash uses Java version 8 (or later) and JavaFX.
These can be either the open **or** Oracle versions.

###### Open Java
Openjdk (http://openjdk.java.net)
Install **openjfx** (which will also install **openjdk-8-jre** if not already
installed). E.g
```
        sudo apt-get install openjfx
```
Openjfx is available for Ubuntu from the wily (15.10) or xenial (16.04)
**universe** repository, but not for previous Ubuntu versions.

If openjfx is not available from your distribution's repositories, try
https://wiki.openjdk.java.net/display/OpenJFX/Main
or
http://chriswhocodes.com/.

###### Oracle Java
Note: Oracle Java 8 includes JavaFX.

Install Oracle Java SE 8 RunTime Environment (jre) from
http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html

##### 7-Zip
```
    sudo apt-get install p7zip-full
```
If p7zip-full is not available from your distribution's repositories, try
http://www.7-zip.org/download.html
or
https://sourceforge.net/projects/sevenzip

#### Windows
All the dependencies are available for Windows XP (I think) 7, 8, and 10.

##### Java
Prebuilt openjfx is not available (as far as I can tell as at 31 May 2016)
for Windows, so use Oracle Java 8 (which includes JavaFX) from
http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html.

##### 7-Zip
Install from http://www.7-zip.org/download.html
or https://sourceforge.net/projects/sevenzip.

If running a 64-bit version of Windows, download and install the 64-bit
version of 7-Zip as BackupGnuCash looks for either
```
      C:\Program Files\7-Zip\7z.exe
      or
      E:\Program Files\7-Zip\7z.exe
```
but not for
```
      C:\Program Files (x86)\7-Zip\7z.exe
      or
      E:\Program Files (x86)\7-Zip\7z.exe
```

## Running
-------

### GNU/Linux
  To run the project from the command line, type the following:
```
    java -jar "[PathTo]/BackupGnuCash.jar" &
```
E.g.
```
    java -jar /home/[USER_NAME]/BackupGnuCash/BackupGnuCash.jar &
```
**Ubuntu**: To set up a BackupGnuCash.desktop file so it can be started from the Unity
Dash:

create either (or both)
```
/usr/share/applications/backup-gnucash.desktop
or 
/home/[USER_NAME]/.local/share/applications/backup-gnucash.desktop
```
containing:
```
[Desktop Entry]
Name=BackupGnuCash
Comment=Backup GnuCash
Exec=java -jar /home/[USER_NAME]/BackupGnuCash/BackupGnuCash.jar
Icon=gnucash-icon
Terminal=false
Type=Application
Categories=Office;Finance;
```
Ensure the **Exec=** line above points to where you put **BackupGnuCash.jar**.

You can also create a shortcut on your **Desktop** by copying backup-gnucash.desktop
to ~/Desktop. Ensure it has execute permissions or you will get error
**Untrusted Application Launcher**. E.g.
```
  cp /usr/share/applications/backup-gnucash.desktop ~/Desktop
  chmod +x ~/Desktop
```

### Windows
Create a shortcut on your desktop:

Right click on the desktop,
New, Shortcut,
Browse to and select your BackupGnuCash.jar file
or just type in the full filestring E.g:
```
C:\Users\[USER_NAME]\Documents\BackupGnuCash\BackupGnuCash.jar
```
Name the shortcut **Backup GnuCash**.

## Internationalization
--------------------

BackupGnuCash is currently English only.


## Building & Installing
---------------------

### Note: This project was developed and tested using:

**GNU/Linux**
```
      Ubuntu 16.04 xenial
      openjdk version 1.8.0_91
      openjfx 8u60-b27-4
      SceneBuilder (Gluon) 8.2.0 
      netbeans IDE 8.1
      7-Zip 9.20 (p7zip-full)
```
**Windows**
```
      Windows 10 64-bit
      Oracle 8 jdk (1.8.0_91) which includes JavaFX
      SceneBuilder (Oracle) 2.0
      netbeans IDE 8.0
      7-Zip 9.20
```
If you wish to build the BackupGnuCash.jar from source, you'll need:

### GNU/Linux:
**Note**: These instructions are for Ubuntu 16.04 but should be similar for other
Gnu/Linux flavours/versions.

#### Java:
BackupGnuCash uses Java version 8 and JavaFX (or OpenJFX).
These can be EITHER the open OR Oracle versions.

##### Openjdk
You'll need the Java Development Kit (jdk) and openjfx: E.g.
```
sudo apt-get install openjdk-8-jdk openjfx
```
OR
##### Oracle Java 8 jdk (includes javaFX)
Download and install Oracle Java SE 8 Development Kit from
http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html.

**Note**: You can download a package which includes both the netbeans IDE and
the jdk from http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html.

#### SceneBuilder
SceneBuilder is the gui tool for modifying the user interface, details
of which are held in BackupGnuCash.fxml.
You only need to install SceneBuilder if you wish to modify the user
interface.

SceneBuilder is NOT available in Ubuntu repositories, and is no longer
    available from Oracle.

Download the .deb from http://gluonhq.com/open-source/scene-builder

E.g. Linux 64 bit: scenebuilder-8.2.0_x64_64.deb

Install:
```
sudo dpkg -i scenebuilder-8.2.0_x64_64.deb
```
#### Netbeans IDE
If you haven't already installed netbeans as part of the Oracle combined
jdk and netbeans:
```
sudo apt-get install netbeans
```
#### 7-Zip
```
    sudo apt-get install p7zip-full
```
If p7zip-full is not available from your distribution's repositories, try
http://www.7-zip.org/download.html
or
https://sourceforge.net/projects/sevenzip/.

### Windows

#### Java
BackupGnuCash uses Java version 8 and JavaFX.
    Openjdk and OpenJFX are NOT available for Windows, so use Oracle versions.

##### Oracle Java 8 jdk (includes javaFX)
Download and install Oracle Java SE 8 Development Kit from
http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html.

**Note**: You can download a package which includes both the netbeans IDE and
the jdk from http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html.

#### SceneBuilder
SceneBuilder is the gui tool for modifying the user interface, details
of which are held in BackupGnuCash.fxml.
You only need to install SceneBuilder if you wish to modify the user
interface.

SceneBuilder is no longer available from Oracle.

Download from http://gluonhq.com/open-source/scene-builder.

#### Netbeans IDE
If you haven't already installed netbeans as part of an Oracle combined
jdk and netbeans:

Download and install from https://netbeans.org/downloads/.

#### 7-Zip
Install from http://www.7-zip.org/download.html
or https://sourceforge.net/projects/sevenzip.

If running a 64-bit version of Windows, download and install the 64-bit
version of 7-Zip as BackupGnuCash looks for either
```
C:\Program Files\7-Zip\7z.exe
or
E:\Program Files\7-Zip\7z.exe
```
but not
```
C:\Program Files (x86)\7-Zip\7z.exe
or
E:\Program Files (x86)\7-Zip\7z.exe
```

#### To download the source files and netbeans project

Note: There are 2 versions of BackupGnuCash on github:
```
https://github.com/goodvibes2/BackupGnuCashWin
```
which is the project for Microsoft Windows using
Oracle Java 8, and netbeans IDE 8.0
    and
```
https://github.com/goodvibes2/BackupGnuCashLinux
```
which is the project for GNU/Linux Ubuntu 16.04 using
Java OpenJDK 8, OpenJFX, and netbeans IDE 8.1.

The java source files in both the above projects should be identical
and the dist/BackupGnuCash.jar files in both should work in both GNU/Linux
or Windows. The differences between these projects are only in the netbeans
project files used for building the project. This is so as to make it easy
to download (or clone) the project, set up the dependencies, and then be
able to open the project in netbeans, and be able to build it without any
further setup.

There are 2 main ways to download the netbeans project from github:

1) If you already have a github account and git installed, you can clone:

   **GNU/Linux**: At the command line:
   ```
      cd
      mkdir NetBeansProjects
      cd NetBeansProjects
      git clone https://github.com/goodvibes2/BackupGnuCashLinux BackupGnuCash
   ```
   **Windows**: In a **git** shell:
   ```
      cd ~/Documents
      mdkir NetBeansProjects
      cd NetBeansProjects
      git clone https://github.com/goodvibes2/BackupGnuCashWin BackupGnuCash
   ```
OR

2) Open the required Linux or Windows URL from above in a web browser,
click on the green **Clone or download** button,
click on **Download ZIP**.
Extract all files from the zip, retaining directories, to
```
  C:\Users\[USER_NAME]\Documents\NetBeansProjects
```
I suggest after extracting, rename folder
```
  C:\Users\[USER_NAME]\Documents\NetBeansProjects\BackupGnuCashWin-master
  to
  C:\Users\[USER_NAME]\Documents\NetBeansProjects\BackupGnuCash
```
    
## Supported Platforms
-------------------

BackupGnuCash 1.1x is known to work with the following operating systems:

- GNU/Linux             -- x86
- Windows               -- x86

BackupGnuCash can probably be made to work on any platform for which GnuCash
does, so long as Java 8 (open or Oracle), JavaFX or openJFX, and 7-Zip are
available.


I hope you find BackupGnuCash useful, and it encourages you to make regular backups
to help protect you from data loss. All hardware fails eventually.

Thank you.
#          BackupGnuCash 2.0.0 README.md file.

This README.md is formatted for GitHub markdown and is most easily read using a web browser
to view https://github.com/goodvibes2/BackupGnuCashWin/blob/master/src/backupgnucash/classes/org/openjfx/README.md.

The last known BackupGnuCash stable series is

|Java Version | BackupGnuCash Stable Series |
|---          | ---                         |
| 8           | 1.3.x                       |
| 11          | 2.0.x                       |

## Table of Contents ##

  - [Overview](#Overview)
  - [Features](#Features)
  - [Selecting BackupGnuCash Version](#BackupGnuCashVersion)
  - [Selecting Precompiled BackupGnuCash or Compile Yourself](#PreCompiledOrCompile)
  - [Dependencies](#Dependencies)
  - [Running](#Running)
  - [Internationalization](#Internationalization)
  - [Building and Installing](#BuildingAndInstalling)
  - [Supported Platforms](#SupportedPlatforms)
  - [Known Issues](#KnownIssues)

![Image of BackupGnuCash](https://github.com/goodvibes2/BackupGnuCashWin/blob/master/BackupGnuCash.PNG)

<a name="Overview"></a>
## Overview ##

BackupGnuCash is an application for easily creating offsite encrypted backups
of the data files used by the **GnuCash** personal finance manager.
See http://www.gnucash.org for more information about GnuCash.

BackupGnuCash is intended to be used with GnuCash data files that are stored in
the default compressed XML format, or uncompressed XML format. It could probably
be used to backup Sqlite format GnuCash data files if the Sqlite data file has
been saved with a .gnucash file name extension (this has NOT been tested).
BackupGnuCash should NOT be used with GnuCash data files saved in MySQL or
PostgreSQL format as the data is spread over multiple files.

This application is written in Java using JavaFX (or OpenJFX) for
the graphical user interface. Java versions before 8 cannot be used with this
application as they do not support JavaFX.

Free software **7-Zip** is used to perform the encryption.

The BackupGnuCash project is _not_ part of the GnuCash project.

If you need help, please email goodchris96@gmail.com and I will help
if I can and have time available.
Please read all of this document before asking for help.

<a name="Features"></a>
## Features ##

Features include

- Available for both GNU/Linux and Microsoft Windows.
BackupGnuCash has been tested in GNU/Linux Ubuntu 16.04 & 18.04 and Windows 10.
GnuCash is also available for Mac OS/X and BackupGnuCash may work
but this has not (yet) been tested.

- An easy-to-use interface.
After finishing a GnuCash session, you just start BackupGnuCash,
select the saved GnuCash book configuration using the Book combobox,
optionally select different directories, enter the password for encryption
and click the Backup button. BackupGnuCash then encrypts the GnuCash data,
and optionally configuration files like saved reports and preferences files to a
date/time stamped file name in the local 3rd party cloud storage directory.

- All the usual GnuCash data files needed for system recovery can be backed up.
  The following folders and files are backed up (if they exist):
  - The **main GnuCash data file** which usually has a .gnucash extension.
    For example
    ```
      GNU/Linux              /home/[USERNAME]/GnuCash/[BOOK].gnucash
      Windows   C:\Users\[USERNAME]\Documents\GnuCash\[BOOK].gnucash
    ```

  - The **GnuCash GSettings** such as saved window positions and other general settings.
    In Windows, these settings are saved in the registry under
    **HKCU\Software\GSettings\org\gnucash**.
    This does **not** include online banking authorisation information.

    BackupGnuCash exports these settings to text file
    **C:\Users\[USERNAME]\.BupGC\GnuCashGSettings.reg**
    and then backs up that file. The settings may be reloaded into the Windows
    registry by using the Registry Editor to import the file.

    In GNU/Linux, GnuCash 2.6 and later, GSettings uses **dconf** for storage.
    BackupGnuCash uses the Linux **dconf** tool to dump GnuCash dconf entries to
    text file **$HOME/.BupGc/gnucash.dconf** which is then backed up. E.g.
    ```
      dconf dump /org/gnucash/ > $HOME/.BupGc/gnucash.dconf
    ```
    The **dconf** tool can also be used to reload the GnuCash dconf entries.

  - The **AqBanking** settings folder, for example
    ```
      GNU/Linux              $HOME/.aqbanking
      Windows   C:\Users\[USERNAME]\aqbanking
    ```

  If **Configuration V2** is ticked:
  - The **main GnuCash configuration** folder, for example
    ```
      GNU/Linux    /home/[USERNAME]/.gnucash
      Windows   C:\Users\[USERNAME]\.gnucash
    ```

    This includes among others
    1. The **saved reports file**, for example
       ```
         GNU/Linux   /home/[USERNAME]/.gnucash/saved-reports-2.4
         Windows  C:\Users\[USERNAME]\.gnucash\saved-reports-2.4
       ```
       **Note** Because the format of the saved reports file has not changed,
       the 2.4 suffix is used for GnuCash 2.4 and 2.6.

    2. The **preferences file** (GnuCash metadata), usually
       ```
         GNU/Linux   /home/[USERNAME]/.gnucash/books/[BOOK].gnucash.gcm
         Windows  C:\Users\[USERNAME]\.gnucash\books\[BOOK].gnucash.gcm
       ```

  - The **GTK2 configuration files**, for example
    ```
      GNU/Linux   /home/[USERNAME]/.gtkrc-2.0.gnucash
      Windows  C:\Users\[USERNAME]\.gtkrc-2.0.gnucash
               C:\Users\[USERNAME]\.gtkrc-2.0
               C:\Program Files (x86)\gnucash\etc\gtk-2.0\gtkrc
    ```

  If **Configuration V3** is ticked:
  - The **main GnuCash configuration** folder, for example
    ```
      GNU/Linux    /home/[USERNAME]/.local/share/gnucash
      Windows   C:\Users\[USERNAME]\AppData\Roaming\GnuCash\
    ```

    This includes among others
    1. The **saved reports file**, for example
       ```
         GNU/Linux       /home/[USERNAME]/.local/share/gnucash/saved-reports-2.n
         Windows   C:\Users\[USERNAME]\AppData\Roaming\GnuCash\saved-reports-2.n
       ```
       **Note** GnuCash 3 will use saved-reports-2.8 if it exists, otherwise
                saved-reports-2.4 but always writes to saved-reports-2.8.


    2. The **preferences file** (GnuCash metadata), usually
       ```
         GNU/Linux     /home/[USERNAME]/.local/share/gnucash/books/[BOOK].gnucash.gcm
         Windows C:\Users\[USERNAME]\AppData\Roaming\GnuCash\books\[BOOK].gnucash.gcm
       ```

    3. The **GnuCash GTK3 configuration file**, for example
       ```
         GNU/Linux                 /home/[USERNAME]/.config/gnucash/gtk-3.0.css
         Windows        C:\Users\[USERNAME]\AppData\Roaming\GnuCash\gtk-3.0.css
       ```

  - The **Non-GnuCash specific GTK3 configuration folder**, for example
    ```
      GNU/Linux   /home/[USERNAME]/.config/gtk-3.0
      Windows  C:\Users\[USERNAME]\AppData\Local\gtk-3.0
    ```
    which may include among others, the following files
    ```
      settings.ini
      gtk.css
      gtk-3.0.css
    ```

  - The file for local customisations to the environment
    ```
      GNU/Linux   /etc/gnucash/environment.local
      Windows     [CE]:\Program Files (x86)\gnucash\etc\gnucash\environment.local

  The 3 **most** important files when restoring or moving to a new computer, are
  usually:
  1. The main GnuCash data file
  2. The saved reports file
  3. The GnuCash metadata file.

  The GnuCash program itself can be downloaded from https://www.gnucash.org/download.

  If you have customised GnuCash in any way that involves files other than
  those mentioned above, you should ensure you either have backups
  of them or instructions for recreating your customisations.
  For example, you need to have a secure record of your online banking
  authorisation information so you can set up online banking again if you
  need to move GnuCash to another computer. This information is held in
  the registry on Windows systems.

  Using BackupGnuCash is NOT a substitute for regular full system backups.
  Most customisations, that are not held in the files saved by this app,
  do not often change, so using BackupGnuCash say daily, along with say
  monthly full system backups, may be a reasonable GnuCash backup strategy.

  BackupGnuCash is also useful for quick 1-off backups, say at end of month
  or end of year, or before doing a major chart of accounts restructure.

- One-time setup of
  1. **Book**.
     
     From BackupGnuCash version 1.20 on, the configuration details for
     up to 100 GnuCash books may defined and saved, and they will be
     automatically loaded when this app starts.

     For example, you may have separate books for your live and test systems,
     or for the books of different companies.

     The most often used book can be flagged as the default, and it will be
     the book showing in the Book combobox each time this app starts.
     The default book shows in **bold** font in the Book combobox dropdown
     list.

     To make a new book the default:
     First select or add the new default book, then check (tick) the Default
     checkbox. It is not permitted to uncheck the Default checkbox.

     To add a new book:
     Enter the new book name in the Book combobox, then press ENTER before
     leaving the combobox, then change the other fields (GnuCash data, Version
     and Dropbox).
     It is not necessary to press ENTER if using JavaFX or OpenJFX version 8u92,
     but it doesn't cause any problems.
     Use the Save Settings button to save the settings for all books.
     
     To delete the book settings for the current book shown in the Book
     combobox, click the **Delete** button.
     The settings for the last remaining book and the default book cannot be
     deleted. To delete the default book, first make another book the default.

  2. **Location and name of the GnuCash data file**.

     Once a GnuCash data file has been selected, the file modification date and
     time is display below it, so the user can check it approximately matches
     the date and time of their last GnuCash session.

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

  3. **GnuCash version** may optionally be specified which
     will add a suffix of _[Version] to the archive file name
     (before the .7z extension).
     While GnuCash developers try hard to make most versions of
     the data files backwards and forwards compatible, some
     versions are not totally backwards and forwards compatible,
     so this information may be useful if a restore is needed.

     For example if you are using GnuCash 3.4, enter version
     ```
       34
     ```
  4. **Configuration V2** may optionally be ticked. If ticked, configuration
     files used by GnuCash V2, in default locations, will be backed up.

  5. **Configuration V3** may optionally be ticked. If ticked, configuration
     files used by GnuCash V3, in default locations, will be backed up.

  6. **Location of the base archive directory**, for example
     ```
       GNU/Linux:    /home/[USERNAME]/Dropbox
       Windows:   C:\Users\[USERNAME]\Dropbox
     ```
     The archive is created in a sub-directory of the base archive
     directory, called "GnuCash". For example
     ```
       GNU/Linux    /home/[USERNAME]/Dropbox/GnuCash
       Windows   C:\Users\[USERNAME]\Dropbox\GnuCash
     ```
     The GnuCash sub-directory must be manually created. If the GnuCash
     sub-directory of the archive directory does not exist, BackupGnuCash
     will show an error message in the Log area at the bottom of the
     window and the Backup button will be disabled.

     As the book file name is part of the archive file name, so long as
     books have unique file names, archive files for multiple books may
     be put in the same archive directory without creating ambiguity.

  After valid entry of
    - the book name
    - the location and name of the main GnuCash data file
    - the location of the base archive directory

  the **Save Settings** button will be enabled, which when clicked,
  will save the BackupGnuCash configuration, in file
  ```
    GNU/Linux: /home/[USERNAME]/.BupGc/defaultProperties
    Windows: C:\Users\USERNAME]\.BupGc/defaultProperties
  ```
  **Note** the password is NOT saved and must be entered each time
  BackupGnuCash is started. The password must be at least 8 characters.

  The next time BackupGnuCash is started, the saved settings will be
  automatically loaded from the defaultProperties file.

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

  The archive file name will be created in the following format
  ```
    GnuCash[BOOK]_yyyyMMddhhmmss[_Version].7z
  ```
  where
  - [BOOK] is the data file name without the .gnucash extension
  - yyyyMMddhhmmss is the current date and time 
  - [_Version] is the optional GnuCash version number if entered.

- Each archive file will contain encrypted copies of the selected data and
  configuration files. The files will be encrypted, using the entered password,
  by free software **7-Zip**, using AES-256 encryption.

  BackupGnuCash is not intended to be used to conceal illegal activities.
  The GnuCash data files are encrypted in the archive because, even though
  3rd party cloud storage services usually try hard to ensure the privacy
  of your data, why risk unencrypted data in the cloud if you don't have to?

  BackupGnuCash does not provide any facility for extracting the data files
  from an encrypted archive. 7-Zip must be installed in order for the
  encrypted archives to be created and for the data files to be extracted.

  **Windows** 7-Zip includes gui tool **7-Zip File Manager** which can be used to
  decrypt the archive files if they need to be restored.
  Alternatively, use the 7-Zip command in a command prompt window, E.g.
  ```
    C:\Program Files\7-Zip\7z.exe e archive.7z
  ```
  extracts all files from archive archive.7z to the current folder. and
  ```
    C:\Program Files\7-Zip\7z.exe e archive.7z -oc:\soft *.gnucash
  ```
  extracts files ending in **.gnucash** from archive archive.7z to **c:\soft** folder.

  **GNU/Linux** **Archive Manager** can be used to manage 7-Zip files if the
  **p7zip-full** package is installed.
  Alternatively, use the 7-Zip command in a terminal window, E.g.
  ```
    7z e archive.7z
  ```
  extracts all files from archive archive.7z to the current directory, and
  ```
    7z e archive.7z -o/tmp "*.gnucash"
  ```
  extracts files ending in .gnucash from archive archive.7z to /tmp directory.

  Of course, the user needs to enter the password carefully, and remember it!
  **If the password is entered incorrectly or forgotten, the encrypted archives
  will be of no use.**

  There is a **Show** checkbox to the right of the password. If this is checked
  (ticked), the password will display. If it is unchecked, the characters of the
  password will display as asterixes.
  After ensuring no-one is watching, show and carefully check the password
  each time you enter it.

<a name="Home Page"></a>
### Home Page ###
None

<a name="BackupGnuCashVersion"></a>
## Selecting BackupGnuCash Version ##

BackupGnuCash V1.x runs in Java 8.

BackupGnuCash V2.x runs in Java 11.

BackupGnuCash versions 1.x and 2.x have the same functionality. Currently (Sep
2019), the only difference is the version of Java they run in. Note that as Java
8 is no longer being developed, future BackupGnuCash enhancements may not be
included in V1.x.

**Definition**

**JRE**
Java Runtime Environment. Simplified - All the files needed to run a Java
program.

If you already have a Java 8 JRE installed, you can run BackupGnuCash V1.x.
You should run BackupGnuCash V2.x if you do not have a Java 8 JRE already
installed.

**Windows**: You can download and install a Java 8 JRE if you wish. A JRE is not
included with Windows.

**GNU/Linux**
  You may wish to use the JRE that is available in the repositories
  for the version of GNU/Linux you are using. For example:

**Ubuntu 16.04 (or Mint 18)**
  Only has Java 8 available so if you already have Java 8 installed, you can
  save some disk space by using the latest BackupGnuCash V1.x. You can still use
  BackupGnuCash V2.x or later if you wish.

**Ubuntu 18.04 (or Mint 19)**
  Has both Java 8 and Java 11 and you can set the system to use either 8 or 11.
  If you have another application that requires Java 8 and you have limited
  disk space, then you could use BackupGnuCash V1.x, otherwise you should use
  V2.x or later.

<a name="DowngradeUbuntu18.04ToJava8"></a>
## Downgrade Ubuntu 18.04 Java 11 to Java 8 ##

If you already have a JRE installed, by default Ubuntu 18.04 will have updated
it to Java 11. To set your Ubuntu 18.04 Java 11 system back to Java 8:
  - Set the *java* command to Java 8
  ```
    sudo update-alternatives --config java
  ```
  and enter the line number for Java 8.
  Check by running
  ```
    java -version
  ```
  - Downgrade the openjfx packages to Java 8
  ```
    sudo apt install openjfx=8u161-b12-1ubuntu2 \
      libopenjfx-java=8u161-b12-1ubuntu2 \
      libopenjfx-jni=8u161-b12-1ubuntu2
  ```
  - To stop the openjfx packages from being automatically updated to the latest
  java 11 packages next update
  ```
    apt-mark hold openjfx libopenjfx-jni libopenjfx-java
  ```
  Note: holding packages at a particular release level may cause
  incompatibilities with other packages so it is preferable not to do so.

  To undo setting a Ubuntu 18.04 system to Java 8
  - Set the *java* command to java 11
  ```
    sudo update-alternatives --config java
  ```
  and enter the line number for Java 11 (auto mode).
  - Unhold the openjfx packages from being automatically updated to the
  latest Java 11 packages next update
  ```
    apt-mark unhold openjfx libopenjfx-jni libopenjfx-java
  ```
  - Update the packages
  ```
    sudo apt-get update
    sudo apt-get upgrade
  ```

<a name="PreCompiledOrCompile"></a>
## Selecting to Use Precompiled BackupGnuCash or Compile Yourself ##

There are 2 ways to use this application

  1. Use the Precompiled Binaries (programs) attached to this GitHub project -
     Download the prebuilt BackupGnuCash.jar (Java 8) or Runtime Image (Java
     11).

     This application comes with no warranty and you should think about the
     security implications of using software downloaded from the internet. You
     are trusting my good nature and the codebase from which this is built!
     This code has not been security audited.

  OR

  2. Download the project source from GitHub, check the code for security and
     build your own binary.

### Precompiled Binaries ###

#### BackupGnuCash V1.x for Java 8 ####

To run **BackupGnuCash V1.x**, you need to have a Java 8 JRE already installed
and you just need to download BackupGnuCash.jar from GitHub.

**To download BackupGnuCash.jar**

Copy and Paste one of the following URL's into a web browser
```
  https://github.com/goodvibes2/BackupGnuCashWin/releases
  or
  https://github.com/goodvibes2/BackupGnuCashLinux/releases
```

Being Java bytecode built from the same Java source files, BackupGnuCash.jar
from either BackupGnuCashWin or BackupGnuCashLinux should work in both
GNU/Linux and Windows.

Find the latest V1.x Release, find the Assets section and click on
BackupGnuCash.jar to download it.

Usually it downloads to your *Downloads* folder.
Move the downloaded BackupGnuCash.jar from your *Downloads* folder to a more
appropriate folder.

I suggest
```
GNU/Linux              /home/[USERNAME]/BackupGnuCash/BackupGnuCash.jar
Windows   C:\Users\[USERNAME]\Documents\BackupGnuCash\BackupGnuCash.jar
```

#### BackupGnuCash V2.x for Java 11 ####

Java modular applications were introduced in Java 9. BackupGnuCash V2.x is a
modular Java 11 application.

It is **not** possible to run a modular java app from a .jar file even if you
have an appropriate JRE already installed.

To run BackupGnuCash V2.x, you do NOT need a Java Runtime Environment (JRE)
already installed as the app is distributed as a Runtime Image which includes
the required JRE and all the files needed.

Disadvantages of a Java Runtime Image:
  - Download is larger as it includes a JRE.
  - Runtime Images work only on the platform (Windows or GNU/Linux in this case)
  for which they are created.

Advantages:
  - Totally independent of any other installed JRE.

**To download a BackupGnuCash V2.x Runtime Image (includes a Java 11 JRE)**

Copy and Paste one of the following URL's into a web browser depending on your
target platform (Windows or Linux)
```
  https://github.com/goodvibes2/BackupGnuCashWin/releases
  or
  https://github.com/goodvibes2/BackupGnuCashLinux/releases
```

Find the latest V2.x Release, then find the Assets section. To download the
Runtime Image archive, click on
```
GNU/Linux       BackupGnuCash_rel2.n.n.tar.gz
Windows         BackupGnuCash_rel2.n.n.zip
```
where 2.n.n is the required release.

Usually a web browser downloads to your *Downloads* folder.
Move the downloaded archive file from your *Downloads* folder to a more
appropriate folder.

I suggest the following folders
```
GNU/Linux              /home/[USERNAME]/BackupGnuCash
Windows   C:\Users\[USERNAME]\Documents\BackupGnuCash
```

Unpack the Runtime Image

**GNU/Linux**
```
cd /home/[USERNAME]/BackupGnuCash/
tar zxf BackupGnuCash_rel2.n.n.tar.gz
```

**Windows**
Use 7-Zip in a command prompt window to unpack all the files from the
Runtime Image archive to the current directory
```
C:
cd \Users\[USERNAME]\Documents\BackupGnuCash
C:\Program Files\7-Zip\7z.exe e BackupGnuCash_rel2.n.n.zip
```
where 2.n.n is the required release.

<a name="Dependencies"></a>
## Dependencies ##

### Dependencies for using prebuilt BackupGnuCash.jar V1.x in Java 8 ###

(See [Building and Installing](#BuildingAndInstalling) below if you wish to
build from source)

If you wish to download and use the prebuilt BackupGnuCash.jar from
this GitHub project, the following packages are required to be installed

#### GNU/Linux ####
These instructions are for Ubuntu 18.04 but should be similar for other
GNU/Linux flavours/versions.

##### Java #####
BackupGnuCash V1.x uses Java version 8 and JavaFX.
These can be either the open **or** Oracle versions.

See also [Known Issues](#KnownIssues).

###### Open Java ######
Openjdk (http://openjdk.java.net)
Install **openjfx** (which will also install **openjdk-8-jre** if not already
installed). E.g
```
  sudo apt-get install openjfx
```

See [Downgrade Ubuntu 18.04 Java 11 to Java 8](#DowngradeUbuntu18.04ToJava8)

Openjfx is available for Ubuntu from the wily (15.10), xenial (16.04) or
bionic (18.04) **universe** repository, but not for previous Ubuntu versions.

If openjfx is not available from your distribution's repositories, try
https://wiki.openjdk.java.net/display/OpenJFX/Main
or
http://chriswhocodes.com/.

###### Oracle Java ######
**Note** Oracle Java 8 includes JavaFX.

Install Oracle Java SE 8 RunTime Environment (jre) from
http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html

##### 7-Zip ######
```
  sudo apt-get install p7zip-full
```
If p7zip-full is not available from your distribution's repositories, try
http://www.7-zip.org/download.html
or
https://sourceforge.net/projects/sevenzip

#### Windows ####
All the dependencies are available for Windows XP (I think) 7, 8, and 10.

##### Java #####
Prebuilt openjfx 8 is not available (as far as I can tell as at 31 May 2016)
for Windows, so use Oracle Java 8 (which includes JavaFX) from
http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html.

See also [Known Issues](#KnownIssues).

##### 7-Zip #####
Install from http://www.7-zip.org/download.html
or https://sourceforge.net/projects/sevenzip.

If your version of Windows is 32-bit, you will need to install 32-bit 7-Zip.

If you're running a 64-bit version of Windows, download and install the 64-bit
version of 7-Zip. BackupGnuCash looks for either
```
      C:\Program Files\7-Zip\7z.exe
      or
      E:\Program Files\7-Zip\7z.exe
      or
      C:\Program Files (x86)\7-Zip\7z.exe
      or
      E:\Program Files (x86)\7-Zip\7z.exe
```

### Dependencies for using prebuilt BackupGnuCash V2.x Runtime Image (Java 11) ###

(See [Building and Installing](#BuildingAndInstalling) below if you wish to
build from source)

If you wish to download and use the prebuilt BackupGnuCash V2.x Runtime Image
from this GitHub project, the following packages need to be installed

#### GNU/Linux ####

##### Java #####

As the Prebuilt BackupGnuCash Runtime Image V2.x includes a Java 11 RTE and
openjfx, it is **not** neccessary to install java or openjfx separately.

##### 7-Zip ######
```
    sudo apt-get install p7zip-full
```
If p7zip-full is not available from your distribution's repositories, try
http://www.7-zip.org/download.html
or
https://sourceforge.net/projects/sevenzip

#### Windows ####

As the Prebuilt BackupGnuCash Runtime Image V2.x includes a Java 11 RTE and
openjfx, it is **not** neccessary to install java or openjfx separately.

##### 7-Zip #####
Install from http://www.7-zip.org/download.html
or https://sourceforge.net/projects/sevenzip.

If your version of Windows is 32-bit, you will need to install 32-bit 7-Zip.

If running a 64-bit version of Windows, download and install the 64-bit
version of 7-Zip. BackupGnuCash looks for either
```
      C:\Program Files\7-Zip\7z.exe
      or
      E:\Program Files\7-Zip\7z.exe
      or
      C:\Program Files (x86)\7-Zip\7z.exe
      or
      E:\Program Files (x86)\7-Zip\7z.exe
```


<a name="Running"></a>
## Running ##

### Running BackupGnuCash V1.x for Java 8 (BackupGnuCash.jar) ###

#### GNU/Linux ####
  To run the app from the command line, type the following
```
    java -jar "[PathTo]/BackupGnuCash.jar" &
```
E.g.
```
    java -jar /home/[USERNAME]/BackupGnuCash/BackupGnuCash.jar &
```
**Ubuntu** To set up a BackupGnuCash.desktop file so it can be started from the Unity
Dash

create either (or both)
```
/usr/share/applications/backup-gnucash.desktop
or 
/home/[USERNAME]/.local/share/applications/backup-gnucash.desktop
```
containing
```
[Desktop Entry]
Name=BackupGnuCash
Comment=Backup GnuCash
Exec=java -jar /home/[USERNAME]/BackupGnuCash/BackupGnuCash.jar
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
  chmod +x ~/Desktop/backup-gnucash.desktop
```

### Windows ###
Create a shortcut on your desktop

Right click on the desktop,
New, Shortcut,
Browse to and select your BackupGnuCash.jar file
or just type in the full filestring E.g
```
C:\Users\[USERNAME]\Documents\BackupGnuCash\BackupGnuCash.jar
```
Name the shortcut **Backup GnuCash**.

### Running BackupGnuCash V2.x for Java 11 (Runtime Image) ###

#### GNU/Linux ####

  To run the app from the command line, type the following
```
  ~/BackupGnuCash/BackupGnuCash_rel2.n.n/dist/jlink/BackupGnuCash/bin/BackupGnuCash &
```
where 2.n.n is the required release.
E.g.
```
  ~/BackupGnuCash/BackupGnuCash_rel2.00/dist/jlink/BackupGnuCash/bin/BackupGnuCash &
```
**Ubuntu** To set up a BackupGnuCash.desktop file so it can be started from the Unity
Dash or *Gnome Applications overview*

create either (or both)
```
/usr/share/applications/backup-gnucash.desktop
or 
/home/[USERNAME]/.local/share/applications/backup-gnucash.desktop
```
containing
```
[Desktop Entry]
Name=BackupGnuCash
Comment=Backup GnuCash
Exec=/home/[UserName]/BackupGnuCash/BackupGnuCash_rel2.n.n/dist/jlink/BackupGnuCash/bin/BackupGnuCash
Icon=gnucash-icon
Terminal=false
Type=Application
Categories=Office;Finance;
```
where 2.n.n is the required release.

You can also create a shortcut on your **Desktop** by copying backup-gnucash.desktop
to ~/Desktop. Ensure it has execute permissions or you will get error
**Untrusted Application Launcher**. E.g.
```
  cp /usr/share/applications/backup-gnucash.desktop ~/Desktop
  chmod +x ~/Desktop/backup-gnucash.desktop
```

#### Windows ####

Create a shortcut on your desktop

Right click on the desktop,
New, Shortcut,
Browse to and select your BackupGnuCash.bat file
or just type in the full filestring E.g
```
C:\Users\[USERNAME]\Documents\BackupGnuCash\BackupGnuCash_rel2.n.n\dist\jlink\BackupGnuCash\bin\BackupGnuCash.bat
```
where 2.n.n is the required release.
Name the shortcut **Backup GnuCash**.


<a name="Internationalization"></a>
## Internationalization ##
--------------------

BackupGnuCash is currently English only.


<a name="BuildingAndInstalling"></a>
## Building and Installing ##
---------------------

There are 2 versions of BackupGnuCash on GitHub
- https://github.com/goodvibes2/BackupGnuCashWin
  which is the NetBeans IDE 8.2 project for Microsoft Windows using
  Oracle Java 8 (Includes JavaFX) or Oracle Java 11 and Gluon JavaFX 11.0.2
- https://github.com/goodvibes2/BackupGnuCashLinux
  which is the NetBeans IDE 8.1 project for GNU/Linux Ubuntu 18.04 using
  Java OpenJDK 8 (and OpenJFX 8) or Oracle Java 11 and Gluon OpenJFX 11.0.2

The java source files in both the above projects should be identical
and the dist/BackupGnuCash.jar files in both, being Java bytecode, should
work in both GNU/Linux and Windows. The differences between these projects
are only in the NetBeans project files used for building the project.
This is so as to make it easy to download (or clone) the project, set up the
dependencies, and then be able to open the project in NetBeans IDE, and
build it without any further setup.

### **Note** This project was developed and tested using ###

**BackupGnuCash V1.x for Java 8**

**GNU/Linux**
```
      Ubuntu 18.04.2 bionic
      openjdk version 1.8.0_191
      openjfx 8u161-b12-1ubuntu2
      SceneBuilder (Gluon) 8.2.0
      NetBeans IDE 8.1
      7-Zip 16.02+dfsg-6 (p7zip-full)
```
**Windows**
```
      Windows 10 64-bit
      Oracle 8 jdk (1.8.0_191) which includes JavaFX
      SceneBuilder (Gluon) 8.5.0
      NetBeans IDE 8.2
      7-Zip 18.06
```

**BackupGnuCash V2.x for Java 11**

**GNU/Linux**
```
      Ubuntu 18.04.2 bionic
      Oracle jdk 11.0.4
      Gluon openjfx 11.0.2
      Gluon jfx jmods 11.0.2
      SceneBuilder (Gluon) 8.2.0
      Apache NetBeans IDE 11.0
      7-Zip 16.02+dfsg-6 (p7zip-full)
```
**Windows**

```
      Windows 10 64-bit
      Oracle jdk (11.0.4)
      Gluon openjfx 11.0.2
      Gluon JavaFX jmods 11.0.2
      SceneBuilder (Gluon) 8.5.0
      Apache NetBeans IDE 11.0
      7-Zip 18.06
```

### Build the V1.x BackupGnuCash.jar from source ###
You will need

#### GNU/Linux ####
**Note** These instructions are for Ubuntu 18.04 but should be similar for other
GNU/Linux flavours/versions.

##### Java #####
BackupGnuCash uses Java version 8 and JavaFX (or OpenJFX).
These can be EITHER the open OR Oracle versions.

###### Openjdk ######
You'll need the Java Development Kit (jdk) and OpenJFX. E.g.
```
sudo apt-get install openjdk-8-jdk openjfx
```

Ubuntu 18.04 includes Java 8 and Java 11.
BackupGnuCash V1.x needs Java 8, so set the default Java version to 8. See
[Downgrade Ubuntu 18.04 Java 11 to Java 8](#DowngradeUbuntu18.04ToJava8)

OR
###### Oracle Java 8 jdk (includes JavaFX) ######
Download and install Oracle Java SE 8 Development Kit from
http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html.

**Note** You can download a package which includes both the NetBeans IDE and
the jdk from http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html.

##### SceneBuilder #####
SceneBuilder is the gui tool for modifying the user interface, details
of which are held in BackupGnuCash.fxml.
You only need to install SceneBuilder if you wish to modify the user
interface.

SceneBuilder is NOT available in Ubuntu repositories, and is no longer
    available from Oracle.

Download the .deb from http://gluonhq.com/open-source/scene-builder

E.g. Linux 64 bit: scenebuilder-8.2.0_x64_64.deb

Install
```
sudo dpkg -i scenebuilder-8.2.0_x64_64.deb
```

##### NetBeans IDE #####
If you haven't already installed NetBeans as part of the Oracle combined
jdk and NetBeans
```
sudo apt-get install netbeans
```

##### 7-Zip #####
```
    sudo apt-get install p7zip-full
```
If p7zip-full is not available from your distribution's repositories, try
http://www.7-zip.org/download.html
or
https://sourceforge.net/projects/sevenzip/.

#### Windows ####

##### Java #####
BackupGnuCash V1.x uses Java version 8 and JavaFX.
    Openjdk and OpenJFX are NOT available for Windows, so use Oracle versions.

###### Oracle Java 8 jdk (includes JavaFX) ######
Download and install Oracle Java SE 8 Development Kit from
http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html.

**Note** You can download a package which includes both the NetBeans IDE and
the jdk from http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html.

##### SceneBuilder #####
SceneBuilder is the gui tool for modifying the user interface, details
of which are held in BackupGnuCash.fxml.
You only need to install SceneBuilder if you wish to modify the user
interface.

SceneBuilder is no longer available from Oracle.

Download from http://gluonhq.com/open-source/scene-builder.

##### NetBeans IDE #####
If you haven't already installed NetBeans as part of an Oracle combined
jdk and NetBeans

Download and install from https://netbeans.org/downloads/.

##### 7-Zip #####
Install from http://www.7-zip.org/download.html
or https://sourceforge.net/projects/sevenzip.

BackupGnuCash looks for
```
\Program Files\7-Zip\7z.exe
or
\Program Files (x86)\7-Zip\7z.exe
```
on both the C: and E: drive.

### Build the V2.x Runtime Image from source ###

Follow the instructions https://openjfx.io/openjfx-docs/#introduction
including the instructions for using JavaFX and NetBeans to build a
Modular app from the IDE (NetBeans).

**Note** The instructions on the above webpage are for Java 12 but BackupGnuCash
V2.x was built for Java 11 as that was the version available in the Ubuntu 18.04
repositories. Substitute Java 11 for Java 12 in the above webpage instructions.

As a Modular Java 11 app is run from a Runtime Image (which includes its
own JRE), and the Ubuntu jdk 11.0.4 is broken when used for a JavaFX Modular
app (so we need to use the Oracle jdk), the Ubuntu jdk is actually irrelevant,
and this app could probably have been built using Java 12 (or later).

You will need

#### GNU/Linux ####
**Note** These instructions are for Ubuntu 18.04 but should be similar for other
GNU/Linux flavours/versions.

##### Java JDK #####
BackupGnuCash V2.x uses Oracle Java jdk version 11

Download jdk-11.0.4_linux-x64_bin.tar.gz from Oracle http://jdk.java.net/11/.
The .gz contains base directory jdk-11.0.4.

Extract (assuming the download is in ~/Downloads)
```
  cd $HOME
  mkdir java
  cd java
  tar zxf ~/Downloads/jdk-11.0.4_linux-x64_bin.tar.gz
```
This will create $HOME/java/jdk-11.0.4/...

###### openjfx ######
Download JavaFX Linux SDK (openjfx-11.0.2_linux-x64_bin-sdk.zip)
from https://gluonhq.com/products/javafx/. The .zip contains base directory
javafx-sdk-11.0.2.

Extract (assuming download is in ~/Downloads)
```
cd $HOME/java
unzip ~/Downloads/openjfx-11.0.2_linux-x64_bin-sdk.zip
```
This will create $HOME/java/javafx-sdk-11.0.2/...

##### jfx jmods #####
Download JavaFX Linux jmods (openjfx-11.0.2_linux-x64_bin-jmods.zip)
from https://gluonhq.com/products/javafx/. The .zip contains base directory
javafx-jmods-11.0.2.

Extract (assuming download is in ~/Downloads)
```
cd $HOME/java
unzip ~/Downloads/openjfx-11.0.2_linux-x64_bin-jmods.zip
```
This will create $HOME/java/javafx-jmods-11.0.2/...

##### SceneBuilder #####
SceneBuilder is the gui tool for modifying the user interface, details
of which are held in BackupGnuCash.fxml.
You only need to install SceneBuilder if you wish to modify the user
interface.

SceneBuilder is NOT available in Ubuntu repositories, and is no longer
    available from Oracle.

Download the .deb from http://gluonhq.com/open-source/scene-builder

E.g. Linux 64 bit: scenebuilder-8.2.0_x64_64.deb

Install
```
sudo dpkg -i scenebuilder-8.2.0_x64_64.deb
```

##### NetBeans IDE #####
Ubuntu 18.04 repositories contains NetBeans 10.0 but we need Apache NetNeans 11.
```
cd ~/java
wget https://www-us.apache.org/dist/incubator/netbeans/incubating-netbeans/incubating-11.0/incubating-netbeans-11.0-bin.zip
unzip incubating-netbeans-11.0-bin.zip
sudo mv netbeans/ /opt/
```
The script to run NetBeans is /opt/netbeans/bin/netbeans

Create NetBeans Desktop Launcher
```
  sudo nano /usr/share/applications/netbeans11.desktop
```
with contents
```
[Desktop Entry]
Name=NetBeans 11 IDE
Comment=NetBeans 11 IDE
Type=Application
Encoding=UTF-8
Exec=/opt/netbeans/bin/netbeans
Icon=/opt/netbeans/nb/netbeans.png
Categories=GNOME;Application;Development;
Terminal=false
StartupNotify=true
```

##### 7-Zip #####
```
    sudo apt-get install p7zip-full
```
If p7zip-full is not available from your distribution's repositories, try
http://www.7-zip.org/download.html
or
https://sourceforge.net/projects/sevenzip/.

#### Windows ####

##### Java #####
BackupGnuCash V2.x uses Java JDK version 11 and openjfx.

###### Oracle Java JDK 11 ######
Openjdk 11 is only available from Oracle. You will need a free Oracle account
to download the JDK.

Download Oracle Java SE 11.0.4 Java Development Kit
(jdk-11.0.4_windows-x64_bin.exe) from
https://www.oracle.com/technetwork/java/javase/downloads/index.html.

Double click on the downloaded .exe to install (by default) to
C:\Program Files\Java\jdk-11.04.

###### Openjfx ######
Download JavaFX Windows SDK (openjfx-11.0.2_windows-x64_bin-sdk.zip)
from https://gluonhq.com/products/javafx.
The .zip contains base directory javafx-sdk-11.0.2.

Use 7-Zip in a command prompt window to unpack all the files from the zip
archive to the current directory
```
C:
cd \Program Files\Java
C:\Program Files\7-Zip\7z.exe e openjfx-11.0.2_windows-x64_bin-sdk.zip
```
This will create C:\Program Files\Java\javafx-sdk-11.0.2\\...

###### jfx jmods ######
Download JavaFX Windows jmods (openjfx-11.0.2_windows-x64_bin-jmods.zip)
from https://gluonhq.com/products/javafx
The .zip contains base directory javafx-jmods-11.0.2.

Use 7-Zip in a command prompt window to unpack all the files from the zip
archive to the current directory
```
C:
cd \Program Files\Java
C:\Program Files\7-Zip\7z.exe e openjfx-11.0.2_windows-x64_bin-jmods.zip
```
This will create C:\Program Files\Java\javafx-jmods-11.0.2\\...

##### SceneBuilder #####
SceneBuilder is the gui tool for modifying the user interface, details
of which are held in BackupGnuCash.fxml.
You only need to install SceneBuilder if you wish to modify the user
interface.

SceneBuilder is no longer available from Oracle.

Download from http://gluonhq.com/open-source/scene-builder.

##### NetBeans IDE #####
Download NetBeans 11.0 LTS Binary (incubating-netbeans-11.0-bin.zip)
from https://netbeans.apache.org/download/. The zip archive files all start with
folder netbeans.

As you may have permissions problems extracting directly to C:\Program Files,
extract to your Downloads folder, then use File Explorer to create folder
C:\Program Files\Netbeans11.0 and move the extracted *netbeans* folder to
C:\Program Files\Netbeans11.0.

Create a desktop shortcut pointing to
  C:\Program Files\Netbeans11.0\netbeans\bin\netbeans64.exe

##### 7-Zip #####
Install from http://www.7-zip.org/download.html
or https://sourceforge.net/projects/sevenzip.

BackupGnuCash looks for
```
\Program Files\7-Zip\7z.exe
or
\Program Files (x86)\7-Zip\7z.exe
```
on both the C: and E: drive.

#### To download the source files and NetBeans project ####

There are 2 versions of BackupGnuCash on GitHub
- https://github.com/goodvibes2/BackupGnuCashWin
  which is the project for Microsoft Windows
- https://github.com/goodvibes2/BackupGnuCashLinux
  which is the project for GNU/Linux

The java source files in both the above projects should be identical.
The differences between these projects are only in the NetBeans project files
used for building the project. This is so as to make it easy to download (or
clone) the project, set up the dependencies, and then be able to open the
project in NetBeans, and be able to build it without any further setup.

There are 2 main ways to download the BackupGnuCash NetBeans project from GitHub

1) **Clone** (if you already have a GitHub account and git installed)

   ##### BackupGnuCash V1.x (Java 8) + BackupGnuCash V2.x (Java 11) #####
     ###### GNU/Linux - At the command line
     ```
        cd
        mkdir NetBeansProjects
        cd NetBeansProjects
        git clone https://github.com/goodvibes2/BackupGnuCashLinux BackupGnuCash
     ```
     ###### Windows - In a **git** shell
     ```
        cd ~/Documents
        mdkir NetBeansProjects
        cd NetBeansProjects
        git clone https://github.com/goodvibes2/BackupGnuCashWin BackupGnuCash
     ```

   ##### BackupGnuCash V1.x Java 8 Extra Task #####
   As the master branch has now been updated to Java 11, you need to reset back to
   the last commit before Java 11 was merged into master. To create a new branch
   (called say java8)
   ```
     cd BackupGnuCash
   GNU/Linux:
     git checkout -b java8 bdbb1501c24f6853a5bc46109c65612536a65b79
   Windows:
     git checkout -b java8 e3d5f4986b19febc59ddd14c5b432f649dc36b57
   ```

OR

2) **Download the source code archive for the required release**

   Copy and Paste one of the following URL's into a web browser depending on your
   target platform (Windows or Linux)
   ```
     https://github.com/goodvibes2/BackupGnuCashWin/releases
    or
     https://github.com/goodvibes2/BackupGnuCashLinux/releases
   ```

   Find the latest V1.x or V2.x Release as required, then find the Assets section.
   Click on
   ```
   GNU/Linux       Source code (tar.gz)
   Windows         Source code (zip)
   ```
   This will download source code archive file
   ```
   GNU/Linux       BackupGnuCashLinux-v.n.n.tar.gz
   Windows         BackupGnuCashWin-v.n.n.zip
   ```
   where v.n.n is the required release.

   Usually a web browser downloads to your *Downloads* folder.
   Move the downloaded archive file from your *Downloads* folder to a more
   appropriate folder.

   I suggest the following folders
   ```
     GNU/Linux              /home/[USERNAME]/NetBeansProjects
     Windows   C:\Users\[USERNAME]\Documents\NetBeansProjects
   ```

   Unpack the source code and project files

   **GNU/Linux**
   ```
     cd /home/[USERNAME]/NetBeansProjects
     tar zxf BackupGnuCashLinux-v.n.n.tar.gz
   ```
   where v.n.n is the required release.

   **Windows**
   Use 7-Zip in a command prompt window to unpack all the files from the
   source code archive to the current directory
   ```
   C:
   cd \Users\[USERNAME]\Documents\NetBeansProjects
   C:\Program Files\7-Zip\7z.exe e BackupGnuCashWin-v.n.n.zip
   ```
   where v.n.n is the required release.

After extracting, before you open the project in NetBeans, you should edit the
following files to ensure the paths match your project
```
  build.xml
  nbproject/private/private.properties
  nbproject/build-impl.xml
  nbproject/project.properties
  nbproject/project.xml
```

<a name="SupportedPlatforms"></a>
## Supported Platforms ##

BackupGnuCash is known to work with the following operating systems

- GNU/Linux             -- x86
- Windows               -- x86

BackupGnuCash can probably be made to work on any platform for which GnuCash
does so long as the [Dependencies](#Dependencies) are available.


<a name="KnownIssues"></a>
## Known Issues ##

1) This program cannot reliably know if environment variables have been used to
   alter the locations where configuration files are stored. Therefore this
   program will only back up configuration files stored in default locations.

2) From BackupGnuCash version 1.20 15 Jul 2016, Java 1.8.0_72 (8u72) or later is
   required due to bug https://bugs.openjdk.java.net/browse/JDK-8136838 as the
   value of ComboBox.getValue() was not correct in previous versions.

   As of 14 Sep 2019, the current Java version on Windows is 1.8.0_221, Ubuntu
   16.04 is 1.8.0_222 and Ubuntu 18.04 is 1.8.0_222.
   Ubuntu 16.04 openjfx is version 8u60-b27-4 (1.8.0_60) which works so long as
   when adding a new book, ENTER is pressed after typing a new book name into
   the Book combobox. I.e. Press ENTER before leaving the combobox.

3) Any new book added to the Book combobox is added to the end of the combobox
   dropdown list, rather than in it's sorted position. The book settings are
   sorted before they are saved to the defaultProperties file, so the combobox
   dropdown list will be sorted next time the program is started.
   This is because of the following bug in in Java 1.8.0_92
     https://bugs.openjdk.java.net/browse/JDK-8087838.
   The use of a SortedList for the combobox can be re-instated after the above
   bug is fixed. See also
   http://stackoverflow.com/questions/38342046/how-to-use-a-sortedlist-with-javafx-editable-combobox-strange-onaction-events


I hope you find BackupGnuCash useful, and encourages you to make regular backups
to help protect from data loss. All hardware fails eventually and every-one
makes mistakes.

Thank you.

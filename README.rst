

=============================
PleftDroid OpenSource Edition
=============================


PREREQUISITES FOR USING PLEFTDROID
----------------------------------

Access to a Pleft Server instance
Pleft is Open Source (GPLv3), you can download and install a copy from:
http://code.google.com/p/pleft/

Thus you will have full contol on both Server side code and client Side code.


PREREQUISITES FOR BUILDING PLEFTDROID
-------------------------------------

A computer with JDK/JRE/OPENJDK 6 and a recent Eclipse installed will do.

BUILDING PLEFTDROID
-------------------

To build PleftDroid You will need:

- the source (if you read this you already got it).
- Eclipse with the Android Development Tools (ADT) installed
  (see http://developer.android.com/tools/help/adt.html).
- you will need to download separately the Gson Library, version 1.7.1
  (later versions may work but I didn't test) which can downloaded from here:
  http://code.google.com/p/google-gson/downloads/list
  You just have to put the jar in the libraries classpath in project settings.

To import the project in Eclipse:
1. unpack the source code in a directory of your choice.
2. click on ``File -> New -> Project...``.
3. select ``Android -> Android Project from exixting code`` then press ``Next``.
4. choose the directory where you unpacked the source code and check
   ``copy projects into workspace``.
5. click on finish.


RELEASING YOUR CUSTOMIZED APP ON GOOGLE PLAY
--------------------------------------------

If you want to release the app on Google Play, you will need to refactor the
package name to reflect your website or to a package name of your choice.
The reason is that package name is used for unique identification for Android
applications.
A naming clash of the package would prevent you from publishing it.

The refactoring is straightforward using eclipse, just be careful in renaming
accordingly all occurrences of the old package name everywhere and not only in
the Java code.
The most relevant files are the AndroidManifest.xml and
res/layout/detail_voting_rowdbc.xml

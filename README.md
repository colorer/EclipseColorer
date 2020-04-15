EclipseColorer
========================
EclipseColorer - is a syntax highlighting plugin for Eclipse
# No longer supported

Structure
------------------------

The plugin consists of
  * src - java library
  * libnative - library in C ++ with the basic functions of text parsing
  * schemes - Colorer schemes

Since libnative is specific for each platform on which this plugin can be run, its build is performed separately 
for each platform. Ready libraries are added to the folder distr/os and are used in the build plugin.

How to build from source
------------------------

To build plugin from source, you will need:

  * git
  * ant 1.8 or higher
  * java development kit 6 (jdk) or higher
  * perl
  * eclipse

Download the source from git repository

    git clone https://github.com/colorer/EclipseColorer.git --recursive

or update local git repository

    git pull
    git submodule update --recursive

Set the path to Eclipse in the `build.properties` file in the variable eclipse.dir
Run build

    ant

When there are build errors for schemes, you need to get acquainted with schemes\README.md.

For libnative build
 
  * run 'ant colorer.jar'
  * in folder libnative cause the make command for the appropriate platform

Links
------------------------

* Project main page: [http://colorer.sourceforge.net/](http://colorer.sourceforge.net/)

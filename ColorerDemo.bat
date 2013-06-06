
rem Self runnable version of Colorer library JNI interface
rem Uses SWT API.

rem To use it, please provide full path to colorer/catalog.xml file
rem in %HOME%/.colorer5catalog  file.

set ECLIPSEHOME=d:\programs\eclipse\

set SWT_JAR=%ECLIPSEHOME%\plugins\org.eclipse.swt.win32.win32.x86_3.3.0.v3346.jar
set SWT_LIB=%ECLIPSEHOME%\plugins\org.eclipse.swt.win32_%ECLIPSEVER%\os\win32\x86

set COLORER5CATALOG=D:\projects\Colorer-take5.be5\catalog.xml

set classpath=colorer.jar;%SWT_JAR%;%classpath%
set path=libnative;./os/win32/x86;%SWT_LIB%;%path%

rem java net.sf.colorer.swt.ColorerDemo
%*
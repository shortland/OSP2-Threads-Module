# Project 1 Notes

To run demo version of OSP 2:

`$ java -classpath .:Demo.jar osp.OSP`

Commands to compile and run OSP 2:

```sh
javac -g -classpath .:OSP.jar: -d . *.java

java -classpath .:OSP.jar:. osp.OSP

jdb -classpath .:OSP.jar:. osp.OSP
```

OSP 2 command line options:

```text
-help - lists all command-line options

-noGUI - runs the simulator without the GUI

-paramFile - use the next argument as the parameter file

-guiFile - use the next argument as GUI configuration file

-userOption - use the next argument to set the global variable userOption

-debugOn - includes debugging messages in the OSP system log
```

How to specify command line arguments to the Make command:

`make run OPTS="-paramFile my-other-param-file.osp -noGUI"`


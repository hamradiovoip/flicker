REM File: Flicker-startup.bat
REM Simple Flicker batch script for Windows.
REM Starts Flicker from its jar file  on the command line.
REM To use more memory on startup, increase 96 (Mbytes) to a larger value.

java -Xmx96M -jar Flicker.jar

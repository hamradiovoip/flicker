#!/bin/sh
# File: Flicker-startup.sh
# Simple Flicker batch script for Unix.
# Starts Flicker from its jar file  on the command line.
# To use more memory on startup, increase 96 (Mbytes) to a larger value.

java -Xmx96M -jar Flicker.jar

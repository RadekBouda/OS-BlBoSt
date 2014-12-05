@echo off
echo.
echo.
echo OS Simulator by team BlBoSt - Compilator
echo Copyright 2014 Blaha, Bouda, Steinberger
echo.

echo Creating directory for compiled sources
mkdir build
echo.
echo Creating a list of source files
echo.
echo Compilation engaged
dir /s /B *.java > sources.txt
javac -d build -nowarn -Xlint:unchecked @sources.txt
echo.
echo Removing a list of source files
del sources.txt
echo.
echo Compilation completed
echo.
pause
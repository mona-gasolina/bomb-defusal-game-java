@echo off
rem Starts the game. Works if bomb-defusal.jar is next to this file or in the target folder.
if exist bomb-defusal.jar (
    java -jar bomb-defusal.jar
) else (
    java -jar target\bomb-defusal.jar
)

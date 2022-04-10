@echo off
IF NOT EXIST %1 (
	echo File '%1' does not exists!
	exit /b 1
)

mongoimport --db patents --collection patents --file %1
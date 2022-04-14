@echo off
IF NOT EXIST %1 (
	echo File '%1' does not exists!
	exit /b 1
)

start mongo_dbtools/bin/mongoimport.exe --db patents --collection patents --file %1
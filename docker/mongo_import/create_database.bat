@echo off
IF NOT EXIST create_database.js (
	echo create_database.js script not found!
	exit /b 1
)

"mongo/mongo.exe" --host localhost:27017 < create_database.js
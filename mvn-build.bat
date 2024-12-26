@echo off
for /f "tokens=*" %%i in (.env) do set %%i
mvn install
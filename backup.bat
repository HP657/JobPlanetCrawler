@echo off
setlocal

REM -- Configuration --
REM The directory where you want to store backups.
SET BACKUP_BASE_DIR=%USERPROFILE%\Desktop\Projects\backups

REM The name of the project directory to back up.
SET PROJECT_DIR_NAME=jobplanetcrawler

REM -- End of Configuration --

REM Create a timestamp in YYYY-MM-DD_HH-MM-SS format
for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /format:list') do set DATETIME=%%I
set TIMESTAMP=%DATETIME:~0,4%-%DATETIME:~4,2%-%DATETIME:~6,2%_%DATETIME:~8,2%-%DATETIME:~10,2%-%DATETIME:~12,2%

SET BACKUP_PATH=%BACKUP_BASE_DIR%\%PROJECT_DIR_NAME%_%TIMESTAMP%
SET SOURCE_DIR=%~dp0

echo Backing up %SOURCE_DIR% to %BACKUP_PATH%

REM Robocopy is more robust for copying directories.
REM /E copies subdirectories, including empty ones.
REM /NFL and /NDL suppress logging of file and directory names to the console.
robocopy "%SOURCE_DIR%" "%BACKUP_PATH%" /E /NFL /NDL

echo Backup complete.

endlocal

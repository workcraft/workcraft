:: Enable command line extensions
VERIFY OTHER 2>NUL
SETLOCAL ENABLEEXTENSIONS
IF ERRORLEVEL 1 (
    ECHO "Cannot enable command line extensions."
    EXIT /B 1
)

:: Change to Workcraft home directory and put it into the WORKCRAFT_HOME variable
SET WORKCRAFT_HOME=%~dp0
CD "%WORKCRAFT_HOME%"

:: SET the JVM executable in JAVA_BIN variable (if not defined yet)
IF NOT DEFINED JAVA_BIN (
    IF NOT DEFINED JAVA_HOME (
        SET JAVA_BIN=javaw.exe
    ) ELSE (
        IF EXIST "%JAVA_HOME%\bin\javaw.exe" (
            SET "JAVA_BIN=%JAVA_HOME%\bin\javaw.exe"
        ) ELSE (
            SET "JAVA_BIN=%JAVA_HOME%\javaw.exe"
        )
    )
)

SET CLASSPATH=%WORKCRAFT_HOME%\workcraft.jar;%WORKCRAFT_HOME%\plugins\*

"%JAVA_BIN%" org.workcraft.Console %*

ENDLOCAL

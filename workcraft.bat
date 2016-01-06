:: Enable command line extensions
VERIFY OTHER 2>NUL
SETLOCAL ENABLEEXTENSIONS
IF ERRORLEVEL 1 (    
    ECHO "Cannot enable command line extensions." 
    EXIT /B 1
)

:: Change to Workcraft home directory and put it into the WORKCRAFT_HOME variable
SET WORKCRAFT_HOME=%~dp0
PUSHD "%WORKCRAFT_HOME%"

:: SET the JVM executable in the JAVA_BIN variable (if not defined yet)
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

:: Add all the plugins and third-party JARs to the CLASSPATH variable
SET CLASSPATH=^
%WORKCRAFT_HOME%\CircuitPlugin\bin;^
%WORKCRAFT_HOME%\CpogsPlugin\bin;^
%WORKCRAFT_HOME%\DfsPlugin\bin;^
%WORKCRAFT_HOME%\FsmPlugin\bin;^
%WORKCRAFT_HOME%\FstPlugin\bin;^
%WORKCRAFT_HOME%\GraphPlugin\bin;^
%WORKCRAFT_HOME%\MpsatSynthesisPlugin\bin;^
%WORKCRAFT_HOME%\MpsatVerificationPlugin\bin;^
%WORKCRAFT_HOME%\PcompPlugin\bin;^
%WORKCRAFT_HOME%\PetriNetPlugin\bin;^
%WORKCRAFT_HOME%\PetrifyExtraPlugin\bin;^
%WORKCRAFT_HOME%\PetrifyPlugin\bin;^
%WORKCRAFT_HOME%\PolicyNetPlugin\bin;^
%WORKCRAFT_HOME%\PunfPlugin\bin;^
%WORKCRAFT_HOME%\SONPlugin\bin;^
%WORKCRAFT_HOME%\STGPlugin\bin;^
%WORKCRAFT_HOME%\ThirdParty\*;^
%WORKCRAFT_HOME%\ThirdParty\batik\*;^
%WORKCRAFT_HOME%\WorkcraftCore\bin;^
%WORKCRAFT_HOME%\XmasPlugin\bin;^
;

:: Run Workcraft with the specific JAVA_BIN and CLASSPATH
"%JAVA_BIN%" org.workcraft.Console %* 

:: Restore the current working directory
POPD
ENDLOCAL

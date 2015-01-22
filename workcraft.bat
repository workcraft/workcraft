:: Change to Workcraft home directory and put it into the WORKCRAFT_HOME variable
set WORKCRAFT_HOME=%~dp0
set TOOLS_DIR=%WORKCRAFT_HOME%\tools
set PETRIFY_DIR=%TOOLS_DIR%\petrify
set PUNF_DIR=%TOOLS_DIR%\punf
set MPSAT_DIR=%TOOLS_DIR%\mpsat
set PCOMP_DIR=%TOOLS_DIR%\pcomp
set SCENCO_DIR=%TOOLS_DIR%\scenco
set ESPRESSO_DIR=%TOOLS_DIR%\espresso
set ABC_DIR=%TOOLS_DIR%\abc
set DOT_DIR=%TOOLS_DIR%\dot

:: Add location of the third-party tools to the PATH variable
set PATH=%PATH%;%PETRIFY_DIR%;%PUNF_DIR%;%MPSAT_DIR%;%PCOMP_DIR%;%SCENCO_DIR%;%ESPRESSO_DIR%;%ABC_DIR%;%DOT_DIR%

pushd "%WORKCRAFT_HOME%"

:: Set the JVM executable in the JAVA_BIN variable (if not defined yet)
IF "%JAVA_BIN%"=="" (
    IF "%JAVA_HOME%"=="" (
        set JAVA_BIN=javaw.exe
    ) ELSE (
        IF EXIST "%JAVA_HOME%\bin\javaw.exe" (
            set JAVA_BIN=%JAVA_HOME%\bin\javaw.exe
        ) ELSE (
            set JAVA_BIN=%JAVA_HOME%\javaw.exe
        )
    )
)

:: Add all the plugins and third-party JARs to the CLASSPATH variable
set CLASSPATH=^
%WORKCRAFT_HOME%\CircuitPlugin\bin;^
%WORKCRAFT_HOME%\CpogsPlugin\bin;^
%WORKCRAFT_HOME%\DfsPlugin\bin;^
%WORKCRAFT_HOME%\FsmPlugin\bin;^
%WORKCRAFT_HOME%\FstPlugin\bin;^
%WORKCRAFT_HOME%\GraphPlugin\bin;^
%WORKCRAFT_HOME%\MpsatPlugin\bin;^
%WORKCRAFT_HOME%\PetriNetPlugin\bin;^
%WORKCRAFT_HOME%\PetrifyPlugin\bin;^
%WORKCRAFT_HOME%\PolicyNetPlugin\bin;^
%WORKCRAFT_HOME%\SONPlugin\bin;^
%WORKCRAFT_HOME%\STGPlugin\bin;^
%WORKCRAFT_HOME%\ThirdParty;^
%WORKCRAFT_HOME%\ThirdParty\batik\batik-anim.jar;^
%WORKCRAFT_HOME%\ThirdParty\batik\batik-awt-util.jar;^
%WORKCRAFT_HOME%\ThirdParty\batik\batik-bridge.jar;^
%WORKCRAFT_HOME%\ThirdParty\batik\batik-codec.jar;^
%WORKCRAFT_HOME%\ThirdParty\batik\batik-css.jar;^
%WORKCRAFT_HOME%\ThirdParty\batik\batik-dom.jar;^
%WORKCRAFT_HOME%\ThirdParty\batik\batik-ext.jar;^
%WORKCRAFT_HOME%\ThirdParty\batik\batik-extension.jar;^
%WORKCRAFT_HOME%\ThirdParty\batik\batik-gui-util.jar;^
%WORKCRAFT_HOME%\ThirdParty\batik\batik-gvt.jar;^
%WORKCRAFT_HOME%\ThirdParty\batik\batik-parser.jar;^
%WORKCRAFT_HOME%\ThirdParty\batik\batik-script.jar;^
%WORKCRAFT_HOME%\ThirdParty\batik\batik-svg-dom.jar;^
%WORKCRAFT_HOME%\ThirdParty\batik\batik-svggen.jar;^
%WORKCRAFT_HOME%\ThirdParty\batik\batik-swing.jar;^
%WORKCRAFT_HOME%\ThirdParty\batik\batik-transcoder.jar;^
%WORKCRAFT_HOME%\ThirdParty\batik\batik-util.jar;^
%WORKCRAFT_HOME%\ThirdParty\batik\batik-xml.jar;^
%WORKCRAFT_HOME%\ThirdParty\batik\xml-apis-ext.jar;^
%WORKCRAFT_HOME%\ThirdParty\batik\xml-apis.jar;^
%WORKCRAFT_HOME%\ThirdParty\commons-logging-1.1.3.jar;^
%WORKCRAFT_HOME%\ThirdParty\flexdock-0.5.1.jar;^
%WORKCRAFT_HOME%\ThirdParty\jackson-core-asl-1.8.10.jar;^
%WORKCRAFT_HOME%\ThirdParty\jackson-mapper-asl-1.8.10.jar;^
%WORKCRAFT_HOME%\ThirdParty\javaparser-1.0.7.jar;^
%WORKCRAFT_HOME%\ThirdParty\jedit.jar;^
%WORKCRAFT_HOME%\ThirdParty\jga-0.8-lgpl.jar;^
%WORKCRAFT_HOME%\ThirdParty\js.jar;^
%WORKCRAFT_HOME%\ThirdParty\junit-4.5.jar;^
%WORKCRAFT_HOME%\ThirdParty\log4j-1.2.8.jar;^
%WORKCRAFT_HOME%\ThirdParty\pcollections-1.0.0.jar;^
%WORKCRAFT_HOME%\ThirdParty\substance.jar;^
%WORKCRAFT_HOME%\ThirdParty\TableLayout-bin-jdk1.5-2009-08-26.jar;^
%WORKCRAFT_HOME%\WorkcraftCore\bin;^
%WORKCRAFT_HOME%\XmasPlugin\bin;^
;

:: Run Workcraft with the specific JAVA_BIN and CLASSPATH
"%JAVA_BIN%" -classpath "%CLASSPATH%" org.workcraft.Console

:: Restore the current working directory
popd

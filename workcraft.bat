@echo off

cd %~dp0

if not exist "WorkcraftCore/build/libs/WorkcraftCore.jar" (
    echo "WorkcraftCore.jar not found. Build Workcraft with './gradlew assemble' first."
    exit /b
)

setlocal enabledelayedexpansion

for /d %%d in (*) do (
    set libdir=%%d\build\libs
    for %%f in (!libdir!\*.jar) do set cp=!cp!;%%f
)

java -cp "%cp%" org.workcraft.Console %*

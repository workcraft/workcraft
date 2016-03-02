#!/usr/bin/env python

import glob, os, subprocess

def classPathString():
  pluginJars = glob.glob("*/build/libs/*.jar")
  return "\"" + os.pathsep.join(pluginJars) + "\""

if os.path.exists("WorkcraftCore/build/libs/WorkcraftCore.jar"):
  subprocess.call(["java", "-cp", classPathString(), "org.workcraft.Console"])
else:
  print ("run './gradlew assemble' first")

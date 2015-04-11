#!/usr/bin/env bash

unsupported_plugin_dirs="BalsaPluginBase BalsaWrapperGenerator DesiJPlugi"
model_plugin_dirs="
    CircuitPlugin 
    CpogsPlugin 
    DfsPlugin 
    FsmPlugin 
    FstPlugin 
    GraphPlugin 
    PetriNetPlugin 
    PolicyNetPlugin 
    SONPlugin 
    STGPlugin 
    XmasPlugin"
tool_plugin_dirs="MpsatPlugin PetrifyPlugin"
third_party_dirs="ThirdParty"
doc_dirs="overview help tutorial"
core_dirs="WorkcraftCore"
core_files="LICENSE README workcraft workcraft.bat"

src_dir="."
distr_dir="../../workcraft_3.0"
template_dir="../../distr-template"

description_msg="`basename $0`: creates a distribution for Workcraft"
usage_msg="Usage: `basename $0` [-s SRC_DIR] [-d DISTR_DIR] [-t TEMPLATE_DIR] [-h | --help]"
params_msg="
  -s SRC_DIR : source directory (default: $src_dir)\n
  -d DISTR_DIR : distribution directory (default: $distr_dir)\n
  -t TEMPLATE_DIRECTORY: template directory (default: $template_dir))\n
  -h, --help : print this help"
help_msg=$description_msg"\n\n"$usage_msg"\n\n"$params_msg

echo_command='echo -e'

# Process parameters
for param in $*
do
  case $param in
    -s) src_dir=$2; shift 2;;
    -d) distr_dir=$2; shift 2;;
    -t) template_dir=$2; shift 2;;
    -h | --help) $echo_command $help_msg; exit 0;
  esac
done

# Check source directory existance
if [[ ! -a $src_dir ]] || [[ ! -d $src_dir ]] 
then
    $echo_command "Error: Source directory not found: $src_dir"
    exit 1
fi

# Create distr directory
if [ -e $distr_dir ] 
then
    $echo_command "Error: Distribution directory already exists: $distr_dir"
    exit 1
else
    mkdir $distr_dir
fi

# Copy the template content
if [[ ! -a $template_dir ]] || [[ ! -d $template_dir ]] 
then
    $echo_command "Warning: Template directory not found: $template_dir"
else
    cp -r $template_dir/*  $distr_dir/
fi

# Copy core and plugin classes, third-party libraries, documentation and misc files
for i in $core_dirs $tool_plugin_dirs $model_plugin_dirs
do
    mkdir $distr_dir/$i
    cp -r $src_dir/$i/bin $distr_dir/$i/
done

for i in $third_party_dirs
do
    mkdir $distr_dir/$i
    cp -r $src_dir/$i/* $distr_dir/$i/
done

for i in $doc_dirs
do
    mkdir $distr_dir/$i
    cp -r $src_dir/$i/* $distr_dir/$i/
done

for i in $core_files
do
    cp -r $src_dir/$i $distr_dir/
done

#!/usr/bin/env bash

unsupported_plugin_dirs="BalsaPluginBase BalsaWrapperGenerator DesiJPlugi"
model_plugin_dirs="
    CircuitPlugin 
    CpogsPlugin 
    DfsPlugin 
    GatesPlugin 
    GraphPlugin 
    PetriNetPlugin 
    PolicyNetPlugin 
    SONPlugin 
    STGPlugin 
    XmasPlugin"
tool_plugin_dirs="MpsatPlugin PetrifyPlugin"
third_party_dirs="ThirdParty"
doc_dirs="help"
core_dirs="Workcraft Workflow"
core_files="LICENSE README workcraft.sh workcraft.bat"

src_dir="."
distr_dir="../workcraft2"
template_dir="../distr-template"

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
    -m) template_dir=$2; shift 2;;
    -h | --help) $echo_command $help_msg; exit 0;
  esac
done

# Check directory existance
if [[ ! -a $src_dir ]] || [[ ! -d $src_dir ]] 
then
    $echo_command "Error: Source directory not found: $src_dir"
    exit 1
fi

if [[ ! -a $template_dir ]] || [[ ! -d $template_dir ]] 
then
    $echo_command "Error: Template directory not found: $template_dir"
    exit 1
fi

if [ -e $distr_dir ] 
then
    $echo_command "Error: Distribution directory already exists: $distr_dir"
    exit 1
fi

# Create distr directory and copy the template content there
if [ ! -e $distr_dir ]
then
    mkdir $distr_dir
fi

if [ -e $template_dir ]
then
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

# Copy config
if [ -e $src_dir/config ]
then
    cp -r $src_dir/config $distr_dir/
fi

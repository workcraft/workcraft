#!/usr/bin/bash -e

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
tool_plugin_dirs="
    MpsatSynthesisPlugin
    MpsatVerificationPlugin
    PcompPlugin
    PunfPlugin
    PetrifyExtraPlugin
    PetrifyPlugin"
core_dirs="WorkcraftCore"
core_files="LICENSE README.md workcraft workcraft.bat"

distr_dir=dist
template_dir="../../distr-template"

description_msg="`basename $0`: creates a distribution for Workcraft"
usage_msg="Usage: `basename $0` [-d DISTR_DIR] [-t TEMPLATE_DIR] [-h | --help]"
params_msg="
  -d DISTR_DIR : distribution directory (default: $distr_dir)
  -t TEMPLATE_DIRECTORY: template directory (default: $template_dir))
  -h, --help : print this help"
help_msg="${description_msg}\n\n${usage_msg}\n${params_msg}\n"

err() {
    echo "Error: $@" >&2
    exit 1
}

# Process parameters
for param in $*
do
    case $param in
        -d) distr_dir=$2; shift 2;;
        -t) template_dir=$2; shift 2;;
        -h | --help) printf "$help_msg"; exit 0;
    esac
done

# Create distr directory
if [[ -e $distr_dir ]]
then
    err "Distribution directory already exists: $distr_dir"
fi
mkdir $distr_dir

# Copy the template content
if [[ ! -d $template_dir ]]; then
    err "Template directory not found: $template_dir"
fi
cp -r $template_dir/* $distr_dir/

# Copy core and plugin classes, third-party libraries, documentation and misc files
for i in $core_dirs $tool_plugin_dirs $model_plugin_dirs
do
    mkdir $distr_dir/$i
    cp -r $i/bin $distr_dir/$i/
done

for d in doc/*; do
    cp -r $d $distr_dir/
done

for i in $core_files
do
    cp -r $i $distr_dir/
done

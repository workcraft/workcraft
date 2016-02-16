#!/bin/sh -e

plugin_dirs="*Plugin/"
core_dir="WorkcraftCore"
core_files="LICENSE README.md workcraft workcraft.bat"

platform=""
bname="$(basename $0)"

usage() {
    cat <<EOF
$bname: create a distribution for Workcraft

Usage: $bname PLATFORM [-h]

  PLATFORM:   distribution platform (linux, windows)
  -h, --help: print this help
EOF
}

err() {
    echo "Error: $@" >&2
    exit 1
}

# Process parameters
for param in $*; do
    case $param in
        -h | --help)
            usage
            exit 0 ;;
        *)
            platform=$1
            shift ;;
    esac
done

if [ -z "$platform" ]; then
    err "No platform was specified"
fi

dist_dir="workcraft-$(git describe --tags)-$platform"
template_dir="dist-template/$platform"

if [ ! -e "$core_dir/build" ]; then
    err "You need to run 'gradle assemble' first"
fi

if [ ! -d "$template_dir" ]; then
    err "Template directory not found: $template_dir"
fi

if [ -e "$dist_dir" ]; then
    err "Distribution directory already exists: $dist_dir"
fi

mkdir -p $dist_dir

cp -r $template_dir/* $dist_dir/

cp $core_dir/build/libs/*.jar $dist_dir/workcraft.jar

mkdir -p $dist_dir/plugins

for d in $plugin_dirs; do
    cp $d/build/libs/*.jar $dist_dir/plugins/
done

for d in doc/*; do
    cp -r $d $dist_dir/
done

for f in $core_files; do
    cp $f $dist_dir/
done

case $platform in
    windows)
        rm -f $dist_dir/workcraft
        7z a -r ${dist_dir}.zip $dist_dir >/dev/null
        ;;
    linux)
        rm -f $dist_dir/workcraft.bat
        tar -czf ${dist_dir}.tar.gz $dist_dir
        ;;
esac

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

dist_dir="dist"
dist_name="workcraft-$(git describe --tags)-$platform"
dist_path="$dist_dir/$dist_name"
template_dir="dist-template/$platform"

if [ ! -e "$core_dir/build" ]; then
    err "You need to run 'gradle assemble' first"
fi

if [ ! -d "$template_dir" ]; then
    err "Template directory not found: $template_dir"
fi

if [ -e "$dist_path" ]; then
    err "Distribution directory already exists: $dist_path"
fi

mkdir -p $dist_path

cp -r $template_dir/* $dist_path/

cp $core_dir/build/libs/*.jar $dist_path/workcraft.jar

mkdir -p $dist_path/plugins

for d in $plugin_dirs; do
    cp $d/build/libs/*.jar $dist_path/plugins/
done

for d in doc/*; do
    cp -r $d $dist_path/
done

for f in $core_files; do
    cp $f $dist_path/
done

case $platform in
    windows)
        rm -f $dist_path/workcraft
        cd $dist_dir
        7z a -r ${dist_name}.zip $dist_name >/dev/null
        ;;
    linux)
        rm -f $dist_path/workcraft.bat
        cd $dist_dir
        tar -czf ${dist_name}.tar.gz $dist_name
        ;;
esac

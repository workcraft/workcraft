#!/bin/bash -e

DIR="dist"
DOC_DIR="doc"
TEMPLATE_DIR="template"
RESULT_DIR="result"
PLUGINS_DIR="workcraft"
PLATFORMS="windows linux osx"

usage() {
    script_name="$(basename $0)"
    cat <<EOF
$script_name: create a distribution for Workcraft as workcraft-TAG-PLATFORM archive

Usage: $script_name [PLATFORMS] [-p DIR] [-t TAG] [-f] [-h]

  PLATFORMS          distribution platforms [$PLATFORMS] (all by default)
  -p, --plugins DIR  additional plugins directory (only '$PLUGINS_DIR' by default)
  -t, --tag TAG      user-defined tag (git tag is used by default)
  -f, --force        force removal of output dir
  -h, --help         print this help
EOF
}

err() {
    echo "Error: $@" >&2
    exit 1
}

copy_jars() {
    bin_from_path="$1/build/bin"
    bin_to_path="$2/bin"
    if [ -e "$bin_from_path" ]; then
        echo "  - adding $plugin_path"
        mkdir -p $bin_to_path
        cp -i $bin_from_path/*.jar $bin_to_path
        # Third-party libraries
        lib_from_path="$1/build/lib"
        lib_to_path="$2/lib"
        if [ -e "$lib_from_path" ]; then
            mkdir -p "$lib_to_path"
            cp -f $lib_from_path/*.jar $lib_to_path
        fi
    fi
}

# Defaults
platforms=""
plugins="$PLUGINS_DIR"
tag="$(git describe --tags)"
force=false

# Process parameters
while [ "$#" -gt 0 ]; do
echo "$@ : param=$1"
    case "$1" in
        -h | --help)
            usage
            exit 0
            ;;
        -p | --plugins)
            plugins="$plugins $2"
            shift 2
            ;;
        -t | --tag)
            tag="$2"
            shift 2
            ;;
        -f | --force)
            force=true
            shift
            ;;
        *)
            platforms="$platforms $1"
            shift
            ;;
    esac
done

# If no platforms are specified, then use all platforms
if [ -z "$platforms" ]; then
    platforms="$PLATFORMS"
fi

# Change to Workcraft root directory
cd "$(dirname "$0")/.."

for platform in $platforms; do

    dist_name="workcraft-${tag}-${platform}"
    echo "Building ${dist_name}..."

    template_path="$DIR/$TEMPLATE_DIR/$platform"
    if [ ! -d "$template_path" ]; then
        err "Template directory not found: $template_path"
    fi

    if [ "$platform" = "osx" ]; then
        dist_rootdir="Workcraft.app"
    else
        dist_rootdir="workcraft"
    fi
    platform_path="$DIR/$RESULT_DIR/$platform"
    dist_path="$platform_path/$dist_rootdir"
    if [ -e "$dist_path" ]; then
        if $force; then
            rm -rf $dist_path
        else
            err "Distribution directory already exists: $dist_path"
        fi
    fi

    mkdir -p $dist_path
    cp -r $template_path/* $dist_path/

    # Set Resources as the distribution path on OS X
    if [ "$platform" = "osx" ]; then
        # Update Info.plist with version tag (OS X `sed -i` requires backup extension, e.g. `sed -i.bak`)
        sed -i.bak "s/__VERSION__/$tag/" ${dist_path}/Contents/Info.plist
        rm -f ${dist_path}/Contents/Info.plist.bak

        dist_path=$dist_path/Contents/Resources
    fi

    for plugin in $plugins; do
        for plugin_path in $plugin/*; do
            if [ -d "$plugin_path" ]; then
                copy_jars "$plugin_path" "$dist_path"
            fi
        done
    done

    doc_path="$DIR/$DOC_DIR"
    for d in $doc_path/*; do
        if [ "$d" != "$doc_path/README.md" ]; then
            cp -r $d $dist_path/
        fi
    done

    cd $platform_path

    case $platform in
        windows)
            7z a -r ${dist_name}.zip $dist_rootdir >/dev/null
            ;;
        linux | osx)
            tar -czf ${dist_name}.tar.gz $dist_rootdir
            ;;
    esac

    cd ../../..
done

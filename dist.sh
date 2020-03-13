#!/bin/bash -e

plugin_dirs="*Plugin/"
core_dir="WorkcraftCore/"
dist_dir="dist"

bname="$(basename $0)"
allplatforms="windows linux osx"


usage() {
    cat <<EOF
$bname: create a distribution for Workcraft as workcraft-tag-platform archive

Usage: $bname [platforms] [-t TAG] [-h]

  platforms:     distribution platforms to build
                 $allplatforms all (all by default)
  -t, --tag TAG: user-defined tag (git tag is used by default)
  -p, --private: include private plugins
  -f, --force:   force removal of output dir
  -h, --help:    print this help
EOF
}

err() {
    echo "Error: $@" >&2
    exit 1
}

copy_jars() {
    bin_from_path="$1/build/libs/"
    bin_to_path="$2/bin/"
    if [ -e "$bin_from_path" ]; then
        mkdir -p $bin_to_path
        cp -f $bin_from_path/*.jar $bin_to_path
    fi
    lib_from_path="$1/build/lib"
    lib_to_path="$2/lib"
    if [ -e "$lib_from_path" ]; then
        mkdir -p $lib_to_path
        cp -f $lib_from_path/*.jar $lib_to_path
    fi
}

# Defaults
platforms="all"
tag="$(git describe --tags)"
private=false
force=false

# Process parameters
for param in "$@"; do
    case "$param" in
        -h | --help)
            usage
            exit 0 ;;
        -t | --tag)
            tag="$2"
            shift 2 ;;
        -p | --private)
            private=true
            shift ;;
        -f | --force)
            force=true
            shift ;;
    esac
done

if [ ! -e "$core_dir/build" ]; then
    err "You need to run './gradlew assemble' first"
fi

if [ -z "$@" ] || [ "$@" = "all" ]; then
    platforms="$allplatforms"
else
    platforms="$@"
fi

for platform in $platforms; do

    if [ "$platform" = "osx" ]; then
        dist_rootdir="Workcraft.app"
    else
        dist_rootdir="workcraft"
    fi

    dist_name="workcraft-${tag}-${platform}"
    dist_path="$dist_dir/$platform/$dist_rootdir"
    template_dir="dist-template/$platform"

    echo "Building ${dist_name}..."

    if [ ! -d "$template_dir" ]; then
        err "Template directory not found: $template_dir"
    fi

    if [ -e "$dist_path" ]; then
        if $force; then
            rm -rf $dist_path
        else
            err "Distribution directory already exists: $dist_path"
        fi
    fi

    mkdir -p $dist_path
    cp -r $template_dir/* $dist_path/

    # Set Resources as the distribution path on OS X
    if [ "$platform" = "osx" ]; then
        # Update Info.plist with version tag (OS X `sed -i` requires backup extension, e.g. `sed -i.bak`)
        sed -i.bak "s/__VERSION__/$tag/" ${dist_path}/Contents/Info.plist
        rm -f ${dist_path}/Contents/Info.plist.bak

        dist_path=$dist_path/Contents/Resources
    fi

    echo "  - adding $core_dir"
    copy_jars "$core_dir" "$dist_path"

    for plugin_dir in $plugin_dirs; do
        # Checking if private plugins should be inkluded (their name strart with underscore)
        case "$plugin_dir" in
            _*)
                if $private; then
                    echo "  - adding $plugin_dir"
                    copy_jars "$plugin_dir" "$dist_path"
                else
                    echo "  - skipping $plugin_dir"
                fi
                ;;
            *)
                echo "  - adding $plugin_dir"
                copy_jars "$plugin_dir" "$dist_path"
                ;;
        esac
    done

    for d in doc/*; do
        if [ "$d" != "doc/README.md" ]; then
            cp -r $d $dist_path/
        fi
    done

    cd $dist_dir/$platform

    case $platform in
        windows)
            7z a -r ${dist_name}.zip $dist_rootdir >/dev/null
            ;;
        linux | osx)
            tar -czf ${dist_name}.tar.gz $dist_rootdir
            ;;
    esac

    cd ../..
done

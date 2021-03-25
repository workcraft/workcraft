#!/bin/bash -e

DIR="dist"
PLUGINS="workcraft"
RESULT="result"
TEMPLATE="template"
PLATFORM_COMMON="common"
PLATFORM_LINUX="linux"
PLATFORM_OSX="osx"
PLATFORM_WINDOWS="windows"
SCRIPT_INTRO="intro.sh"
SCRIPT_OUTRO="outro.sh"
PLATFORMS="$PLATFORM_LINUX $PLATFORM_OSX $PLATFORM_WINDOWS"

usage() {
    script_name="$(basename $0)"
    cat <<EOF
$script_name: create a distribution for Workcraft as workcraft-TAG-PLATFORM archive

Usage: $script_name [PLATFORMS] [-e SUFFIX] [-t TAG] [-f] [-h]

  PLATFORMS           distribution platforms [$PLATFORMS] (all by default)
  -e, --extra SUFFIX  suffix for extra plugin and template directory (none by default)
  -t, --tag TAG       user-defined tag (git tag is used by default)
  -f, --force         replace previously built distribution
  -p, --pack          create archive file
  -h, --help          print this help
EOF
}

###
# Print error MESSAGE and terminate with exit code 1
# Usage: err MESSAGE
err() {
    echo "Error: $@" >&2
    exit 1
}

###
# Source script file FILE_PATH if it exists or skip without error otherwise
# Usage: source_if_present FILE_PATH
source_if_present() {
    if [ -e "$1" ]; then
        echo "    + $1"
        source $1
    fi
}

###
# Copy content of FROM_PATH (if exists) into TO_PATH (create if necessary)
# Usage: copy_content FROM_PATH TO_PATH
copy_content() {
    if [ -d "$1" ]; then
        echo "    + $1"
        mkdir -p $2
        cp -r $1/* $2/
    fi
}

###
# Copy JAR files from FROM_PATH/build/bin/ (if exists) into TO_PATH/bin/ (create if necessary)
# Usage: copy_jars FROM_PATH TO_PATH
copy_jars() {
    bin_from_path="$1/build/bin"
    bin_to_path="$2/bin"
    if [ -e "$bin_from_path" ]; then
        echo "    + $plugin_path"
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

###
# Adjust file permissions under PATH (recursively)
# Usage: adjust_permissions PATH
adjust_permissions() {
    path="$1"
    if [ -e "$path" ]; then
        if [ -d "$path" ]; then
            chmod 0755 $path
            for child in $path/*; do
                adjust_permissions "$child"
            done
        else
            if [ -x "$path" ]; then
                chmod 0755 $path
            else
                chmod 0644 $path
            fi
        fi
    fi
}

# Defaults
platforms=""
flavor=""
plugins="$PLUGINS"
templates="$TEMPLATE"
tag="$(git describe --tags --always)"
force=false
pack=false

# Process parameters
while [ "$#" -gt 0 ]; do
    case "$1" in
        -h | --help)
            usage
            exit 0
            ;;
        -e | --extra)
            flavor="$flavor-$2"
            plugins="$plugins $PLUGINS-$2"
            templates="$templates $TEMPLATE-$2"
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
        -p | --pack)
            pack=true
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
    dist_name="workcraft-${tag}${flavor}-${platform}"
    echo "Building Workcraft distribution $dist_name"

    # Prepare distribution directory for the current platform
    platform_path="$DIR/$RESULT/$platform"
    if [ -e "$platform_path" ]; then
        if $force; then
            rm -rf $platform_path
        else
            err "Distribution directory already exists: $platform_path"
        fi
    fi

    echo "  * Executing intro scripts..."
    workcraft="workcraft"
    common=""
    for template in $templates; do
        source_if_present "$DIR/$template/$PLATFORM_COMMON-$SCRIPT_INTRO"
        source_if_present "$DIR/$template/$platform-$SCRIPT_INTRO"
    done

    echo "  * Copying templates..."
    for template in $templates; do
        copy_content "$DIR/$template/$PLATFORM_COMMON" "$platform_path/$workcraft/$common"
        copy_content "$DIR/$template/$platform" "$platform_path/$workcraft"
    done

    echo "  * Copying plugins..."
    for plugin in $plugins; do
        for plugin_path in $plugin/*; do
            if [ -d "$plugin_path" ]; then
                copy_jars "$plugin_path" "$platform_path/$workcraft/$common"
            fi
        done
    done

    echo "  * Executing outro scripts..."
    for template in $templates; do
        source_if_present "$DIR/$template/$PLATFORM_COMMON-$SCRIPT_OUTRO"
        source_if_present "$DIR/$template/$platform-$SCRIPT_OUTRO"
    done

    echo "  * Adjusting file attributes..."
    adjust_permissions "$platform_path"

    if $pack; then
        echo "  * Creating distribution archive..."
        cd $platform_path
        case $platform in
            $PLATFORM_WINDOWS)
                7z a -r ${dist_name}.zip $workcraft >/dev/null
                ;;
            $PLATFORM_LINUX | $PLATFORM_OSX)
                tar -czf ${dist_name}.tar.gz $workcraft
                ;;
        esac
        cd ../../..
    fi

done

# Creating Workcraft distribution

This directory contains distribution templates and a build script to
create distribution archives for Linux, OSX and Windows platforms.

The default distribution template is in `dist/template/` directory, pointing to
[workcraft-dist-template](https://github.com/workcraft/workcraft-dist-template)
submodule. Additional templates can be put in `dist/template-*/`
directories -- these are excluded from Git tracking and also need
to be explicitly added as a parameter to the distribution script.

Each template directory may have the following subdirectories:

  * `common/` -- distribution artefacts for all platforms
  * `linux/` -- content specific for Linux
  * `osx/` -- content specific for OSX
  * `windows/` -- content specific for Windows

A template directory may also contain a set of scripts to be executed before
(`*-intro.sh`) and after (`*-outro.sh`) each distribution build:

  * `common-intro.sh`, `common-outro.sh` -- for all platforms
  * `linux-intro.sh`, `linux-outro.sh`  -- only for Linux
  * `osx-intro.sh`, `osx-outro.sh` -- only for OSX
  * `windows-intro.sh`, `windows-outro.sh` -- only for Windows

Distribution archives can be automatically generated with `dist/run.sh`
script as follows:

  * `dist/run.sh` -- create Workcraft distribution for all platforms
using the default template from `dist/template/` and the standard plugins
from `workcraft` directory
  * `dist/run.sh linux --force --extra extra` -- build Linux distribution
with all the default plugins and templates, and also include additional
plugins from `workcraft-extra/` and backend tools from `dist/template-extra/`

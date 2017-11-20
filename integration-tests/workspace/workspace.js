var files =
    [ "vme-tm.circuit.work"
    , "vme.stg.work"
    ];

// Load work files
for (var i = 0; i < files.length; i++) {
    var f = files[i];
    load(f);
}

// Iterate over loaded works and get their types
s = "";
for each (work in getWorks()) {
    s += work.getTitle() + "\n";
    s += "  * Descriptor: " + getModelDescriptor(work) + "\n";
    s += "  * Title: " + getModelTitle(work) + "\n";
    s += "  * File: " + getWorkFile(work) + "\n";
    s += "\n";
}

write(s, "workspace.txt");

exit();

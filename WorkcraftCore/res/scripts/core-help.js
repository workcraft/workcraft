// Help functions
function getHelp(substring, searchDescription) {
    return framework.getJavaScriptHelp(".*" + substring + ".*", searchDescription);
}

function help(substring, fileName) {
    if (arguments.length < 2) {
        write(getHelp(substring, false));
    } else {
        write(getHelp(substring, false), fileName);
    }
}

function apropos(substring, fileName) {
    if (arguments.length < 2) {
        write(getHelp(substring, true));
    } else {
        write(getHelp(substring, true), fileName);
    }
}

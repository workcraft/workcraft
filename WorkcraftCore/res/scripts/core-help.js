// Help functions

framework.addJavaScriptHelp("getHelp", "return a string with all helper functions \
whose name contains the 'substring'; if the 'searchDescription' is true, then also look \
for the 'substring' in the function description");

function getHelp(substring, searchDescription) {
    return framework.getJavaScriptHelp(".*(?i:" + substring + ").*", searchDescription);
}


framework.addJavaScriptHelp("help", "output all the helper functions whose name contains the given 'substring'");

function help(substring, fileName) {
    if (arguments.length < 1) {
        write(getHelp("", false));
    } else if (arguments.length < 2) {
        write(getHelp(substring, false));
    } else {
        write(getHelp(substring, false), fileName);
    }
}


framework.addJavaScriptHelp("apropos", "output all the helper functions whose name or description contains the given 'substring'");

function apropos(substring, fileName) {
    if (arguments.length < 2) {
        write(getHelp(substring, true));
    } else {
        write(getHelp(substring, true), fileName);
    }
}

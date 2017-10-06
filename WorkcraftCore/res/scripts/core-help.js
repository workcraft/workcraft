// Help functions

/*
 * Return a string with all helper functions whose name contains the 'substring';
 * if the 'searchDescription' is true, then also search the function description");
 */
function getHelp(substring, searchDescription) {
    return framework.getJavaScriptHelp(".*(?i:" + substring + ").*", searchDescription);
}


framework.addJavaScriptHelp("help", "substring, fileName",
    "output all the helper functions whose name contains the given 'substring'");

function help(substring, fileName) {
    if (arguments.length > 1) {
        write(getHelp(substring, false), fileName);
    } else if (arguments.length > 0) {
        write(getHelp(substring, false));
    } else {
        write(getHelp("", false));
    }
}


framework.addJavaScriptHelp("apropos", "substring, fileName",
    "output all the helper functions whose name or description contains the given 'substring'");

function apropos(substring, fileName) {
    if (arguments.length > 1) {
        write(getHelp(substring, true), fileName);
    } else if (arguments.length > 0) {
        write(getHelp(substring, true));
    } else {
        write(getHelp("", true));
    }
}


framework.addJavaScriptHelp("helpRegex", "regex, fileName",
    "output all helper functions whose name matches 'regex'");

function helpRegex(regex, fileName) {
    if (arguments.length > 1) {
        write(framework.getJavaScriptHelp(regex, false), fileName);
    } else if (arguments.length > 0) {
        write(framework.getJavaScriptHelp(regex, false));
    }
}

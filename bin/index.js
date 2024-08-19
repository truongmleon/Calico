#!/usr/bin/env node
"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var commander_1 = require("commander");
var fs = require("node:fs");
var path = require("node:path");
var indent = "\n                ";
var program = new commander_1.Command();
var projectPath = path.resolve(__dirname, "..");
String.prototype.replaceAt = function (index, replacement, extra) {
    return this.substring(0, index) + replacement + this.substring(index + extra + 1);
};
/**
 * Creates CSS files if wanted.
 *
 * @param {string} name - File extension to be used (either CSS or SCSS).
 */
var makeCSS = function (name) {
    // Create styles folder if it doesn't exist.
    if (!fs.existsSync("".concat(projectPath, "/assets/styles")))
        fs.mkdirSync("".concat(projectPath, "/assets/styles"));
    // Creates styles file if it doesn't exist.
    if (!fs.existsSync("".concat(projectPath, "/assets/styles/styles.").concat(name))) {
        fs.open("".concat(projectPath, "/assets/styles/styles.").concat(name), "w", function (err, file) {
            if (err)
                throw err;
            console.log("".concat(name.toUpperCase(), " is created."));
            fs.close(file, function (err) { if (err)
                throw err; });
        });
    }
    else {
        console.log("".concat(name.toUpperCase(), " already exists."));
    }
};
/**
 * Use inputted titles will have dashes and we will need to remove them with this function.
 *
 * @param {string} title - Makes the first letter of each word uppercase and removes '-'.
 * @returns {string} - The formatted title.
 */
var formatTitle = function (title) {
    title = title.charAt(0).toUpperCase() + title.slice(1);
    var index = title.indexOf("-");
    while (title.indexOf("-") != -1) {
        title = title.slice(0, index) + " " + title.charAt(index + 1).toUpperCase() + title.slice(index + 2);
        index = title.indexOf("-", index + 1);
    }
    return title;
};
/**
 * Given a day of the month, append the suffix.
 *
 * @param {number} day - Day of the month.
 * @returns {string} - Day with the suffix.
 */
var getDaySuffix = function (day) {
    var single = day % 10;
    if (single === 1)
        return "1st";
    if (single === 2)
        return "2nd";
    if (single === 3)
        return "3rd";
    return day + "th";
};
/**
 * Repeats given tag.
 *
 * @param {string} tag - Tag to be repeated.
 * @param {number} level - The number of times to repeat.
 * @returns {string[]} - The string of repeated tags and whitespace.
 */
var repeatTag = function (tag, level) {
    var body = "";
    var whitespace = "";
    for (var i = 0; i < level - 1; i++) {
        whitespace += "    ";
    }
    for (var i = 0; i < level; i++) {
        body += "".concat(whitespace, "   ").concat(tag).concat(indent);
    }
    return [body, whitespace];
};
/**
 * Gets a header tag.
 *
 * @param {string} text - Text inside of header.
 * @returns {string} - Header tag HTML string.
 */
var getHeaderTag = function (text) {
    var body = "";
    var count = 2;
    text = checkEmphasis(text);
    //h2 to h6 tags, hence j < 7
    for (var j = 2; j < 7; j++) {
        if (text[j] === "#")
            count++;
        else
            break;
    }
    if (text[count] === " ") {
        body += "<h".concat(count, ">").concat(text.substring(count + 1), "</h").concat(count, ">").concat(indent);
    }
    else {
        body += "<p>".concat(text, "</p>").concat(indent);
    }
    return body;
};
/**
 * Gets all UL tags.
 *
 * @param {string[]} lines - String of lines in the markdown file.
 * @param {number} i - Index of current line.
 * @returns {any[]} - Tags in index 0, new current line in index 1.
 */
var getULListTags = function (lines, i) {
    var body = "<ul>".concat(indent);
    while (lines[++i].indexOf("- ") != -1) {
        var index = lines[i].indexOf("- ");
        var level = index / 2;
        var repeats = repeatTag("<ul>", level);
        body += repeats[0];
        body += "".concat(repeats[1], "    <li>").concat(lines[i].substring(index + 2), "</li>").concat(indent);
        body += repeatTag("</ul>", level)[0];
    }
    body += "</ul>".concat(indent);
    body = checkEmphasis(body);
    return [body, i];
};
/**
 * Gets all OL tags.
 *
 * @param {string[]} lines - String of lines in the markdown file.
 * @param {number} i - Index of current line.
 * @returns {any[]} - Tags in index 0, new current line in index 1.
 */
var getOLListTags = function (lines, i) {
    var body = "<ol>".concat(indent);
    var count = 1;
    while (lines[++i].indexOf("".concat(count, ". ")) === 0) {
        body += "   <li>".concat(lines[i].substring(3), "</li>").concat(indent);
        count++;
    }
    body += "</ol>".concat(indent);
    body = checkEmphasis(body);
    return [body, i];
};
/**
 * Gets all Code tags.
 *
 * @param {string[]} lines - String of lines in the markdown file.
 * @param {number} i - Index of current line.
 * @returns {any[]} - Tags in index 0, new current line in index 1.
 */
var getCodeTags = function (lines, i) {
    var language = lines[i].substring(lines[i].indexOf("```") + 3);
    var lineCount = 0;
    var codeBody = "<code data-lang=\"".concat(language, "\">").concat(indent);
    while (lines[++i] !== "```") {
        codeBody += "   " + lines[i] + indent;
        lineCount++;
    }
    codeBody += "</code>";
    codeBody = checkEmphasis(codeBody);
    if (lineCount > 1)
        return ["<pre>".concat(codeBody, "</pre>").concat(indent), i];
    return [codeBody + indent, i];
};
/**
 * Gets plain HTML code written in markdown.
 *
 * @param {string[]} lines - String of lines in the markdown file.
 * @param {number} i - Index of current line.
 * @returns {any[]} - Tags in index 0, new current line in index 1.
 */
var getPlainHTML = function (lines, i) {
    var body = "";
    while (lines[++i].indexOf("</") !== 0 && lines[i].indexOf(">") !== lines[i].length - 1) {
        body += "   " + lines[i] + indent;
    }
    body += lines[i] + indent;
    return [body, i];
};
/**
 * Adds special tags in the middle of strings such as <strong></strong> and <em></em>.
 *
 * @param {string} text - Text to add possible special tags.
 * @param {string} check - Indicator of markdown to HTML (e.g. * for italics).
 * @param {string} tag - Tag used.
 * @returns {string} - HTML content.
 */
var addSpecialTag = function (text, check, tag) {
    if (text.indexOf(check) === -1)
        return text;
    var checkLength = check.length;
    var tagLength = tag.length + checkLength % 2;
    var extra = checkLength - 1; // For extra * in bold text.
    var indexOfFirst = text.indexOf(check);
    var indexOfLast = text.indexOf(check, indexOfFirst + checkLength);
    while (indexOfLast !== -1) {
        text = text.replaceAt(indexOfFirst, "<".concat(tag, ">"), extra);
        text = text.replaceAt(indexOfLast + tagLength, "</".concat(tag, ">"), extra);
        indexOfFirst = text.indexOf(check, indexOfLast + checkLength);
        if (indexOfFirst == -1)
            break;
        indexOfLast = text.indexOf(check, indexOfFirst + checkLength);
    }
    return text;
};
/**
 * Runs through the special tags to be added in the text.
 *
 * @param {string} text - Text to add possible special tags.
 * @returns {string} - Either text or text with special tags.
 */
var checkEmphasis = function (text) {
    text = addSpecialTag(text, "**", "strong");
    text = addSpecialTag(text, "*", "em");
    text = addSpecialTag(text, "~~", "del");
    text = addSpecialTag(text, "~", "sup");
    text = addSpecialTag(text, "^", "sub");
    return text;
};
// Description
program
    .name("calico")
    .description("An easy to use static site generator.")
    .version("0.0.1");
// start | go command
program
    .command("start")
    .alias("go")
    .description("Calico set-up files.")
    .option("--css", "add css in assets folder")
    .option("--scss", "add scss in assets folder")
    .action(function (options) {
    if (!fs.existsSync("".concat(projectPath, "/assets"))) {
        fs.mkdirSync("".concat(projectPath, "/assets"));
        console.log("Assets folder created.");
    }
    if (!fs.existsSync("".concat(projectPath, "/content"))) {
        fs.mkdirSync("".concat(projectPath, "/content"));
        fs.mkdirSync("".concat(projectPath, "/content/markdown"));
        fs.mkdirSync("".concat(projectPath, "/content/html"));
        console.log("Content folder created.");
    }
    if (!fs.existsSync("".concat(projectPath, "/calico.toml"))) {
        var content = "languageCode = \"en-us\"\ntitle = \"My New Calico Site\"";
        fs.writeFile("".concat(projectPath, "/calico.toml"), content, function (err) {
            if (err)
                throw err;
            console.log("calico.toml is created.");
        });
    }
    if (options.css)
        makeCSS("css");
    if (options.scss)
        makeCSS("scss");
});
// make command
program
    .command("make")
    .description("Create markdown file for a post.")
    .argument("<string>", "filename")
    .option("-t, --title <string>", "title of markdown")
    .action(function (filename, options) {
    var title = options.title ? options.title : filename;
    if (options.t !== "title of markdown" && title == filename) {
        title = options.t ? options.t : filename;
    }
    title = formatTitle(title);
    /* FORMAT:
    +++
    title = "Hi There"
    date = 29/6/2024
    +++
    */
    var date = new Date();
    var year = date.getFullYear();
    var month = date.toLocaleString("default", { month: "long" });
    var day = getDaySuffix(date.getDate());
    var content = "+++\ntitle = \"".concat(title, "\"\ndate = ").concat(month, " ").concat(day, ", ").concat(year, "\n+++");
    if (!fs.existsSync("".concat(projectPath, "/content/markdown/").concat(filename, ".md"))) {
        fs.writeFile("".concat(projectPath, "/content/markdown/").concat(filename, ".md"), content, function (err) {
            if (err)
                throw err;
            console.log("".concat(filename, ".md is created."));
        });
    }
    else {
        console.log("".concat(filename, ".md is already created."));
    }
});
// make-html command
program
    .command("make-html")
    .description("Calico turning all markdown files to HTML.")
    .action(function () {
    // Opens markdown folder to convert all markdown files
    var dir = fs.opendirSync("".concat(projectPath, "/content/markdown"));
    var current = undefined;
    // While there's still markdown files to convert
    while ((current = dir.readSync()) !== null) {
        var lines = fs.readFileSync("".concat(projectPath, "/content/markdown/").concat(current.name), "utf8").split("\n");
        var currentFootnote = 1;
        var body = "";
        var footnotes = "";
        var title = lines[1].substring(9, lines[1].length - 1);
        var date = lines[2].substring(7, lines[2].length);
        body += "<h1>".concat(checkEmphasis(title), "</h1>").concat(indent);
        if (date !== "")
            body += "<time>".concat(checkEmphasis(date), "</time>").concat(indent);
        for (var i = 4; i < lines.length; i++) {
            var currentLine = lines[i];
            if (currentLine === "" || currentLine.indexOf("<!--") == 0 && currentLine.indexOf("-->") != -1)
                continue;
            if (currentLine.indexOf("##") === 0) {
                body += getHeaderTag(currentLine);
            }
            else if (currentLine.indexOf("> ") === 0) {
                body += "<blockquote>".concat(checkEmphasis(currentLine.substring(2)), "</blockquote>").concat(indent);
            }
            else if (currentLine.indexOf("- ") === 0) {
                if (currentLine.indexOf("[ ]") === 2) {
                    body += "<p>".concat(indent, "   <input type=\"checkbox\">    ").concat(indent, "    ").concat(checkEmphasis(currentLine.substring(6))).concat(indent, "</p>").concat(indent);
                    continue;
                }
                else if (currentLine.toLowerCase().indexOf("[x]") == 2) {
                    body += "<p>".concat(indent, "   <input type=\"checkbox\" checked>    ").concat(indent, "    ").concat(checkEmphasis(currentLine.substring(6))).concat(indent, "</p>").concat(indent);
                    continue;
                }
                var listTags = getULListTags(lines, --i);
                body += listTags[0];
                i = listTags[1];
            }
            else if (currentLine.indexOf("1. ") === 0) {
                var listTags = getOLListTags(lines, --i);
                body += listTags[0];
                i = listTags[1];
            }
            else if (currentLine.indexOf("```") === 0) {
                var codeTags = getCodeTags(lines, i);
                body += codeTags[0];
                i = codeTags[1];
            }
            else if (currentLine.indexOf("<") === 0 && currentLine.indexOf(">") === currentLine.length - 1) {
                var plainHTML = getPlainHTML(lines, i);
                body += currentLine + indent;
                body += plainHTML[0];
                i = plainHTML[1];
            }
            else if (currentLine.indexOf("<") === 0 && currentLine.indexOf("/>") === currentLine.length - 2) {
                body += currentLine + indent;
            }
            else if (currentLine.indexOf("[^".concat(currentFootnote, "]")) === 0) {
                currentLine = currentLine.replaceAt(0, "".concat(currentFootnote, "."), 4);
                footnotes += "<p id=\"#fn".concat(currentFootnote, "\">").concat(currentLine, " <a href=\"fnote").concat(currentFootnote, "\">&#8617</a></p>").concat(indent);
                currentFootnote++;
            }
            else if (currentLine.indexOf("[^".concat(currentFootnote, "]")) !== -1) {
                var note = "<sup><a id=\"fnote".concat(currentFootnote, "\" href=\"#fn").concat(currentFootnote, "\">[").concat(currentFootnote, "]</a></sup>");
                currentLine = currentLine.replaceAt(currentLine.indexOf("[^".concat(currentFootnote, "]")), note, 3);
                body += "<p>".concat(currentLine, "</p>").concat(indent);
            }
            else if (currentLine.indexOf("(") !== -1 && currentLine.indexOf(")") !== -1 && currentLine.indexOf("[") !== -1 && currentLine.indexOf("]") !== -1) {
            }
            else {
                currentLine = checkEmphasis(currentLine);
                body += "<p>".concat(checkEmphasis(currentLine), "</p>").concat(indent);
            }
        }
        var content = " \n            <!DOCTYPE html>\n            <html lang=\"en\">\n            <head>\n                <meta charset=\"UTF-8\">\n                <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n                <title>".concat(title, "</title>\n            </head>\n            <body>\n                ").concat(body + footnotes, "\n            </body>\n            </html>");
        console.log(content);
    }
});
program.parse();

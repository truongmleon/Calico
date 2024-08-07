#!/usr/bin/env node
"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var commander_1 = require("commander");
var fs = require("node:fs");
var path = require("node:path");
var indent = "\n                ";
var program = new commander_1.Command();
var projectPath = path.resolve(__dirname, "..");
String.prototype.replaceAt = function (index, replacement) {
    return this.substring(0, index) + replacement + this.substring(index + 1);
};
var makeCSS = function (name) {
    if (!fs.existsSync("".concat(projectPath, "/assets/styles"))) {
        fs.mkdirSync("".concat(projectPath, "/assets/styles"));
    }
    if (!fs.existsSync("".concat(projectPath, "/assets/styles/styles.").concat(name))) {
        fs.open("".concat(projectPath, "/assets/styles/styles.").concat(name), "w", function (err, file) {
            if (err)
                throw err;
            console.log("".concat(name.toUpperCase(), " is created."));
            fs.close(file, function (err) {
                if (err)
                    throw err;
            });
        });
    }
    else {
        console.log("".concat(name.toUpperCase(), " already exists."));
    }
};
var formatTitle = function (title) {
    title = title.charAt(0).toUpperCase() + title.slice(1);
    var index = title.indexOf("-");
    while (title.indexOf("-") != -1) {
        title = title.slice(0, index) + " " + title.charAt(index + 1).toUpperCase() + title.slice(index + 2);
        index = title.indexOf("-", index + 1);
    }
    return title;
};
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
var getheaderTag = function (text) {
    var body = "";
    var count = 2;
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
    return [body, i];
};
var getOLListTags = function (lines, i) {
    var body = "<ol>".concat(indent);
    var count = 1;
    while (lines[++i].indexOf("".concat(count, ". ")) === 0) {
        body += "<li>".concat(lines[i].substring(3), "</li>").concat(indent);
        count++;
    }
    body += "</ol>".concat(indent);
    return [body, i];
};
var getCodeTags = function (lines, i) {
    var language = lines[i].substring(lines[i].indexOf("```") + 3);
    var lineCount = 0;
    var codeBody = "<code data-lang=\"".concat(language, "\">").concat(indent);
    while (lines[++i] !== "```") {
        codeBody += "   " + lines[i] + indent;
        lineCount++;
    }
    codeBody += "</code>";
    if (lineCount > 1)
        return ["<pre>".concat(codeBody, "</pre>").concat(indent), i];
    return [codeBody + indent, i];
};
var addEMTag = function (text) {
    if (text.indexOf("*") === -1)
        return text;
    var indexOfFirstItalic = text.indexOf("*");
    var indexOfLastItalic = text.indexOf("*", indexOfFirstItalic + 1);
    while (indexOfLastItalic !== -1) {
        text = text.replaceAt(indexOfFirstItalic, "<em>");
        text = text.replaceAt(indexOfLastItalic + 3, "</em>");
        indexOfFirstItalic = text.indexOf("*", indexOfLastItalic + 1);
        if (indexOfFirstItalic == -1)
            break;
        indexOfLastItalic = text.indexOf("*", indexOfFirstItalic + 1);
    }
    return text;
};
//Description
program
    .name("calico")
    .description("An easy to use static site generator.")
    .version("0.0.1");
//start | go command
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
//make command
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
//make-html command
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
        var body = "";
        var title = lines[1].substring(9, lines[1].length - 1);
        var date = lines[2].substring(7, lines[2].length);
        body += "<h1>".concat(title, "</h1>").concat(indent);
        body += "<time>".concat(date, "</time>").concat(indent);
        for (var i = 4; i < lines.length; i++) {
            if (lines[i].indexOf("<!--") == 0 && lines[i].indexOf("-->") != -1)
                continue;
            if (lines[i].indexOf("##") === 0) {
                body += getheaderTag(lines[i]);
            }
            else if (lines[i].indexOf("> ") === 0) {
                body += "<blockquote>".concat(lines[i].substring(2), "</blockquote>").concat(indent);
            }
            else if (lines[i].indexOf("- ") === 0) {
                if (lines[i].indexOf("[ ]") === 2) {
                    body += "<p>".concat(indent, "   <input type=\"checkbox\">    ").concat(indent, "    ").concat(lines[i].substring(6)).concat(indent, "</p>").concat(indent);
                    continue;
                }
                else if (lines[i].toLowerCase().indexOf("[x]") == 2) {
                    body += "<p>".concat(indent, "   <input type=\"checkbox\" checked>    ").concat(indent, "    ").concat(lines[i].substring(6)).concat(indent, "</p>").concat(indent);
                    continue;
                }
                var listTags = getULListTags(lines, --i);
                body += listTags[0];
                i = listTags[1];
            }
            else if (lines[i].indexOf("1. ") === 0) {
                var listTags = getOLListTags(lines, --i);
                body += listTags[0];
                i = listTags[1];
            }
            else if (lines[i].indexOf("```") === 0) {
                var codeTags = getCodeTags(lines, i);
                body += codeTags[0];
                i = codeTags[1];
            }
            else {
                /*
                Paragraph tags
                */
                if (lines[i] == "Hello *EMP* bruh") {
                    console.log(addEMTag(lines[i]));
                }
            }
        }
        var content = " \n            <!DOCTYPE html>\n            <html lang=\"en\">\n            <head>\n                <meta charset=\"UTF-8\">\n                <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n                <title>".concat(title, "</title>\n            </head>\n            <body>\n                ").concat(body, "\n            </body>\n            </html>");
        console.log(content);
    }
});
program.parse();

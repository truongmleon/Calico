#!/usr/bin/env node
"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var commander_1 = require("commander");
var fs = require("node:fs");
var path = require("node:path");
var readline = require("node:readline");
var program = new commander_1.Command();
var projectPath = path.resolve(__dirname, "..");
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
    var dir = fs.opendirSync("".concat(projectPath, "/content/markdown"));
    var title = "", date = "", current = undefined;
    while ((current = dir.readSync()) !== null) {
        var body = "";
        var indent = "\n                ";
        var rl = readline.createInterface({
            input: fs.createReadStream("".concat(projectPath, "/content/markdown/").concat(current.name))
        });
        var lines = fs.readFileSync("".concat(projectPath, "/content/markdown/").concat(current.name), "utf8").split("\n");
        //console.log(lines)
        for (var i = 1; i < lines.length; i++) {
            if (lines[i] == "+++")
                continue;
            if (lines[i].indexOf("title") === 0) {
                title = lines[i].substring(9, lines[i].length - 1);
                body += "<h1>".concat(title, "</h1>").concat(indent);
            }
            else if (lines[i].indexOf("date") === 0) {
                date = lines[i].substring(7, lines[i].length);
                body += "<time>".concat(date, "</time>").concat(indent);
            }
            else if (lines[i].indexOf("##") === 0) {
                var count = 2;
                //h2 to h6 tags, hence j < 7
                for (var j = 2; j < 7; j++) {
                    if (lines[i][j] === "#")
                        count++;
                    else
                        break;
                }
                if (lines[i][count] === " ") {
                    body += "<h".concat(count, ">").concat(lines[i].substring(count + 1), "</h").concat(count, ">").concat(indent);
                }
                else {
                    body += "<p>".concat(lines[i], "</p>").concat(indent);
                }
            }
            else if (lines[i].indexOf("- ") === 0) {
                var nextIndex = 0;
                body += "<ul>".concat(indent);
                body += "<li>".concat(lines[i].substring(2), "</li>").concat(indent);
                while (lines[i + 1].indexOf("- ") === 0) {
                    i++;
                    body += "<li>".concat(lines[i].substring(2), "</li>").concat(indent);
                }
                body += "</ul>".concat(indent);
            }
        }
        var content = " \n            <!DOCTYPE html>\n            <html lang=\"en\">\n            <head>\n                <meta charset=\"UTF-8\">\n                <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n                <title>".concat(title, "</title>\n            </head>\n            <body>\n                ").concat(body, "\n            </body>\n            </html>");
        console.log(content);
    }
});
program.parse();

#!/usr/bin/env node
"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var commander_1 = require("commander");
var fs = require("node:fs");
var path = require("node:path");
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
    title = title.charAt(0).toUpperCase() + title.slice(1);
    var index = title.indexOf("-");
    while (title.indexOf("-") != -1) {
        title = title.slice(0, index) + " " + title.charAt(index + 1).toUpperCase() + title.slice(index + 2);
        index = title.indexOf("-", index + 1);
    }
    /* FORMAT:
    +++
    title = "Hi There"
    date = 29/6/2024
    +++
    */
    var date = new Date();
    var content = "+++\ntitle = \"".concat(title, "\"\ndate = ").concat(date.getDate(), "/").concat(date.getMonth(), "/").concat(date.getFullYear(), "\n+++");
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
    .description("Calico turning markdown files to HTML.");
program.parse();

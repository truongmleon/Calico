#!/usr/bin/env node
import { Command } from "commander";
import * as fs from 'node:fs';
import * as path from 'node:path';

const program: Command = new Command();
const projectPath: string = path.resolve(__dirname, "..");

const makeCSS: (name: string) => void = (name: string) => {
    if (!fs.existsSync(`${projectPath}/assets/styles`)) {
        fs.mkdirSync(`${projectPath}/assets/styles`);
    }

    if (!fs.existsSync(`${projectPath}/assets/styles/styles.${name}`)) {
        fs.open(`${projectPath}/assets/styles/styles.${name}`, "w", (err, file) => {
            if (err) throw err;
            console.log(`${name.toUpperCase()} is created.`);
    
            fs.close(file, (err) => {
                if (err) throw err;
            });
        });
    } else {
        console.log(`${name.toUpperCase()} already exists.`);
    }
}

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
    .action((options) => {
        if (!fs.existsSync(`${projectPath}/assets`)) {
            fs.mkdirSync(`${projectPath}/assets`);
            console.log("Assets folder created.");
        } 

        if (!fs.existsSync(`${projectPath}/content`)) {
            fs.mkdirSync(`${projectPath}/content`);
            fs.mkdirSync(`${projectPath}/content/markdown`);
            fs.mkdirSync(`${projectPath}/content/html`);
            console.log("Content folder created.");
        }

        if (!fs.existsSync(`${projectPath}/calico.toml`)) {
            const content = `languageCode = "en-us"\ntitle = "My New Calico Site"`
            fs.writeFile(`${projectPath}/calico.toml`, content, err => {
                if (err) throw err;
                console.log("calico.toml is created.");
              });
        }

        if (options.css) makeCSS("css");
        if (options.scss) makeCSS("scss");
    });

//make command
program
    .command("make")
    .description("Create markdown file for a post.")
    .argument("<string>", "filename")
    .option("-t, --title <string>", "title of markdown")
    .action((filename, options) => {
        let title = options.title ? options.title : filename;

        if (options.t !== "title of markdown" && title == filename) {
            title = options.t ? options.t : filename;
        }
        
        title = title.charAt(0).toUpperCase() + title.slice(1);
        let index = title.indexOf("-");

        while (title.indexOf("-") != -1) {
            title = title.slice(0, index) + " " + title.charAt(index + 1).toUpperCase() + title.slice(index + 2)
            index = title.indexOf("-", index + 1);
        }
        
        /* FORMAT:
        +++
        title = "Hi There"
        date = 29/6/2024
        +++
        */

        const date = new Date();
        const content = `+++\ntitle = "${title}"\ndate = ${date.getDate()}/${date.getMonth()}/${date.getFullYear()}\n+++`;

        if (!fs.existsSync(`${projectPath}/content/markdown/${filename}.md`)) {
            fs.writeFile(`${projectPath}/content/markdown/${filename}.md`, content, err => {
                if (err) throw err;
                console.log(`${filename}.md is created.`);
              });
        } else {
            console.log(`${filename}.md is already created.`);
        }
    });

//make-html command
program
    .command("make-html")
    .description("Calico turning markdown files to HTML.")

program.parse();
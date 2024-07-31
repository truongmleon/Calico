#!/usr/bin/env node
import { Command } from "commander";
import * as fs from 'node:fs';
import * as path from 'node:path';
import * as readline from 'node:readline';

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

const formatTitle: (title: string) => string = (title: string) => {
    title = title.charAt(0).toUpperCase() + title.slice(1);
    let index = title.indexOf("-");

    while (title.indexOf("-") != -1) {
        title = title.slice(0, index) + " " + title.charAt(index + 1).toUpperCase() + title.slice(index + 2)
        index = title.indexOf("-", index + 1);
    }

    return title;
}

const getDaySuffix: (day: number) => string = (day: number) => {
    const single = day % 10;

    if (single === 1) return "1st";
    if (single === 2) return "2nd";
    if (single === 3) return "3rd";
    return day + "th";
}

const repeatTag: (tag: string, level: number) => string[] = (tag: string, level: number) => {
    let body: string = "";
    let whitespace = ""

    for (let i = 0; i < level - 1; i++) {
        whitespace += "    ";
    }

    for (let i = 0; i < level; i++) {
        body += `${whitespace}   ${tag}\n                `;
    }

    return [body, whitespace];
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
        
        title = formatTitle(title);
        
        /* FORMAT:
        +++
        title = "Hi There"
        date = 29/6/2024
        +++
        */

        const date: Date = new Date();
        const year: number = date.getFullYear();
        const month: string = date.toLocaleString("default", { month: "long" });
        const day: string = getDaySuffix(date.getDate());

        const content = `+++\ntitle = "${title}"\ndate = ${month} ${day}, ${year}\n+++`;

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
    .description("Calico turning all markdown files to HTML.")
    .action(() => {
        const dir: fs.Dir = fs.opendirSync(`${projectPath}/content/markdown`);
        let title: string = "", date: string = "", current: any = undefined;

        while ((current = dir.readSync()) !== null) {
            let body: string = "";
            const indent: string = "\n                ";

            const lines: string[] = fs.readFileSync(`${projectPath}/content/markdown/${current.name}`, "utf8").split("\n");

            for (let i: number = 1; i < lines.length; i++) {
                if (lines[i] == "+++") continue;

                if (lines[i].indexOf("title") === 0) {
                    title = lines[i].substring(9, lines[i].length - 1);
                    body += `<h1>${title}</h1>${indent}`;
                } else if (lines[i].indexOf("date") === 0) {
                    date = lines[i].substring(7, lines[i].length);
                    body += `<time>${date}</time>${indent}`;
                } else if (lines[i].indexOf("##") === 0) {
                    let count: number = 2;

                    //h2 to h6 tags, hence j < 7
                    for (let j = 2; j < 7; j++) {
                        if (lines[i][j] === "#") count++;
                        else break;
                    }

                    if (lines[i][count] === " ") {
                        body += `<h${count}>${lines[i].substring(count + 1)}</h${count}>${indent}`;
                    } else {
                        body += `<p>${lines[i]}</p>${indent}`;
                    }
                } else if (lines[i].indexOf("- ") === 0) {
                    i--;
                    body += `<ul>${indent}`;

                    if (lines[i + 1].indexOf("- [ ]") != -1 || lines[i + 1].toLowerCase().indexOf("- [x]") != -1) {
                        
                        continue;
                    }

                    while (lines[i + 1].indexOf("- ") != -1) {
                        i++;
                        const index: number = lines[i].indexOf("- ");
                        const level: number = index / 2;
                        const repeats: string[] = repeatTag("<ul>", level)

                        body += repeats[0];
                        body += `${repeats[1]}    <li>${lines[i].substring(index + 2)}</li>${indent}`;
                        body += repeatTag("</ul>", level)[0];
                    }

                    body += `</ul>${indent}`;
                }
            }

            const content: string = ` 
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>${title}</title>
            </head>
            <body>
                ${body}
            </body>
            </html>`   

            console.log(content)
        }

        
        
    });

program.parse();
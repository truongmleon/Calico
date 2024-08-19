#!/usr/bin/env node
import { Command } from "commander";
import * as fs from 'node:fs';
import * as path from 'node:path';

declare global {
    interface String {
      replaceAt: (index: number, replacement: string, extra: number) => string;
    }
}

const indent: string = "\n                ";
const program: Command = new Command();
const projectPath: string = path.resolve(__dirname, "..");

String.prototype.replaceAt = function (index: number, replacement: string, extra: number): string {
    return this.substring(0, index) + replacement + this.substring(index + extra + 1);
}

/**
 * Creates CSS files if wanted.
 * 
 * @param {string} name - File extension to be used (either CSS or SCSS).
 */

const makeCSS: (name: string) => void = (name: string) => {
    // Create styles folder if it doesn't exist.
    if (!fs.existsSync(`${projectPath}/assets/styles`)) fs.mkdirSync(`${projectPath}/assets/styles`);

    // Creates styles file if it doesn't exist.
    if (!fs.existsSync(`${projectPath}/assets/styles/styles.${name}`)) {
        fs.open(`${projectPath}/assets/styles/styles.${name}`, "w", (err, file) => {
            if (err) throw err;
            console.log(`${name.toUpperCase()} is created.`);
    
            fs.close(file, (err) => {if (err) throw err;});
        });
    } else {
        console.log(`${name.toUpperCase()} already exists.`);
    }
}

/**
 * Use inputted titles will have dashes and we will need to remove them with this function.
 * 
 * @param {string} title - Makes the first letter of each word uppercase and removes '-'.
 * @returns {string} - The formatted title.
 */

const formatTitle: (title: string) => string = (title: string): string => {
    title = title.charAt(0).toUpperCase() + title.slice(1);
    let index = title.indexOf("-");

    while (title.indexOf("-") != -1) {
        title = title.slice(0, index) + " " + title.charAt(index + 1).toUpperCase() + title.slice(index + 2)
        index = title.indexOf("-", index + 1);
    }

    return title;
}

/**
 * Given a day of the month, append the suffix.
 * 
 * @param {number} day - Day of the month.
 * @returns {string} - Day with the suffix.
 */

const getDaySuffix: (day: number) => string = (day: number): string => {
    const single = day % 10;

    if (single === 1) return "1st";
    if (single === 2) return "2nd";
    if (single === 3) return "3rd";
    return day + "th";
}

/**
 * Repeats given tag.
 * 
 * @param {string} tag - Tag to be repeated.
 * @param {number} level - The number of times to repeat.
 * @returns {string[]} - The string of repeated tags and whitespace.
 */

const repeatTag: (tag: string, level: number) => string[] = (tag: string, level: number): string[] => {
    let body: string = "";
    let whitespace = ""

    for (let i = 0; i < level - 1; i++) {
        whitespace += "    ";
    }

    for (let i = 0; i < level; i++) {
        body += `${whitespace}   ${tag}${indent}`;
    }

    return [body, whitespace];
}

/**
 * Gets a header tag.
 * 
 * @param {string} text - Text inside of header.
 * @returns {string} - Header tag HTML string.
 */

const getHeaderTag: (text: string) => string = (text: string): string => {
    let body: string = "";
    let count: number = 2;
    text = checkEmphasis(text);

    //h2 to h6 tags, hence j < 7
    for (let j = 2; j < 7; j++) {
        if (text[j] === "#") count++;
        else break;
    }

    if (text[count] === " ") {
        body += `<h${count}>${text.substring(count + 1)}</h${count}>${indent}`;
    } else {
        body += `<p>${text}</p>${indent}`;
    }

    return body;
}

/**
 * Gets all UL tags.
 * 
 * @param {string[]} lines - String of lines in the markdown file. 
 * @param {number} i - Index of current line.
 * @returns {any[]} - Tags in index 0, new current line in index 1.
 */

const getULListTags: (lines: string[], i: number) => any[] = (lines: string[], i: number): any[] => {
    let body: string = `<ul>${indent}`;

    while (lines[++i].indexOf("- ") != -1) {
        const index: number = lines[i].indexOf("- ");
        const level: number = index / 2;
        const repeats: string[] = repeatTag("<ul>", level)

        body += repeats[0];
        body += `${repeats[1]}    <li>${lines[i].substring(index + 2)}</li>${indent}`;
        body += repeatTag("</ul>", level)[0];
    }

    body += `</ul>${indent}`;
    body = checkEmphasis(body);

    return [body, i];
}

/**
 * Gets all OL tags.
 * 
 * @param {string[]} lines - String of lines in the markdown file. 
 * @param {number} i - Index of current line.
 * @returns {any[]} - Tags in index 0, new current line in index 1.
 */

const getOLListTags: (lines: string[], i: number) => any[] = (lines: string[], i: number): any[] => {
    let body: string = `<ol>${indent}`;
    let count = 1;

    while (lines[++i].indexOf(`${count}. `) === 0) {
        body += `   <li>${lines[i].substring(3)}</li>${indent}`;
        count++;
    }

    body += `</ol>${indent}`;
    body = checkEmphasis(body);

    return [body, i];
}

/**
 * Gets all Code tags.
 * 
 * @param {string[]} lines - String of lines in the markdown file. 
 * @param {number} i - Index of current line.
 * @returns {any[]} - Tags in index 0, new current line in index 1.
 */

const getCodeTags: (lines: string[], i: number) => any[] = (lines: string[], i: number): any[] => {
    const language = lines[i].substring(lines[i].indexOf("```") + 3);
    let lineCount: number = 0;
    let codeBody: string = `<code data-lang="${language}">${indent}`;
    
    while (lines[++i] !== "```") {
        codeBody += "   " + lines[i] + indent;
        lineCount++;
    }

    codeBody += `</code>`;
    codeBody = checkEmphasis(codeBody);
    
    if (lineCount > 1) return [`<pre>${codeBody}</pre>${indent}`, i];
    return [codeBody + indent, i];
}

/**
 * Gets plain HTML code written in markdown.
 * 
 * @param {string[]} lines - String of lines in the markdown file. 
 * @param {number} i - Index of current line.
 * @returns {any[]} - Tags in index 0, new current line in index 1.
 */

const getPlainHTML: (lines: string[], i: number) => any[] = (lines: string[], i: number): any[] => {
    let body: string = "";
    while (lines[++i].indexOf("</") !== 0 && lines[i].indexOf(">") !== lines[i].length - 1) {
        body += "   " + lines[i] + indent;
    }

    body += lines[i] + indent;

    return [body, i];
}

/**
 * Adds special tags in the middle of strings such as <strong></strong> and <em></em>.
 * 
 * @param {string} text - Text to add possible special tags.
 * @param {string} check - Indicator of markdown to HTML (e.g. * for italics).
 * @param {string} tag - Tag used.
 * @returns {string} - HTML content.
 */

const addSpecialTag: (text: string, check: string, tag: string) => string = (text: string, check: string, tag: string): string => {
    if (text.indexOf(check) === -1) return text;
    
    const checkLength = check.length;
    const tagLength: number = tag.length + checkLength % 2;
    const extra: number = checkLength - 1; // For extra * in bold text.
    let indexOfFirst: number = text.indexOf(check);
    let indexOfLast: number = text.indexOf(check, indexOfFirst + checkLength);

    while (indexOfLast !== -1) {
        text = text.replaceAt(indexOfFirst, `<${tag}>`, extra);
        text = text.replaceAt(indexOfLast + tagLength, `</${tag}>`, extra);

        indexOfFirst = text.indexOf(check, indexOfLast + checkLength);
        if (indexOfFirst == -1) break;
        indexOfLast = text.indexOf(check, indexOfFirst + checkLength);
    }

    return text;
}

/**
 * Runs through the special tags to be added in the text.
 * 
 * @param {string} text - Text to add possible special tags.
 * @returns {string} - Either text or text with special tags.
 */

const checkEmphasis: (text: string) => string = (text: string): string => {
    text = addSpecialTag(text, "**", "strong");
    text = addSpecialTag(text, "*", "em")
    text = addSpecialTag(text, "~~", "del");
    text = addSpecialTag(text, "~", "sup");
    text = addSpecialTag(text, "^", "sub");

    return text;
}

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

// make command
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

// make-html command
program
    .command("make-html")
    .description("Calico turning all markdown files to HTML.")
    .action(() => {
        // Opens markdown folder to convert all markdown files
        const dir: fs.Dir = fs.opendirSync(`${projectPath}/content/markdown`);
        let current: any = undefined;

        // While there's still markdown files to convert
        while ((current = dir.readSync()) !== null) {
            const lines: string[] = fs.readFileSync(`${projectPath}/content/markdown/${current.name}`, "utf8").split("\n");
            let currentFootnote: number = 1;
            let body: string = "";
            let footnotes: string = "";
            let title: string = lines[1].substring(9, lines[1].length - 1);
            let date: string = lines[2].substring(7, lines[2].length);

            body += `<h1>${checkEmphasis(title)}</h1>${indent}`;
            if (date !== "") body += `<time>${checkEmphasis(date)}</time>${indent}`;

            for (let i: number = 4; i < lines.length; i++) {
                let currentLine = lines[i];
                if (currentLine === "" || currentLine.indexOf("<!--") == 0 && currentLine.indexOf("-->") != -1) continue;

                if (currentLine.indexOf("##") === 0) {
                    body += getHeaderTag(currentLine);
                } else if (currentLine.indexOf("> ") === 0) { 
                    body += `<blockquote>${checkEmphasis(currentLine.substring(2))}</blockquote>${indent}`;
                } else if (currentLine.indexOf("- ") === 0) {
                    if (currentLine.indexOf("[ ]") === 2) {
                        body += `<p>${indent}   <input type="checkbox">    ${indent}    ${checkEmphasis(currentLine.substring(6))}${indent}</p>${indent}`;
                        continue;
                    } else if (currentLine.toLowerCase().indexOf("[x]") == 2) {
                        body += `<p>${indent}   <input type="checkbox" checked>    ${indent}    ${checkEmphasis(currentLine.substring(6))}${indent}</p>${indent}`;
                        continue;
                    }

                    const listTags = getULListTags(lines, --i);
                    body += listTags[0];
                    i = listTags[1];
                } else if (currentLine.indexOf("1. ") === 0) {
                    const listTags = getOLListTags(lines, --i);
                    body += listTags[0];
                    i = listTags[1];
                } else if (currentLine.indexOf("```") === 0) {
                    const codeTags = getCodeTags(lines, i);
                    body += codeTags[0];
                    i = codeTags[1];
                } else if (currentLine.indexOf("<") === 0 && currentLine.indexOf(">") === currentLine.length - 1) {
                    const plainHTML = getPlainHTML(lines, i);
                    body += currentLine + indent;
                    body += plainHTML[0];
                    i = plainHTML[1];
                } else if (currentLine.indexOf("<") === 0 && currentLine.indexOf("/>") === currentLine.length - 2) {
                    body += currentLine + indent;
                } else if (currentLine.indexOf(`[^${currentFootnote}]`) === 0) {
                    currentLine = currentLine.replaceAt(0, `${currentFootnote}.`, 4);
                    footnotes += `<p id="#fn${currentFootnote}">${currentLine} <a href="fnote${currentFootnote}">&#8617</a></p>${indent}`;
                    currentFootnote++;
                } else if (currentLine.indexOf(`[^${currentFootnote}]`) !== -1) {
                    const note: string = `<sup><a id="fnote${currentFootnote}" href="#fn${currentFootnote}">[${currentFootnote}]</a></sup>`;
                    currentLine = currentLine.replaceAt(currentLine.indexOf(`[^${currentFootnote}]`), note, 3);
                    body += `<p>${currentLine}</p>${indent}`;
                } else if (currentLine.indexOf("(") !== -1 && currentLine.indexOf(")") !== -1 && currentLine.indexOf("[") !== -1 && currentLine.indexOf("]") !== -1) {
                        
                } else {
                    currentLine = checkEmphasis(currentLine);
                    body += `<p>${checkEmphasis(currentLine)}</p>${indent}`;
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
                ${body + footnotes}
            </body>
            </html>`   

            console.log(content)
        }

        
    });

program.parse();
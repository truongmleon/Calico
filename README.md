# Calico

Calico is a static-site generator that I made because I'm tired of using the ones already on the internet. This is in development, but I have decent hopes.

## How to Use Calico

Calico CLI starts with the ```cali``` keyword.

To create a new project, run:

```cli
cali start
```

If you want CSS/SCSS:

```cli
cali start --css
```

```cli
cali start --scss
```

The ```start``` command creates the folder structure for the project.

Nodemon is a major dependency for managing TypeScript code and refreshing. Basically, Nodemon checks when the TypeScript file is being edited to convert to JavaScript and when markdown files in the content folder are being edited. This allows for less commands in the CLI and more focus on making the project.

Install using npm:

```cli
npm i
nodemon
```

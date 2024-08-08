# Calico

Calico is a static-site generator that I made because I'm tired of using the ones already on the internet. I made it so it's the easiest static-site generator you will ever use. Perhaps I'll make some themes too. The plan is to use Calico to make blogs and website portfolios.

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

Nodemon is a major dependency for when you refresh markdown files you want into HTML. Basically, Nodemon checks when the TypeScript file is being edited to convert to JavaScript and when markdown files in the content folder are being edited. This allows for less commands in the CLI and more focus on making the project.

Install using npm:

```cli
npm i
nodemon
```

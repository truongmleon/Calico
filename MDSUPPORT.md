# Markdown Support

The heading of the markdown contains basic information about your file. It occupies the first four lines. The title is used as an ```h1``` tag. The date is used in the post as well with a ```date``` tag. And yes, you may use *, **, etc. in the basic information for emphasis.

Example Usage:

```txt
+++
title = "First CLI Post"
date = July 30th, 2024
+++
```

Example with Emphasis:

```txt
+++
title = "First **CLI** ~~News~~ Post"
date = *July* 30th, 2024
+++
```

Example with no Information (this is fine, but keep the lines though):

```txt
+++
title = ""
date = 
+++
```

Other Headings (no single ```#``` since ```h1``` (#) was used in the title):

```md
## Heading 2
### Heading 3
<!-- and so on to h6 -->
```

## Heading 2

### Heading 3

## Basic Emphasis

```md
**BOLD**
*EMP*
***BOTH***
~~STRIKE~~
***~~COMBO~~***
> QUOTES
```

**BOLD**
*EMP*
~~STRIKE~~
***~~COMBO~~***
> QUOTES

## Links

Use image tags for resizing.

```md
Visit [Leon's website](https://leontt.vercel.app)!

<img src="https://leontt.vercel.app/switch/1.webp" alt="Switch" width="200"/>

[README.md](README.md)
```

Visit [Leon's website](https://leontt.vercel.app)!

<img src="https://leontt.vercel.app/switch/1.webp" alt="Switch" width="200"/>

Check out the [README.md](README.md).

## Code Blocks

````md
```py
print("Java sucks!")
```
````

```py
print("Java sucks!")
```

## Subscript and Superscript

This isn't showing on GitHub, but it does work.

```md
Hello~hi~

Bye^Bye^
```

Hello~hi~

Bye^Bye^

## Lists

Use two spaces for list levels, not four.

```md
- Today
- It is
- Good

1. Yes
2. No

- First
    - Next
        - Next Again

- [ ] Not Complete
- [x] Complete
```

- Today
- It is
- Good

1. Yes
2. No

- First
  - Next
    - Next Again

- [ ] Not Complete
- [x] Complete

## Footnotes

```md
JavaScript[^1].

[^1]: An OK language.
```

JavaScript[^1].

[^1]: An OK language.

## Tables

```md
| Syntax | Description |
| ----------- | ----------- |
| Header | Title |
| Paragraph | Text |
```

| Syntax | Description |
| ----------- | ----------- |
| Header | Title |
| Paragraph | Text |

## Comments

```md
<!-- COMMENT ME! -->
```

## HTML

You can write HTML if you would like. It will be rendered.

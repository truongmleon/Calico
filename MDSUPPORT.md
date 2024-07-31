# Markdown Support

The heading of the markdown contains basic information about your file. It occupies the first four lines. The title is used as an ```h1``` tag. The date is used in the post as well. The date may be empty, but the title may not.

```txt
+++
title = "First Post"
date = 29/6/2024
+++
```

Other headings (no ```h1```):

```md
## Heading 2
### Heading 3
<!-- and so on to h6 -->
```

## Heading 2

### Heading 3

## Basic styling

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

## Subscripts and Superscripts

This isn't showing on GitHub, but it does work.

```md
Hello~hi~

Bye^Bye^
```

Hello~hi~

Bye^Bye^

## Lists

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
<!-- COMMENT -->
```

## HTML

You can write HTML if you would like also. It will be rendered.

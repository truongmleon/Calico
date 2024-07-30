# Markdown Support

The heading of the markdown contains basic information about your file. It should occupy the first four lines. The title is used as an ```h1``` tag. The date is used in the title of the post.

```md
+++
title = "First Post"
date = 29/6/2024
+++
```

Other headings can be used:

```md
## Heading 2
### Heading 3
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

```md
Visit [Leon's website]("https://leontt.vercel.app")!

![Switch](https://leontt.vercel.app/switch/1.webp)

[README.md](README.md)
```

Visit [Leon's website]("https://leontt.vercel.app")!

![Switch](https://leontt.vercel.app/switch/1.webp)

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

```md
Hello~hi~

Bye^Bye^
```

Hello~hi~

Bye^Bye^

## Lists

```md
- Today
- I
- Am Good

1. Yes
2. No

- Fist
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

- Fist
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
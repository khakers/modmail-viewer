@import static com.github.khakers.modmailviewer.jte.JteContext.*
@import com.github.khakers.modmailviewer.Page
@import gg.jte.Content


@param Page page
@param Content content


<!doctype html>
<html lang="en" xmlns="http://www.w3.org/1999/html">
<head>
    <title>${page.getTitle()}</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    @template.macros.HeaderImports()
</head>

<body>
<div class="page-container">
    @template.macros.navbar(page = page)
    <main>
        <div class="content" up-main data-nsfw="${page.isNSFW()}">
            ${content}
        </div>
    </main>
    @template.macros.footer(page = page)
</div>
</body>
</html>
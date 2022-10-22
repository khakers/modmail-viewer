@param java.util.List<com.github.khakers.data.ModMailLogEntry> logEntries
<!doctype html>
<html lang="en" xmlns="http://www.w3.org/1999/html">
<head>
    <title>Modmail Logs</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.2/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-Zenh87qX5JnK2Jl0vWa8Ck2rdkQ2Bzep5IDxbcnCeuOxjzrPF/et3URy9Bv1WTRi" crossorigin="anonymous">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.9.1/font/bootstrap-icons.css">
</head>
<body style="padding-top: 56px;">

@template.navbar()

<div class="container pt-5">
    <ol class="list-group">
        @for(com.github.khakers.data.ModMailLogEntry entry : logEntries)
            <li class="list-group-item">
                <h2><a href="/logs/${entry.getKey()}">${entry.getKey()}</a></h2>
                @if(entry.isNsfw())
                    <span class="badge rounded-pill text-bg-danger fs-6"><i class="bi bi-exclamation-triangle"></i> Closed</span>
                @endif
                @if(entry.isOpen())
                    <span class="badge rounded-pill text-bg-success fs-6">Open</span>
                @else
                    <span class="badge rounded-pill text-bg-danger fs-6"><i class="bi bi-x-circle"></i> Closed</span>
                @endif
                <span>Created by:</span>
                <span class="fw-semibold">${entry.getCreator().name()}</span><span
                        class="text-muted">#${entry.getCreator().discriminator()}</span>
                <span>on ${entry.getCreationTime().toString()}</span>
                ·
                <span>${entry.getMessages().size()} messages</span>
            </li>
        @endfor

    </ol>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.2/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-OERcA2EqjJCMA+/3y+gxIOqMEjwtxJY7qPCqsdltbNJuaOe923+mo//f6V8Qbsw3"
        crossorigin="anonymous"></script>
<script>
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]')
    const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl))
</script>
</body>
</html>
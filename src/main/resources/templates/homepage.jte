@param java.util.List<com.github.khakers.data.ModMailLogEntry> logEntries
@param int page
@param int pageCount
<!doctype html>
<html lang="en" xmlns="http://www.w3.org/1999/html">
<head>
    <title>Modmail Logs</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.2/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-Zenh87qX5JnK2Jl0vWa8Ck2rdkQ2Bzep5IDxbcnCeuOxjzrPF/et3URy9Bv1WTRi" crossorigin="anonymous">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.9.1/font/bootstrap-icons.css">
<%--    <link rel="stylesheet" href="styles.css">--%>
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
                        class="text-secondary">#${entry.getCreator().discriminator()}</span>
                <span>on <span class="text-muted" timestamp="${entry.getCreationTime().toString()}" timestamp-type="detailed"></span></span>
                ·
                <span>${entry.getMessages().size()} messages</span>
            </li>
        @endfor

    </ol>
</div>
!{String previousClass = null;}
@if(page==1)
    !{previousClass="disabled";}
@endif
!{String nextClass = null;}
@if(page==pageCount)
    !{nextClass="disabled";}
@endif
<div class="container col d-flex justify-content-center p-4">
    <nav aria-label="Page navigation example">
        <ul class="pagination">
            <li class="page-item"><a class="page-link ${previousClass}" href="?page=${Math.max(page-1, 1)}">Previous</a>
            </li>
            @for(int i = 1; i < pageCount+1; ++i)
                !{String isActive = null;}
                @if(i == page)
                    !{isActive="active";}
                @endif
                <li class="page-item">
                    @if(i == page)
                        <span class="page-link ${isActive}">${i}</span>
                    @else
                        <a class="page-link" href="?page=${i}">${i}</a>
                    @endif
                </li>
            @endfor
            <li class="page-item"><a class="page-link ${nextClass}" href="?page=${page+1}">Next</a></li>
        </ul>
    </nav>
</div>

<script src="timeformat.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.2/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-OERcA2EqjJCMA+/3y+gxIOqMEjwtxJY7qPCqsdltbNJuaOe923+mo//f6V8Qbsw3"
        crossorigin="anonymous"></script>
<script>
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]')
    const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl))
</script>
</body>
</html>
function update(key, value) {
    const searchParams = new URLSearchParams(window.location.search);
    if (value !== '')
        searchParams.set(key, value);
    else
        searchParams.delete(key);
    window.location.search = searchParams.toString();
}
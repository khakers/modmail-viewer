@import com.github.khakers.modmailviewer.data.User
@import com.github.khakers.modmailviewer.util.DiscordUtils


@param User user

<span class="user-select-all d-inline-flex" data-bs-toggle="tooltip"
      data-bs-title="${user.id()}">
    <span class="fw-semibold">${user.name()}</span>
    <span class="text-muted">${DiscordUtils.getDiscriminatorString(user)}</span>
</span>
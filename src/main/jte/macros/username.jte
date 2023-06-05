@import com.github.khakers.modmailviewer.data.User
@import com.github.khakers.modmailviewer.util.DiscordUtils


@param User user

<span class="user-select-all" data-bs-toggle="tooltip"
      data-bs-title="${user.id()}">
    <span class="fw-semibold">${user.name()}</span>
    @if(!DiscordUtils.isMigratedUserName(user))
        <span class="text-muted">#${user.discriminator()}</span>

    @endif
</span>
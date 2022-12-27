@import com.github.khakers.modmailviewer.data.User

@param User user

<span class="user-select-all" data-bs-toggle="tooltip"
      data-bs-title="${user.id()}"><span
            class="fw-semibold">${user.name()}</span><span
            class="text-muted">#${user.discriminator()}</span>
                            </span>
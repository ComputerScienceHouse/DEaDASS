from functools import wraps
from flask import session
from markupsafe import Markup


def credential_html(value):
    """Render a credential as an inline ``<code>`` block followed by a copy button.

    Returns a :class:`markupsafe.Markup` so it can be embedded in ``flash()``
    messages via ``Markup.format()`` without losing escaping on the surrounding
    template text. The value is HTML-escaped (by ``Markup.format``) before being
    placed both in the visible code block and in the button's
    ``data-deadass-copy`` attribute.
    """
    return Markup(
        '<code class="deadass-credential">{value}</code>'
        ' <button type="button"'
        ' class="btn btn-sm btn-outline-secondary deadass-copy"'
        ' data-deadass-copy="{value}"'
        ' aria-label="Copy to clipboard">'
        '<i class="fas fa-copy" aria-hidden="true"></i>'
        ' <span class="deadass-copy-label">Copy</span>'
        '</button>'
    ).format(value=str(value))


def csh_user_auth(func):
    @wraps(func)
    def wrapped_function(*args, **kwargs):
        uid = str(session["userinfo"].get("preferred_username", ""))
        last = str(session["userinfo"].get("family_name", ""))
        first = str(session["userinfo"].get("given_name", ""))
        picture = "https://profiles.csh.rit.edu/image/" + uid
        groups = session["userinfo"].get("groups", [])
        is_eboard = "eboard" in groups
        is_rtp = "rtp" in groups
        auth_dict = {
            "uid": uid,
            "first": first,
            "last": last,
            "picture": picture,
            "admin": is_eboard or is_rtp,
        }
        kwargs["auth_dict"] = auth_dict
        return func(*args, **kwargs)

    return wrapped_function


def latin_to_utf8(string):
    return str(bytes(string, encoding="latin1"), encoding="utf8")


def get_user(func):
    @wraps(func)
    def wrapped_function(*args, **kwargs):
        username = str(session["userinfo"].get("preferred_username", ""))

        user_dict = {
            "username": username
            #'account': account,
            #'student': current_student
        }

        kwargs["user_dict"] = user_dict
        return func(*args, **kwargs)

    return wrapped_function

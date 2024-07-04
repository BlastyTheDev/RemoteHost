usernamefocus = false;
passwordfocus = false;

function togglePasswordClass() {
    if (passwordfocus || password.value.length != 0) {return;}

    passwordLabel.classList.toggle('active');
    passwordLabel.classList.toggle('not-active');
}

function toggleUsernameClass() {
    if (usernamefocus || username.value.length != 0) {return;}

    usernameLabel.classList.toggle('active');
    usernameLabel.classList.toggle('not-active');
}

username.onmouseover = () => toggleUsernameClass();
password.onmouseover = () => togglePasswordClass();

username.onmouseout = () => toggleUsernameClass();
password.onmouseout = () => togglePasswordClass();

username.onfocus = () => usernamefocus = true;
password.onfocus = () => passwordfocus = true;

username.addEventListener('focusout', () => {
    usernamefocus = false;
    toggleUsernameClass(usernameLabel);
});

password.addEventListener('focusout', () => {
    passwordfocus = false;
    togglePasswordClass(passwordLabel);
});
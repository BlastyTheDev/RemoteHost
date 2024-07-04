// Remember if the user is focused on the input
user = false;
pass = false;
 // adds effect to the labels of the inputs
username_inp.onmouseover = () => {username_label.classList.add("foccus")};
username_inp.onmouseout = () => {if (!user) {username_label.classList.remove("foccus")}};

// Toggles if clicked
username_inp.onfocus = () => {user = true;};
username_inp.onblur = () => {user = false; username_label.classList.remove("foccus");};

// same for password
password_inp.onmouseover = () => {password_label.classList.add("foccus")};
password_inp.onmouseout = () => {if (!pass) {password_label.classList.remove("foccus")}};

password_inp.onfocus = () => {pass = true;};
password_inp.onblur = () => {pass = false; password_label.classList.remove("foccus");};
function hide() { 
    document.getElementById('login').classList.toggle('hidden');
    document.getElementById('signup').classList.toggle('hidden');
}

create.onclick = hide;
cancel.onclick = hide;
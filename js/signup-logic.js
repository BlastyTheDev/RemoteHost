stage = 1;
next = document.getElementsByClassName('Next');
back = document.getElementsByClassName('Back');

one = document.getElementById('one');
two = document.getElementById('two');
three = document.getElementById('three');
four = document.getElementById('four');
five = document.getElementById('five');

stage1 = document.getElementById('1');
stage2 = document.getElementById('2');
stage3 = document.getElementById('3');
stage4 = document.getElementById('4');

function updateStage() {
    if (stage > 5) {
        window.location.href = 'login.html';
        return;
    }

    switch (stage) {
        case 1:
            one.classList.remove('hidden');
            two.classList.add('hidden');
            stage1.textContent = 'radio_button_checked';
            break;
        case 2:
            one.classList.add('hidden');
            two.classList.remove('hidden');
            three.classList.add('hidden');
            stage1.classList.add('viewed');
            stage2.textContent = 'radio_button_checked';
            break;
        case 3:
            two.classList.add('hidden');
            three.classList.remove('hidden');
            four.classList.add('hidden');
            stage2.classList.add('viewed');
            stage3.textContent = 'radio_button_checked';
            break;
        case 4:
            three.classList.add('hidden');
            four.classList.remove('hidden');
            five.classList.add('hidden');
            stage3.classList.add('viewed');
            stage4.textContent = 'radio_button_checked';
            document.getElementsByClassName('steps')[0].classList.remove('hidden');
            break;
        case 5: 
            four.classList.add('hidden');
            five.classList.remove('hidden');
            stage4.classList.add('viewed');
            document.getElementsByClassName('steps')[0].classList.add('hidden');
            break;
    }
}

for (let i = 0; i < next.length; i++) {
  next[i].addEventListener('click', () => {
   stage++;
   updateStage();
  });
}

for (let i = 0; i < back.length; i++) {
  back[i].addEventListener('click', () => {
    stage--;
    updateStage();
  });
}


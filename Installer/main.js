import './style.css'

const installBtn = document.getElementById('install-btn');
const progressWrapper = document.getElementById('progress-wrapper');
const progressFill = document.getElementById('progress-fill');
const statusText = document.getElementById('status-text');

installBtn.addEventListener('click', async () => {
  // Hide button, show progress
  installBtn.style.display = 'none';
  progressWrapper.classList.remove('hidden');
  
  statusText.innerText = 'Richiesta al server di installazione...';
  progressFill.style.width = '30%';

  try {
    const isCleanInstall = document.getElementById('clean-install').checked;
    
    const response = await fetch('http://localhost:3000/install', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ clean: isCleanInstall })
    });
    
    const result = await response.json();
    if (result.success) {
      progressFill.style.width = '100%';
      statusText.innerText = 'Installation Complete! 🚀';
      
      setTimeout(() => {
        progressWrapper.classList.add('hidden');
        installBtn.style.display = 'block';
        installBtn.style.cursor = 'default';
        installBtn.querySelector('.btn-text').innerText = 'Installazione Completata!';
        installBtn.onclick = (e) => e.preventDefault();
        
        // Try to close the browser window after 3 seconds
        setTimeout(() => window.close(), 3000);
      }, 1500);

    } else {
      progressFill.style.width = '100%';
      progressFill.style.backgroundColor = 'red';
      statusText.innerText = 'Installation failed: ' + result.message;
      setTimeout(() => installBtn.style.display = 'block', 2000);
    }
  } catch (error) {
    progressFill.style.width = '100%';
    progressFill.style.backgroundColor = 'red';
    statusText.innerText = 'Server offline or error occurred.';
    setTimeout(() => installBtn.style.display = 'block', 2000);
  }
});

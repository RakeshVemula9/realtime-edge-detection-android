let uploadedImages = [];

async function loadUploadedImages() {
    try {
        const response = await fetch('/images');
        const images = await response.json();

        if (images.length > 0) {
            uploadedImages = images;
            displayImages();
        } else {
            document.getElementById('status').textContent = 'Waiting for images...';
        }
    } catch (error) {
        console.error('Error loading images:', error);
        document.getElementById('status').textContent = 'Waiting for images...';
    }
}

function displayImages() {
    const gallery = document.getElementById('gallery');
    const status = document.getElementById('status');

    if (uploadedImages.length === 0) {
        status.textContent = 'Waiting for images...';
        gallery.innerHTML = '';
        return;
    }

    status.textContent = `${uploadedImages.length} image(s) uploaded`;
    gallery.innerHTML = '';

    uploadedImages.forEach(image => {
        const card = document.createElement('div');
        card.className = 'image-card';

        const img = document.createElement('img');
        img.src = `/uploads/${image.filename}`;
        img.alt = image.filename;

        const info = document.createElement('div');
        info.className = 'image-info';

        const name = document.createElement('div');
        name.className = 'image-name';
        name.textContent = image.filename;

        const meta = document.createElement('div');
        meta.className = 'image-meta';

        const filterType = image.filename.includes('processed') ? '📷 Processed' : '📸 Original';
        const sizeKB = (image.size / 1024).toFixed(2);

        meta.innerHTML = `
            <div><strong>Filter:</strong> ${filterType}</div>
            <div><strong>Size:</strong> ${sizeKB} KB</div>
        `;

        info.appendChild(name);
        info.appendChild(meta);
        card.appendChild(img);
        card.appendChild(info);
        gallery.appendChild(card);
    });
}

setInterval(loadUploadedImages, 2000);
loadUploadedImages();
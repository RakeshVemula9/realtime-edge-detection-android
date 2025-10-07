const express = require('express');
const multer = require('multer');
const path = require('path');
const fs = require('fs');

const app = express();
const PORT = 3000;

// Create uploads directory if it doesn't exist
const uploadsDir = path.join(__dirname, 'uploads');
if (!fs.existsSync(uploadsDir)) {
    fs.mkdirSync(uploadsDir);
}

// Configure multer for file uploads
const storage = multer.diskStorage({
    destination: function (req, file, cb) {
        cb(null, uploadsDir);
    },
    filename: function (req, file, cb) {
        const timestamp = Date.now();
        const originalName = file.originalname || `photo_${timestamp}.jpg`;
        cb(null, originalName);
    }
});

const upload = multer({ storage: storage });

// Serve static files
app.use(express.static('public'));
app.use('/uploads', express.static(uploadsDir));

// Upload endpoint
app.post('/upload', upload.single('image'), (req, res) => {
    if (req.file) {
        console.log('✓ Image uploaded:', req.file.filename);
        res.json({ success: true, filename: req.file.filename });
    } else {
        console.log('✗ Upload failed: No file received');
        res.status(400).json({ success: false, error: 'No file uploaded' });
    }
});

// Get list of uploaded images
app.get('/images', (req, res) => {
    fs.readdir(uploadsDir, (err, files) => {
        if (err) {
            return res.status(500).json({ error: 'Failed to read uploads directory' });
        }

        const imageFiles = files
            .filter(file => file.match(/\.(jpg|jpeg|png)$/i))
            .map(filename => {
                const filepath = path.join(uploadsDir, filename);
                const stats = fs.statSync(filepath);
                return {
                    filename: filename,
                    size: stats.size,
                    uploaded: stats.mtime
                };
            })
            .sort((a, b) => b.uploaded - a.uploaded);

        res.json(imageFiles);
    });
});

app.listen(PORT, '0.0.0.0', () => {
    const networkInterfaces = require('os').networkInterfaces();
    let localIP = 'localhost';

    Object.keys(networkInterfaces).forEach(interfaceName => {
        networkInterfaces[interfaceName].forEach(iface => {
            if (iface.family === 'IPv4' && !iface.internal) {
                localIP = iface.address;
            }
        });
    });

    console.log(`Server running on http://localhost:${PORT}`);
    console.log(`Server also accessible at http://${localIP}:${PORT}`);
});
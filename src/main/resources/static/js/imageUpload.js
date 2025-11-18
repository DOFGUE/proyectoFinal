/**
 * Script para manejo de subida de imágenes
 * Todas las validaciones se realizan en ImageController del servidor
 */

// Subir imagen al servidor
document.getElementById('btnSubirImagen').addEventListener('click', async function() {
    const fileInput = document.getElementById('imagenUpload');
    const file = fileInput.files[0];
    
    if (!file) {
        alert('Por favor selecciona una imagen');
        return;
    }
    
    // UI: Mostrar progreso
    document.getElementById('uploadProgress').style.display = 'block';
    this.disabled = true;
    
    const formData = new FormData();
    formData.append('file', file);
    
    try {
        // Enviar al servidor - validaciones y guardado en ImageController
        const response = await fetch('/api/images/upload', {
            method: 'POST',
            body: formData
        });
        
        const data = await response.json();
        
        if (data.success) {
            document.getElementById('rutaImagenProducto').value = data.filename;
            document.getElementById('imagePreview').innerHTML = 
                `<img src="${data.url}" alt="Preview" style="max-height: 250px; object-fit: cover;" class="rounded">`;
            showAlert('success', '✓ Imagen subida exitosamente');
        } else {
            showAlert('danger', '✗ Error: ' + data.error);
        }
    } catch (error) {
        console.error('Error:', error);
        showAlert('danger', '✗ Error al subir la imagen');
    } finally {
        document.getElementById('uploadProgress').style.display = 'none';
        this.disabled = false;
    }
});

// Mostrar alertas temporales
function showAlert(type, message) {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
    alertDiv.innerHTML = `${message}<button type="button" class="btn-close" data-bs-dismiss="alert"></button>`;
    document.querySelector('.card-body').insertBefore(alertDiv, document.querySelector('.card-body').firstChild);
    setTimeout(() => alertDiv.remove(), 5000);
}

// Drag & Drop (solo UI, procesamiento en servidor)
const dropZone = document.getElementById('imagePreview');
['dragenter', 'dragover', 'dragleave', 'drop'].forEach(e => 
    dropZone.addEventListener(e, ev => { ev.preventDefault(); ev.stopPropagation(); })
);
['dragenter', 'dragover'].forEach(e => 
    dropZone.addEventListener(e, () => dropZone.style.backgroundColor = '#e7f3ff')
);
['dragleave', 'drop'].forEach(e => 
    dropZone.addEventListener(e, () => dropZone.style.backgroundColor = '')
);
dropZone.addEventListener('drop', (e) => {
    document.getElementById('imagenUpload').files = e.dataTransfer.files;
    document.getElementById('btnSubirImagen').click();
});

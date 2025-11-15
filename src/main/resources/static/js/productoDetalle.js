/**
 * productoDetalle.js
 * Lógica de interactividad para la página de detalle de producto
 * Separa completamente la lógica de negocio del HTML
 */

document.addEventListener('DOMContentLoaded', function() {
    const btnWhatsApp = document.getElementById('btnWhatsApp');
    if (btnWhatsApp) {
        btnWhatsApp.addEventListener('click', abrirWhatsApp);
    }
});

/**
 * Genera el link de WhatsApp llamando al backend
 * La lógica de negocio está centralizada en el controlador
 */
function abrirWhatsApp() {
    const productoId = document.getElementById('btnWhatsApp').getAttribute('data-producto-id');
    
    // Llamar al endpoint del backend que genera el link
    fetch(`/user/api/whatsapp-link/${productoId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Error al obtener el link de WhatsApp');
            }
            return response.json();
        })
        .then(data => {
            if (data.whatsappLink) {
                window.open(data.whatsappLink, '_blank');
            } else if (data.error) {
                alert('Error: ' + data.error);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('No se pudo generar el link de WhatsApp. Por favor, intenta más tarde.');
        });
}

    package com.multipagos.p2p_backend.backend.dto;

    public class MarcarPagoIniciadoRequest {
        private String urlComprobante;

        // Constructor vacío
        public MarcarPagoIniciadoRequest() {
        }

        // Constructor con todos los campos
        public MarcarPagoIniciadoRequest(String urlComprobante) {
            this.urlComprobante = urlComprobante;
        }

        // Getter
        public String getUrlComprobante() {
            return urlComprobante;
        }

        // Setter
        public void setUrlComprobante(String urlComprobante) {
            this.urlComprobante = urlComprobante;
        }
    }
    
// SPDX-FileCopyrightText: 2026 David Ruiz Duarte (viajatech) <https://viajatech.com>
// SPDX-License-Identifier: LicenseRef-Agente-Isabella-1.0
// Agente Isabella · Autor: David Ruiz Duarte (viajatech). Uso no comercial y con atribución: ver LICENSE y NOTICE.
package com.viajatech.lab.isabellallamadas;

import android.content.Intent;
import android.net.Uri;
import android.telecom.Call;
import android.telecom.CallAudioState;
import android.telecom.InCallService;
import android.telecom.VideoProfile;

/**
 * InCallService OPCIONAL y NO visual (meta-data android.telecom.IN_CALL_SERVICE_UI = false).
 *
 * El sistema solo liga este servicio si el appop MANAGE_ONGOING_CALLS está concedido (por adb en
 * el cel de pruebas de David). Cuando está ligado sirve para cuatro cosas que la ruta clásica no da
 * bien: empezar la conversación en Call.STATE_ACTIVE real (al contestar, no al empezar a marcar),
 * el tope de timbre (DIALING sin ACTIVE en max_timbre_s → cuelga antes del buzón), poner el altavoz
 * con setAudioRoute(ROUTE_SPEAKER) (salvo con `--es bocina 0`, 2.6: entonces no se toca la ruta y solo se
 * anota la que puso Telecom, y cada cambio) y colgar con Call.disconnect().
 *
 * Solo sigue la llamada SALIENTE de la app: con el appop, el sistema también le avisa de las entrantes
 * (o de una en espera), y esas se ignoran para no colgar ni confundir la de Isabella.
 *
 * Cambio de Alex8729: si «contestar» está activado (config --es contestar 1) y el número que llama está en la lista
 * de permitidos (config --es permitidos "…"), una entrante que timbra se le pasa a LlamadaService, que abre la
 * sesión de Isabella y la contesta con contestarEntrante(). Las demás entrantes se siguen ignorando.
 *
 * Si el appop no está concedido, el sistema no liga esto y la app funciona igual que antes por la
 * ruta clásica de LlamadaService (OFFHOOK + altavoz al marcar). LlamadaService.EstadoLlamada
 * (TelephonyCallback) sigue como fusible de colgado independiente aunque esto reporte DISCONNECTED.
 */
public class ServicioEnLlamada extends InCallService {

    // volatile: los escribe el hilo principal (callbacks del sistema) y los lee el hilo de acción (hallazgo 4).
    private static volatile ServicioEnLlamada instancia;
    private static volatile Call llamada;

    static boolean enLlamada() {
        return instancia != null && llamada != null;
    }

    // setAudioRoute está deprecado en API 34 a favor de requestCallEndpointChange, pero sigue
    // siendo el camino directo y funcional para forzar el altavoz; se usa a propósito.
    @SuppressWarnings("deprecation")
    static void rutaAltavoz() {
        ServicioEnLlamada s = instancia;
        if (s != null) {
            try {
                s.setAudioRoute(CallAudioState.ROUTE_SPEAKER);
            } catch (Exception e) {
                Bitacora.log("InCallService: setAudioRoute falló: " + e.getMessage());
            }
        }
    }

    /** 2.6: la ruta de audio que tiene la llamada según Telecom (para la bitácora con bocina 0). */
    static String rutaActual() {
        ServicioEnLlamada s = instancia;
        if (s == null) return "InCallService no ligado";
        try {
            return describir(s.getCallAudioState());
        } catch (Exception e) {
            return "no se pudo leer (" + e.getMessage() + ")";
        }
    }

    private static String describir(CallAudioState a) {
        if (a == null) return "desconocida";
        return CallAudioState.audioRouteToString(a.getRoute())
                + (a.getActiveBluetoothDevice() != null ? " (Bluetooth activo)" : "")
                + (a.isMuted() ? " · silenciada" : "");
    }

    /** Alex8729: contesta la entrante que se le pasó a LlamadaService. false si ya no está timbrando. */
    static boolean contestarEntrante() {
        Call c = llamada;
        if (c == null || c.getState() != Call.STATE_RINGING) return false;
        try {
            c.answer(VideoProfile.STATE_AUDIO_ONLY);
            return true;
        } catch (Exception e) {
            Bitacora.log("InCallService: answer falló: " + e.getMessage());
            return false;
        }
    }

    /** Alex8729: deja de seguir una entrante que al final no se contesta (otra sesión en curso): sigue timbrando normal. */
    static void soltarEntrante() {
        ServicioEnLlamada s = instancia;
        Call c = llamada;
        if (s == null || c == null || !esEntrante(c)) return;
        try {
            c.unregisterCallback(s.callback);
        } catch (Exception ignorado) {
        }
        llamada = null;
    }

    static void colgar() {
        Call c = llamada;
        if (c != null) {
            try {
                c.disconnect();
            } catch (Exception e) {
                Bitacora.log("InCallService: disconnect falló: " + e.getMessage());
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instancia = this;
        Bitacora.log("InCallService ligado (appop MANAGE_ONGOING_CALLS concedido).");
    }

    @Override
    public void onDestroy() {
        instancia = null;
        llamada = null;
        Bitacora.log("InCallService desligado.");
        super.onDestroy();
    }

    @Override
    public void onCallAdded(Call call) {
        super.onCallAdded(call);
        if (esEntrante(call)) {
            if (!quiereContestar(call)) return;
            llamada = call;
            call.registerCallback(callback);
            String numero = numeroDe(call);
            Bitacora.log("InCallService: llamada entrante de un número permitido; se la paso a Isabella.");
            try {
                Intent i = new Intent(this, LlamadaService.class)
                        .putExtra(LlamadaService.EXTRA_CMD, "contestar")
                        .putExtra(LlamadaService.EXTRA_NUMERO, numero);
                startForegroundService(i);
            } catch (Exception e) {
                Bitacora.log("InCallService: no pude arrancar a Isabella para la entrante (" + e.getMessage() + "); la dejo timbrar.");
                soltarEntrante();
            }
            return;
        }
        llamada = call;
        call.registerCallback(callback);
        int estado = call.getState();
        Bitacora.log("InCallService: llamada agregada (estado " + nombreEstado(estado) + ").");
        if (estado == Call.STATE_ACTIVE) LlamadaService.alActivaReal();
        else if (estado == Call.STATE_DIALING) LlamadaService.alMarcandoReal();
    }

    @Override
    public void onCallRemoved(Call call) {
        super.onCallRemoved(call);
        if (call != llamada) return; // una entrante ignorada, o una que ya no se sigue
        try {
            call.unregisterCallback(callback);
        } catch (Exception ignorado) {
        }
        llamada = null;
        Bitacora.log("InCallService: llamada quitada.");
        LlamadaService.alColgadaReal("la línea se desconectó (InCallService)");
    }

    /** 2.6: cada cambio de ruta de la llamada que la app sigue queda en la bitácora (p. ej. BLUETOOTH → SPEAKER). */
    @Override
    @SuppressWarnings("deprecation")
    public void onCallAudioStateChanged(CallAudioState audioState) {
        super.onCallAudioStateChanged(audioState);
        if (llamada != null) Bitacora.log("InCallService: ruta de audio → " + describir(audioState) + ".");
    }

    /** Alex8729: ¿Isabella debe contestar esta entrante? Solo si está activado, timbra, no hay otra llamada que
     *  se siga y el número está en la lista de permitidos. Las demás siguen como cualquier llamada. */
    private boolean quiereContestar(Call call) {
        if (!Ajustes.contestar(this)) {
            Bitacora.log("InCallService: llamada entrante; «contestar» está apagado, la ignoro.");
            return false;
        }
        if (call.getState() != Call.STATE_RINGING || llamada != null) {
            Bitacora.log("InCallService: llamada entrante que no timbra o con otra en curso; la ignoro.");
            return false;
        }
        if (!Ajustes.numeroPermitido(Ajustes.permitidos(this), numeroDe(call))) {
            Bitacora.log("InCallService: llamada entrante de un número que no está en la lista de permitidos; la ignoro.");
            return false;
        }
        return true;
    }

    private static String numeroDe(Call call) {
        try {
            Uri u = call.getDetails().getHandle();
            return u != null ? u.getSchemeSpecificPart() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private static boolean esEntrante(Call call) {
        try {
            Call.Details d = call.getDetails();
            return d != null && d.getCallDirection() == Call.Details.DIRECTION_INCOMING;
        } catch (Exception e) {
            return false;
        }
    }

    private final Call.Callback callback = new Call.Callback() {
        @Override
        public void onStateChanged(Call call, int estado) {
            Bitacora.log("InCallService: estado → " + nombreEstado(estado) + ".");
            if (estado == Call.STATE_ACTIVE) {
                LlamadaService.alActivaReal();
            } else if (estado == Call.STATE_DIALING) {
                LlamadaService.alMarcandoReal(); // marcando o timbrando: arma el tope de timbre
            } else if (estado == Call.STATE_DISCONNECTED) {
                LlamadaService.alColgadaReal("la línea se desconectó (InCallService)");
            }
        }
    };

    private static String nombreEstado(int e) {
        switch (e) {
            case Call.STATE_NEW: return "NEW";
            case Call.STATE_CONNECTING: return "CONNECTING";
            case Call.STATE_DIALING: return "DIALING";
            case Call.STATE_RINGING: return "RINGING";
            case Call.STATE_ACTIVE: return "ACTIVE";
            case Call.STATE_HOLDING: return "HOLDING";
            case Call.STATE_DISCONNECTING: return "DISCONNECTING";
            case Call.STATE_DISCONNECTED: return "DISCONNECTED";
            default: return "estado " + e;
        }
    }
}

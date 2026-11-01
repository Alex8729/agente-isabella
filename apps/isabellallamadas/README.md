# Isabella Llamadas (app Android)

> Agente Isabella — creado por David Ruiz Duarte (viajatech) · https://viajatech.com

App en Java, sin Gradle (minSdk 34 / Android 14+). Compila con `../../compilar.sh isabellallamadas` e instala con
`../../instalar.sh isabellallamadas`. Configúrala con `../../configurar_app.sh`.

| Archivo | Qué hace |
|---|---|
| `LlamadaService.java` | Servicio principal: órdenes por adb/UI, marcado, fusibles, bitácora |
| `SesionGemini.java` · `ClienteWebSocket.java` | Sesión de Gemini Live por WebSocket |
| `Tokens.java` · `Ajustes.java` · `ArchivoSecreto.java` | Token del Worker, configuración guardada, secreto del aparato |
| `audio/SumideroRaiz.java` · `audio/FuenteRaiz.java` · `audio/RaizUtil.java` | Ruta raíz: tinyplay/tinycap vía `su` |
| `MainActivity.java` · `Confirmacion.java` | Pantalla, confirmación de 3 s contra toques accidentales |
| `ServicioEnLlamada.java` | InCallService (permiso `MANAGE_ONGOING_CALLS`) |
| `magisk_modulo/` | Opcional: instalar la app como privilegiada con Magisk |
| `pruebas_mac/` | Pruebas en la Mac sin celular ni cuota |

Órdenes útiles (`adb shell am start-foreground-service -n com.viajatech.lab.isabellallamadas/.LlamadaService …`):
`--es cmd isabella` (platicar sin llamada) · `--es cmd llamar --es numero <TU_NUMERO> --es raiz 1 --es saludar 1` ·
`--es cmd colgar` · `--es cmd config --es tokenurl <URL> --es maxmin 3 --es modo servicios` · extras `--es grabar 1`,
`--es bocina 0`, `--es ganancia_tts 45`, `--es preenfasis 12`, `--es hpf_hz 100`.

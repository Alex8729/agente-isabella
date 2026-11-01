<!-- 🌐 REPO PÚBLICO · agente-isabella · versión para redes, SIN llaves ni datos privados. El laboratorio privado de trabajo es otro repositorio. -->

> **🌐 REPOSITORIO PÚBLICO** — versión abierta y limpia de *Agente Isabella*: sin llaves, sin tokens y sin datos privados.
> Para que funcione, **tú pones tus propias llaves** (se explica paso a paso).

<h1 align="center">📞 Agente Isabella</h1>

<p align="center">
  <img alt="Agente Isabella — arte de viajatech" src="docs/img/portada-ciudad.jpg" width="330">
</p>

<p align="center">
  <b>Convierte un celular Android en una asistente de voz con IA que hace llamadas telefónicas reales.</b><br>
  Gemini Live (voz «Zephyr») · app Android propia · root con Magisk · Worker de Cloudflare para las llaves.<br>
  Creado por <b>David Ruiz Duarte</b> · <a href="https://viajatech.com"><b>viajatech</b></a>
</p>

<p align="center">
  <img alt="Android 14+" src="https://img.shields.io/badge/Android-14%2B-3DDC84?logo=android&logoColor=white">
  <img alt="Gemini Live" src="https://img.shields.io/badge/Gemini-Live%20%C2%B7%20Zephyr-8E75B2?logo=googlegemini&logoColor=white">
  <img alt="Magisk" src="https://img.shields.io/badge/root-Magisk-00AF9C">
  <img alt="Cloudflare Workers" src="https://img.shields.io/badge/Cloudflare-Workers-F38020?logo=cloudflare&logoColor=white">
  <img alt="Sin Gradle" src="https://img.shields.io/badge/build-sin%20Gradle-555">
  <img alt="Licencia" src="https://img.shields.io/badge/licencia-no%20comercial%20%2B%20atribuci%C3%B3n-blue">
</p>

---

## ¿Qué es esto?

**Isabella Llamadas** es una app Android que marca un número desde **tu propia línea** y conversa en tiempo real con
la persona que contesta, usando la voz en vivo de **Google Gemini**. Sin Twilio, sin números rentados, sin VoIP de
terceros: tu celular, tu SIM, tu llamada.

La voz de Isabella entra **digitalmente** al canal de la llamada (no por la bocina), así que se oye limpia y sin eco.
Para eso el celular necesita **root con Magisk**: Android no deja que una app normal meta audio a una llamada.

| Lo que hace | Cómo |
|---|---|
| 🗣️ Conversa en vivo por teléfono | Gemini Live por WebSocket, audio PCM 16/24 kHz |
| 🔐 Tus llaves nunca viven en el cel | Un Worker de Cloudflare (gratis) entrega tokens efímeros de un solo uso |
| 🤖 Se presenta SIEMPRE como IA | Primera frase obligatoria: «Soy una inteligencia artificial…» |
| ⏱️ No gasta de más | 8 fusibles: techo por llamada, topes por minuto/día, cuelga en silencio, nunca reconecta… |
| 📵 Contra toques accidentales | Confirmación de 3 s antes de marcar |
| 🎚️ Audio afinable | Volumen TTS, pre-énfasis, filtro de graves, soft-clip |
| 💬 Modo sin llamada | Platica con Isabella por el micrófono y la bocina del cel (no requiere root) |
| 🧪 Pruebas sin gastar | Gemini falso y Worker local para probar todo en la Mac |

## ¿Cómo lo instalo? (con tu agente de IA)

Este repo está escrito para que **tu agente de IA te guíe** (Claude Code, Codex, Gemini CLI, Copilot, Cursor…):

```bash
git clone https://github.com/viajatech/agente-isabella.git
cd agente-isabella
# Abre aquí tu agente y escríbele:
#   «Lee AGENTS.md y guíame paso a paso para instalar Isabella Llamadas en mi celular.»
```

El agente hace lo de la computadora; **tú** haces lo que solo una persona puede hacer: tocar el celular, apretar los
botones de volumen y encendido, aceptar borrados y escribir tus contraseñas. En cada uno de esos momentos el agente
**se detiene, te explica y espera**.

### Lo que necesitas

| | |
|---|---|
| 📱 Un celular Android **14 o más nuevo** que puedas rootear | Probado en **Samsung Galaxy A72** (Qualcomm). Úsalo de pruebas: el root **borra todo**. |
| 💻 Una Mac (Apple Silicon o Intel) | Los scripts están hechos para macOS + Homebrew. |
| 🔌 Un cable USB **de datos** | Uno que solo carga no sirve. |
| 🔑 Tu llave de **Google AI Studio** | Se crea gratis en <https://aistudio.google.com/apikey>. Gemini tiene nivel gratuito con límites; algunos usos pueden cobrarse: revisa <https://ai.google.dev/pricing>. |
| ☁️ Una cuenta de **Cloudflare** | Gratis. Ahí vive el Worker que guarda tu llave. |
| 📞 Un segundo teléfono **tuyo** | Para las pruebas: Isabella llama a ese número. |

### El camino, en 8 fases

```
0 Diagnóstico ─ 1 Preparar la Mac ─ 2 Preparar el cel ─ 3 Llaves y Worker
4 Compilar e instalar (ya puedes platicar con Isabella sin llamada)
5 Root con Magisk 🛑 ─ 6 Configurar la app ─ 7 Primera llamada 🛑 ─ 8 Afinar el audio
```

🛑 = fases donde **tú** tienes el celular en la mano. Todo el detalle está en **[AGENTS.md](AGENTS.md)**.

## Dónde van TUS llaves (y dónde NO)

| Qué | Dónde va | Cómo |
|---|---|---|
| Llave de Gemini (AI Studio) | **Solo** en el Worker de Cloudflare | `npx wrangler secret put GEMINI_API_KEY_AQ` (la pegas tú en tu Terminal) |
| Secreto del aparato | En el Worker y en la app | `./configurar_app.sh` lo genera y lo pasa por tubería, sin mostrarlo |
| URL de tu Worker | `mi_config.env` (local, fuera de git) | `cp mi_config.env.example mi_config.env` |
| Llave de firma del APK | `llaves/` (local, fuera de git) | `./compilar.sh` la crea la primera vez; su contraseña va al Llavero de macOS |

> ⚠️ **Nunca** pegues una llave en un chat, en un archivo que vaya a git ni en la línea de `adb shell`.
> Antes de publicar tu copia, corre `python3 herramientas/escanear_secretos.py .`

## Estructura

```
apps/isabellallamadas/   App Android (Java, sin Gradle): llamada, audio raíz, Gemini Live, UI
  magisk_modulo/         Módulo Magisk opcional (app privilegiada)
  pruebas_mac/           Pruebas de la app en la Mac, sin cel ni cuota
token-worker/            Worker de Cloudflare: guarda tu llave y acuña tokens efímeros
puente/                  Persona de Isabella (EDITABLE), generador de setup y Gemini falso
herramientas/            Diagnóstico de la ruta raíz (prueba 0r) y escáner de secretos
compilar.sh · instalar.sh · preparar_mac.sh · vincular_cel.sh · configurar_app.sh
docs/                    Arquitectura, problemas resueltos y rescate si el cel no arranca tras el root
  img/                   Arte del proyecto y la imagen de vista previa para redes
```

## Personaliza a Isabella

Edita la sección **«TU CONFIGURACIÓN»** de [`puente/lib/isabella.mjs`](puente/lib/isabella.mjs): a nombre de quién
llama, qué sabe (tu negocio, tu proyecto) y la voz. Luego `cd token-worker && npm run genera-setup && npx wrangler deploy`.
El celular no se toca: la persona viaja dentro de cada token.

## Uso responsable

Isabella **siempre** dice que es una IA. Llama **solo** a números tuyos o de personas que aceptaron. Nada de spam,
fraude, suplantación ni grabaciones sin aviso. Lee **[USO_RESPONSABLE.md](USO_RESPONSABLE.md)**: el mal uso revoca la
licencia.

## Licencia y créditos

**Licencia Agente Isabella 1.0** — atribución obligatoria · uso no comercial · uso responsable ([LICENSE](LICENSE)).
Si lo usas, lo compartes o haces un video, da crédito:

> **Agente Isabella — creado por David Ruiz Duarte (viajatech)** · <https://viajatech.com> · <https://github.com/viajatech/agente-isabella>

Sin garantía: rootear y flashear puede borrar tus datos o dañar tu equipo. No nos hacemos responsables del uso que le des.

El arte de este repositorio (`docs/img/`) es de **viajatech** y queda cubierto por la misma licencia: no lo uses
para otra cosa ni le quites el crédito.

### Sigue a viajatech

[🌐 viajatech.com](https://viajatech.com) ·
[🎙️ Isabella en vivo](https://viajatech.com/isabella) ·
[▶️ YouTube](https://www.youtube.com/@viajatech) ·
[📸 Instagram](https://www.instagram.com/viajatech/) ·
[🎵 TikTok](https://www.tiktok.com/@viajatech) ·
[𝕏 X](https://x.com/viajatech) ·
[🧵 Threads](https://www.threads.com/@viajatech) ·
[🐙 GitHub](https://github.com/viajatech)

---

<details>
<summary><b>English summary</b></summary>

**Agente Isabella** turns a rooted Android phone (Android 14+) into an AI voice assistant that places **real phone
calls** over your own SIM, powered by **Google Gemini Live** (voice "Zephyr"). Audio is injected digitally into the
call through the Qualcomm audio mixers via Magisk root (`tinyplay`/`tinycap`), so there's no echo and no speakerphone.
Your Gemini API key never touches the phone: a free **Cloudflare Worker** stores it and mints single-use ephemeral
tokens. The app is built **without Gradle or Android Studio** (aapt2 → javac → d8 → apksigner) on macOS.
Clone it, open your AI coding agent and say: *"Read AGENTS.md and guide me step by step."* Bring your own keys.
Non-commercial use with mandatory attribution to **David Ruiz Duarte (viajatech)**; misuse revokes the license.
Tested on a Samsung Galaxy A72 (Snapdragon); other phones may need mixer adaptation (see `docs/ARQUITECTURA.md`).

Keywords: AI phone call agent, Gemini Live Android, AI voice assistant phone calls, Magisk root audio injection,
VoLTE AI caller, Android without Gradle, Cloudflare Worker ephemeral tokens, vibe coding.
</details>

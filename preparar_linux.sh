#!/bin/bash
# SPDX-FileCopyrightText: 2026 David Ruiz Duarte (viajatech) <https://viajatech.com>
# SPDX-License-Identifier: LicenseRef-Agente-Isabella-1.0
# Agente Isabella · Autor: David Ruiz Duarte (viajatech). Uso no comercial y con atribución: ver LICENSE y NOTICE.
# Modificado por Alex8729: versión para Linux y para Linux dentro de Windows (WSL, Ubuntu/Debian) de preparar_mac.sh.
# Prepara la computadora para compilar Agente Isabella. Se puede correr las veces que sea: solo instala lo que falta
# y al final verifica todo.
#   ./preparar_linux.sh --verificar          solo revisa, no instala nada
#   ./preparar_linux.sh --acepto-licencia    instala lo que falte (pide tu contraseña de Linux para apt); aceptar la
#                                            licencia del SDK de Android (Google) es decisión tuya, por eso va como
#                                            bandera explícita
set -uo pipefail

MODO="${1:-}"
RAIZ="$(cd "$(dirname "$0")" && pwd)"
source "$RAIZ/comun.sh"
SDK="$(ruta_sdk)"
PAQUETES=("platforms;android-34" "build-tools;34.0.0" "platform-tools")
# Herramientas de línea de comandos de Android para Linux (página oficial: https://developer.android.com/studio#command-tools)
CMDLINE_URL="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
SDKMANAGER="$SDK/cmdline-tools/latest/bin/sdkmanager"
falla=0

if es_mac; then echo "Estás en una Mac: usa ./preparar_mac.sh"; exit 2; fi

if [ "$MODO" = "--acepto-licencia" ]; then
    command -v apt-get >/dev/null || { echo "⛔ Este script usa apt (Ubuntu/Debian). En otra distribución instala a mano: Java 17, unzip, zip, curl, openssl, Node.js 18+"; exit 1; }
    FALTAN=()
    command -v javac >/dev/null && javac -version 2>&1 | grep -qE 'javac (1[7-9]|2[0-9])' || FALTAN+=(openjdk-17-jdk-headless)
    for p in unzip zip curl openssl; do command -v "$p" >/dev/null || FALTAN+=("$p"); done
    if [ "${#FALTAN[@]}" -gt 0 ]; then
        echo "→ Instalando con apt: ${FALTAN[*]} (pide tu contraseña de Linux)"
        sudo apt-get update && sudo apt-get install -y "${FALTAN[@]}" || { echo "⛔ falló apt"; exit 1; }
    fi
    if [ ! -x "$SDKMANAGER" ]; then
        echo "→ Bajando las herramientas de línea de comandos de Android…"
        dl="$(mktemp -d)"
        curl -fL --retry 3 -o "$dl/cmdline.zip" "$CMDLINE_URL" || { echo "⛔ no se pudo bajar $CMDLINE_URL"; exit 1; }
        mkdir -p "$SDK/cmdline-tools" && unzip -q "$dl/cmdline.zip" -d "$dl/x" && rm -rf "$SDK/cmdline-tools/latest" \
            && mv "$dl/x/cmdline-tools" "$SDK/cmdline-tools/latest" || { echo "⛔ no se pudo descomprimir"; exit 1; }
    fi
    # OJO: `yes | sdkmanager` con pipefail mata el script (yes muere por SIGPIPE). Por eso aquí no hay set -e
    # y la licencia se comprueba después por su archivo.
    yes | "$SDKMANAGER" --sdk_root="$SDK" --licenses >/dev/null 2>&1
    "$SDKMANAGER" --sdk_root="$SDK" "${PAQUETES[@]}" >/dev/null
    command -v npx >/dev/null || echo "• Falta Node.js 18+ (para el Worker). Recomendado: https://github.com/nvm-sh/nvm y luego «nvm install --lts»."
elif [ "$MODO" != "--verificar" ]; then
    echo "Uso: ./preparar_linux.sh --verificar | --acepto-licencia"; exit 2
fi

echo "== Verificación"
revisa() { if eval "$2" >/dev/null 2>&1; then echo "✓ $1"; else echo "✗ $1"; falla=1; fi; }
revisa "Java 17 o más nuevo (javac)"    "javac -version 2>&1 | grep -qE 'javac (1[7-9]|2[0-9])'"
revisa "unzip, zip, curl y openssl"     "command -v unzip && command -v zip && command -v curl && command -v openssl"
revisa "Herramientas de Android"        "[ -x '$SDKMANAGER' ]"
revisa "Licencia del SDK aceptada"      "[ -f '$SDK/licenses/android-sdk-license' ]"
revisa "Plataforma Android 14"          "[ -f '$SDK/platforms/android-34/android.jar' ]"
revisa "build-tools 34 (aapt2, d8…)"    "[ -x '$SDK/build-tools/34.0.0/aapt2' ] && [ -f '$SDK/build-tools/34.0.0/core-lambda-stubs.jar' ]"
revisa "Node.js (para el Worker)"       "command -v npx"
if [ "$ADB_WINDOWS" = "1" ]; then
    echo "✓ adb: se usa el adb.exe de Windows (celular por cable USB)"
else
    revisa "adb" "command -v adb"
    es_wsl && echo "• En WSL, para usar el cable USB instala las platform-tools de Android en Windows y ponlas en el PATH de Windows (ver docs/WINDOWS_WSL.md)."
fi
[ -f "$RAIZ/llaves/agente-isabella.jks" ] && echo "✓ Llave de firma (tuya)" || \
    echo "• Sin llave de firma: compilar.sh crea una nueva, TUYA, la primera vez (respáldala después)"
exit $falla

# Agente Isabella en Windows (con Linux/WSL) o en Linux

> Agente Isabella — creado por David Ruiz Duarte (viajatech) · https://viajatech.com · https://github.com/viajatech/agente-isabella
> Esta guía y los cambios para Linux/WSL son una modificación de Alex8729 (ver `CAMBIOS.md`).

El proyecto original está hecho para Mac. Con estos cambios también se puede compilar e instalar desde **Linux** o desde
**Linux dentro de Windows (WSL, Ubuntu)**. Todo lo demás de `AGENTS.md` sigue igual; aquí solo cambian las fases 0, 1,
2 y 4.

## Qué cambia respecto a la Mac

| En Mac | En Linux / WSL |
|---|---|
| `./preparar_mac.sh` | `./preparar_linux.sh --verificar` y luego `./preparar_linux.sh --acepto-licencia` (pide tu contraseña de Linux para `apt`) |
| Java 17 Temurin en `~/Library/Java` | Java 17 del sistema (`openjdk-17-jdk-headless`) |
| SDK de Android de Homebrew | SDK en `~/Android/Sdk` (o el que diga `ANDROID_HOME`) |
| Contraseña de la llave de firma en el Llavero de macOS | En `llaves/.contrasena`, que solo tu usuario puede leer y nunca se sube a git. **Respalda toda la carpeta `llaves/`.** |
| Node.js con Homebrew | Node.js 18 o más nuevo; recomendado con [nvm](https://github.com/nvm-sh/nvm): `nvm install --lts` |

## Conectar el celular desde WSL

En WSL el cable USB lo ve **Windows**, no Linux. Hay dos caminos; los scripts detectan solos el primero:

1. **Por cable, con el adb de Windows (recomendado).** Baja las *SDK Platform-Tools* para Windows desde
   <https://developer.android.com/tools/releases/platform-tools>, descomprímelas (por ejemplo en `C:\platform-tools`) y
   agrega esa carpeta al PATH de Windows (Configuración › Sistema › Información › Configuración avanzada del sistema ›
   Variables de entorno › Path). Cierra y abre la terminal de Ubuntu y prueba `adb.exe devices`. Si responde, los
   scripts usan `adb.exe` automáticamente.
2. **Por Wi-Fi, con el adb de Linux.** En el cel: Opciones de desarrollador › Depuración inalámbrica › «Vincular
   dispositivo con código». Corre `ADB_WINDOWS=0 ./vincular_cel.sh <código> <ip:puerto>` con la IP y el puerto que
   muestra esa ventana, y luego `adb connect <ip:puerto>` con los de la pantalla principal de Depuración inalámbrica.
   En WSL la búsqueda automática (mDNS) casi nunca funciona; por eso se pasan a mano.

## Pasos

```bash
git clone https://github.com/Alex8729/agente-isabella.git
cd agente-isabella
./preparar_linux.sh --verificar
./preparar_linux.sh --acepto-licencia   # aceptar la licencia del SDK de Android es decisión tuya
# Fase 3 (Worker de Cloudflare) igual que en AGENTS.md. «npx wrangler login» abre el navegador de Windows.
./compilar.sh isabellallamadas
./instalar.sh isabellallamadas
./configurar_app.sh
```

## Root en Windows

La herramienta para flashear depende de la marca: Samsung usa Odin y Xiaomi/POCO usa Mi Unlock para desbloquear el
gestor de arranque; las dos son programas de Windows. Sigue la guía oficial de Magisk
(<https://topjohnwu.github.io/Magisk/install.html>) y las reglas de la fase 5 de `AGENTS.md`: desbloquear **borra
todo el celular**.

# Cambios respecto al original

Esta copia es una **obra derivada** de «Agente Isabella» — creado por David Ruiz Duarte (viajatech) ·
https://viajatech.com · https://github.com/viajatech/agente-isabella (sección 3 de `LICENSE`).
No está respaldada ni patrocinada por viajatech.

Cambios de Alex8729:

- **Soporte para Linux y Windows con WSL** (2026-11-06): `preparar_linux.sh` nuevo; `comun.sh`, `compilar.sh`,
  `instalar.sh`, `vincular_cel.sh` y `herramientas/llamadas/comun_llamadas.sh` ahora funcionan fuera de macOS (ruta
  del SDK, Java, contraseña de la llave de firma, adb de Windows desde WSL, `stat`). En Mac todo sigue igual. Guía en
  `docs/WINDOWS_WSL.md`.

# puente — persona de Isabella y herramientas de prueba

> Agente Isabella — creado por David Ruiz Duarte (viajatech) · https://viajatech.com

- `lib/isabella.mjs` — **la persona** (prompt, voz «Zephyr», función `colgar`, VAD). Edita solo «TU CONFIGURACIÓN».
- `genera-setup.mjs` — genera `../token-worker/src/setup.json` (lo llama `npm run genera-setup` en `token-worker/`).
- `mock-live.mjs` — un Gemini Live falso para probar el Worker y la app sin gastar cuota (`npm install` aquí, una vez).

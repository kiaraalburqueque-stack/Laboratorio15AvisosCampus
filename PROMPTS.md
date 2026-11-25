# PROMPTS.md – Fase 2 (rama `mejora-ia`)

Mejoras elegidas (3):

1. **Dos canales** – `Notificador`: canal «Urgentes» con `IMPORTANCE_HIGH` (emergente) además de «Avisos»; el recordatorio usa el urgente.
2. **Texto largo** – `Notificador`: `BigTextStyle` muestra el texto completo al expandir la notificación.
3. **Receptor del cargador** – Receiver + Modelo + **ViewModel** + View: `ACTION_POWER_CONNECTED` / `ACTION_POWER_DISCONNECTED` con su propio switch. *Esta mejora cambia el ViewModel.*

> Asistente usado: IA generativa (el docente autorizó el uso de IA). La IA propuso; yo revisé cada
> cambio contra las reglas: nada de `Context`, `NotificationManager` ni `BroadcastReceiver` en
> `viewmodel/`, todo receptor se quita en `onDispose` y los `PendingIntent` son inmutables.

| Prompt que escribí | Qué respondió la IA | Qué cambié o descarté | Por qué |
|---|---|---|---|
| «Agrega un canal Urgentes con IMPORTANCE_HIGH y haz que el recordatorio lo use.» | Subir la importancia del canal «avisos» existente a `IMPORTANCE_HIGH`. | Descarté cambiar el canal existente: creé un canal nuevo `urgentes` y registré los dos con `createNotificationChannels`. | Android no deja cambiar la importancia de un canal ya creado (el usuario la controla). Si la app ya estaba instalada, el cambio no tendría efecto. |
| «¿Dónde decido qué canal usa cada aviso?» | Pasar un `Context` al ViewModel para que elija el canal y llame a `notify`. | El ViewModel solo emite el aviso con su `origen`; `Notificador.canalDe(aviso)` elige el canal (RECORDATORIO → Urgentes). | `Context` está prohibido en el ViewModel. El origen ya dice qué tipo de aviso es, así que no hace falta otro dato. |
| «En Android 7 el recordatorio no sale emergente.» | Explicó que antes de Android 8 no hay canales y la importancia la da `setPriority`. | Agregué `PRIORITY_HIGH` para el recordatorio y `PRIORITY_DEFAULT` para el resto. | Con `minSdk = 24` la app corre en Android 7, donde el canal no existe y se ignoraría. |
| «Muestra el texto completo de la notificación al expandirla con BigTextStyle.» | `setStyle(BigTextStyle().bigText(texto))` reemplazando `setContentText`. | Mantuve `setContentText` (una línea al estar contraída) y en `bigText` agregué el origen y la hora. | Sin `setContentText`, la notificación contraída sale sin texto. Los textos ficticios son cortos: con origen y hora se nota la diferencia al expandir. |
| «Escucha cuando se conecta o desconecta el cargador y avisa, con su propio switch.» | Declarar el receptor en el `AndroidManifest.xml`. | Lo registré en la pantalla con `DisposableEffect(escuchandoCargador)` y `RECEIVER_NOT_EXPORTED`, y lo quito en `onDispose`. | Desde Android 8, un receptor del manifiesto no recibe `ACTION_POWER_CONNECTED`. Además, el switch debe poder apagarlo, igual que el del modo avión. |
| «Revisa si algo de la mejora rompe la arquitectura.» | Sugirió guardar el receptor como propiedad del ViewModel para no perderlo al rotar. | Descarté la idea: el receptor vive en la pantalla; el ViewModel solo guarda `escuchandoCargador` (Boolean). | `BroadcastReceiver` está prohibido en el ViewModel. Al rotar, `onDispose` lo quita y el `DisposableEffect` lo registra de nuevo porque el switch sigue en `true`. |

## Lo que revisé en todo lo generado

- `grep` en `viewmodel/`: sin `Context`, `NotificationManager` ni `BroadcastReceiver`.
- Cada `registerReceiver` tiene su `unregisterReceiver` en `onDispose`.
- El único `PendingIntent` usa `FLAG_IMMUTABLE`.
- `onReceive` de los dos receptores solo traduce el anuncio a `true`/`false`.

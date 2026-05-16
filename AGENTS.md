# AGENTS.md

Reglas para futuras tareas en Javatekk:

- Mantener Java 17.
- Mantener libGDX + LWJGL3 para escritorio local en Windows y macOS.
- Mantener `core` para codigo compartido del juego y `lwjgl3` para el launcher desktop.
- Mantener la clase principal del juego en `javatekk.engine.JavatekkGame`.
- Mantener el launcher desktop en `javatekk.lwjgl3.Lwjgl3Launcher`.
- No introducir assets externos obligatorios.
- No agregar dependencias innecesarias.
- Preferir cambios pequenos, compilables y verificables.
- Si se reorganiza el codigo, mantener la logica compartida en `core/src/main/java`.
- No agregar fisicas, greedy meshing, texturas, instancing complejo ni sistemas de render avanzados hasta que se pida explicitamente.
- Si se agrega Gradle Wrapper, debe ser el wrapper oficial generado por Gradle; no inventar jars ni scripts equivalentes.

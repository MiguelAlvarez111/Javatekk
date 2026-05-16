# Javatekk MVP - Voxel World Demo

Javatekk es un motor/prototipo voxel en Java con libGDX + LWJGL3. La demo usa chunks de `16x16x16`, guarda los IDs de bloque en `byte[]`, aplica culling de caras ocultas y permite navegar el mundo en 3D con una camara fly mode.

## Requisitos

- JDK 17 o superior.
- Windows o macOS.
- Gradle Wrapper incluido en el repo.
- Conexion a internet en la primera compilacion para descargar dependencias de Gradle/Maven.

## Correr en macOS/Linux

```sh
./gradlew lwjgl3:run
```

## Correr en Windows

```bat
gradlew.bat lwjgl3:run
```

## Si no hay wrapper

Instala Gradle localmente y ejecuta:

```sh
gradle lwjgl3:run
```

## Controles

- `W`, `A`, `S`, `D`: moverse.
- Mouse: mirar alrededor.
- `Space`: subir.
- `Shift`: bajar.
- `ESC`: liberar o capturar el mouse.
- `F1`: mostrar u ocultar ayuda.

## Arquitectura

- `javatekk.world`: datos del mundo, chunks, bloques y generacion procedural.
- `javatekk.render`: construccion de mallas y render voxel.
- `javatekk.engine`: ciclo principal del juego, camara y HUD.
- `ByteChunkData`: almacenamiento compacto de IDs de bloque en `byte[]`.
- `BlockRegistry`: registro central de prototipos de bloque.
- `VoxelRenderer`: renderer que dibuja los chunks como mallas.
- `FlyCameraController`: camara de primera persona/fly mode.

## Optimizaciones actuales

- `byte[]` por chunk para guardar IDs de bloque.
- ID `0` reservado para `Air`.
- Hidden-face culling: no se generan caras entre bloques opacos.
- Un `Mesh` por chunk.
- No se crean cubos individuales ni se hace un draw call por bloque.
- Colores por vertice, sin texturas obligatorias.
- Niebla simple por shader para suavizar el horizonte.
- Limites de mundo y colision simple de camara contra bloques opacos.

## Trabajo futuro

- Greedy meshing.
- Frustum culling.
- Chunks infinitos o streaming de chunks.
- Colisiones y gravedad.
- Texturas.
- Culling vertical experimental.
- Buffers off-heap avanzados.

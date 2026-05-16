# Presentacion de Javatekk MVP

## Guion de 2 minutos

Hola, este proyecto se llama **Javatekk MVP - Voxel World Demo**. Es un prototipo de mundo voxel hecho en Java 17 con libGDX y LWJGL3 para escritorio.

En la demo se ve un mundo 3D pequeno, con terreno, arboles, una plataforma y una estructura simple. La idea no es tener todavia un juego completo, sino mostrar la base tecnica de un motor voxel: datos del mundo, render de bloques y navegacion en primera persona.

El mundo esta dividido en chunks de `16x16x16`. Cada bloque no se guarda como un objeto pesado, sino como un ID dentro de un `byte[]`. El ID `0` esta reservado para Air, y los demas IDs representan bloques como Grass, Dirt, Stone, Wood y Leaves. Los datos de cada bloque, como nombre, opacidad y color, viven en `BlockRegistry`.

Para renderizar, el proyecto no dibuja cubo por cubo. En cambio, construye un solo mesh por chunk. Tambien usa hidden-face culling: si una cara esta pegada contra otro bloque opaco, esa cara no se genera. Eso reduce mucho la cantidad de geometria comparado con dibujar todos los cubos completos.

La demo se puede recorrer con una camara fly mode. Uso WASD para moverme, el mouse para mirar, Space para subir, Shift para bajar, ESC para liberar o capturar el mouse y F1 para mostrar u ocultar la ayuda.

Lo que queda para una version futura seria agregar greedy meshing, frustum culling, chunks infinitos, colisiones, texturas y optimizaciones mas avanzadas de buffers. Pero para este MVP ya queda demostrada la base: almacenamiento compacto, chunks, culling de caras ocultas, un mesh por chunk y navegacion 3D.

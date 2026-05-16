package javatekk.world;

import java.util.Objects;

public final class Block {
    public final int id;
    public final String name;
    public final boolean opaque;
    public final float r;
    public final float g;
    public final float b;

    public Block(int id, String name, boolean opaque, float r, float g, float b) {
        if (id < 0 || id > 255) {
            throw new IllegalArgumentException("Block ID must be in range 0..255: " + id);
        }
        this.id = id;
        this.name = Objects.requireNonNull(name, "name");
        this.opaque = opaque;
        this.r = r;
        this.g = g;
        this.b = b;
    }
}

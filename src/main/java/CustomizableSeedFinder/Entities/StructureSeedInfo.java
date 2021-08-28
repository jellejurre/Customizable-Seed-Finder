package CustomizableSeedFinder.Entities;

import kaptainwutax.mcutils.rand.ChunkRand;
import kaptainwutax.mcutils.util.pos.BPos;

public class StructureSeedInfo {
    public ChunkRand chunkRand;
    public long structureSeed;
    public BPos[] ironLocations = new BPos[4];
    public BPos[] entryLocations = new BPos[4];
    public BPos[] entryNetherLocations = new BPos[4];
    public BPos[] fortressLocations = new BPos[4];
    public BPos[] bastionLocations = new BPos[4];
    public BPos[] fortressOverworldLocations = new BPos[4];
    public boolean[] strongholdsCloseEnough = new boolean[3];
    public StructureSeedInfo(long structureSeed, ChunkRand chunkRand){
        this.chunkRand = chunkRand;
        this.structureSeed = structureSeed;
    }
}

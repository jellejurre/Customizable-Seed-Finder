package CustomizableSeedFinder;

import static CustomizableSeedFinder.StructureSeedFunctions.checkStructureSeed;


import CustomizableSeedFinder.Entities.FilterFunction;
import CustomizableSeedFinder.Entities.StructureSeedInfo;
import CustomizableSeedFinder.Entities.WorldSeedInfo;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import kaptainwutax.biomeutils.source.NetherBiomeSource;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.mcutils.rand.ChunkRand;
import kaptainwutax.mcutils.rand.seed.StructureSeed;
import kaptainwutax.mcutils.rand.seed.WorldSeed;
import kaptainwutax.terrainutils.terrain.OverworldTerrainGenerator;
import nl.jellejurre.seedchecker.SeedChecker;
import nl.jellejurre.seedchecker.SeedCheckerDimension;

public class TestTask implements Runnable{

    public List<Integer> entries;
    public List<Long> times;
    public FilterFunction function;
    public TestTask(List<Integer> entries, List<Long> times, FilterFunction function) {
        this.entries = entries;
        this.times = times;
        this.function = function;
    }

    @Override
    public void run() {
        long structureSeed = WorldSeed.toStructureSeed(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE));
        StructureSeedInfo structureInfo = new StructureSeedInfo(structureSeed, new ChunkRand());
        while(!checkStructureSeed(structureInfo)){
            structureInfo = new StructureSeedInfo(++structureSeed, new ChunkRand());
        }
        boolean done = false;
        int biomeseed = 0;
        while (!done && biomeseed < 20) {
            WorldSeedInfo
                info =
                new WorldSeedInfo(StructureSeed.toWorldSeed(structureInfo.structureSeed, biomeseed),
                    structureInfo);
            int index = 0;
            for (int position = 0; position < info.structures.fortressLocations.length; position++) {
                if(info.structures.fortressLocations[position]!=null){
                    index = position;
                }
            }
            long time = System.nanoTime();
            if (function.predicate.test(index, info)) {
                entries.add(biomeseed);
                done = true;
            }
            if(!info.shouldContinue){
                done = true;
            }
            times.add(System.nanoTime() - time);
            biomeseed++;
        }
    }
}

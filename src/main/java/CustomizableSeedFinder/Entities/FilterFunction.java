package CustomizableSeedFinder.Entities;

import java.util.function.BiPredicate;

public class FilterFunction {
    public String name;
    public BiPredicate<Integer, WorldSeedInfo> predicate;
    public boolean needsOBS;
    public boolean needsNBS;
    public boolean needsOTG;
    public boolean needsOverworldChecker;
    public boolean needsNetherChecker;
    public int cutoff;
    public double averageTime;

    public FilterFunction(String name, BiPredicate<Integer, WorldSeedInfo> predicate, boolean needsOBS, boolean needsNBS, boolean needsOTG,
                          boolean needsOverworldChecker, boolean needsNetherChecker) {
        this.name = name;
        this.predicate = predicate;
        this.needsOBS = needsOBS;
        this.needsNBS = needsNBS;
        this.needsOTG = needsOTG;
        this.needsOverworldChecker = needsOverworldChecker;
        this.needsNetherChecker = needsNetherChecker;
    }
}

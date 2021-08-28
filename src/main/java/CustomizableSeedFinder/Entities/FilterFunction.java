package CustomizableSeedFinder.Entities;

import java.util.function.BiPredicate;

public class FilterFunction {
    public String name;
    public BiPredicate<Integer, WorldSeedInfo> predicate;
    public int cutoff;
    public double averageTime;

    public FilterFunction(String name, BiPredicate<Integer, WorldSeedInfo> predicate) {
        this.name = name;
        this.predicate = predicate;
    }
}

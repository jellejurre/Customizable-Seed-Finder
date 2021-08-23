package CustomizableSeedFinder.Util;

import java.util.ArrayList;
import java.util.Arrays;
import kaptainwutax.mcutils.util.pos.BPos;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import java.util.List;
import nl.jellejurre.seedchecker.SeedChecker;

public class Util {

    public static int getTopBlockCount(SeedChecker checker, Block block, Box box){
        return getTopBlockCount(checker, block, box, new ArrayList<>(Arrays.asList(Blocks.AIR, Blocks.WATER, Blocks.CAVE_AIR)));
    }

    public static int getTopBlockCount(SeedChecker checker, Block block, Box box, List<Block> above){
        int count = 0;
        List<Block> checkBlocks = new ArrayList<>(Arrays.asList(Blocks.AIR, Blocks.WATER, Blocks.CAVE_AIR));
        for (int x = (int)box.minX; x <= (int)box.maxX; x++) {
            for (int z = (int)box.minZ; z <= (int)box.maxZ; z++) {
                int y = 90;
                Block checkblock;
                BlockPos pos;
                do{
                    pos = new BlockPos(x, y, z);
                    checkblock = checker.getBlock(pos);
                    y--;
                }while(checkBlocks.contains(checkblock));
                if(checkblock==block&&above.contains(checker.getBlock(pos.add(0, 1, 0)))){
                    count++;
                }
            }
        }
        return count;
    }

    public static int goodEyeCount(SeedChecker checker, Box box){
        int count = 0;
        for (int x = (int)box.minX; x <= (int)box.maxX; x++) {
            for (int z = (int)box.minZ; z <= (int)box.maxZ; z++) {
                for (int y = 80; y > 0; y--) {
                    BlockState state = checker.getBlockState(x,y,z);
                    if(state.getBlock()==Blocks.END_PORTAL_FRAME&&((Boolean)state.getEntries().get(BooleanProperty.of("eye")))){
                        count++;
                    }
                }
            }
        }
        return count;
    }

    public static BlockPos locateBlockFromTop(SeedChecker checker, Block block, Box box){
        return locateBlockFromTop(checker, block, box, new ArrayList<>(Arrays.asList(Blocks.AIR, Blocks.WATER, Blocks.CAVE_AIR)));
    }

    public static BlockPos locateBlockFromTop(SeedChecker checker, Block block, Box box, List<Block> above){
        List<Block> checkBlocks = new ArrayList<>(Arrays.asList(Blocks.AIR, Blocks.WATER, Blocks.CAVE_AIR));
        for (int x = (int)box.minX; x <= (int)box.maxX; x++) {
            for (int z = (int)box.minZ; z <= (int)box.maxZ; z++) {
                int y = 90;
                Block checkblock;
                BlockPos pos;
                do{
                    pos = new BlockPos(x, y, z);
                    checkblock = checker.getBlock(pos);
                    y--;
                }while(checkBlocks.contains(checkblock));
                if(checkblock==block&&above.contains(checker.getBlock(pos.add(0, 1, 0)))){
                    return pos;
                }
            }
        }
        return new BlockPos(0, 0, 0);
    }
    public static boolean isArrayEmpty(BPos[] array){
        for (BPos bPos : array) {
            if(bPos!=null){
                return false;
            }
        }
        return true;
    }

    public static boolean isArrayFalse(boolean[] array){
        for (boolean bool : array) {
            if(bool){
                return false;
            }
        }
        return true;
    }

    public static int getArrayMax(int[] array){
        int max = 0;
        for (int i : array) {
            if(i>max){
                max = i;
            }
        }
        return max;
    }

    public static Box posToBox(BPos pos, int distance){
        return posToBox(pos, distance, 0, 255);
    }

    public static Box posToBox(BPos pos, int distance, int min, int max){
        return new Box(pos.getX()-distance, min, pos.getZ()-distance, pos.getX()+distance, max,
            pos.getZ()+distance);
    }
}

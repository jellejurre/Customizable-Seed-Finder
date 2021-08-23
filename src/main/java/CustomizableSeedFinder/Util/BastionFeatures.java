package CustomizableSeedFinder.Util;

import CustomizableSeedFinder.Config;
import kaptainwutax.featureutils.loot.LootContext;
import kaptainwutax.featureutils.loot.MCLootTables;
import kaptainwutax.featureutils.loot.item.ItemStack;
import kaptainwutax.featureutils.loot.item.Items;
import kaptainwutax.mcutils.rand.ChunkRand;
import kaptainwutax.mcutils.util.block.BlockMirror;
import kaptainwutax.mcutils.util.block.BlockRotation;
import kaptainwutax.mcutils.util.pos.BPos;
import kaptainwutax.mcutils.util.pos.CPos;
import kaptainwutax.mcutils.version.MCVersion;

import java.util.List;

public class BastionFeatures {

    public long seed;
    public MCVersion MCVersion;

    public static final String[] POOLS = new String[]{"housing","stables","treasure","bridge"};

    public static String getType(long seed, CPos pos, ChunkRand rand){
        rand.setCarverSeed(seed, pos.getX(), pos.getZ(), Config.version);
        int bastionType = rand.nextInt(4);
        String type = POOLS[bastionType];
        return type;
    }

    public static final BlockRotation[] rotations = new BlockRotation[]{
        BlockRotation.NONE,
        BlockRotation.CLOCKWISE_90,
        BlockRotation.CLOCKWISE_180,
        BlockRotation.COUNTERCLOCKWISE_90
    };

    public BastionFeatures(long seed, MCVersion version){
        this.seed = seed;
        this.MCVersion = version;
    }

    public boolean generate(CPos pos) {
        ChunkRand rand = new ChunkRand();

        MCLootTables.BASTION_OTHER_CHEST.apply(MCVersion);
        if (pos == null) return false;
        rand.setCarverSeed(seed, pos.getX(), pos.getZ(), MCVersion);
        int bastionType = rand.nextInt(4);
        String type = POOLS[bastionType];
        BlockRotation bastionRotation = rotations[rand.nextInt(4)];

        if (type.equals("housing")) {
            BPos offset = new BPos(-8, 0, 24).transform(BlockMirror.NONE, bastionRotation, new BPos(0, 0, 0));

            BPos chunkEdgeOfChestsRampart1 = pos.toBlockPos().add(offset.getX(), 0, offset.getZ()).toChunkCorner();

            rand.setDecoratorSeed(seed, chunkEdgeOfChestsRampart1.getX(), chunkEdgeOfChestsRampart1.getZ(), 40012,MCVersion);
            LootContext a1 = new LootContext(rand.nextLong());
            LootContext a2 = new LootContext(rand.nextLong());

            //System.out.println("\nBOTTOM CHEST: ");
            //displayAllChests(ItemList);

            LootContext a3 = new LootContext(rand.nextLong());
            LootContext a4 = new LootContext(rand.nextLong());
            LootContext a5 = new LootContext(rand.nextLong());

            List<ItemStack> ItemList2 = MCLootTables.BASTION_OTHER_CHEST.generate(a3);
            ItemList2.addAll(MCLootTables.BASTION_OTHER_CHEST.generate(a4));
            ItemList2.addAll(MCLootTables.BASTION_OTHER_CHEST.generate(a5));

            int obsidianCount = 0;
            for (ItemStack itemStack : ItemList2) {
                if (itemStack.getItem().equals(Items.OBSIDIAN)) {
                    obsidianCount += itemStack.getCount();
                }
            }
            if (obsidianCount >= 10) {
                return true;
            }

            //(These 3 chests aren't guaranteed. Could be a 1 chest rampart)
            BPos offset2 =
                new BPos(-8, 0, 32).transform(BlockMirror.NONE, bastionRotation, new BPos(0, 0, 0));
            BPos chunkEdgeOfChestsRampart2 =
                pos.toBlockPos().add(offset2.getX(), 0, offset2.getZ()).toChunkCorner();

            rand.setDecoratorSeed(seed, chunkEdgeOfChestsRampart2.getX(), chunkEdgeOfChestsRampart2.getZ(), 40012, MCVersion);

            LootContext a6 = new LootContext(rand.nextLong());
            LootContext a7 = new LootContext(rand.nextLong());
            LootContext a8 = new LootContext(rand.nextLong());

            List<ItemStack> ItemList3 = MCLootTables.BASTION_OTHER_CHEST.generate(a6);
            ItemList3.addAll(MCLootTables.BASTION_OTHER_CHEST.generate(a7));
            ItemList3.addAll(MCLootTables.BASTION_OTHER_CHEST.generate(a8));

            obsidianCount = 0;
            for (ItemStack itemStack : ItemList3) {
                if (itemStack.getItem().equals(Items.OBSIDIAN)) {
                    obsidianCount += itemStack.getCount();
                }
            }
            if (obsidianCount >= 10) {
                return false;
            }
        }
        return false;
    }
}

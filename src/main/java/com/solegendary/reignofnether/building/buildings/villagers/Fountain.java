package com.solegendary.reignofnether.building.buildings.villagers;

import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingBlock;
import com.solegendary.reignofnether.building.BuildingBlockData;
import com.solegendary.reignofnether.building.BuildingClientEvents;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.tutorial.TutorialClientEvents;
import com.solegendary.reignofnether.tutorial.TutorialStage;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;

import java.util.ArrayList;
import java.util.List;

import static com.solegendary.reignofnether.building.BuildingUtils.getAbsoluteBlockData;

public class Fountain extends Building {
    public final static String buildingName = "Fountain";
    public final static String structureName = "fountain";
    public final static ResourceCost cost = ResourceCost.Building(0,0,100,0);

    private static final int ICE_CHECK_TICKS_MAX = 100;
    private int ticksToNextIceCheck = ICE_CHECK_TICKS_MAX;

    public Fountain(Level level, BlockPos originPos, Rotation rotation, String ownerName) {
        super(
            level,
            originPos,
            rotation,
            ownerName,
            getAbsoluteBlockData(getRelativeBlockData(level), level, originPos, rotation),
            false
        );
        this.name = buildingName;
        this.ownerName = ownerName;
        this.portraitBlock = Blocks.PRISMARINE_BRICKS;
        this.icon = new ResourceLocation("minecraft", "textures/block/prismarine_bricks.png");

        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
        this.popSupply = cost.population;

        this.startingBlockTypes.add(Blocks.PRISMARINE_BRICKS);
        this.startingBlockTypes.add(Blocks.PRISMARINE_SLAB);
        this.explodeChance = 0;
    }

    public Faction getFaction() {return Faction.VILLAGERS;}

    public static ArrayList<BuildingBlock> getRelativeBlockData(LevelAccessor level) {
        return BuildingBlockData.getBuildingBlocks(structureName, level);
    }

    public static AbilityButton getBuildButton(Keybinding hotkey) {
        return new AbilityButton(
            Fountain.buildingName,
            new ResourceLocation("minecraft", "textures/block/prismarine_bricks.png"),
            hotkey,
            () -> BuildingClientEvents.getBuildingToPlace() == Fountain.class,
            TutorialClientEvents::isEnabled,
            () -> BuildingClientEvents.hasFinishedBuilding(TownCentre.buildingName) ||
                    ResearchClient.hasCheat("modifythephasevariance"),
            () -> BuildingClientEvents.setBuildingToPlace(Fountain.class),
            null,
            List.of(
                    FormattedCharSequence.forward(Fountain.buildingName, Style.EMPTY.withBold(true)),
                    ResourceCosts.getFormattedCost(cost),
                    FormattedCharSequence.forward("", Style.EMPTY),
                    FormattedCharSequence.forward("A prismarine fountain, how pretty!", Style.EMPTY),
                    FormattedCharSequence.forward("Passive healing aura for living units.", Style.EMPTY)
            ),
            null
        );
    }

    @Override
    public void tick(Level tickLevel) {
        super.tick(tickLevel);
        if(!tickLevel.isClientSide()) {
            ticksToNextIceCheck -= 1;
            if(ticksToNextIceCheck <= 0) {
                for (BuildingBlock bb : blocks)
                    if (tickLevel.getBlockState(bb.getBlockPos()).getBlock() == Blocks.ICE)
                        tickLevel.setBlockAndUpdate(bb.getBlockPos(), Blocks.WATER.defaultBlockState());
                ticksToNextIceCheck = ICE_CHECK_TICKS_MAX;
            }
        }
    }
}

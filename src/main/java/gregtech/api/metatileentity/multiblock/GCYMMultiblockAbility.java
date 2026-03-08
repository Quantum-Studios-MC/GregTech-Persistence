package gregtech.api.metatileentity.multiblock;

import gregtech.api.capability.IParallelHatch;
import gregtech.api.metatileentity.ITieredMetaTileEntity;

@SuppressWarnings("InstantiationOfUtilityClass")
public final class GCYMMultiblockAbility {

    public static final MultiblockAbility<IParallelHatch> PARALLEL_HATCH = new MultiblockAbility<>("parallel_hatch",
            IParallelHatch.class);

    public static final MultiblockAbility<ITieredMetaTileEntity> TIERED_HATCH = new MultiblockAbility<>("tiered_hatch",
            ITieredMetaTileEntity.class);

    private GCYMMultiblockAbility() {}
}

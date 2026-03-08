package gregtech.common.covers.filter;

import gregtech.api.items.metaitem.stats.IItemComponent;
import gregtech.api.util.IDirtyNotifiable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface IFilter {

    @Deprecated
    default void initUI(Consumer<gregtech.api.gui.Widget> widgetGroup) {}

    @NotNull
    ModularPanel createPopupPanel(PanelSyncManager syncManager);

    @NotNull
    ModularPanel createPanel(PanelSyncManager syncManager);

    @NotNull
    Widget<?> createWidgets(PanelSyncManager syncManager);

    ItemStack getContainerStack();

    void setDirtyNotifiable(@Nullable IDirtyNotifiable dirtyNotifiable);

    void markDirty();

    int getMaxTransferSize();

    void setMaxTransferSize(int maxTransferSize);

    boolean showGlobalTransferLimitSlider();

    MatchResult match(Object toMatch);

    boolean test(Object toTest);

    int getTransferLimit(Object stack, int transferSize);

    default int getTransferLimit(int slot, int transferSize) {
        return transferSize;
    }

    void readFromNBT(NBTTagCompound tagCompound);

    FilterType getType();

    enum FilterType {

        ITEM,
        FLUID;

        public boolean isError() {
            return false;
        }
    }

    static Factory factory(Factory factory) {
        return factory;
    }

    @FunctionalInterface
    interface Factory extends IItemComponent {

        @NotNull
        BaseFilter create(@NotNull ItemStack stack);
    }
}

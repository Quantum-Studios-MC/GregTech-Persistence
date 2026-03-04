package gregtech.common.covers;

import gregtech.api.util.ITranslatable;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.ITooltip;
import org.jetbrains.annotations.NotNull;

public enum TransferMode implements ITranslatable {

    TRANSFER_ANY("cover.%s.transfer_mode.transfer_any", 1),
    TRANSFER_EXACT("cover.%s.transfer_mode.transfer_exact", Integer.MAX_VALUE),
    KEEP_EXACT("cover.%s.transfer_mode.keep_exact", Integer.MAX_VALUE),
    RETAIN_EXACT("cover.%s.transfer_mode.retain_exact", Integer.MAX_VALUE);

    public static final TransferMode[] VALUES = values();
    private final String localeName;
    public final int maxStackSize;

    TransferMode(String localeName, int maxStackSize) {
        this.localeName = localeName;
        this.maxStackSize = maxStackSize;
    }

    @Override
    public @NotNull String getName() {
        throw new UnsupportedOperationException(
                "TransferMode#getName() called, this wouldn't produce any usable output, use the keyed getName instead!");
    }

    @Override
    public @NotNull String getName(@NotNull String key) {
        return String.format(localeName, key);
    }

    @Override
    public void handleTooltip(@NotNull ITooltip<?> tooltip, @NotNull String key) {
        tooltip.addTooltipLine(getKey(key));
        tooltip.addTooltipLine(IKey.lang(getName(key) + ".description"));
    }

    public boolean isTransferAny() {
        return this == TRANSFER_ANY;
    }

    public boolean isTransferExact() {
        return this == TRANSFER_EXACT;
    }

    public boolean isKeepExact() {
        return this == KEEP_EXACT;
    }

    public boolean isRetainExact() {
        return this == RETAIN_EXACT;
    }
}

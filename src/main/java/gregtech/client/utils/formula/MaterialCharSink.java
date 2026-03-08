package gregtech.client.utils.formula;

import gregtech.api.unification.material.Material;

@FunctionalInterface
public interface MaterialCharSink {

    boolean accept(int index, char c, Material material);
}

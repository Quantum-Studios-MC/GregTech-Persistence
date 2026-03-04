package gregtech.api.recipes.roll;

import java.util.List;

public interface ListWithRollInformation<E> extends List<E> {

    long[] comprehensiveRoll(int rollBoost, int max, int scale);
}

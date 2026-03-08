package gregtech.api.recipes.logic;

import gregtech.api.util.GuardedData;

import java.util.Map;

public class RecipeLogicHelper {

    protected final GuardedData<Map<MapKey<?>, Object>> data;

    public RecipeLogicHelper(GuardedData<Map<MapKey<?>, Object>> data) {
        this.data = data;
    }

    protected Map<MapKey<?>, Object> tData() {
        return data.getTransientData();
    }
}

package com.example.utils;

import lombok.Data;

/**
 * Date:2020/11/25
 * Decription:<描述>
 *
 * @Author:oyoyoyoyoyoyo
 */
@Data
public class GridOffSetData {
    public int earthID;
    public int xOffSet;
    public int yOffSet;

    private double x;
    private double y;

    public GridOffSetData(int earthID, int xOffSet, int yOffSet) {
        this.earthID = earthID;
        this.xOffSet = xOffSet;
        this.yOffSet = yOffSet;
    }

    @Override
    public boolean equals(Object other) {
        final boolean b = other instanceof GridOffSetData && this.equalsInternal((GridOffSetData) other);
        return b;
    }

    private boolean equalsInternal(GridOffSetData other) {
        return this.earthID == other.earthID && this.xOffSet == other.xOffSet && this.yOffSet == other.yOffSet;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + this.earthID;
        result = 37 * result + this.xOffSet;
        result = 37 * result + this.yOffSet;
        return result;
    }
}

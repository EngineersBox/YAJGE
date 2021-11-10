package com.engineersbox.yajge.util;

import org.eclipse.collections.impl.collector.Collectors2;
import org.eclipse.collections.impl.factory.primitive.FloatLists;
import org.eclipse.collections.impl.factory.primitive.IntLists;

import java.util.List;

public class ListUtils {

    private ListUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static float[] floatListToArray(final List<Float> floatList) {
        return floatList.stream()
                .collect(Collectors2.collectFloat(
                        Float::floatValue,
                        FloatLists.mutable::empty
                )).toArray();
    }

    public static int[] intListToArray(final List<Integer> intList) {
        return intList.stream()
                .collect(Collectors2.collectInt(
                        Integer::intValue,
                        IntLists.mutable::empty
                )).toArray();
    }

}

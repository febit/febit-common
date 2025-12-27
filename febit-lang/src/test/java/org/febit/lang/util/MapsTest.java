package org.febit.lang.util;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class MapsTest {
    final String[] abc = new String[]{"a", "b", "c"};

    @Test
    void compute() {
        assertThat(Maps.compute(abc, String::toUpperCase))
                .hasSize(3)
                .containsEntry("a", "A")
                .containsEntry("b", "B")
                .containsEntry("c", "C");

        assertThat(Maps.compute(abc, v -> "b".equals(v) ? null : v.toUpperCase()))
                .hasSize(2)
                .containsEntry("a", "A")
                .containsEntry("c", "C");

        assertThat(Maps.compute(List.of(abc), Function.identity()))
                .hasSize(3)
                .containsEntry("a", "a")
                .containsEntry("b", "b")
                .containsEntry("c", "c");
    }

    @Test
    void mapping() {
        assertThat(Maps.mapping(abc, v -> null))
                .hasSize(1)
                .containsEntry(null, "c");

        assertThat(Maps.mapping(abc, String::toUpperCase))
                .hasSize(3)
                .containsEntry("A", "a")
                .containsEntry("B", "b")
                .containsEntry("C", "c");

        assertThat(Maps.mapping(List.of(abc), Function.identity()))
                .hasSize(3)
                .containsEntry("a", "a")
                .containsEntry("b", "b")
                .containsEntry("c", "c");

        assertThat(Maps.mapping(abc, String::toUpperCase, s -> s + s))
                .hasSize(3)
                .containsEntry("A", "aa")
                .containsEntry("B", "bb")
                .containsEntry("C", "cc");

        assertThat(Maps.mapping(List.of(abc), s -> s + s, String::toUpperCase))
                .hasSize(3)
                .containsEntry("aa", "A")
                .containsEntry("bb", "B")
                .containsEntry("cc", "C");
    }

    @Test
    void mappingMultiKeys() {
        assertThat(Maps.mappingMultiKeys(abc, Function.identity(), String::toUpperCase))
                .hasSize(6)
                .containsEntry("a", "a")
                .containsEntry("A", "a")
                .containsEntry("b", "b")
                .containsEntry("B", "b")
                .containsEntry("c", "c")
                .containsEntry("C", "c");

        assertThat(Maps.mappingMultiKeys(List.of(abc), Function.identity(), String::toUpperCase))
                .hasSize(6)
                .containsEntry("a", "a")
                .containsEntry("A", "a")
                .containsEntry("b", "b")
                .containsEntry("B", "b")
                .containsEntry("c", "c")
                .containsEntry("C", "c");
    }

    @Test
    void grouping() {
        assertThat(Maps.grouping(abc, Function.identity(), String::toUpperCase))
                .hasSize(3)
                .containsEntry("a", List.of("A"))
                .containsEntry("b", List.of("B"))
                .containsEntry("c", List.of("C"));

        assertThat(Maps.grouping(List.of(abc), String::length,
                String::toUpperCase))
                .hasSize(1)
                .containsEntry(1, List.of("A", "B", "C"));

        assertThat(Maps.grouping(abc, String::length))
                .hasSize(1)
                .containsEntry(1, List.of("a", "b", "c"));

        assertThat(Maps.grouping(List.of(abc), s -> "b".equals(s) ? 2 : 1))
                .hasSize(2)
                .containsEntry(1, List.of("a", "c"))
                .containsEntry(2, List.of("b"));
    }

    @Test
    void groupingSet() {
        assertThat(Maps.groupingSet(abc, Function.identity(), String::toUpperCase))
                .hasSize(3)
                .containsEntry("a", Set.of("A"))
                .containsEntry("b", Set.of("B"))
                .containsEntry("c", Set.of("C"));

        assertThat(Maps.groupingSet(List.of(abc), String::length,
                String::toUpperCase))
                .hasSize(1)
                .containsEntry(1, Set.of("A", "B", "C"));

        assertThat(Maps.groupingSet(abc, String::length))
                .hasSize(1)
                .containsEntry(1, Set.of("a", "b", "c"));

        assertThat(Maps.groupingSet(List.of(abc), s -> "b".equals(s) ? 2 : 1))
                .hasSize(2)
                .containsEntry(1, Set.of("a", "c"))
                .containsEntry(2, Set.of("b"));
    }

    @Test
    void create() {
        var map = Maps.<String, Integer>create(10);
        assertThat(map).isEmpty();

        assertDoesNotThrow(() -> Maps.create(-1));
        assertDoesNotThrow(() -> Maps.create(0));
        assertDoesNotThrow(() -> Maps.create(1));
        assertDoesNotThrow(() -> Maps.create(2));
    }

    @Test
    void transfer() {
        var source = Maps.mapping(abc, String::toUpperCase);
        assertThat(Maps.transfer(source, s -> s + s, String::toUpperCase))
                .hasSize(3)
                .containsEntry("AA", "A")
                .containsEntry("BB", "B")
                .containsEntry("CC", "C");

        assertThat(Maps.transfer(source, s -> "x", String::toLowerCase))
                .hasSize(1)
                .containsKey("x");
    }

    @Test
    void transferValue() {
        var source = Maps.mapping(abc, String::toUpperCase);
        assertThat(Maps.transferValue(source, s -> s + s))
                .hasSize(3)
                .containsEntry("A", "aa")
                .containsEntry("B", "bb")
                .containsEntry("C", "cc");
    }
}

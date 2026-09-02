package com.shreeai.os.platform.kernels.developer.patch;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <b>ImportMergerTest</b>
 *
 * <p>8 test cases for the ImportMerger utility.</p>
 *
 * @since Sprint-17
 */
public class ImportMergerTest {

    private final ImportMerger merger = new ImportMerger();

    @Test
    void merge_addsNewImportToSource() {
        String source = """
                package com.example;
                public class Demo {}
                """;
        var result = merger.merge(source, List.of("import java.util.List;"));

        assertEquals(1, result.added());
        assertEquals(0, result.duplicates());
        assertTrue(result.source().contains("import java.util.List;"));
    }

    @Test
    void merge_dedupesIdenticalImports() {
        String source = """
                package com.example;
                import java.util.List;
                public class Demo {}
                """;
        var result = merger.merge(source, List.of("import java.util.List;"));

        assertEquals(0, result.added());
        long count = result.source().lines().filter(l -> l.contains("import java.util.List")).count();
        assertEquals(1, count);
    }

    @Test
    void merge_handlesMultipleImports() {
        String source = """
                package com.example;
                public class Demo {}
                """;
        var result = merger.merge(source, List.of(
                "import java.util.List;",
                "import java.util.Map;",
                "import java.util.Set;"
        ));

        assertEquals(3, result.added());
        assertTrue(result.source().contains("import java.util.List;"));
        assertTrue(result.source().contains("import java.util.Map;"));
        assertTrue(result.source().contains("import java.util.Set;"));
    }

    @Test
    void merge_sortsImportsAlphabetically() {
        String source = """
                package com.example;
                import java.util.Map;
                public class Demo {}
                """;
        var result = merger.merge(source, List.of("import java.util.List;"));

        int listIdx = result.source().indexOf("import java.util.List;");
        int mapIdx = result.source().indexOf("import java.util.Map;");
        assertTrue(listIdx < mapIdx, "List should sort before Map");
    }

    @Test
    void merge_handlesStaticImports() {
        String source = """
                package com.example;
                public class Demo {}
                """;
        var result = merger.merge(source, List.of(
                "import static org.junit.Assert.assertEquals;"
        ));

        assertEquals(1, result.added());
        assertTrue(result.source().contains("import static org.junit.Assert.assertEquals;"));
    }

    @Test
    void merge_ignoresEmptyOrNullImports() {
        String source = """
                package com.example;
                public class Demo {}
                """;
        java.util.List<String> importList = new java.util.ArrayList<>();
        importList.add("");
        importList.add(null);
        importList.add("   ");
        var result = merger.merge(source, importList);

        assertEquals(0, result.added());
    }

    @Test
    void merge_insertsImportsAfterPackageStatement() {
        String source = """
                package com.example;
                public class Demo {}
                """;
        var result = merger.merge(source, List.of("import java.util.List;"));

        int packageIdx = result.source().indexOf("package com.example;");
        int importIdx = result.source().indexOf("import java.util.List;");
        int classIdx = result.source().indexOf("class Demo");

        assertTrue(packageIdx < importIdx);
        assertTrue(importIdx < classIdx);
    }

    @Test
    void extract_returnsAllImportsFromSource() {
        String source = """
                package com.example;
                import java.util.List;
                import java.util.Map;
                import static org.junit.Assert.assertEquals;
                public class Demo {}
                """;
        var imports = merger.extract(source);

        assertEquals(3, imports.size());
        assertTrue(imports.stream().anyMatch(i -> i.contains("import java.util.List")));
        assertTrue(imports.stream().anyMatch(i -> i.contains("import java.util.Map")));
        assertTrue(imports.stream().anyMatch(i -> i.contains("import static org.junit.Assert.assertEquals")));
    }
}

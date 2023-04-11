package me.smilingleo.framework;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;

import me.smilingleo.RenderContext;
import me.smilingleo.services.Wig;
import me.smilingleo.utils.JsonUtils;
import me.smilingleo.utils.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(Parameterized.class)
public class DataDrivenTest {
    private static ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private static ObjectMapper objectMapper = new ObjectMapper();

    @Parameter(value = 0)
    public String caseFileName;
    @Parameter(value = 1)
    public TestCase testCase;

    @Parameters(name = "{index}: File: {0}: {1}")
    public static Collection<Object[]> data() {
        try {
            String path = DataDrivenTest.class.getClassLoader().getResource("data-driven").getPath();
            List<String> caseFileNames = Files.walk(Paths.get(path))
                    .map(x -> x.toString())
                    .filter(f -> f.endsWith("test-cases.yml"))
                    .collect(Collectors.toList());
            Collection<Object[]> col = null;
            for (String caseFileName : caseFileNames) {
                // start from "test-classes",  end with ".yml"
                int start = caseFileName.indexOf("data-driven/");
                int end = caseFileName.indexOf(".yml");
                Collection<Object[]> newCol = loadYamlTestCaseString(caseFileName)
                        .filter(str -> str != null && str.trim().length() > 0)
                        .map(str -> read(str))
                        .map(testCase -> new Object[]{caseFileName.substring(start, end), testCase})
                        .collect(Collectors.toList());
                if (col == null) {
                    col = newCol;
                } else {
                    col.addAll(newCol);
                }
            }
            return col;

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void test() throws IOException {
        String template = testCase.getTemplate();

        RenderContext renderContext = new RenderContext();
        renderContext.setTimeZone("UTC");
        RenderContext.runWithContext(renderContext, () -> {

            Map rawData = JsonUtils.uncheckedStringToJson(testCase.getData(), new TypeReference<Map>() {}, objectMapper);
            Wig wig = new Wig(renderContext);
            String rendered = wig.render(template, rawData);
            if (testCase.isIgnoreWhitespace()) {
                compareIgnoreWhitespace(testCase.getExpectedRendered(), rendered);
            } else {
                assertEquals(format("%s got a different rendered result", testCase.getName()),
                        testCase.getExpectedRendered(), rendered);
            }
            return true;
        });
    }


    private static TestCase read(String str) {
        try {
            return yamlMapper.readValue(str, TestCase.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Can't read test case from content:\n" + str, e);
        }
    }

    private void compareIgnoreWhitespace(String expected, String actual) {
        String left = ignoreWhitespace(expected);
        String right = ignoreWhitespace(actual);
        assertEquals(format("%s got a different rendered result", testCase.getName()), left, right);
    }

    private String ignoreWhitespace(String expected) {
        return Stream.of(expected.split("\n"))
                .filter(line -> StringUtils.notNullOrBlank(line))
                .map(line -> line.trim())
                .collect(Collectors.joining("\n"));
    }

    private static Stream<String> loadYamlTestCaseString(String filePath) throws IOException {
        Path path = new File(filePath).toPath();
        String testDataStr = Files.readAllLines(path).stream().collect(Collectors.joining("\n"));
        return Arrays.asList(testDataStr.split("---")).stream();
    }
}

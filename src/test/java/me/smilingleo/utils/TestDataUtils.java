package me.smilingleo.utils;

import static java.lang.String.join;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class TestDataUtils {

    private static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * read resource file.
     *
     * @param resourceFileName file under `resources` folder.
     * @return
     */
    public static String getFileContent(String resourceFileName) {
        try {
            String path = TestDataUtils.class.getClassLoader().getResource(resourceFileName).getPath();
            return join("\n", Files.readAllLines(Paths.get(path)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Map<String, Object> loadJsonAsMap(String jsonFilePath) {
        String content = getFileContent(jsonFilePath);
        return JsonUtils.uncheckedStringToJson(content, new TypeReference<Map<String, Object>>() {}, objectMapper);
    }
}

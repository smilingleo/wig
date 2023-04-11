package me.smilingleo.framework;

import me.smilingleo.utils.StringUtils;
import me.smilingleo.utils.TestDataUtils;

public class TestCase {
    private String name;
    private String description;
    private String template;
    private String templateFile;
    private String data;
    private String dataFile;
    private String expectedRendered;
    private boolean ignoreWhitespace = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTemplate() {
        return getOrLoad(template, templateFile);
    }

    private String getOrLoad(String content, String contentFile) {
        return StringUtils.notNullOrBlank(content) ? content
                : StringUtils.notNullOrBlank(contentFile) ? TestDataUtils.getFileContent(contentFile) : null;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public void setTemplateFile(String templateFile) {
        this.templateFile = templateFile;
    }

    public String getData() {
        return getOrLoad(data, dataFile);
    }

    public void setData(String data) {
        this.data = data;
    }
    public void setDataFile(String dataFile) {
        this.dataFile = dataFile;
    }

    public String getExpectedRendered() {
        return expectedRendered;
    }

    public void setExpectedRendered(String expectedRendered) {
        this.expectedRendered = expectedRendered;
    }

    public boolean isIgnoreWhitespace() {
        return ignoreWhitespace;
    }

    public void setIgnoreWhitespace(boolean ignoreWhitespace) {
        this.ignoreWhitespace = ignoreWhitespace;
    }

    @Override
    public String toString() {
        return name;
    }
}

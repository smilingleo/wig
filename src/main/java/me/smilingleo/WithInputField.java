package me.smilingleo;

import me.smilingleo.decorators.MergeField;
import me.smilingleo.decorators.MergeFieldParser;

public interface WithInputField {

    String CURRENT_OBJECT = ".";

    /**
     * <p>The input field name is used by json path to retrieve the input for the function.</p>
     *
     * @return
     */
    String getInputFieldName();

    default MergeField getInputField() {
        String fieldName = getInputFieldName();
        return MergeFieldParser.parse(fieldName);
    }
}

package me.smilingleo.decorators;

public class SortByArgument {

    private MergeField field;
    private SortOrder sortOrder;

    public SortByArgument(MergeField field, SortOrder sortOrder) {
        this.field = field;
        this.sortOrder = sortOrder;
    }

    public MergeField getField() {
        return field;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }
}

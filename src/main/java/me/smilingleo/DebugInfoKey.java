package me.smilingleo;

public enum DebugInfoKey {
    request(false),
    metadata(false),
    trieTree(false),
    queryBuilder(true),
    fetchData(true),
    dataTransform(false),
    mustacheRender(false),
    runtimeError(false);

    private boolean withMultipleRecords;

    DebugInfoKey(boolean withMultipleRecords) {
        this.withMultipleRecords = withMultipleRecords;
    }

    public boolean isWithMultipleRecords() {
        return this.withMultipleRecords;
    }
}

package me.smilingleo;

import java.util.List;

public interface WithFieldArg {

    /**
     * <p>Return all the argument merge fields (valid metadata).</p>
     *
     * <p>It's used to help construct the GraphQL query, determine which fields are required by the template.</p>
     *
     * <p>If the argument is a variable derived from a merge field, its original field name should be returned.</p>
     * @return list of field names.
     */
    List<String> getArgFieldNames();
}

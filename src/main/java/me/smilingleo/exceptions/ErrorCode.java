package me.smilingleo.exceptions;

public enum ErrorCode {
    MustacheSyntaxError,
    UnknownField,
    UnknownDecorator,
    UnknownCommand,
    UnknownFunction,
    /**
     * For both Function and Decorator functions.
     */
    InvalidFunctionArgument,
    InvalidCommandArgument,
    InvalidExpression,
    /**
     * In case of runtime validation error, and you can't determine if the value is a function argument, use this error code.
     */
    InvalidValue,
    /**
     * Used when dependent services fail to response.
     */
    InternalError,
    NotImplemented,
    InnerTextRequired,
    MissingRootObject,
    InvalidRootObject,
    InvalidDataPath,
    TooManyRecords,
    InconsistentData,
    QueryFailed,
    CaseSensitivity,
    Unknown,
}

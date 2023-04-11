package me.smilingleo.decorators;

import static java.lang.String.format;

import me.smilingleo.exceptions.ErrorCode;
import me.smilingleo.exceptions.ValidationException;

public enum Operator {
    LT {
        @Override
        public boolean compare(Object leftSide, Object rightSide) {
            return compareTo(leftSide, rightSide) < 0;
        }
    },
    LE {
        @Override
        public boolean compare(Object leftSide, Object rightSide) {
            return compareTo(leftSide, rightSide) <= 0;
        }
    },
    GT {
        @Override
        public boolean compare(Object leftSide, Object rightSide) {
            return compareTo(leftSide, rightSide) > 0;
        }
    },
    GE {
        @Override
        public boolean compare(Object leftSide, Object rightSide) {
            return compareTo(leftSide, rightSide) >= 0;
        }
    },
    EQ {
        @Override
        public boolean compare(Object leftSide, Object rightSide) {
            return compareTo(leftSide, rightSide) == 0;
        }
    },
    NE {
        @Override
        public boolean compare(Object leftSide, Object rightSide) {
            return compareTo(leftSide, rightSide) != 0;
        }
    },
    IS_NULL {
        @Override
        public boolean compare(Object leftSide, Object rightSide) {
            return leftSide == null;
        }
    },
    NOT_NULL {
        @Override
        public boolean compare(Object leftSide, Object rightSide) {
            return leftSide != null;
        }
    },
    ;

    public abstract boolean compare(Object leftSide, Object rightSide);

    public static int compareTo(Object leftSide, Object rightSide) {
        if (leftSide == null && rightSide == null) {
            return 0;
        }
        if (leftSide == null) {
            return -1;
        }
        if (rightSide == null) {
            return 1;
        }
        // both are comparable and same type
        if (leftSide instanceof Comparable && rightSide instanceof Comparable && leftSide.getClass()
                .equals(rightSide.getClass())) {
            return ((Comparable) leftSide).compareTo(rightSide);
        }
        // in case they are not the same type
        if (leftSide instanceof Number) {
            return Double.valueOf(leftSide.toString()).compareTo(Double.valueOf(rightSide.toString()));
        } else if (leftSide instanceof Boolean) {
            return Boolean.valueOf(leftSide.toString()).compareTo(Boolean.valueOf(rightSide.toString()));
        } else if (leftSide instanceof String) {
            return leftSide.toString().compareTo(rightSide.toString());
        }
        // Operator is only used for decorator functions.
        throw new ValidationException(ErrorCode.InvalidFunctionArgument,
                format("%s is not comparable to %s", leftSide.getClass().getSimpleName(), rightSide.getClass().getSimpleName()));
    }

}

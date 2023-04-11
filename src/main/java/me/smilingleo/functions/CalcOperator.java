package me.smilingleo.functions;

import static me.smilingleo.exceptions.ErrorCode.InvalidFunctionArgument;
import static me.smilingleo.utils.StringUtils.isBoolean;
import static me.smilingleo.utils.StringUtils.isNumber;

import me.smilingleo.exceptions.ValidationException;

import java.math.BigDecimal;

public enum CalcOperator {
    Add {
        @Override
        public Object calulate(Object leftValue, Object rightValue) {
            if (leftValue == null && rightValue == null) {
                return "";
            }
            if (leftValue == null) {
                return rightValue.toString();
            }
            // leftValue is not null.
            if (rightValue == null) {
                return leftValue.toString();
            }

            String leftValueStr = leftValue.toString();
            String rightValueStr = rightValue.toString();
            if (isNumber(leftValueStr) && isNumber(rightValueStr)) {
                return new BigDecimal(leftValueStr).add(new BigDecimal(rightValueStr)).toString();
            } else {
                throw new ValidationException(InvalidFunctionArgument,
                        "The left and right arguments must be numeric fields when the operator is 'Add'.");
            }
        }
    },
    Subtract {
        @Override
        public Object calulate(Object leftValue, Object rightValue) {
            if (leftValue == null && rightValue == null) {
                return "";
            }

            if (rightValue == null) {
                return leftValue.toString();
            }

            String rightValueStr = rightValue.toString();
            boolean rightValueIsNumber = isNumber(rightValueStr);
            if (leftValue == null && rightValueIsNumber) {
                return new BigDecimal(rightValueStr).multiply(BigDecimal.valueOf(-1L)).toString();
            } else if (!rightValueIsNumber) {
                // rightValueStr is not number
                throw new ValidationException(InvalidFunctionArgument,
                        "The right arguments must be numeric fields when the operator is 'Subtract'.");
            }

            String leftValueStr = leftValue.toString();
            if (isNumber(leftValueStr) && rightValueIsNumber) {
                return new BigDecimal(leftValueStr).subtract(new BigDecimal(rightValueStr)).toString();
            } else {
                throw new ValidationException(InvalidFunctionArgument,
                        "The left and right arguments must be numeric fields when the operator is 'Subtract'.");
            }
        }
    },
    Multiply {
        @Override
        public Object calulate(Object leftValue, Object rightValue) {
            if (leftValue == null || rightValue == null) {
                return "";
            }

            String leftValueStr = leftValue.toString();
            String rightValueStr = rightValue.toString();
            if (isNumber(leftValueStr) && isNumber(rightValueStr)) {
                return new BigDecimal(leftValueStr).multiply(new BigDecimal(rightValueStr)).toString();
            } else {
                throw new ValidationException(InvalidFunctionArgument,
                        "The left and right arguments must be numeric fields when the operator is 'Multiply'.");
            }
        }
    },
    Divide {
        @Override
        public Object calulate(Object leftValue, Object rightValue) {
            if (leftValue == null || rightValue == null) {
                return "";
            }

            String leftValueStr = leftValue.toString();
            String rightValueStr = rightValue.toString();
            if (rightValueStr.equals("0")) {
                return "";
            }
            if (isNumber(leftValueStr) && isNumber(rightValueStr)) {
                return new BigDecimal(leftValueStr).divide(new BigDecimal(rightValueStr)).toString();
            } else {
                throw new ValidationException(InvalidFunctionArgument,
                        "The left and right arguments must be numeric fields when the operator is 'Divide'.");
            }
        }
    },
    And {
        @Override
        public Object calulate(Object leftValue, Object rightValue) {
            if (leftValue == null || rightValue == null) {
                return false;
            }

            String leftValueStr = leftValue.toString();
            String rightValueStr = rightValue.toString();
            if (isBoolean(leftValueStr) && isBoolean(rightValueStr)) {
                return Boolean.parseBoolean(leftValueStr) && Boolean.parseBoolean(rightValueStr);
            } else {
                throw new ValidationException(InvalidFunctionArgument,
                        "The left and right arguments must be numeric fields when the operator is 'Add'.");
            }
        }
    },
    Or {
        @Override
        public Object calulate(Object leftValue, Object rightValue) {
            if (leftValue == null && rightValue == null) {
                return false;
            }

            if (leftValue == null) {
                return Boolean.parseBoolean(rightValue.toString());
            }
            // leftValue is not null.
            if (rightValue == null) {
                return Boolean.parseBoolean(leftValue.toString());
            }

            String leftValueStr = leftValue.toString();
            String rightValueStr = rightValue.toString();
            if (isBoolean(leftValueStr) && isBoolean(rightValueStr)) {
                return Boolean.parseBoolean(leftValueStr) || Boolean.parseBoolean(rightValueStr);
            } else {
                throw new ValidationException(InvalidFunctionArgument,
                        "The left and right arguments must be numeric fields when the operator is 'Add'.");
            }
        }
    },
    Xor {
        @Override
        public Object calulate(Object leftValue, Object rightValue) {
            if (leftValue == null && rightValue == null) {
                return false;
            }
            // not both null.
            if (leftValue == null || rightValue == null) {
                return true;
            }

            String leftValueStr = leftValue.toString();
            String rightValueStr = rightValue.toString();
            if (isBoolean(leftValueStr) && isBoolean(rightValueStr)) {
                return Boolean.logicalXor(Boolean.parseBoolean(leftValueStr), Boolean.parseBoolean(rightValueStr));
            } else {
                throw new ValidationException(InvalidFunctionArgument,
                        "The left and right arguments must be numeric fields when the operator is 'Add'.");
            }
        }
    };

    public abstract Object calulate(Object leftValue, Object rightValue);
}

package me.smilingleo.utils;

import static java.util.stream.Collectors.joining;

import me.smilingleo.exceptions.ErrorCode;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class StringUtils {

    public static boolean isNullOrBlank(String string) {
        return string == null || string.trim().length() == 0;
    }

    public static boolean notNullOrBlank(String string) {
        return !isNullOrBlank(string);
    }

    public static boolean equalsIgnoreCase(String value1, String value2) {
        if (value1 == null || value2 == null) {
            return false;
        }
        return value1.equalsIgnoreCase(value2);
    }

    public static boolean equalsIgnoreCaseButNotSame(String value1, String value2) {
        if (value1 == null || value2 == null) {
            return false;
        }
        return value1.equalsIgnoreCase(value2) && !value1.equals(value2);
    }

    public static String unquote(String text) {
        if (isNullOrBlank(text)) {
            return "";
        }
        String trimmed = text.trim();
        if (isText(trimmed)) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

    public static String headOfDottedPath(String dottedPath) {
        int brackets = 0;
        for (int i = 0; i < dottedPath.length(); i++) {
            char c = dottedPath.charAt(i);
            if (c == '(') {
                brackets += 1;
            } else if (c == ')') {
                brackets -= 1;
            } else if (c == '.') {
                if (brackets == 0) {
                    return dottedPath.substring(0, i);
                }

            }
        }
        return dottedPath;
    }

    /**
     * return 'B.C' for path 'A.B.C'. In case `A` is a decorated merge field like `A|FilterByValue(a.b.c,EQ,0)`, still
     * need to be able to work.
     */
    public static String tailOfDottedPath(String dottedPath) {
        int brackets = 0;
        for (int i = 0; i < dottedPath.length(); i++) {
            char c = dottedPath.charAt(i);
            if (c == '(') {
                brackets += 1;
            } else if (c == ')') {
                brackets -= 1;
            } else if (c == '.') {
                if (brackets == 0) {
                    return dottedPath.substring(i + 1);
                }

            }
        }
        // in case no early return, it's not dotted, return original value.
        return dottedPath;
    }

    public static List<String> dottedPathToList(String dottedPath) {
        String string = dottedPath;
        List<String> list = new LinkedList<>();
        while (!string.isEmpty()) {
            String head = headOfDottedPath(string);
            if (!head.isEmpty()) {
                list.add(head);
            }
            string = string.substring(head.length());
            if (string.startsWith(".")) {
                string = string.substring(1);
            }
        }
        return list;
    }

    public static String[] dottedPathToArray(String dottedPath) {
        return dottedPathToList(dottedPath).toArray(new String[]{});
    }

    /**
     * Check if a merge field argument is a Text instead of a field reference.
     */
    public static boolean isText(String string) {
        if (string == null) {
            return false;
        }

        char first = string.charAt(0);
        char last = string.charAt(string.length() - 1);
        return (first == '\'' && last == '\'') || (first == '\"' && last == '\"');
    }

    public static boolean isNumber(String string) {
        try {
            new BigDecimal(string);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public static boolean isBoolean(String string) {
        return notNullOrBlank(string) && (string.equalsIgnoreCase("true") || string.equalsIgnoreCase("false"));
    }

    public static String urlDecode(String string) {
        try {
            return URLDecoder.decode(string, "utf8");
        } catch (UnsupportedEncodingException e) {
            return string;
        }
    }

    /**
     * Use `,` as argument separator, need to handle the case that a decorated merge field as argument, for example:
     * (Fn_Calc(Invoice.InvoiceItems|FilterByValue(ChargeAmount,EQ,0)|Size,Add,10)).
     *
     * @param functionLabel the function text.
     * @return
     */
    public static List<String> parseFunctionArguments(String functionLabel) {
        if (isNullOrBlank(functionLabel) || functionLabel.indexOf('(') < 0) {
            return Collections.emptyList();
        }

        String content = functionLabel.substring(functionLabel.indexOf('('));
        Asserts.assertTrue(content.charAt(0) == '(' && content.charAt(content.length() - 1) == ')',
                ErrorCode.InvalidFunctionArgument, "function functionLabel should be enclosed by parenthesis");
        content = content.substring(1, content.length() - 1);
        List<String> list = new LinkedList<>();
        int brackets = 0;
        int startPos = 0;
        boolean lastIsComma = false;
        boolean singleQuoteStarted = false;
        boolean doubleQuoteStarted = false;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '(') {
                brackets += 1;
            } else if (c == ')') {
                brackets -= 1;
            } else if (c == ',') {
                if (brackets == 0 && !singleQuoteStarted && !doubleQuoteStarted) {
                    String argument = content.substring(startPos, i);
                    list.add(argument);
                    startPos = i + 1;
                }
            } else if (c == '"') {
                if (lastIsComma) {
                    doubleQuoteStarted = true;
                } else if (doubleQuoteStarted) {
                    doubleQuoteStarted = false;
                }
            } else if (c == '\'') {
                if (lastIsComma) {
                    singleQuoteStarted = true;
                } else if (singleQuoteStarted) {
                    singleQuoteStarted = false;
                }
            }
            lastIsComma = c == ',' ? true : false;
        }
        if (startPos <= content.length() - 1) {
            list.add(content.substring(startPos));
        }

        return list;
    }

    public static String repeat(int n, String s) {
        return Stream.generate(() -> s).limit(n).collect(joining());
    }
}

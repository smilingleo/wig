package me.smilingleo.decorators;

import me.smilingleo.exceptions.ErrorCode;
import me.smilingleo.exceptions.ValidationException;
import me.smilingleo.utils.ReflectionUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MergeFieldParser {

    private static List<Class<? extends Decorator>> allDecoratorClass = new LinkedList<>();

    static {
        allDecoratorClass.add(Default.class);
        allDecoratorClass.add(DateAdd.class);
        allDecoratorClass.add(EqualToVal.class);
        allDecoratorClass.add(EqualToVar.class);
        allDecoratorClass.add(FilterByRef.class);
        allDecoratorClass.add(FilterByValue.class);
        allDecoratorClass.add(First.class);
        allDecoratorClass.add(FlatMap.class);
        allDecoratorClass.add(GroupBy.class);
        allDecoratorClass.add(IsBlank.class);
        allDecoratorClass.add(IsEmpty.class);
        allDecoratorClass.add(Last.class);
        allDecoratorClass.add(Localise.class);
        allDecoratorClass.add(Symbol.class);
        allDecoratorClass.add(Map.class);
        allDecoratorClass.add(Max.class);
        allDecoratorClass.add(Min.class);
        allDecoratorClass.add(Nth.class);
        allDecoratorClass.add(Size.class);
        allDecoratorClass.add(SortBy.class);
        allDecoratorClass.add(Substr.class);
        allDecoratorClass.add(Sum.class);
        allDecoratorClass.add(Uniq.class);
        allDecoratorClass.add(Concat.class);
        allDecoratorClass.add(Round.class);
        allDecoratorClass.add(Format.class);
        allDecoratorClass.add(Skip.class);
    }

    public static MergeField parse(String label) {
        if (label == null || label.length() == 0) {
            return null;
        }

        int firstPipeIdx = label.indexOf('|');
        if (firstPipeIdx < 0) {
            return new MergeField(label);
        }

        String fieldName = label.substring(0, firstPipeIdx);
        String decoratorStr = label.substring(firstPipeIdx + 1);
        return new MergeField(fieldName, parseDecorators(decoratorStr).toArray(new Decorator[]{}));
    }

    public static List<Decorator> parseDecorators(String decoratorStr) {
        String label = decoratorStr;
        List<Decorator> decorators = new LinkedList<>();

        while (label.length() > 0) {
            if (label.startsWith("|")) {
                label = label.substring(1);
            }

            boolean found = false;
            for (Class<? extends Decorator> decoratorClazz : allDecoratorClass) {
                if (label.startsWith(decoratorClazz.getSimpleName())) {
                    String section = cut(label);
                    found = true;
                    decorators.add(ReflectionUtils.invokeParse(decoratorClazz, section));
                    label = label.substring(section.length());
                    break;
                }
            }

            if (!found && label.length() > 0) {
                throw new ValidationException(ErrorCode.UnknownDecorator, "Unknown decorator in merge field: " + label);
            }
        }

        return decorators;
    }

    private static String cut(String label) {
        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < label.length(); i++) {
            char cursorChar = label.charAt(i);
            if (cursorChar == '(') {
                queue.add(i);
            } else if (cursorChar == ')') {
                queue.remove();
                if (queue.isEmpty()) {
                    return label.substring(0, i + 1);
                }
            } else if (cursorChar == '|' && queue.isEmpty()) {
                return label.substring(0, i);
            }
        }
        return label;
    }

}

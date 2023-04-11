package me.smilingleo.services;

import static me.smilingleo.utils.StringUtils.dottedPathToList;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import me.smilingleo.ChildrenAware;
import me.smilingleo.RenderContext;
import me.smilingleo.Variable;
import me.smilingleo.WithFieldArg;
import me.smilingleo.WithInputField;
import me.smilingleo.commands.CommandParser;
import me.smilingleo.decorators.MergeField;
import me.smilingleo.decorators.MergeFieldParser;
import me.smilingleo.decorators.VariableField;
import me.smilingleo.exceptions.ErrorCode;
import me.smilingleo.exceptions.ExceptionUtils;
import me.smilingleo.exceptions.ValidationException;
import me.smilingleo.functions.FunctionParser;
import me.smilingleo.trie.TrieNode;
import me.smilingleo.utils.Asserts;
import me.smilingleo.utils.ListUtils;
import me.smilingleo.utils.StringUtils;

import com.github.mustachejava.Code;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheException;
import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.codes.IterableCode;
import com.github.mustachejava.codes.ValueCode;
import com.github.mustachejava.codes.WriteCode;
import me.smilingleo.utils.Constants;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

class TrieTreeParser {

    private RenderContext renderContext;

    public TrieTreeParser(RenderContext renderContext) {
        RenderContext.validate(renderContext);
        this.renderContext = renderContext;
    }

    public List<TrieNode> parse(String template) {
        MustacheFactory factory = new DefaultMustacheFactory();
        Mustache mustache = null;
        try {
            mustache = factory.compile(new StringReader(template), "template");
        } catch (MustacheException me) {
            throw new ValidationException(ErrorCode.MustacheSyntaxError, me.getMessage(), me);
        }
        // code is in hierarchical structure, the first level codes are for root objects.
        Code[] rootCodes = mustache.getCodes();

        return RenderContext.runWithContext(renderContext, () -> {

            List<TrieNode> roots = asList(rootCodes).stream()
                    .filter(code -> interestedCode(code))
                    .map(code -> codeToNode(code, null)) // code could be decorated
                    .collect(toList());  // use list to respect the sequence when the tag is defined.
            return roots;

        });
    }

    private boolean interestedCode(Code code) {
        return code instanceof ValueCode || code instanceof IterableCode;
    }

    private static WithInputField parseField(String codeName) {
        WithInputField field = null;
        if (codeName.startsWith(Constants.CMD_PREFIX)) {
            field = CommandParser.parse(codeName);
        } else if (codeName.startsWith(Constants.FUNC_PREFIX)) {
            field = FunctionParser.parse(codeName);
        } else if (RenderContext.getContext().isVariable(codeName)) {
            field = new VariableField(codeName);
        } else {
            field = MergeFieldParser.parse(codeName);
        }
        return field;
    }

    private WithInputField parseField(Code code) {
        WithInputField field = null;
        String codeName = code.getName();
        try {
            field = parseField(codeName);
            return field;
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            ValidationException ve = e instanceof ValidationException ? (ValidationException) e :
                    rootCause instanceof ValidationException ? (ValidationException) rootCause : null;

            if (ve != null) {
                if (ErrorCode.InnerTextRequired.equals(ve.getErrorCode())) {
                    if (code.getCodes() == null || code.getCodes().length != 1) {
                        throw new ValidationException(ErrorCode.InvalidCommandArgument,
                                "Invalid merge field:" + codeName, e);
                    }

                    Code writeCode = code.getCodes()[0];
                    Asserts.assertTrue(writeCode instanceof WriteCode, ErrorCode.InnerTextRequired,
                            codeName + " expects an inner text.");
                    WriteCode textCode = (WriteCode) writeCode;
                    StringWriter stringWriter = new StringWriter();
                    textCode.identity(stringWriter);
                    codeName = codeName + "\n" + stringWriter.getBuffer().toString();
                    field = parseField(codeName);
                    return field;
                } else {
                    throw ve;
                }
            }
        }

        throw new ValidationException(ErrorCode.UnknownField, "Unable to parse merge field:" + codeName);
    }

    private TrieNode codeToNode(Code code, TrieNode parent) {
        WithInputField field = parseField(code);

        List<String> metaDataPath = findMetaDataPath(field, parent);
        TrieNode node = new TrieNode(field, metaDataPath, parent);

        if (parent != null && parent.getInputField() instanceof ChildrenAware) {
            ((ChildrenAware) parent.getInputField()).addChild(field);
        }
        if (code.getCodes() != null) {
            for (Code childCode : code.getCodes()) {
                if (interestedCode(childCode)) {
                    codeToNode(childCode, node);
                }
            }
        }
        return node;
    }

    /**
     * <p>Given a inputFieldName, in context of a TrieTree, return the Metadata path
     * from root to the field in question.</p>
     *
     * <p>The `parent` node acts as the hint for path finding.</p>
     *
     * @return meta data path from root to the field.
     */
    private List<String> findMetaDataPath(WithInputField field, TrieNode parent) {
        String inputFieldName =
                field instanceof VariableField ? ((VariableField) field).getVariableName() : field.getInputFieldName();
        RenderContext context = RenderContext.getContext();
        Variable variable = context.getVariable(inputFieldName);
        boolean isGlobalVariable = variable != null && variable.isGlobal();
        // global variable has no metadata.
        if (isGlobalVariable) {
            return Collections.emptyList();
        }

        String rootName =
                parent == null ? inputFieldName : parent.root().getInputField().getInputFieldName();
        // some command fields have no input field.
        if (StringUtils.isNullOrBlank(inputFieldName)) {
            return Collections.emptyList();
        }

        boolean currentObjectAsInput = Objects.equals(inputFieldName, WithInputField.CURRENT_OBJECT);
        boolean isWrapper = inputFieldName.startsWith(Constants.WRAPPER_PREFIX);
        boolean isSpecialField = currentObjectAsInput || isWrapper;

        if (isSpecialField && parent != null) {
            return parent.inputObjectMetaPath();
        } else if (parent != null) {
            // if it's special field and parent is null.
            List<String> stackPath = getStackPath(rootName, parent);
            stackPath.add(inputFieldName);
            // turn dotted path into path list.
            stackPath = stackPath.stream()
                    .flatMap(fieldName -> dottedPathToList(fieldName).stream())
                    .filter(path -> !path.startsWith(Constants.WRAPPER_PREFIX))
                    .collect(toList());
            return ListUtils.flatten(stackPath);
        } else {
            return ListUtils.flatten(Arrays.asList(inputFieldName));
        }
    }
    /**
     * Find the stack path in context of data
     */
    private List<String> getStackPath(String rootName, TrieNode parent) {
        if (parent == null) {
            List<String> rtn = new LinkedList<>();
            rtn.add(rootName);
            return rtn;
        }
        return parent.pathFromRoot().stream()
                // since one node label could contain multiple fields
                // for example, {{#InvoiceItems|Map(RatePlanCharge)}}
                .flatMap(node -> {
                    WithInputField inputField = node.getInputField();
                    List<String> inputPath = new LinkedList<>();
                    inputPath.add(inputField.getInputFieldName());
                    if (inputField instanceof MergeField) {
                        List<String> additionalFields = ((MergeField) inputField)
                                .getDecorators()
                                .map(decorators -> decorators.stream()
                                        .filter(decorator -> Constants.SWITCH_CONTEXT_DECORATORS.contains(
                                                decorator.getClass()))
                                        .filter(decorator -> decorator instanceof WithFieldArg)
                                        .flatMap(decorator -> ((WithFieldArg) decorator).getArgFieldNames()
                                                .stream())
                                        .collect(toList()))
                                .orElse(new LinkedList<>());
                        inputPath.addAll(additionalFields);
                    }
                    return inputPath.stream();
                })
                // command field might have no input field name.
                .filter(StringUtils::notNullOrBlank)
                // To support {{^InvoiceItems|IsEmpty}} {{#InvoiceItems}} ...,
                // same contiguous keys in the path.
                // Note: can't use `stream.distinct()` here.
                .reduce(new LinkedList<String>(),
                        (acc, item) -> {
                            if (!acc.isEmpty() && item.equals(acc.getLast())) {
                                return acc;
                            }
                            acc.add(item);
                            return acc;
                        },
                        (l1, l2) -> {
                            LinkedList<String> all = new LinkedList<>();
                            all.addAll(l1);
                            all.addAll(l2);
                            return all;
                        }
                );
    }
}

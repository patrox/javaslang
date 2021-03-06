/*     / \____  _    _  ____   ______  / \ ____  __    _______
 *    /  /    \/ \  / \/    \ /  /\__\/  //    \/  \  //  /\__\   JΛVΛSLΛNG
 *  _/  /  /\  \  \/  /  /\  \\__\\  \  //  /\  \ /\\/ \ /__\ \   Copyright 2014-2016 Javaslang, http://javaslang.io
 * /___/\_/  \_/\____/\_/  \_/\__\/__/\__\_/  \_//  \__/\_____/   Licensed under the Apache License, Version 2.0
 */
package javaslang.match;

import javax.annotation.processing.Messager;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.tools.Diagnostic;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;

class Generator {

    // corresponds to the number of Javaslang Tuples.
    private static final int ARITY = 8;

    private static final Set<String> ATOMICS = new HashSet<>(Arrays.asList(
            java.lang.Boolean.class.getName(),
            java.lang.Byte.class.getName(),
            java.lang.Character.class.getName(),
            java.lang.Double.class.getName(),
            java.lang.Float.class.getName(),
            java.lang.Integer.class.getName(),
            java.lang.Long.class.getName(),
            java.lang.Number.class.getName(),
            java.lang.Short.class.getName(),
            java.lang.String.class.getName(),
            java.math.BigDecimal.class.getName(),
            java.math.BigInteger.class.getName()
    ));

    private static final Map<String, String> UNBOXED = new HashMap<>();

    static {
        UNBOXED.put("java.lang.Boolean", "boolean");
        UNBOXED.put("java.lang.Byte", "byte");
        UNBOXED.put("java.lang.Character", "char");
        UNBOXED.put("java.lang.Double", "double");
        UNBOXED.put("java.lang.Float", "float");
        UNBOXED.put("java.lang.Integer", "int");
        UNBOXED.put("java.lang.Long", "long");
        UNBOXED.put("java.lang.Short", "short");
    }

    // ENTRY POINT: Expands one @Patterns class
    static Optional<String> generate(TypeElement typeElement, Messager messager) {
        List<ExecutableElement> executableElements = getMethods(typeElement, messager);
        if (executableElements.isEmpty()) {
            messager.printMessage(Diagnostic.Kind.WARNING, "No @Unapply methods found.", typeElement);
            return Optional.empty();
        } else {
            final String _package = Elements.getPackage(typeElement);
            final String _class = Elements.getSimpleName(typeElement);
            final ImportManager im = ImportManager.of(_package);
            im.getStatic("javaslang.Match.*");
            final String methods = generate(im, typeElement, executableElements);
            final String result = (_package.isEmpty() ? "" : "package " + _package + ";\n\n") +
                    im.getImports() + "\n\n" +
                    "// GENERATED <<>> JAVASLANG\n" +
                    "// derived from " + typeElement.getQualifiedName() + "\n\n" +
                    "public final class " + _class + " {\n\n" +
                    "    private " + _class + "() {\n" +
                    "    }\n\n" +
                    methods +
                    "}\n";
            return Optional.of(result);
        }
    }

    // Expands the @Unapply methods of a @Patterns class
    private static String generate(ImportManager im, TypeElement typeElement, List<ExecutableElement> executableElements) {
        final StringBuilder builder = new StringBuilder();
        for (ExecutableElement executableElement : executableElements) {
            generate(im, typeElement, executableElement, builder);
            builder.append("\n");
        }
        return builder.toString();
    }

    // Expands one @Unapply method
    private static void generate(ImportManager im, TypeElement type, ExecutableElement elem, StringBuilder builder) {
        final String typeName = Elements.getRawParameterType(elem, 0);
        final String name = elem.getSimpleName().toString();
        int arity = getArity(elem);
        if (arity == 0) {
            builder.append("    public static final Pattern0 ")
                    .append(name).append(" = Pattern0.create(").append(typeName).append(".class);\n");
        } else {

            final int[] maxArity = getMaxArity(Elements.getReturnTypeArgs(elem));
            final List<List<Param>> variations = Lists.crossProduct(Arrays.asList(Param.values()), arity)
                    .stream()
                    .filter(params -> params.stream().map(Param::arity).reduce((a, b) -> a + b).get() <= ARITY)
                    .filter(params -> isDecomposable(params, maxArity))
                    .collect(Collectors.toList());
            for (List<Param> variation : variations) {
                final String method = "public static " +
                        getGenerics(im, elem, variation) + " " +
                        getReturnType(im, elem, variation) + " " +
                        name + getParams(elem, variation) + " {\n" +
                        "        return " + generateBody(type, elem, variation) + "\n" +
                        "    }";
                builder.append("    ").append(method).append("\n");
            }
        }
    }

    private static int[] getMaxArity(String[] types) {
        int[] maxArity = new int[types.length];
        int i = 0;
        for (String type : types) {
            // TODO: Class.forName(type).isAssignableFrom(any of ATOMICS) ? 1 : ARITY
            maxArity[i++] = ATOMICS.contains(type) ? 1 : ARITY;
        }
        return maxArity;
    }

    private static boolean isDecomposable(List<Param> params, int[] maxArity) {
        int i = 0;
        for (Param param : params) {
            if (param.arity > maxArity[i++]) {
                return false;
            }
        }
        return true;
    }

    // Expands the generics part of a method declaration
    private static String getGenerics(ImportManager im, ExecutableElement elem, List<Param> variation) {
        List<String> result = new ArrayList<>();
        result.add("__ extends " + Elements.getParameterType(elem, 0));
        result.addAll(Arrays.asList(Elements.getTypeParameters(elem)));
        int j = 1;
        for (Param param : variation) {
            // Pattern0 has no result types, InversePattern takes pre-defined result tuple type parameter
            if (param != Param.Pattern0 && param != Param.InversePattern) {
                for (int i = 1; i <= param.arity; i++) {
                    result.add("T" + (j++));
                }
            }
        }
        return result.stream().collect(joining(", ", "<", ">"));
    }

    // Expands the return type of a method declaration
    private static String getReturnType(ImportManager im, ExecutableElement elem, List<Param> variation) {
        final int resultArity = Param.getArity(variation);
        if (resultArity == 0) {
            return "Pattern0";
        } else {
            final List<String> resultTypes = new ArrayList<>();
            resultTypes.add(Elements.getParameterType(elem, 0));
            resultTypes.addAll(getResultTypeArgs(elem, variation));
            return "Pattern" + resultArity + "<" + resultTypes.stream().collect(joining(", ")) + ">";
        }
    }

    // Generic type arguments of result
    private static List<String> getResultTypeArgs(ExecutableElement elem, List<Param> variation) {
        final List<String> resultTypes = new ArrayList<>();
        final String[] tupleArgTypes = Elements.getReturnTypeArgs(elem);
        int j = 1;
        for (int i = 0; i < variation.size(); i++) {
            Param param = variation.get(i);
            if (param == Param.InversePattern) {
                resultTypes.add(tupleArgTypes[i]);
            } else if (param != Param.T && param != Param.Pattern0) {
                for (int k = 1; k <= param.arity; k++) {
                    resultTypes.add("T" + (j++));
                }
            }
        }
        return resultTypes;
    }

    // Expands the parameters of a method declaration
    private static String getParams(ExecutableElement elem, List<Param> variation) {
        StringBuilder builder = new StringBuilder("(");
        final String[] tupleArgTypes = Elements.getReturnTypeArgs(elem);
        int j = 1;
        for (int i = 0; i < variation.size(); i++) {
            Param param = variation.get(i);
            if (param == Param.T) {
                builder.append(unboxed(tupleArgTypes[i]));
            } else if (param == Param.InversePattern) {
                builder.append(param.name()).append("<? extends ").append(tupleArgTypes[i]).append(">");
            } else if (param == Param.Pattern0) {
                builder.append("Pattern0");
            } else {
                builder.append(param.name()).append("<? extends ").append(tupleArgTypes[i]).append(", ");
                for (int k = 1; k <= param.arity; k++) {
                    builder.append("T").append(j++);
                    if (k < param.arity) {
                        builder.append(", ");
                    }
                }
                builder.append(">");
            }
            builder.append(" ").append("p").append(i + 1);
            if (i < variation.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append(")");
        return builder.toString();
    }

    // fqn = full qualified name
    private static String unboxed(String fqn) {
        return UNBOXED.containsKey(fqn) ? UNBOXED.get(fqn) : fqn;
    }

    // Expands the method body (used for unapplied tuple arity >= 1)
    private static String generateBody(TypeElement type, ExecutableElement elem, List<Param> variation) {
        final StringBuilder builder = new StringBuilder();
        final int unapplyArity = getArity(elem);
        final int resultArity = Param.getArity(variation);
        final String matchableType = Elements.getRawParameterType(elem, 0);
        final String annotatedType = type.getSimpleName().toString();
        final String methodName = elem.getSimpleName().toString();
        final String typeHint = (resultArity == 0) ? "<" + Elements.getParameterType(elem, 0) + ">" : "";
        builder.append("Pattern" + resultArity + "." + typeHint + "create(" + matchableType + ".class, t -> " + annotatedType + "." + methodName + "(t).transform(");
        if (unapplyArity == 1) {
            builder.append("t1");
        } else {
            builder.append("(" + IntStream.rangeClosed(1, unapplyArity).boxed().map(i -> "t" + i).collect(joining(", ")) + ")");
        }
        builder.append(" -> ");
        int j = 1;
        int ignored = 1;
        for (int i = 1; i <= variation.size(); i++) {
            Param param = variation.get(i - 1);
            if (param == Param.T) {
                builder.append("Pattern0.equals(t" + i + ", p" + i + ")");
            } else if (param == Param.InversePattern) {
                builder.append("InversePattern.narrow(p" + i + ").apply(t" + i + ")");
            } else {
                builder.append("p" + i + ".apply(t" + i + ")");
            }
            if (i < variation.size()) {
                final String v = (param.arity == 0) ? "_" + (ignored++) : "v" + (j++);
                builder.append(".flatMap(" + v + " -> ");
            } else {
                // the last pattern contains the relevant information, the patterns before were ignored
                boolean isOptimal = j == 1;
                if (!isOptimal) {
                    // this line needs to remain here because j is increased
                    final String v = (param.arity == 0) ? "_" + (ignored++) : "v" + (j++);
                    builder.append(".map(" + v + " -> ");
                    if (j == 2) {
                        builder.append("v1");
                    } else {
                        builder.append("javaslang.Tuple.of(");
                        List<String> args = new ArrayList<>();
                        int z = 1;
                        for (int k = 1; k <= variation.size(); k++) {
                            final Param p = variation.get(k - 1);
                            if (p.arity > 0) {
                                final String vv = "v" + (z++);
                                if (p.arity == 1) {
                                    args.add(vv);
                                } else {
                                    args.add(IntStream.rangeClosed(1, p.arity).boxed().map(l -> vv + "._" + l).collect(joining(", ")));
                                }
                            }
                        }
                        builder.append(args.stream().collect(joining(", ")));
                        builder.append(")");
                    }
                    builder.append(")");
                }
            }
        }
        for (int i = 1; i <= variation.size(); i++) {
            builder.append(")");
        }
        builder.append(");");
        return builder.toString();
    }

    // returns all @Unapply methods of a @Patterns class
    private static List<ExecutableElement> getMethods(TypeElement typeElement, Messager messager) {
        if (Patterns.Checker.isValid(typeElement, messager)) {
            return typeElement.getEnclosedElements().stream()
                    .filter(element -> element.getAnnotationsByType(Unapply.class).length == 1 &&
                            element instanceof ExecutableElement &&
                            Unapply.Checker.isValid((ExecutableElement) element, messager))
                    .map(element -> (ExecutableElement) element)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    // Not part of Elements helper because specific for this use-case (return type Tuple)
    private static int getArity(ExecutableElement elem) {
        final DeclaredType returnType = (DeclaredType) elem.getReturnType();
        final String simpleName = returnType.asElement().getSimpleName().toString();
        return Integer.parseInt(simpleName.substring("Tuple".length()));
    }

    private enum Param {

        T(0),               // equals
        InversePattern(1),  // $()
        Pattern0(0),        // $_
        Pattern1(1),        // $("test")
        Pattern2(2),        // combinations of the above...
        Pattern3(3),
        Pattern4(4),
        Pattern5(5),
        Pattern6(6),
        Pattern7(7),
        Pattern8(8);

        final int arity;

        Param(int arity) {
            this.arity = arity;
        }

        int arity() {
            return arity;
        }

        static int getArity(List<Param> params) {
            return params.stream().mapToInt(Param::arity).sum();
        }
    }
}

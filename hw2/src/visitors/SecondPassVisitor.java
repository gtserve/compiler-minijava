package visitors;

import syntaxtree.*;
import visitor.GJDepthFirst;

import types.*;

import java.util.ArrayList;
import java.util.Stack;

import static types.EntryType.*;


/*
 * SEM_CHECKS:
 *   - If not basic, Type must exist in Global scope.
 *   - New object must have declared type.
 */

public class SecondPassVisitor extends GJDepthFirst<String, String> {

    private Entry current = null;

    /* For SEM_CHECK of message send. */
    private final Stack<ArrayList<VarEntry>> argStack;

    private final SymbolTable classes;

    public SecondPassVisitor(SymbolTable classes) {
        this.classes = classes;
        argStack = new Stack<>();
    }

    private boolean isBasicType(String type) {
        return (type.equals("int") || type.equals("boolean")
                || type.equals("int[]") || type.equals("boolean[]"));
    }

    /* Overridden visit() methods. */

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
    public String visit(MainClass n, String argu) throws Exception {

        String mainClassId;

        mainClassId = n.f1.accept(this, "decl");

        current = classes.get(mainClassId);
        current = current.lookup("main", METHOD_ENTRY);

        n.f11.accept(this, "decl");

        n.f14.accept(this, argu);
        n.f15.accept(this, argu);

        current = current.getParent();
        current = current.getParent();

        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    public String visit(ClassDeclaration n, String argu) throws Exception {
        String id;

        id = n.f1.accept(this, "decl");
        current = classes.get(id);

        n.f3.accept(this, argu);
        n.f4.accept(this, argu);

        current = current.getParent();

        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    public String visit(ClassExtendsDeclaration n, String argu) throws Exception {
        String classId;

        classId = n.f1.accept(this, "decl");
        current = classes.get(classId);

        n.f3.accept(this, "decl");

        n.f5.accept(this, argu);
        n.f6.accept(this, argu);

        current = current.getParent();

        return null;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public String visit(VarDeclaration n, String argu) throws Exception {
        String type = n.f0.accept(this, argu);
        String id = n.f1.accept(this, "decl");

        if (!isBasicType(type)) {
            ClassEntry classType = (ClassEntry) classes.get(type);
            VarEntry ve = (VarEntry) current.lookup(id, VAR_ENTRY);
            ve.setClassType(classType);
        }

        return null;
    }

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    public String visit(MethodDeclaration n, String argu) throws Exception {
        String id, retExpr, methodType;

        id = n.f2.accept(this, "decl");

        ClassEntry currentClass = (ClassEntry) current;
        current = currentClass.getMethods().get(id);

        n.f4.accept(this, argu);
        n.f7.accept(this, argu);
        n.f8.accept(this, argu);

        retExpr = n.f10.accept(this, argu);
        methodType = current.getType();

        /* SEM_CHECK: Return type and return expression must match. */
        if (isBasicType(retExpr) || isBasicType(methodType)) {
            if (!methodType.equals(retExpr)) {
                throw new SemanticException("SEM_ERROR: MethodDeclaration: Return "
                        + "expression does not match return type.");
            }
        } else {
            /* SEM_CHECK: subtyping */
            if (!methodType.equals(retExpr)) {
                ClassEntry ce = (ClassEntry) classes.get(retExpr);
                if (!ce.inherits(methodType)) {
                    throw new SemanticException("SEM_ERROR: MethodDeclaration: Return "
                            + "expression does not match return type.");
                }
            }
        }

        /* Go to parent SymbolTable. */
        current = current.getParent();

        return null;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    public String visit(FormalParameter n, String argu) throws Exception {
        String type = n.f0.accept(this, argu);
        String id = n.f1.accept(this, "decl");

        if (!isBasicType(type)) {
            ClassEntry classType = (ClassEntry) classes.get(type);
            VarEntry ve = (VarEntry) current.lookup(id, VAR_ENTRY);
            ve.setClassType(classType);
        }

        return null;
    }

    /**
     * f0 -> ArrayType()
     * | BooleanType()
     * | IntegerType()
     * | Identifier()
     */
    public String visit(Type n, String argu) throws Exception {
        return n.f0.accept(this, "typeId");
    }

    /**
     * f0 -> "boolean"
     * f1 -> "["
     * f2 -> "]"
     */
    public String visit(BooleanArrayType n, String argu) throws Exception {
        return "boolean[]";
    }

    /**
     * f0 -> "int"
     * f1 -> "["
     * f2 -> "]"
     */
    public String visit(IntegerArrayType n, String argu) throws Exception {
        return "int[]";
    }

    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    public String visit(AssignmentStatement n, String argu) throws Exception {
        String id = n.f0.accept(this, argu);
        String expr = n.f2.accept(this, argu);

        /* SEM_CHECK: Identifier type and expression must match. */
        if (isBasicType(expr) || isBasicType(id)) {
            if (!id.equals(expr)) {
                throw new SemanticException("SEM_ERROR: AssignmentStatement identifier "
                        + "does not match expression");
            }
        } else {
            /* SEM_CHECK: subtyping */
            if (!id.equals(expr)) {
                ClassEntry ce = (ClassEntry) classes.get(expr);
                if (!ce.inherits(id)) {
                    throw new SemanticException("SEM_ERROR: AssignmentStatement identifier "
                            + "does not match expression or wrong subtyping");
                }
            }
        }

        return null;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "["
     * f2 -> Expression()
     * f3 -> "]"
     * f4 -> "="
     * f5 -> Expression()
     * f6 -> ";"
     */
    public String visit(ArrayAssignmentStatement n, String argu) throws Exception {

        String id = n.f0.accept(this, argu);
        /* SEM_CHECK: id must be int[]/boolean[]. */
        if (!id.equals("int[]") && !id.equals("boolean[]")) {
            throw new SemanticException("SEM_ERROR: Identifier in ArrayAssignment must"
                    + " be of type int[] or boolean[]");
        }

        String expr1 = n.f2.accept(this, argu);
        /* SEM_CHECK: Expression must be int. */
        if (!expr1.equals("int")) {
            throw new SemanticException("SEM_ERROR: Expression in ArrayAssignment must"
                    + " be int");
        }

        String expr2 = n.f5.accept(this, argu);

        if (id.equals("int[]")) {
            if (!expr2.equals("int")) {
                throw new SemanticException("SEM_ERROR: Assignment expression identifier"
                        + " in ArrayAssignment must be of type int");
            }
        } else {
            if (!expr2.equals("boolean")) {
                throw new SemanticException("SEM_ERROR: Assignment expression identifier"
                        + " in ArrayAssignment must be of type boolean");
            }
        }

        return null;
    }

    /**
     * f0 -> "if"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     * f5 -> "else"
     * f6 -> Statement()
     */
    public String visit(IfStatement n, String argu) throws Exception {

        String expr = n.f2.accept(this, argu);
        /* SEM_CHECK: Expression must be boolean. */
        if (!expr.equals("boolean")) {
            throw new SemanticException("SEM_ERROR: Expression in if statements must"
                    + " be boolean");
        }

        n.f4.accept(this, argu);
        n.f6.accept(this, argu);

        return null;
    }

    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     */
    public String visit(WhileStatement n, String argu) throws Exception {

        String expr = n.f2.accept(this, argu);
        /* SEM_CHECK: Expression must be boolean. */
        if (!expr.equals("boolean")) {
            throw new SemanticException("SEM_ERROR: Expression in while statements must"
                    + " be boolean");
        }

        n.f4.accept(this, argu);

        return null;
    }

    /**
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */
    public String visit(PrintStatement n, String argu) throws Exception {

        String expr = n.f2.accept(this, argu);
        /* SEM_CHECK: Expression in PrintStatement must be only integer. */
        if (!expr.equals("int")) {
            throw new SemanticException("SEM_ERROR: Expression in PrintStatement "
                    + "must be int.");
        }

        return null;
    }

    /**
     * f0 -> Clause()
     * f1 -> "&&"
     * f2 -> Clause()
     */
    public String visit(AndExpression n, String argu) throws Exception {

        String expr1 = n.f0.accept(this, argu);
        /* SEM_CHECK: Expression must be boolean. */
        if (!expr1.equals("boolean")) {
            throw new SemanticException("SEM_ERROR: AndExpression");
        }

        String expr2 = n.f2.accept(this, argu);
        /* SEM_CHECK: Expression must be boolean. */
        if (!expr2.equals("boolean")) {
            throw new SemanticException("SEM_ERROR: AndExpression");
        }

        return "boolean";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public String visit(CompareExpression n, String argu) throws Exception {

        String expr1 = n.f0.accept(this, argu);
        /* SEM_CHECK: Expression must be int. */
        if (!expr1.equals("int")) {
            throw new SemanticException("SEM_ERROR: CompareExpression");
        }

        String expr2 = n.f2.accept(this, argu);
        /* SEM_CHECK: Expression must be int. */
        if (!expr2.equals("int")) {
            throw new SemanticException("SEM_ERROR: CompareExpression");
        }

        return "boolean";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public String visit(PlusExpression n, String argu) throws Exception {

        String expr1 = n.f0.accept(this, argu);
        /* SEM_CHECK: Expression must be int. */
        if (!expr1.equals("int")) {
            throw new SemanticException("SEM_ERROR: PlusExpression");
        }

        String expr2 = n.f2.accept(this, argu);
        /* SEM_CHECK: Expression must be int. */
        if (!expr2.equals("int")) {
            throw new SemanticException("SEM_ERROR: PlusExpression");
        }

        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public String visit(MinusExpression n, String argu) throws Exception {

        String expr1 = n.f0.accept(this, argu);
        /* SEM_CHECK: Expression must be int. */
        if (!expr1.equals("int")) {
            throw new SemanticException("SEM_ERROR: MinusExpression");
        }

        String expr2 = n.f2.accept(this, argu);
        /* SEM_CHECK: Expression must be int. */
        if (!expr2.equals("int")) {
            throw new SemanticException("SEM_ERROR: MinusExpression");
        }

        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public String visit(TimesExpression n, String argu) throws Exception {

        String expr1 = n.f0.accept(this, argu);
        /* SEM_CHECK: Expression must be int. */
        if (!expr1.equals("int")) {
            throw new SemanticException("SEM_ERROR: TimesExpression");
        }

        String expr2 = n.f2.accept(this, argu);
        /* SEM_CHECK: Expression must be int. */
        if (!expr2.equals("int")) {

            throw new SemanticException("SEM_ERROR: TimesExpression");
        }

        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    public String visit(ArrayLookup n, String argu) throws Exception {
        String expr1, expr2;

        expr1 = n.f0.accept(this, argu);

        /* SEM_CHECK: Expression must be int[]/boolean[]. */
        if (!expr1.equals("int[]") && !expr1.equals("boolean[]")) {
            throw new SemanticException("SEM_ERROR: ArrayLookup");
        }

        expr2 = n.f2.accept(this, argu);

        /* SEM_CHECK: Expression must be only integer. */
        if (!expr2.equals("int")) {
            throw new SemanticException("SEM_ERROR: ArrayLookup");
        }

        if (expr1.equals("int[]"))
            return "int";
        else
            return "boolean";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public String visit(ArrayLength n, String argu) throws Exception {
        String expr = n.f0.accept(this, argu);

        /* SEM_CHECK: Expression must be int[]/boolean[]. */
        if (!expr.equals("int[]") && !expr.equals("boolean[]")) {
            throw new SemanticException("SEM_ERROR: ArrayLength");
        }

        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    public String visit(MessageSend n, String argu) throws Exception {
        String type = n.f0.accept(this, argu);
        String id = n.f2.accept(this, "methodId");

        /* SEM_CHECK: Method must belong to class. */
        MethodEntry me = (MethodEntry) classes.get(type).lookup(id, METHOD_ENTRY);
        if (me == null) {
            throw new SemanticException("SEM_ERROR: Class '" + type
                    + "' can't resolve method '" + id + "'");
        }

        argStack.push(new ArrayList<>());

        n.f4.accept(this, argu);

//        System.out.println("MESSAGE_SEND: Arguments:");
//        for (VarEntry argument: argStack.peek())
//            argument.print();

        /* SEM_CHECK: Method call must match method prototype. */
        if (!me.matchArgs(argStack.pop())) {
            throw new SemanticException("SEM_ERROR: Incorrect method call for '"
                    + id + "'");
        }

//        System.out.println("MESSAGE_SEND: End Arguments:");

        return me.getType();
    }

    /**
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
    public String visit(ExpressionList n, String argu) throws Exception {
        String expr;
        VarEntry ve;

        expr = n.f0.accept(this, argu);

        if (isBasicType(expr)) {
            ve = new VarEntry(null, 0, "dummy", expr);
            argStack.peek().add(ve);
        } else {
            ve = new VarEntry(null, 0, "dummy", expr);
            ve.setClassType((ClassEntry) classes.get(expr));
            argStack.peek().add(ve);
        }

        n.f1.accept(this, argu);

        return null;
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    public String visit(ExpressionTerm n, String argu) throws Exception {
        String expr;
        VarEntry ve;

        expr = n.f1.accept(this, argu);

        if (isBasicType(expr)) {
            ve = new VarEntry(null, 0, "dummy", expr);
            argStack.peek().add(ve);
        } else {
            ve = new VarEntry(null, 0, "dummy", expr);
            ve.setClassType((ClassEntry) classes.get(expr));
            argStack.peek().add(ve);
        }

        return null;
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    public String visit(IntegerLiteral n, String argu) throws Exception {
        return "int";
    }

    /**
     * f0 -> "true"
     */
    public String visit(TrueLiteral n, String argu) throws Exception {
        return "boolean";
    }

    /**
     * f0 -> "false"
     */
    public String visit(FalseLiteral n, String argu) throws Exception {
        return "boolean";
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    public String visit(Identifier n, String argu) throws Exception {
        Entry entry;
        String id = n.f0.accept(this, argu);

        if (argu != null) {
            if (argu.equals("decl") || argu.equals("methodId")) {
                return id;
            } else if (argu.equals("typeId")) {
                /* SEM_CHECK: Types/Classes must be declared. */
                if (!classes.contains(id)) {
                    throw new SemanticException("SEM_ERROR: Can't resolve type '"
                            + id + "'");
                } else {
                    return id;
                }
            }
        }

        if (!isBasicType(id)) {
            /* SEM_CHECK: Variables must be declared. */
            entry = current.lookupForUse(id, VAR_ENTRY);
            if (entry == null) {
                throw new SemanticException("SEM_ERROR: Can't resolve variable '"
                        + id + "'");
            } else {
                return entry.getType();
            }
        }

        return id;
    }

    /**
     * f0 -> "this"
     */
    public String visit(ThisExpression n, String argu) throws Exception {
        return current.getParent().getName();
    }

    /**
     * f0 -> "new"
     * f1 -> "boolean"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public String visit(BooleanArrayAllocationExpression n, String argu) throws Exception {
        String expr = n.f3.accept(this, argu);

        /* SEM_CHECK: Expression must be only integer. */
        if (!expr.equals("int")) {
            throw new SemanticException("SEM_ERROR: BooleanArrayAllocationExpression");
        }

        return "boolean[]";
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public String visit(IntegerArrayAllocationExpression n, String argu) throws Exception {
        String expr = n.f3.accept(this, argu);

        /* SEM_CHECK: Expression must be only integer. */
        if (!expr.equals("int")) {
            throw new SemanticException("SEM_ERROR: IntegerArrayAllocationExpression");
        }

        return "int[]";
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public String visit(AllocationExpression n, String argu) throws Exception {
        return n.f1.accept(this, "typeId");
    }

    /**
     * f0 -> "!"
     * f1 -> Clause()
     */
    public String visit(NotExpression n, String argu) throws Exception {
        return "boolean";
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    public String visit(BracketExpression n, String argu) throws Exception {
        return n.f1.accept(this, argu);
    }


    public String visit(NodeToken n, String argu) throws Exception {
        return n.toString();
    }
}

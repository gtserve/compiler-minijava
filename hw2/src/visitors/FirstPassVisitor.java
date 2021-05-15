package visitors;

import syntaxtree.*;
import visitor.GJDepthFirst;
import types.*;

import static types.EntryType.*;


/*
 * SEM_CHECKS:
 *   - Super Class must be declared
 *   - Unique Method name
 *   - Correct Method overriding
 *   - Unique Variable name
 */

public class FirstPassVisitor extends GJDepthFirst<String, String> {

    private int counter = 0;
    private Entry current = null;
    private SymbolTable classes = new SymbolTable("Global");

    public SymbolTable getClasses() {
        return classes;
    }

    private boolean isBasicType(String type) {
        return (type.equals("int") || type.equals("boolean")
                || type.equals("int[]") || type.equals("boolean[]"));
    }

    public void printDeclarations() {
        System.out.println("----------------- Declarations -----------------");
        classes.print("");
    }

    public void printOffsets() {
        System.out.println("-------------------- Offsets -------------------");
        for (Entry entry : classes.getEntries())
            entry.printOffsets();
    }

    /* --------------------- Overridden visit() methods --------------------- */

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

        mainClassId = n.f1.accept(this, argu);

        /* Entry for Main class */
        SymbolTable methods = new SymbolTable(mainClassId);
        ClassEntry mainClass = new ClassEntry(null, counter++,
                mainClassId, null, methods);
        classes.insert(mainClass);
        current = mainClass;

        /* Entry for main method */
        SymbolTable params = new SymbolTable("main");
        SymbolTable locals = new SymbolTable("main");
        MethodEntry mainMethod = new MethodEntry(current, counter++, "main",
                "void", params, locals);
        current.insertMethod(mainMethod);
        current = mainMethod;

        n.f11.accept(this, argu);
        /* Ignore main parameter */

        n.f14.accept(this, argu);
        n.f15.accept(this, argu);

        /* Go to parent Entry x2. */
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

        id = n.f1.accept(this, argu);

        /* Entry and SymbolTable for class. */
        SymbolTable fields = new SymbolTable(id);
        SymbolTable methods = new SymbolTable(id);
        ClassEntry ce = new ClassEntry(null, counter++, id,
                fields, methods);
        classes.insert(ce);
        current = ce;

        n.f3.accept(this, argu);
        n.f4.accept(this, argu);

        ce.makeOffsets();

        /* Go to parent Entry. */
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
        String classId, superClassId;

        classId = n.f1.accept(this, argu);
        superClassId = n.f3.accept(this, argu);

        /* SEM_CHECK: Super Class must be declared. */
        ClassEntry superClass = (ClassEntry) classes.get(superClassId);
        if (superClass == null) {
            throw new SemanticException("SEM_ERROR: Class '" + superClassId
                    + "' hasn't been declared!");
        }

        /* Entry and SymbolTable for class. */
        SymbolTable fields = new SymbolTable(classId);
        SymbolTable methods = new SymbolTable(classId);
        ClassEntry ce = new ClassEntry(null, counter++, classId,
                fields, methods, superClass);
        classes.insert(ce);
        current = ce;

        n.f5.accept(this, argu);
        n.f6.accept(this, argu);

        ce.makeOffsets();

        /* Go to parent Entry. */
        current = current.getParent();

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
        String id, type;

        type = n.f1.accept(this, argu);
        id = n.f2.accept(this, argu);

        /* SEM_CHECK: Unique Method name. */
        ClassEntry currentClass = (ClassEntry) current;
        if (currentClass.getMethods().contains(id)) {
            throw new SemanticException("SEM_ERROR: Method '" + id
                    + "' is already defined in this class!");
        }

        /* Entry for method. */
        SymbolTable params = new SymbolTable(id);
        SymbolTable locals = new SymbolTable(id);
        MethodEntry me = new MethodEntry(current, counter++, id, type,
                params, locals);

        current = me;
        n.f4.accept(this, argu);
        current = current.getParent();

        /* SEM_CHECK: Correct Method overriding. */
        MethodEntry me2 = (MethodEntry) me.getParent().lookup(id, METHOD_ENTRY);
        if (me2 != null) {
            if (!me.matches(me2)) {
                throw new SemanticException("SEM_ERROR: Incorrect override with "
                        + "method '" + id + "'");
            }
        }

        current.insertMethod(me);
        current = me;

        n.f7.accept(this, argu);
        n.f8.accept(this, argu);

        n.f10.accept(this, argu);

        /* Go to parent Entry. */
        current = current.getParent();

        return null;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    public String visit(FormalParameter n, String argu) throws Exception {
        String id, type;

        type = n.f0.accept(this, argu);
        id = n.f1.accept(this, argu);

        /* SEM_CHECK: Unique Parameter name. */
        if (current.lookup(id, VAR_ENTRY) != null) {
            throw new SemanticException("SEM_ERROR: Parameter '" + id
                    + "' is already defined in this scope!");
        }

        /* For every parameter, a new Entry. */
        VarEntry ve = new VarEntry(current, counter++, id, type);
        current.insertParam(ve);

        return null;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public String visit(VarDeclaration n, String argu) throws Exception {
        String id, type;

        type = n.f0.accept(this, argu);
        id = n.f1.accept(this, argu);

        /* For every variable, a new Entry. */
        VarEntry ve = new VarEntry(current, counter++, id, type);

        /* SEM_CHECK: Unique Variable name. */
        if (current.getEntryType() == CLASS_ENTRY) {
            ClassEntry currentClass = (ClassEntry) current;
            if (currentClass.getFields().contains(id)) {
                throw new SemanticException("SEM_ERROR: Field '" + id
                        + "' is already defined in this class!");
            }
            currentClass.insertField(ve);
        } else if (current.getEntryType() == METHOD_ENTRY) {
            MethodEntry currentMethod = (MethodEntry) current;
            if (currentMethod.getParams().contains(id)
                    || currentMethod.getLocals().contains(id)) {
                throw new SemanticException("SEM_ERROR: Variable '" + id
                        + "' is already defined in this scope!");
            }
            currentMethod.insertLocal(ve);
        }

        return null;
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

    public String visit(NodeToken n, String argu) throws Exception {
        return n.toString();
    }
}

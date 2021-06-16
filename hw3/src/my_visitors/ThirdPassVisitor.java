package my_visitors;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import my_types.*;

import static my_types.EntryType.*;

import syntaxtree.*;
import visitor.GJDepthFirst;


/*  Third-Pass Visitor: Generate LLVM IR */


public class ThirdPassVisitor extends GJDepthFirst<String, String> {

    private final SymbolTable global;
    private final FileWriter fileWriter;
    private Integer labelCounter = 0;
    private Integer tempCounter = 0;
    private Entry curScope;

    public ThirdPassVisitor(SymbolTable global, FileWriter fileWriter) {
        this.global = global;
        this.fileWriter = fileWriter;
        this.curScope = null;
    }

    private String getIRType(String type) {
        switch (type) {
            case "int":
                return "i32";
            case "boolean":
                return "i1";
            case "int[]":
                return "i32*";
            case "boolean[]":
                return "i8*";
            default:
                return "i8*";
        }
    }

    private String newLabel(String name) {
        return "%" + (labelCounter++).toString() + "_" + name;
    }

    private String newTemp() {
        return "%_" + (tempCounter++).toString();
    }

    private void emit(String str) {
        try {
            fileWriter.write(str + "\n");
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }

    private void emitVTables() {
        ArrayList<Entry> classes = global.getEntries();
        ArrayList<Entry> methods;
        ClassEntry ce;

        for (int i = 0; i < classes.size(); i++) {
            ce = (ClassEntry) classes.get(i);
            methods = getTableEntries(ce);

            if (methods.isEmpty() || i == 0) {
                /* Empty vtable for classes without methods and MainClass. */
                emit("@." + ce.getName() + "_vtable = global [0 x i8*] []");
                emit("");
            } else {
                ArrayList<Entry> params;
                MethodEntry me;

                emit("@." + ce.getName() + "_vtable = global ["
                        + methods.size() + " x i8*] [");

                for (int j = 0; j < methods.size(); j++) {
                    me = (MethodEntry) methods.get(j);
                    params = me.getParams().getEntries();
                    StringBuilder str = new StringBuilder();

                    str.append("\ti8* bitcast (");
                    str.append(getIRType(me.getType()));
                    str.append(" (i8*");

                    for (Entry pe : params) {
                        str.append(",").append(getIRType(pe.getType()));
                    }

                    str.append(")* @");
                    str.append(me.getParent().getName()).append(".");
                    str.append(me.getName());

                    if (j < methods.size() - 1)
                        str.append(" to i8*),");
                    else {
                        str.append(" to i8*)");
                    }

                    emit(str.toString());
                }

                emit("]");
                emit("");
            }
        }
    }

    private ArrayList<Entry> getTableEntries(ClassEntry ce) {

        if (ce.getSuperClass() == null) {
            return ce.getMethods().getEntries();
        } else {
            ArrayList<Entry> superMethods = getTableEntries(ce.getSuperClass());
            ArrayList<Entry> methods = ce.getMethods().getEntries();
            ArrayList<Entry> tableEntries = new ArrayList<>();

            for (int i = 0; i < superMethods.size(); i++) {
                for (int j = 0; j < methods.size(); j++) {
                    if (superMethods.get(i).getName().equals(methods.get(j).getName())) {
                        superMethods.remove(i);
                        superMethods.add(i, methods.get(j));
                        methods.remove(j);
                        break;
                    }
                }
            }

            tableEntries.addAll(superMethods);
            tableEntries.addAll(methods);

            return tableEntries;
        }
    }

    /* --------------------- Overridden visit() methods --------------------- */

    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */
    public String visit(Goal n, String argu) throws Exception {

        emitVTables();

        /* Boilerplate code */
        emit("declare i8* @calloc(i32, i32)");
        emit("declare i32 @printf(i8*, ...)");
        emit("declare void @exit(i32)");
        emit("");
        emit("@_cint = constant [4 x i8] c\"%d\\0a\\00\"");
        emit("@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"");
        emit("@_cNSZ = constant [15 x i8] c\"Negative size\\0a\\00\"");
        emit("");
        emit("define void @print_int(i32 %i) {");
        emit("\t%_str = bitcast [4 x i8]* @_cint to i8*");
        emit("\tcall i32 (i8*, ...) @printf(i8* %_str, i32 %i)");
        emit("\tret void");
        emit("}");
        emit("");
        emit("define void @throw_oob() {");
        emit("\t%_str = bitcast [15 x i8]* @_cOOB to i8*");
        emit("\tcall i32 (i8*, ...) @printf(i8* %_str)");
        emit("\tcall void @exit(i32 1)");
        emit("\tret void");
        emit("}");
        emit("");
        emit("define void @throw_nsz() {");
        emit("\t%_str = bitcast [15 x i8]* @_cNSZ to i8*");
        emit("\tcall i32 (i8*, ...) @printf(i8* %_str)");
        emit("\tcall void @exit(i32 1)");
        emit("\tret void");
        emit("}");
        emit("");

        n.f0.accept(this, argu);
        n.f1.accept(this, argu);

        return null;
    }

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

        curScope = global.get(mainClassId);
        curScope = curScope.lookup("main", METHOD_ENTRY);
        tempCounter = 0;

        emit("define i32 @main() {");

        n.f14.accept(this, argu);
        //n.f15.accept(this, argu);

        emit("\tret i32 0");
        emit("}\n");

        curScope = curScope.getParent();
        curScope = curScope.getParent();

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
        curScope = global.get(id);

        //n.f3.accept(this, argu);
        n.f4.accept(this, argu);

        curScope = curScope.getParent();

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

        classId = n.f1.accept(this, argu);
        curScope = global.get(classId);

        //n.f3.accept(this, argu);

        //n.f5.accept(this, argu);
        n.f6.accept(this, argu);

        curScope = curScope.getParent();

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

        /* Allocate space in stack for local variable. */
        emit("\t%" + id + " = alloca " + getIRType(type));

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
        String mId, mType, retExpr;

        mType = n.f1.accept(this, argu);
        mId = n.f2.accept(this, argu);

        curScope = curScope.lookup(mId, METHOD_ENTRY);
        tempCounter = 0;
        MethodEntry me = (MethodEntry) curScope;
        ArrayList<Entry> params = me.getParams().getEntries();
        StringBuilder str = new StringBuilder();

        /* Create method definition line. */
        str.append("define ");
        //TODO correct method type - return expr
        str.append(getIRType(mType)).append(" ");
        //str.append("void ");
        str.append("@").append(me.getParent().getName());
        str.append(".").append(me.getName());
        str.append("(i8* %this");
        for (Entry pe : params) {
            str.append(", ").append(getIRType(pe.getType())).append(" ");
            str.append("%p.").append(pe.getName());
        }
        str.append(") {");
        emit(str.toString());

        //n.f4.accept(this, argu);      // Probably useless

        /* Allocate space in stack for parameters. */
        String pId, pType;
        for (Entry pe : params) {
            pId = pe.getName();
            pType = getIRType(pe.getType());
            emit("\t%" + pId + " = alloca " + pType);
            emit("\tstore " + pType + " %p." + pId + ", "
                    + pType + "* %" + pId);
        }
        emit("");

        /* Allocate space in stack for local variables. */
        n.f7.accept(this, argu);
        emit("");

        //n.f8.accept(this, argu);

        retExpr = n.f10.accept(this, argu);

        emit("\tret " + getIRType(mType) + " " + retExpr);
        emit("}\n");

        curScope = curScope.getParent();

        return null;
    }

    /**
     * f0 -> Clause()
     * f1 -> "&&"
     * f2 -> Clause()
     */
    public String visit(AndExpression n, String argu) throws Exception {
        String temp, clause1, clause2;

        temp = newTemp();
        clause1 = n.f0.accept(this, argu);
        clause2 = n.f2.accept(this, argu);

        //TODO Short-circuiting
        emit("\t" + temp + " = and i1 " + clause1 + ", " + clause2);

        return temp;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public String visit(CompareExpression n, String argu) throws Exception {
        String temp, expr1, expr2;

        temp = newTemp();
        expr1 = n.f0.accept(this, argu);
        expr2 = n.f2.accept(this, argu);

        emit("\t" + temp + " = icmp slt i32 " + expr1 + ", " + expr2);

        return temp;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public String visit(PlusExpression n, String argu) throws Exception {
        String temp, expr1, expr2;

        temp = newTemp();
        expr1 = n.f0.accept(this, argu);
        expr2 = n.f2.accept(this, argu);

        emit("\t" + temp + " = add i32 " + expr1 + ", " + expr2);

        return temp;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public String visit(MinusExpression n, String argu) throws Exception {
        String temp, expr1, expr2;

        temp = newTemp();
        expr1 = n.f0.accept(this, argu);
        expr2 = n.f2.accept(this, argu);

        emit("\t" + temp + " = sub i32 " + expr1 + ", " + expr2);

        return temp;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public String visit(TimesExpression n, String argu) throws Exception {
        String temp, expr1, expr2;

        temp = newTemp();
        expr1 = n.f0.accept(this, argu);
        expr2 = n.f2.accept(this, argu);

        emit("\t" + temp + " = mul i32 " + expr1 + ", " + expr2);

        return temp;
    }

    //TODO

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    public String visit(ArrayLookup n, String argu) throws Exception {
        String _ret = null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        return _ret;
    }

    //TODO

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public String visit(ArrayLength n, String argu) throws Exception {
        String _ret = null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return _ret;
    }

    //TODO

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    public String visit(MessageSend n, String argu) throws Exception {
        String _ret = null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "int"
     * f1 -> "["
     * f2 -> "]"
     */
    public String visit(ArrayType n, String argu) throws Exception {
        return "int[]";
    }

    /**
     * f0 -> "!"
     * f1 -> Clause()
     */
    public String visit(NotExpression n, String argu) throws Exception {
        String temp, clause;
        temp = newTemp();
        clause = n.f1.accept(this, argu);
        emit("\t" + temp + " = sub i1 1, " + clause);
        return temp;
    }

    /**
     * f0 -> IntegerLiteral()
     * | TrueLiteral()
     * | FalseLiteral()
     * | Identifier()
     * | ThisExpression()
     * | ArrayAllocationExpression()
     * | AllocationExpression()
     * | BracketExpression()
     */
    public String visit(PrimaryExpression n, String argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> "true"
     */
    public String visit(TrueLiteral n, String argu) throws Exception {
        return "1";
    }

    /**
     * f0 -> "false"
     */
    public String visit(FalseLiteral n, String argu) throws Exception {
        return "0";
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

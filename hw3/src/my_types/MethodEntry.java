package my_types;

import java.util.ArrayList;

import static my_types.EntryType.*;

public class MethodEntry extends Entry {

    private final String returnType;
    private final SymbolTable params;
    private final SymbolTable locals;

    public MethodEntry(Entry parent, int id, String name, String returnType,
                       SymbolTable params, SymbolTable locals) {
        super(parent, id, name);
        this.returnType = returnType;
        this.params = params;
        this.locals = locals;
    }

    public String getType() {
        return returnType;
    }

    public SymbolTable getParams() {
        return params;
    }

    public SymbolTable getLocals() {
        return locals;
    }

    public int getBytes() {
        return 8;
    }

    public EntryType getEntryType() {
        return METHOD_ENTRY;
    }

    public void insertParam(Entry e) {
        params.insert(e);
    }

    public void insertLocal(Entry e) {
        locals.insert(e);
    }

    public Entry lookup(String name, EntryType entryType) {
        Entry entry;

        if (entryType == VAR_ENTRY) {
            entry = params.get(name);
            if (entry == null)
                entry = locals.get(name);
        } else {
            System.err.println("MY_ERROR: MethodEntry.lookup - wrong EntryType");
            return null;
        }

        return entry;
    }

    public Entry lookupForUse(String name, EntryType entryType) {
        Entry entry = null;

        switch (entryType) {
            case VAR_ENTRY:
                entry = params.get(name);
                if (entry == null)
                    entry = locals.get(name);
                if (entry == null)
                    entry = this.getParent().lookup(name, entryType);
                break;
            case METHOD_ENTRY:
                entry = this.getParent().lookup(name, entryType);
                break;
            case CLASS_ENTRY:
                System.err.println("MY_ERROR: MethodEntry.lookupForUse "
                        + "- wrong EntryType");
                return null;
        }

        return entry;
    }

    public boolean matches(MethodEntry me) {
        if (!returnType.equals(me.getType()))
            return false;

        ArrayList<Entry> params1 = this.getParams().getEntries();
        ArrayList<Entry> params2 = me.getParams().getEntries();
        if (params1.size() != params2.size())
            return false;

        for (int i = 0; i < params1.size(); i++)
            if (!params1.get(i).matches((VarEntry) params2.get(i)))
                return false;

        return true;
    }

    public boolean matchArgs(ArrayList<VarEntry> arguments) {
        ArrayList<Entry> params = this.getParams().getEntries();

        if (params.size() != arguments.size())
            return false;

        for (int i = 0; i < params.size(); i++) {
            VarEntry param = (VarEntry) params.get(i);
            VarEntry argum = arguments.get(i);

            if (param.isBasicType() || argum.isBasicType()) {
                if (!param.matches(argum)) {
                    return false;
                }
            } else {
                if (!param.matches(argum)) {
                    if (!argum.subTypes(param)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public boolean isBasicType() {
        return (returnType.equals("int") || returnType.equals("boolean")
                || returnType.equals("int[]") || returnType.equals("boolean[]"));
    }

    public void print() {
        System.out.println("[" + getId() + "] > " + returnType
                + " " + getName());
    }

    public void print(String prevScope) {
        System.out.println("[" + getId() + "] > " + prevScope
                + returnType + " " + getName());
        params.print(prevScope);
        locals.print(prevScope);
    }
}

package my_types;

import static my_types.EntryType.VAR_ENTRY;

public class VarEntry extends Entry {

    private final String type;
    private ClassEntry classType;

    public VarEntry(Entry parent, int id, String name, String type) {
        super(parent, id, name);
        this.type = type;
        this.classType = null;
    }

    public String getType() {
        return type;
    }

    public ClassEntry getClassType() {
        return classType;
    }

    public void setClassType(ClassEntry classType) {
        this.classType = classType;
    }

    public int getBytes() {
        if (type.equals("int")) {
            return 4;
        } else if (type.equals("boolean")) {
            return 1;
        } else {
            /* int[], boolean[], class obj */
            return 8;
        }
    }

    public EntryType getEntryType() {
        return VAR_ENTRY;
    }

    public void insert() {
        System.err.println("Error: VarEntry.insert()");
    }

    public boolean matches(VarEntry ve) {
        return type.equals(ve.getType());
    }

    public boolean subTypes(VarEntry ve) {
        if (classType == null)
            return false;
        return classType.inherits(ve.getType());
    }

    public boolean isBasicType() {
        return (type.equals("int") || type.equals("boolean")
                || type.equals("int[]") || type.equals("boolean[]"));
    }

    public void print() {
        System.out.println("[" + getId() + "] > " + type);
    }

    public void print(String prevScope) {
        System.out.println("[" + getId() + "] > " + prevScope
                + type + " " + getName());
    }
}

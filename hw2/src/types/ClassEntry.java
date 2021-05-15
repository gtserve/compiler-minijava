package types;

import java.util.ArrayList;

import static types.EntryType.*;

public class ClassEntry extends Entry {

    private final SymbolTable fields;
    private final SymbolTable methods;
    private final ClassEntry superClass;
    private final ArrayList<EntryOffset> entryOffsets = new ArrayList<>();

    public ClassEntry(Entry parent, int id, String name, SymbolTable fields,
                      SymbolTable methods) {
        super(parent, id, name);
        this.fields = fields;
        this.methods = methods;
        this.superClass = null;
    }

    public ClassEntry(Entry parent, int id, String name, SymbolTable fields,
                      SymbolTable methods, ClassEntry superClass) {
        super(parent, id, name);
        this.fields = fields;
        this.methods = methods;
        this.superClass = superClass;
    }

    public SymbolTable getFields() {
        return fields;
    }

    public SymbolTable getMethods() {
        return methods;
    }

    public ClassEntry getSuperClass() {
        return superClass;
    }

    public ArrayList<EntryOffset> getEntryOffsets() {
        return entryOffsets;
    }

    public EntryType getEntryType() {
        return CLASS_ENTRY;
    }

    public int getNextVarByte() {
        int nextByte = 0;
        for (EntryOffset eo : entryOffsets)
            if (eo.getEntry().getEntryType() == VAR_ENTRY)
                nextByte = eo.getOffset() + eo.getEntry().getBytes();
        return nextByte;
    }

    public int getNextMethodByte() {
        int nextByte = 0;
        for (EntryOffset eo : entryOffsets)
            if (eo.getEntry().getEntryType() == METHOD_ENTRY)
                nextByte = eo.getOffset() + eo.getEntry().getBytes();
        return nextByte;
    }

    public void makeOffsets() {
        EntryOffset newEO;
        ArrayList<Entry> entries;
        int vOffset = 0, mOffset = 0;
        if (superClass != null) {
            vOffset = superClass.getNextVarByte();
            mOffset = superClass.getNextMethodByte();
        }

        entries = fields.getEntries();
        for (Entry e : entries) {
            newEO = new EntryOffset(e, vOffset);
            entryOffsets.add(newEO);
            vOffset += e.getBytes();
        }

        entries = methods.getEntries();
        for (Entry e : entries) {
            /* Ignore override methods. */
            if (superClass != null)
                if (superClass.lookup(e.getName(), METHOD_ENTRY) != null)
                    continue;

            newEO = new EntryOffset(e, mOffset);
            entryOffsets.add(newEO);
            mOffset += e.getBytes();
        }
    }

    public void insertField(Entry e) {
        fields.insert(e);
    }

    public void insertMethod(Entry e) {
        methods.insert(e);
    }

    public Entry lookup(String name, EntryType entryType) {
        Entry entry = null;

        switch (entryType) {
            case VAR_ENTRY:
                if (fields != null)
                    entry = fields.get(name);
                break;
            case METHOD_ENTRY:
                entry = methods.get(name);
                break;
            case CLASS_ENTRY:
                System.err.println("MY_ERROR: ClassEntry.lookup "
                        + " - wrong EntryType");
                return null;
        }

        if (entry != null)
            return entry;

        if (superClass != null)
            return superClass.lookup(name, entryType);

        return entry;
    }

    public boolean inherits(String name) {
        if (superClass == null)
            return false;

        if (superClass.getName().equals(name)) {
            return true;
        } else {
            return superClass.inherits(name);
        }
    }

    public void print() {
        String buffer = "[" + getId() + "] > " + getName();
        if (superClass != null)
            buffer += " extends '" + superClass.getName() + "'";
        System.out.println(buffer);
    }

    public void print(String prevScope) {
        String buffer = "[" + getId() + "] > " + prevScope + getName();
        if (superClass != null)
            buffer += " extends '" + superClass.getName() + "'";
        System.out.println(buffer);

        if (fields != null)
            fields.print(prevScope);
        methods.print(prevScope);
    }

    public void printOffsets() {
        for (EntryOffset eo : entryOffsets) {
            Entry entry = eo.getEntry();
            System.out.println(entry.getParent().getName() + "." + entry.getName()
                    + ": " + eo.getOffset());
        }
    }
}

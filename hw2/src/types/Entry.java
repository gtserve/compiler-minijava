package types;

public abstract class Entry implements Comparable<Entry> {

    private final Entry parent;
    private final int id;
    private final String name;

    public Entry(Entry parent, int id, String name) {
        this.parent = parent;
        this.id = id;
        this.name = name;
    }

    public Entry getParent() {
        return parent;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /* Overridden method. Never used. */
    public String getType() {
        System.err.println("Error: Entry.getType()");
        return null;
    }

    /* Overridden method. Never used. */
    public int getBytes() {
        System.err.println("Error: Entry.getBytes() or ClassEntry.getBytes()");
        return 0;
    }

    /* Overridden method. Never used. */
    public EntryType getEntryType() {
        System.err.println("Error: Entry.getEntryType()");
        return null;
    }

    /* Overridden method. Never used. */
    public void insertParam(Entry e) {
        System.err.println("Error: Entry.insertParam()");
    }

    /* Overridden method. Never used. */
    public void insertLocal(Entry e) {
        System.err.println("Error: Entry.insertLocal()");
    }

    /* Overridden method. Never used. */
    public void insertField(Entry e) {
        System.err.println("Error: Entry.insertField()");
    }

    /* Overridden method. Never used. */
    public void insertMethod(Entry e) {
        System.err.println("Error: Entry.insertMethod()");
    }

    /* Overridden method. Never used. */
    public Entry lookup(String name, EntryType entryType) {
        System.err.println("Error: Entry.lookup()");
        return null;
    }

    /* Overridden method. Never used. */
    public Entry lookupForUse(String name, EntryType entryType) {
        System.err.println("Error: Entry.lookup()");
        return null;
    }

    /* Overridden method. Never used. */
    public boolean matches(VarEntry ve) {
        System.err.println("Error: Entry.match()");
        return false;
    }

    /* Overridden method. Never used. */
    public boolean matches(MethodEntry me) {
        System.err.println("Error: Entry.match()");
        return false;
    }

    /* Overridden method. Never used. */
    public boolean matches(ClassEntry ce) {
        System.err.println("Error: Entry.match()");
        return false;
    }

    /* Overridden method. Never used. */
    public boolean subTypes(ClassEntry ce) {
        System.err.println("Error: Entry.subTypes()");
        return false;
    }

    /* Overridden method. Never used. */
    public boolean isBasicType() {
        System.err.println("Error: Entry.isBasicType()");
        return false;
    }

    public void print() {
        System.out.println("[" + id + "] > " + name);
    }

    public void print(String prevScope) {
        System.out.println("[" + id + "] > " + prevScope + " :: " + name);
    }

    public void printOffsets() {
        /* Overridden method. Used only by ClassEntry. */
        System.err.println("Error: Entry.printOffsets()");
    }

    @Override
    public int compareTo(Entry entry) {
        return (this.id - entry.getId());
    }
}

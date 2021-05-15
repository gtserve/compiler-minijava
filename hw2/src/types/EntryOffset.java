package types;

public class EntryOffset {
    private final Entry entry;
    private final int offset;

    public EntryOffset(Entry entry, int offset) {
        this.entry = entry;
        this.offset = offset;
    }

    public Entry getEntry() {
        return entry;
    }

    public int getOffset() {
        return offset;
    }
}

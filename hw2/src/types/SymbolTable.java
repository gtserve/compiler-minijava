package types;

import java.util.*;


public class SymbolTable {

    private final String scope;
    private final Hashtable<String, Entry> table = new Hashtable<>();

    public SymbolTable(String scope) {
        this.scope = scope;
    }

    public String getScope() {
        return scope;
    }

    public ArrayList<Entry> getEntries() {
        Set<String> keys = table.keySet();
        ArrayList<Entry> entries = new ArrayList<>();
        for (String key : keys) {
            entries.add(table.get(key));
        }
        Collections.sort(entries);
        return entries;
    }

    public void insert(Entry e) {
        table.put(e.getName(), e);
    }

    public boolean contains(String name) {
        return table.containsKey(name);
    }

    public Entry get(String key) {
        return table.get(key);
    }

    public int size() {
        return table.size();
    }

    public void print(String prevScope) {
        prevScope += scope + " :: ";

        ArrayList<Entry> entries = getEntries();
        for (Entry e : entries)
            e.print(prevScope);
    }
}


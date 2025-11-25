package jaredbgreat.climaticbiome.generation.cache;

import jaredbgreat.climaticbiome.generation.map.RegionMap;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * A cache system using a homebrewed hash map.  The reason for not using 
 * java.util.HashMap is that for this use case and the processing needed 
 * for the intended caching function would actually be far more complex 
 * in terms of both code and processing than this, due unusual requirements 
 * of the caching.
 * 
 * This does not include a linked list, but instead skips until finding a 
 * place to put items if the correct spot is taken by a hash collision.
 * 
 * @author Jared Blackburn
 * @param <T>
 */
public class Cache <T extends ICachable> {
    private T[] data;
    private final int minSize;
    private final int maxEntries;
    private int capacity;
    private int lowLimit;
    private int length;
    private final LinkedHashSet<T> usageOrder = new LinkedHashSet<>();
    
    
    /**
     * Creates a cache with a default starting size elements.
     */
    public Cache(int size) {
        this(size, 0);
    }


    /**
     * Creates a cache with a default starting size elements and
     * a maximum number of entries.
     */
    public Cache(int size, int maxEntries) {
        data = (T[]) new ICachable[size];
        minSize = size;
        this.maxEntries = maxEntries;
        capacity = (size * 3) / 4;
        lowLimit = ((size - minSize) * 3) / 16;
        length = 0;
    }
    
    
    /**
     * Creates a cache with a default starting size of 16 elements.
     */
    public Cache() {
        this(16, 0);
    }
    
    
    /**
     * At a new element to the cache.
     * 
     * @param item the object to be added.
     */
    public void add(T item) {
        if(get(item.getCoords()) != null) {
            return;
        }
        ensureSpaceForNewEntry();
        int bucket = (item.getCoords().hashCode() & 0x7fffffff) % data.length;
        int offset = 0;
        while(offset < data.length) {
            int slot = (bucket + offset) % data.length;
            if(data[slot] == null) {
                data[slot] = item;
                data[slot].use();
                touch(data[slot]);
                //System.out.println("**ADDING item  " + item  + " to cache**");
                if(++length > capacity) {
                    grow();
                }
                return;
            } else {
                offset++;
            }
        }
    }
    
    
    /**
     * Return the element at the given Coords.
     * @param coords
     * @return the object stored for those coordinates, or null.
     */
    public T get(Coords coords) {
        int bucket = (coords.hashCode() & 0x7fffffff) % data.length;
        int offset = 0;
        while(offset < data.length) {
            int slot = (bucket + offset) % data.length;
            if(data[slot] == null) {
                return null;
            } else if(data[slot].getCoords().equals(coords)) {
                data[slot].use();
                touch(data[slot]);
                return (T)data[slot];
            } else {
                offset++;
            }
        }
        return null;
    }
    
    
    /**
     * Return the element at the given Coords.
     * @param coords
     * @return the object stored for those coordinates, or null.
     */
    public T get(MutableCoords coords) {
        int bucket = (coords.hashCode() & 0x7fffffff) % data.length;
        int offset = 0;
        while(offset < data.length) {
            int slot = (bucket + offset) % data.length;
            if(data[slot] == null) {
                return null;
            } else if(data[slot].getCoords().equals(coords)) {
                data[slot].use();
                touch(data[slot]);
                return (T)data[slot];
            } else {
                offset++;
            }
        }
        return null;
    }
    
    
    /**
     * Return the element at the given coordinate values x and z.
     * 
     * @param x
     * @param z
     * @return the object stored for those coordinates, or null.
     */
    public T get(int x, int z) {
        int bucket = (Coords.hashCoords(x, z) & 0x7fffffff) % data.length;
        int offset = 0;
        while(offset < data.length) {
            int slot = (bucket + offset) % data.length;
            if(data[slot] == null) {
                return null;
            } else if(data[slot].getCoords().equals(x, z)) {
                data[slot].use();
                touch(data[slot]);
                return (T)data[slot];
            } else {
                offset++;
            }
        }
        return null;
    }
    
    
    /**
     * Will tell if an item for the given coordinates is in the cache.
     * 
     * @param coords
     * @return 
     */
    public boolean contains(Coords coords) {
        int bucket = (coords.hashCode() & 0x7fffffff) % data.length;
        int offset = 0;
        while(offset < data.length) {
            int slot = (bucket + offset) % data.length;
            if(data[slot] == null) {
                return false;
            } else if(data[slot].getCoords().equals(coords)) {
                return true;
            } else {
                offset++;
            }
        }        
        return false;
    }
    
    
    /**
     * Will tell if an item for the given coordinates is in the cache.
     * 
     * @param x
     * @param z
     * @return 
     */
    public boolean contains(int x, int z) {
        int bucket = (Coords.hashCoords(x, z) & 0x7fffffff) % data.length;
        int offset = 0;
        while(offset < data.length) {
            int slot = (bucket + offset) % data.length;
            if(data[slot] == null) {
                return false;
            } else if(data[slot].getCoords().equals(x, z)) {
                return true;
            } else {
                offset++;
            }
        }        
        return false;
    }
    
    
    /**
     * Will tell if an item the same coords is in the cache.
     * 
     * @param in
     * @return 
     */
    public boolean contains(T in) {
        Coords coords = in.getCoords();
        int bucket = (coords.hashCode() & 0x7fffffff) % data.length;
        int offset = 0;
        while(offset <= data.length) {
            int slot = (bucket + offset) % data.length;
            if(data[slot] == null) {
                return false;
            } else if(data[slot].getCoords().equals(coords)) {
                return true;
            } else {
                offset++;
            }
        }
        return false;
    }


    /**
     * Ensure there is room for a new entry, removing an old one if the
     * cache has reached a configured maximum size.
     */
    private void ensureSpaceForNewEntry() {
        if((maxEntries > 0) && (length >= maxEntries)) {
            cleanup();
            if(length >= maxEntries) {
                evictOldest();
            }
        }
    }

    private void touch(T item) {
        if(item != null) {
            usageOrder.remove(item);
            usageOrder.add(item);
        }
    }


    /**
     * This will grow the data size when needed.
     */
    private void grow() {
        T[] old = data;
        data = (T[]) new ICachable[(old.length * 3) / 2];
        for(int i = 0; i < old.length; i++) {
            if(old[i] != null) {
                rebucket(old[i]);
            }
        }
        capacity = (data.length * 3) / 4;
        lowLimit = ((data.length - minSize) * 3) / 16;
    }
    
    
    /**
     * This will shrink the data size when needed.
     */
    private void shrink() {
        T[] old = data;
        data = (T[]) new ICachable[Math.max(old.length / 2, minSize)];
        for(int i = 0; i < old.length; i++) {
            if(old[i] != null) {
                rebucket(old[i]);
            }
        }
        capacity = (data.length * 3) / 4;
        lowLimit = ((data.length - minSize) * 3) / 16;
    }
    
    
    private void rebucket(T item) {
        int bucket = (item.getCoords().hashCode() & 0x7fffffff) % data.length;
        int offset = 0;
        while(offset <= data.length) {
            int slot = (bucket + offset) % data.length;
            if((data[slot] == null) || (data[slot].equals(item))) {
                data[slot] = item;
                return;
            }else {
                offset++;
            }
        }
    }

    private int findSlot(T item) {
        int bucket = (item.getCoords().hashCode() & 0x7fffffff) % data.length;
        int offset = 0;
        while(offset <= data.length) {
            int slot = (bucket + offset) % data.length;
            if(data[slot] == null) {
                return -1;
            } else if(data[slot].equals(item)) {
                return slot;
            } else {
                offset++;
            }
        }
        return -1;
    }


    /**
     * Rehash the backing array to close any gaps left by removals.
     */
    private void rehash() {
        T[] old = data;
        data = (T[]) new ICachable[old.length];
        for(int i = 0; i < old.length; i++) {
            if(old[i] != null) {
                rebucket(old[i]);
            }
        }
    }


    /**
     * Evict the least recently used entry to maintain a bounded cache.
     */
    private void evictOldest() {
        Iterator<T> iterator = usageOrder.iterator();
        while(iterator.hasNext()) {
            T candidate = iterator.next();
            iterator.remove();
            int slot = findSlot(candidate);
            if(slot >= 0) {
                if(data[slot] instanceof RegionMap) {
                    RegionMap.logEviction(data[slot].getCoords());
                }
                data[slot] = null;
                length--;
                rehash();
                return;
            }
        }
    }


    /**
     * This will iterate the cache items and remove any that identify
 themselves as isOldData().  Usually this should mean removing items from
 the cache that haven't been used in a set amount of time (most often 
 30 seconds), though other criteria for isOldData() could be created.
     */
    public void cleanup() {
        boolean removed = false;
        for(int i = 0; i < data.length; i++) {
            if((data[i] != null) && (data[i].isOldData())) {
                if(data[i] instanceof RegionMap) {
                    RegionMap.logEviction(data[i].getCoords());
                }
                usageOrder.remove(data[i]);
                data[i] = null;
                length--;
                removed = true;
            }
        }
        boolean resized = false;
        if(length < lowLimit) {
            shrink();
            resized = true;
        }
        if(removed && !resized) {
            rehash();
        }
    }
}

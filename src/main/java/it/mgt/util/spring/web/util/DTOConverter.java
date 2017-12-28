package it.mgt.util.spring.web.util;

import java.util.*;

public abstract class DTOConverter<T, U> {
    
    public abstract U to(T t);

    public Collection<U> to(Collection<T> coll) {
        ArrayList<U> list = new ArrayList<>();
        for(T t : coll)
            list.add(to(t));

        return list;
    }

    public List<U> to(List<T> coll) {
        ArrayList<U> list = new ArrayList<>();
        for(T t : coll)
            list.add(to(t));

        return list;
    }

    public ArrayList<U> to(ArrayList<T> coll) {
        ArrayList<U> list = new ArrayList<>();
        for(T t : coll)
            list.add(to(t));

        return list;
    }

    public LinkedList<U> to(LinkedList<T> coll) {
        LinkedList<U> list = new LinkedList<>();
        for(T t : coll)
            list.add(to(t));

        return list;
    }

    public Set<U> to(Set<T> coll) {
        LinkedHashSet<U> set = new LinkedHashSet<>();
        for(T t : coll)
            set.add(to(t));

        return set;
    }

    public HashSet<U> to(HashSet<T> coll) {
        HashSet<U> set = new HashSet<>();
        for(T t : coll)
            set.add(to(t));

        return set;
    }

    public LinkedHashSet<U> to(LinkedHashSet<T> coll) {
        LinkedHashSet<U> set = new LinkedHashSet<>();
        for(T t : coll)
            set.add(to(t));

        return set;
    }

    public Queue<U> to(Queue<T> coll) {
        LinkedList<U> set = new LinkedList<>();
        for(T t : coll)
            set.add(to(t));

        return set;
    }

    public PriorityQueue<U> to(PriorityQueue<T> coll) {
        PriorityQueue<U> set = new PriorityQueue<>();
        for(T t : coll)
            set.add(to(t));

        return set;
    }

    public Deque<U> to(Deque<T> coll) {
        LinkedList<U> set = new LinkedList<>();
        for(T t : coll)
            set.add(to(t));

        return set;
    }

    public ArrayDeque<U> to(ArrayDeque<T> coll) {
        ArrayDeque<U> set = new ArrayDeque<>();
        for(T t : coll)
            set.add(to(t));

        return set;
    }

    public abstract T from(U u);

    public Collection<T> from(Collection<U> coll) {
        ArrayList<T> list = new ArrayList<>();
        for (U u : coll)
            list.add(from(u));

        return list;
    }

    public List<T> from(List<U> coll) {
        ArrayList<T> list = new ArrayList<>();
        for (U u : coll)
            list.add(from(u));

        return list;
    }
    
    public ArrayList<T> from(ArrayList<U> coll) {
        ArrayList<T> list = new ArrayList<>();
        for (U u : coll)
            list.add(from(u));
        
        return list;
    }

    public LinkedList<T> from(LinkedList<U> coll) {
        LinkedList<T> list = new LinkedList<>();
        for (U u : coll)
            list.add(from(u));

        return list;
    }

    public Set<T> from(Set<U> coll) {
        LinkedHashSet<T> set = new LinkedHashSet<>();
        for (U u : coll)
            set.add(from(u));

        return set;
    }

    public HashSet<T> from(HashSet<U> coll) {
        HashSet<T> set = new HashSet<>();
        for (U u : coll)
            set.add(from(u));

        return set;
    }

    public LinkedHashSet<T> from(LinkedHashSet<U> coll) {
        LinkedHashSet<T> set = new LinkedHashSet<>();
        for (U u : coll)
            set.add(from(u));

        return set;
    }

    public Queue<T> from(Queue<U> coll) {
        LinkedList<T> set = new LinkedList<>();
        for (U u : coll)
            set.add(from(u));

        return set;
    }

    public PriorityQueue<T> from(PriorityQueue<U> coll) {
        PriorityQueue<T> set = new PriorityQueue<>();
        for (U u : coll)
            set.add(from(u));

        return set;
    }

    public Deque<T> from(Deque<U> coll) {
        LinkedList<T> set = new LinkedList<>();
        for (U u : coll)
            set.add(from(u));

        return set;
    }

    public ArrayDeque<T> from(ArrayDeque<U> coll) {
        ArrayDeque<T> set = new ArrayDeque<>();
        for (U u : coll)
            set.add(from(u));

        return set;
    }
    
}

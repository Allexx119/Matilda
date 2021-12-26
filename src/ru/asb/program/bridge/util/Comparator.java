package ru.asb.program.bridge.util;

import ru.asb.program.operation.records.Webi;

import java.util.ArrayList;
import java.util.List;

public class Comparator {
    private List<Webi> actual = new ArrayList<>();
    private List<Webi> changed = new ArrayList<>();

    public Comparator(List<Webi> oldList, List<Webi> newList) {
        for (Webi webi : newList) {
            if (oldList.contains(webi)) {
                actual.add(find(oldList, webi));
            } else {
                changed.add(webi);
            }
        }
    }

    public List<Webi> getChanged() {
        return changed;
    }

    public List<Webi> getActual(){
        return actual;
    }

    private Webi find(List<Webi> webiList, Webi webi) {
        return webiList.get(webiList.indexOf(webi));
    }

}

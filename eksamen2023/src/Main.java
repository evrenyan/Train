import java.util.Iterator;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.ArrayList;

abstract class Skinnegaende {
    final String id;
    final int sporvidde;
    Skinnegaende neste;
    Skinnegaende forrige;
    public Skinnegaende(String id, int sporvidde) {
        this.sporvidde = sporvidde;
        this.id  = id;
    }
    public String hentId() {
        return id;
    }
    public int hentSporvidde() {
        return sporvidde;
    }
}
interface Motordrevet{
    boolean fossilt();
    int trekkkraft();
}
class Lokomotiv extends Skinnegaende implements Motordrevet{
    final boolean fossilt;
    final int trekkKraft;
    public Lokomotiv(String id, int sporvidde, boolean fossilt, int trekkKraft) {
        super(id, sporvidde);
        this.fossilt = fossilt;
        this.trekkKraft = trekkKraft;
    }
    @Override
    public boolean fossilt() {
        return fossilt;
    }
    @Override
    public int trekkkraft() {
        return trekkKraft;
    }
}
abstract class Vogn extends Skinnegaende {
    int lengde;
    public Vogn(String id, int sporvidde, int lengde) {
        super(id, sporvidde);
        this.lengde = lengde;
    }
    public int hentLengde() {
        return lengde;
    }
}
class Godsvogn extends Vogn{
    final double maks_tillat_lastevekt;
    public Godsvogn(String id, int sporvidde, int lengde, double maks_tillat_lastevekt) {
        super(id, sporvidde, lengde);
        this.maks_tillat_lastevekt = maks_tillat_lastevekt;
    }
}
class Passasjervogn extends Vogn {
    int maks_antall_passasjer;
    public Passasjervogn(String id, int sporvidde, int lengde, int maks_antall_passasjer) {
        super(id, sporvidde, lengde);
        this.maks_antall_passasjer = maks_antall_passasjer;
    }
}
class Tog implements Iterable<Skinnegaende> {
    Skinnegaende forste = null;
    Skinnegaende siste = null;
    public Tog() {}
    public void leggTil(Skinnegaende skinnegaende) {
        if (forste == null && siste == null) {
            forste = siste = skinnegaende;
        } else {
            siste.neste = skinnegaende;
            skinnegaende.forrige = siste;
            siste = skinnegaende;
        }
    }
    public Skinnegaende taUt(Skinnegaende skinnegaende) {
        if (forste == skinnegaende && siste == skinnegaende) {
            forste = siste = null;
            return skinnegaende;
        } else if (forste == skinnegaende) {
            forste = forste.neste;
            forste.forrige = null;
        } else if (siste == skinnegaende) {
            siste = siste.forrige;
            siste.neste = null;
        } else {
            skinnegaende.neste.forrige = skinnegaende.forrige;
            skinnegaende.forrige.neste = skinnegaende.neste;
        }
        skinnegaende.neste = skinnegaende.forrige = null;
        return skinnegaende;
    }
    private Skinnegaende finn(String id) {
        Skinnegaende peker = forste;
        while (peker != null && !(peker.hentId().equals(id))) {
            peker = peker.neste;
        }
        if (peker != null) {
            return peker;
        }
        return null;
    }
    public Skinnegaende finnTaUt(String id) {
        Skinnegaende s = finn(id);
        if (s != null) {
            return taUt(s);
        }
        return null;
    }
    public void leggTilForan(Skinnegaende s, Skinnegaende ny) {
        ny.neste = s;
        s.forrige = ny.forrige;
        if (s == forste) {
            forste = ny;
        } else {
            s.forrige.neste = ny;
        }
        s.forrige = ny;
    }
    public Iterator<Skinnegaende> iterator() {
        return new SkinnegaendeIterator();
    }
    private class SkinnegaendeIterator implements Iterator<Skinnegaende> {
        Skinnegaende sIter = forste;
        @Override
        public boolean hasNext() {
            return sIter != null;
        }
        @Override
        public Skinnegaende next() {
            Skinnegaende res = sIter;
            sIter = res.neste;
            return res;
        }
    }
    public Passasjervogn[] hentPassasjervogner () {
        int antPassasjervogn = 0;
        Skinnegaende skinnegaende = forste;
        while (skinnegaende != null) {
            if (skinnegaende instanceof Passasjervogn) {
                antPassasjervogn++;
            }
            skinnegaende = skinnegaende.neste;
        }
        Passasjervogn[] passasjervogns = new Passasjervogn[antPassasjervogn];
        int ant = 0;
        for (Skinnegaende s : this) {
            if (s instanceof Passasjervogn) {
                passasjervogns[ant++] = (Passasjervogn) s;
            }
        }
        return passasjervogns;
    }
    public void sjekkSporvidde() {
        if (forste == null) {
            return;
        }
        int vidde1 = forste.hentSporvidde();
        for (Skinnegaende sx : this) {
            if (sx.hentSporvidde() != vidde1) {
                throw new FeilSporvidde();
            }
        }
    }
    public void leggTilSikker(Skinnegaende skinnegaende) {
        if (forste != null) {
            sjekkSporvidde();
            if (forste.hentSporvidde() != skinnegaende.hentSporvidde()) {
                throw new FeilSporvidde();
            }
        }
        leggTil(skinnegaende);
    }
    public void sjekkR(Skinnegaende skinnegaende) {
        if (skinnegaende == null) {
            return;
        }
        if (skinnegaende.forrige != null && skinnegaende.hentSporvidde() != skinnegaende.forrige.hentSporvidde()) {
            throw new FeilSporvidde();
        }
        sjekkR(skinnegaende.neste);
    }
}
class FeilSporvidde extends RuntimeException {
    public FeilSporvidde() {}
    public FeilSporvidde(String beskjed) {
        super("alle vogn og lokomotiv i tog må ha samme sporvidde");
    }
}
class SammeSporvidde extends RuntimeException {
    public SammeSporvidde() {
    }

    public SammeSporvidde(String beskjed) {
        super("denne skinnegpende er allerede i toget.");
    }
}
class Monitor {
    private Lock lock = new ReentrantLock();
    private Condition ikkeTom = lock.newCondition();
    private ArrayList<Skinnegaende>  objekter = new ArrayList<>();
    private int antallLeter;
    public Monitor(int antallLeter) {
        this.antallLeter = antallLeter;
    }
    public void leggTil(Skinnegaende objekt) {
        lock.lock();
        try {
            objekter.add(objekt);
            ikkeTom.signal();
        } finally {
            lock.unlock();
        }
    }
    public void ferdigLeting() {
        lock.lock();
        try  {
            antallLeter--;
            if (antallLeter == 0) {
                ikkeTom.signal();
            }
        } finally {
            lock.unlock();
        }
    }
    public Skinnegaende hentNeste() {
        lock.lock();
        try {
            while (objekter.isEmpty() && antallLeter > 0) {
                ikkeTom.await();
            }
            return objekter.isEmpty() ? null : objekter.remove(0);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            lock.unlock();
        }
    }
}
class Resultat implements Runnable {
    private Monitor monitor;

    public Resultat(Monitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public void run() {
        Skinnegaende objekt;

        while ((objekt = monitor.hentNeste()) != null) {
            System.out.println(objekt.hentId());
        }
    }
}

class Leter implements Runnable {
    private Tog tog;
    private Monitor monitor;
    private String prefix;

    public Leter(Tog tog, Monitor monitor, String prefix) {
        this.tog = tog;
        this.monitor = monitor;
        this.prefix = prefix;
    }

    @Override
    public void run() {
        Iterator<Skinnegaende> iterator = tog.iterator();

        while (iterator.hasNext()) {
            Skinnegaende objekt = iterator.next();

            if (objekt.hentId().startsWith(prefix)) {
                monitor.leggTil(objekt);
            }
        }

        monitor.ferdigLeting();
    }
}


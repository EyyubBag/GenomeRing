package com.genomeRing.model.structure;

import java.util.Collections;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * added the file because it has no other dependencies in mayday
 * TODO trying to replace those with listners and properties in JavaFX
 */

/** A helper class to make event firing simpler to implement. Events are sent to the listeners in no defined
 * order, each Listener will only be notified once, regardless of how often it is added.
 * @author battke
 *
 * @param <TE> the Event Object Type
 * @param <TL> the Event Listener Type
 */
public abstract class EventFirer<TE, TL extends EventListener> {

    private Set<TL> eventListeners;
    //FIXME GJ: nested settings?
    //FIXME GJ: introducing is_firing, leads to unhandeled events during painting in all plots!
    //That's why i removed it until a better solution is found!
    //private boolean is_firing=false;

    protected abstract void dispatchEvent(TE event, TL listener);

    public synchronized void addListener(TL listener) {
        if (eventListeners==null)
            eventListeners = new HashSet<TL>();
        eventListeners.add(listener);
    }

    public synchronized void removeListener(TL listener) {
        if (eventListeners==null)
            return;
        eventListeners.remove(listener);
        if (eventListeners.size()==0)
            eventListeners = null;
    }

    @SuppressWarnings("unchecked")
    protected void fireEvent_immediate(TE event) {
        for ( Object olistener : eventListeners.toArray()) {
            TL listener = (TL)olistener;
            try {
                dispatchEvent(event,listener);
            } catch (Exception e) {
                System.out.println("Event dispatching resulted in a "+e);
                e.printStackTrace();
            }
        }
    }

    public synchronized void fireEvent(TE event) {
        if (eventListeners==null || eventListeners.size()==0 || event==null)
            return;

        fireEvent_immediate(event);
    }

    public synchronized Set<TL> getListeners() {
        if (eventListeners!=null) {
            return Collections.unmodifiableSet(new HashSet<TL>(eventListeners));
        }
        return Collections.emptySet();
    }

}

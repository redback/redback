package org.codehaus.redback.jsecurity;

import java.util.Collection;
import org.jsecurity.realm.Realm;
import org.jsecurity.realm.RealmFactory;

public class RedbackRealmFactory implements RealmFactory
{
    public Collection<Realm> getRealms() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

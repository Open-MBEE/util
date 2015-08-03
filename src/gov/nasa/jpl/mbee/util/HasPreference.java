package gov.nasa.jpl.mbee.util;

import java.util.HashMap;
import java.util.List;

public interface HasPreference< T > {
    public boolean prefer( T t1, T t2 );
    
    
    
    public static class Helper< T > implements HasPreference< T > {

        final List< T > totalOrder;
        HashMap< T, Integer > rank = new HashMap< T, Integer >();
        
        public Helper( List< T > totalOrder ) {
            this.totalOrder = totalOrder;
            if ( totalOrder != null ) {
                int ct = 0;
                for ( T t : totalOrder ) {
                    rank.put( t, ct++ );
                }
            }
        }
        
        @Override
        public boolean prefer( T t1, T t2 ) {
            boolean preferred = rank.get( t1 ) < rank.get( t2 );
            return preferred;
        }
        
    }
}

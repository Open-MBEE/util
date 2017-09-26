/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").  
 * U.S. Government sponsorship acknowledged.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 * 
 *  - Redistributions of source code must retain the above copyright notice, this list of 
 *    conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list 
 *    of conditions and the following disclaimer in the documentation and/or other materials 
 *    provided with the distribution.
 *  - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory, 
 *    nor the names of its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER  
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.nasa.jpl.mbee.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;

/**
 *
 */
public class CompareUtils {

  public static class GenericComparator< T > implements Comparator< T > {

    protected static GenericComparator<?> instance = new GenericComparator();
    
    @Override
    public int compare( T o1, T o2 ) {
      return CompareUtils.compare( o1, o2, true, true );
    }

    public static <TT> GenericComparator< TT > instance() {
      // TODO Auto-generated method stub
      return (GenericComparator< TT >)instance;
    }

  }
  
  public static class MappedValueComparator< K, V > implements Comparator< K > {

    protected Map<K, V> map = null;
    protected Comparator< V > valueComparator = null;  
    public MappedValueComparator( Map<K, V> map, Comparator< V > valueComparator ) {
      this.map = map;
      this.valueComparator = valueComparator;
    }
      
    @Override
    public int compare( K A, K B ) {
        if ( A == B ) return 0;
        if ( A == null ) return -1;
        if ( B == null ) return 1;
        if ( map == null ) return GenericComparator.instance().compare( A, B );
        V resultA = map.get(A);
        V resultB = map.get(B);
        return getValueComparator().compare( resultA, resultB );
    }

    protected Comparator< V > getValueComparator() {
      if ( valueComparator == null ) {
        valueComparator = GenericComparator.instance();
      }
      return valueComparator;
    }
      
  }
  
  public static <T1, T2> int compare( T1 o1, T2 o2, boolean checkComparable ) {
    return compare( o1, o2, checkComparable, false );
  }
  @SuppressWarnings( "unchecked" )
  public static <T1, T2> int compare( T1 o1, T2 o2, boolean checkComparable,
                                      boolean useId ) {
    if ( o1 == o2 ) return 0;
    if ( o1 == null ) return -1;
    if ( o2 == null ) return 1;
    if ( useId && o1 instanceof HasId && o2 instanceof HasId ) {
      return CompareUtils.compare( ( (HasId)o1 ).getId(), ( (HasId)o2 ).getId() );
    }
    if ( o1 instanceof Number && o2 instanceof Number ) {
        Number n1 = (Number)o1;
        Number n2 = (Number)o2;
        if ( n1 instanceof Double || n2 instanceof Double ) {
            return compare(n1.doubleValue(), n2.doubleValue());
        }
        if ( n1 instanceof Float || n2 instanceof Float ) {
            return compare(n1.floatValue(), n2.floatValue());
        }
        if ( n1 == n2 ) return 0; 
        return ((Double)((Number)o1).doubleValue()).compareTo( ((Number)o2).doubleValue() );
    }
    if ( checkComparable ) {
      if ( o1 instanceof Comparable ) {
          try {
              return ((Comparable<T2>)o1).compareTo( o2 );
          } catch ( ClassCastException e ) {
          }
      }
    }
    int compare = o1.getClass().getName().compareTo( o2.getClass().getName() );
    if ( compare != 0 ) return compare;

    if (o1 instanceof Collection && o2 instanceof Collection ) {
      return CompareUtils.compareCollections( (Collection)o1, (Collection)o2,
                                 checkComparable, useId );
    }
    if (o1 instanceof Object[] && o2 instanceof Object[] ) {
      return CompareUtils.compareCollections( (Object[])o1, (Object[])o2,
                                 checkComparable, useId );
    }
    if (o1 instanceof Map && o2 instanceof Map ) {
        if (o1 instanceof SortedMap && o2 instanceof SortedMap ) {
            return CompareUtils.compareCollections( (SortedMap)o1, (SortedMap)o2,
                                                    checkComparable, useId );
        } else {
            return CompareUtils.compareCollections( (Map)o1, (Map)o2,
                                                    checkComparable, useId );
        }
    }
    if (o1 instanceof Map.Entry && o2 instanceof Map.Entry ) {
        return CompareUtils.compareEntries( (Map.Entry)o1, (Map.Entry)o2,
                                   checkComparable, useId );
      }
    if ( o1 instanceof Wraps || o2 instanceof Wraps ) {
        Object wo1 = o1 instanceof Wraps ? ((Wraps<?>)o1).getValue( false ) : o1;
        Object wo2 = o2 instanceof Wraps ? ((Wraps<?>)o2).getValue( false ) : o2;
        // check wrapped and unwrapped objects for equality first
        if ( ( wo1 != o1 && wo1.equals( o2 ) )
             || ( wo2 != o2 && wo2.equals( o1 ) ) ) {
            return 0;
        }
        // If they weren't found to be equal, 
        compare = compare(wo1, wo2, checkComparable, useId );
        return compare;
    }
    compare = CompareUtils.compareToStringNoHash( o1, o2 );
    if ( compare != 0 ) return compare;
    return compare;
  }

  public static int compare( Object o1, Object o2 ) {
    return compare( o1, o2, false, false );  // default false to avoid infinite recursion
  }

  public static < T > int compareCollections( Collection< T > coll1,
                                              Collection< T > coll2,
                                              boolean checkComparable,
                                              boolean useId ) {
    if ( coll1 == coll2 ) return 0;
    if ( coll1 == null ) return -1;
    if ( coll2 == null ) return 1;
    Iterator< T > i1 = coll1.iterator();
    Iterator< T > i2 = coll2.iterator();
    int compare = 0;
    int ct = 0;
    T lastT2 = null;
    while ( i1.hasNext() && i2.hasNext() ) {
      T t1 = i1.next();
      T t2 = i2.next();
      compare = compare( t1, t2, checkComparable, useId );
      if ( compare != 0 ) {
//          if ( Utils.valuesEqual( t2, lastT2 ) ) {
//              System.out.println( "SSSSSSSSSSSSSSSSSSTOPPPPPPPPPPPPPPPPP" );
//          }
          return compare;
      }
      ++ct;
      lastT2 = t2;
    }
    if ( i1.hasNext() ) return 1;
    if ( i2.hasNext() ) return -1;
    return 0;
  }

  public static < T > int compareCollections( T[] arr1, T[] arr2,
                                              boolean checkComparable,
                                              boolean useId) {
    if ( arr1 == arr2 ) return 0;
    if ( arr1 == null ) return -1;
    if ( arr2 == null ) return 1;
    int i = 0;
    int compare = 0;
    for ( i = 0; i < Math.min( arr1.length, arr2.length ); ++i ) {
      T t1 = arr1[i];
      T t2 = arr2[i];
      compare = compare( t1, t2, checkComparable, useId );
      if ( compare != 0 ) return compare;
    }
    if ( i < arr1.length ) return 1;
    if ( i < arr2.length ) return -1;
    return 0;
  }

  public static <K,V> int
    compareCollections( Map< K, V > m1,
                        Map< K, V > m2,
                        boolean checkComparable,
                        boolean useId ) {
    if ( m1 == m2 ) return 0;
    if ( m1 == null ) return -1;
    if ( m2 == null ) return 1;
    for ( K k : m1.keySet() ) {
        V v1 = m1.get( k );
        V v2 = m2.get( k );
        int comp = compare( v1, v2, checkComparable, useId );
        if ( comp != 0 ) return comp;
    }
    if ( m1.size() == m2.size() ) return 0;
    int comp = compareCollections( m1.keySet(), m2.keySet(), checkComparable, useId );
    return comp;
  }

  public static <K,V> int
  compareCollections( SortedMap< K, V > m1,
                      SortedMap< K, V > m2,
                      boolean checkComparable,
                      boolean useId ) {
  if ( m1 == m2 ) return 0;
  if ( m1 == null ) return -1;
  if ( m2 == null ) return 1;
  return compareCollections( m1.entrySet(), m2.entrySet(), checkComparable, useId );
}

  public static <K,V> int
  compareEntries( Map.Entry< K, V > e1, Map.Entry< K, V > e2,
                      boolean checkComparable,
                      boolean useId ) {
      int comp = compare(e1.getKey(), e2.getKey(), checkComparable, useId);
      if ( comp != 0 )
          return comp;
      comp = compare(e1.getValue(), e2.getValue(), checkComparable, useId);
      return comp;
  }
  
  public static int compareToStringNoHash( Object o1, Object o2 ) {
    assert o1 != null;
    assert o2 != null;
    int pos = 0;
    String s1 = o1.toString();
    String s2 = o2.toString();
    if ( s1 == s2 ) return 0;
    if ( s1 == null ) return -1;
    if ( s2 == null ) return 1;
    boolean gotAmp = false;
    for ( pos = 0; pos < Math.min( s1.length(), s2.length() ); ++pos ) {
      char c1 = s1.charAt(pos);
      char c2 = s2.charAt(pos);
      if ( gotAmp ) {
        if ( Character.isDigit( c1 ) || Character.isDigit( c2 ) ) {
          System.err.println( "Warning! Assumed comparing hash codes!" );
          return 0;
        } else {
          gotAmp = false;
        }
      }
      if ( c1 < c2 )
          return -1;
      if ( c1 > c2 )
          return 1;
      if ( c1 == '@' ) gotAmp = true;
    }
    if ( pos < s1.length() )
        return 1;
    if ( pos < s2.length() )
        return -1;
    return 0;
//    int compare = Utils.toStringNoHash(o1).compareTo( Utils.toStringNoHash(o2) );
//    return compare;
  }
  
  public static int compare( double d1, double d2 ) {
      if ( d1 == d2 ) return 0;
      double diff = Math.abs( d1 - d2 );
      double epsilon = Math.abs( d1 * 1.0e-14 );
      if ( diff < epsilon ) {
          return 0;
      }
      return Double.compare( d1,  d2 );
  }
  
  public static int compare( float f1, float f2 ) {
      if ( f1 == f2 ) return 0;
      float diff = Math.abs( f1 - f2 );
      if ( diff < Math.abs( f1 * 1.0e-5) ) {
          return 0;
      }
      return Float.compare( f1,  f2 );
  }
  
  public static int compare( int i1, int i2 ) {
    if ( i1 < i2 ) return -1;
    if ( i1 > i2 ) return 1;
    return 0;
  }
  public static int compare( String s1, String s2 ) {
    if ( s1 == s2 ) return 0;
    if ( s1 == null ) return -1;
    if ( s2 == null ) return 1;
    return s1.compareTo( s2 );
  }

  public static void main( String args[] ) {
      double d1 = 3.333e100;
      double d3 = Math.sqrt( d1 );
      d3 = Math.sqrt( d3 );
      System.out.println( "d1 = " + d1 );
      System.out.println( "d3 = sqrt(sqrt(d1)) = " + d3 );
      
      d3 = d3 * d3 * d3 * d3;
      System.out.println( "d3 = d3 * d3 * d3 * d3 = " + d3 );

      int comp = CompareUtils.compare(d3, d1);
      assert(comp == 0);
      System.out.println("CompareUtils.compare(d3, d1) = " + comp );

      comp = Double.compare(d3, d1);
      System.out.println("Double.compare(d3, d1) = " + comp );
      
      comp = CompareUtils.compare(3.332999999e100, d1);
      assert(comp == -1);
      System.out.println("CompareUtils.compare(3.332999999e100, d1) = " + comp );
  }
}

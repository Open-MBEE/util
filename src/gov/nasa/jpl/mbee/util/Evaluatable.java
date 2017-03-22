package gov.nasa.jpl.mbee.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeMap;

/**
 * Allow for multiple interpretations of the value. For example, a Variable may
 * be interpreted as the Variable object itself, or its value.
 * <p>
 * Consider moving this out to utils where Wraps may extend Evaluatable.
 * 
 */
public interface Evaluatable {
  public <T> T evaluate(Class<T> cls, boolean propagate);
    
  public static class Helper {

    /**
     * Find a value for Object that is of a particular type, T, taking into
     * account Wraps and optionally taking into account Evaluatable. Be careful
     * to avoid infinite recursion when calling this from
     * Evaluatable.evaluate().
     * 
     * @param o
     *          the object to evaluate
     * @param cls
     *          the type of the value desired
     * @param propagate
     *          whether to update dependent values
     * @param checkEvaluatable
     *          if true, check if o is Evaluatable, and if so, try
     *          Evaluatable.evaluate(). Do not pass in true when o is
     *          Evaluatable and when calling from o.evaluate() since it would
     *          trigger infinite recursion.
     * @return a value of the desired type, T, for o or null if o cannot be
     *         interpreted as having a T value.
     */
    public static < T > T evaluate( Object o, Class< T > cls, boolean propagate,
                             boolean checkEvaluatable ) {
      return evaluate(o, cls, true, propagate, checkEvaluatable, null);
    }
    
    /**
     * Find a value for Object that is of a particular type, T, taking into
     * account Wraps and optionally taking into account Evaluatable. Be careful
     * to avoid infinite recursion when calling this from
     * Evaluatable.evaluate().
     * 
     * @param o
     *          the object to evaluate
     * @param cls
     *          the type of the value desired
     * @param strictString
     *          if true, don't call toString() when cls == String.class
     * @param propagate
     *          whether to update dependent values
     * @param checkEvaluatable
     *          if true, check if o is Evaluatable, and if so, try
     *          Evaluatable.evaluate(). Do not pass in true when o is
     *          Evaluatable and when calling from o.evaluate() since it would
     *          trigger infinite recursion.
     * @param seen a set of "seen" objects that we don't want to revisit
     * @return a value of the desired type, T, for o or null if o cannot be
     *         interpreted as having a T value.
     */
    public static < T > T evaluate( Object object, Class< T > cls, boolean strictString, boolean propagate,
                               boolean checkEvaluatable, Seen<Object> seen ) {
      if ( object == null ) return null;
      
      // Check for an exact type match with o.
      if ( cls != null && object.getClass().equals( cls ) ) {
        return (T)object;
      }

      // Make sure we're not recursing infinitely.
      Pair< Boolean, Seen< Object > > p = Utils.seen( object, true, seen );
      if ( p.first ) return null;
      seen = p.second;

      T t = null;
      
      // Check if o is already Evaluatable.
      if ( cls != null && object instanceof Evaluatable ) {
        t = ( (Evaluatable)object ).evaluate( cls, propagate );
        if ( t != null ) return t;
      }

      // Try to evaluate object or dig inside to get the object of the right type.
      Object value = null;

      if ( object instanceof Wraps ) {
          Object wrappedObj = ( (Wraps)object ).getValue( propagate );
          try {
              value = evaluate( wrappedObj, cls, true, propagate, checkEvaluatable, seen );
              if ( value != null ) return (T)value;
          } catch ( Throwable e ) {
              // ignore
          }
      }
      
      if ( cls != null && cls.isInstance( object ) ) {
          try {
              return (T)object;
          } catch ( Throwable e ) {
              // ignore
          }
      }
      
      if ( object instanceof Collection ) {
          Collection<?> coll = (Collection<?>)object;
          if ( coll.size() == 1 ) {
              value = coll.iterator().next();
              if ( value != null ) {
                  value = evaluate( value, cls, true, propagate, checkEvaluatable, seen );
                  if ( value != null ) return (T)value;
              }
          }
      }
      
      if ( cls != null && Collection.class.isAssignableFrom( cls ) ) {
         if ( cls.isAssignableFrom( ArrayList.class ) ) {
             return (T)Utils.newList( object );
         }
         if ( cls.isAssignableFrom( Set.class ) ) {
             return (T)Utils.newSet( object );
         }
      }
      if ( cls != null && cls.isAssignableFrom( TreeMap.class ) &&
           object instanceof HasId ) {
          return (T)Utils.newMap( new Pair(((HasId)object).getId(), object ) );
      }
      
      //    if ( object instanceof Parameter ) {
//        value = ( (Parameter)object ).getValue( propagate );
//        return evaluate( value, cls, propagate, allowWrapping );
//      }
//      else if ( object instanceof Expression ) {
//        Expression< ? > expr = (Expression<?>)object;
//        if ( cls != null && cls.isInstance( expr.expression ) &&
//             expr.form != Form.Function) {
//          return (TT)expr.expression;
//        }
//        value = expr.evaluate( propagate );
//        return evaluate( value, cls, propagate, allowWrapping );
//      }
//      else if ( object instanceof Call) {
//        value = ( (Call)object ).evaluate( propagate );
//        return evaluate( value, cls, propagate, allowWrapping );
//      } else
      if ( cls != null && ClassUtils.isNumber( cls ) ) {
          if ( ClassUtils.isNumber( object.getClass() ) ) {
              try {
                  Number n = (Number)object;
                  T tt = ClassUtils.castNumber( n, cls );
                  if ( tt != null || object == null ) {
                      return tt;
                  }
              } catch ( Exception e ) {
                  // warning?  we shouldn't get here, right?
              }
          } else {
              // try to make the string a number
              try {
                  String s = evaluate( object, String.class, true, propagate, checkEvaluatable, seen );
                  Double d = new Double( s );
                  if ( d != null ) {
                      return evaluate( d, cls, true, propagate, checkEvaluatable, seen );
                  }
              } catch (Throwable throwable) {}
          }
      }

      // Cheat if cls == String.class and print to string
      if ( !strictString && cls != null && cls.isAssignableFrom( String.class ) ) {
          @SuppressWarnings( "unchecked" )
          T r = (T)object.toString();
          return r;
      }
      
      return null;
////      else if ( allowWrapping && cls != null ){
////        // If evaluating doesn't work, maybe we need to wrap the value in a parameter.
////        if ( cls.isAssignableFrom( Parameter.class ) ) {
////          if ( Debug.isOn() ) Debug.error( false, "Warning: wrapping value with a parameter with null owner!" );
////          return (TT)( new Parameter( null, null, object, null ) );
////        } else if ( cls.isAssignableFrom( Expression.class ) ) {
////          return (TT)( new Expression( object ) );
////        }
////      }
//      // Try to force it?!
//      T r = null;
//      try {
//        r = (T)object;
//      } catch ( ClassCastException cce ) {
//          if ( Debug.isOn() ) Debug.errln( "Warning! No evaluation of " + object + " with type " + cls.getName() + "!" );
//        throw cce;
//      }
//      if ( cls != null && r != null && (cls.isInstance( r ) || cls == r.getClass() ) ) {
//        return r;
//      }
//      return null;
      
//      t = ClassUtils.evaluate( o, cls, strictString, propagate, checkEvaluatable, seen );
//      
//      return t
//      
//      // Check to see if o wraps a T. So, if o is not exactly a T but an
//      // extension of T, and o wraps a T, we choose to unwrap o.
//      if ( o instanceof Wraps ) {
//        Object v = ( (Wraps< ? >)o ).getValue( propagate );
//        if ( cls == null || cls.isInstance( v ) ) {
//          return (T)v;
//        }
//      }
//      // Check to see if o is a T
//      if ( cls == null || cls.isInstance( o ) ) {
//        return (T)o;
//      }
//      T t = ClassUtils.evaluate( o, cls, propagate );
//      //
//      return t;
    }
  }
}

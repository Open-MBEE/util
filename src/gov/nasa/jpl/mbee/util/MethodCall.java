package gov.nasa.jpl.mbee.util;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

public class MethodCall {
    /**
     * Create a new MethodCall, fully specifying its attributes.
     * 
     * @param objectOfCall
     *            This is the Object whose method is called. If it is null
     *            and the method is not static, the indexOfObjectArgument
     *            must be 0 to indicate that the objects will be substituted
     *            such that the method is called from each of them. If the
     *            method is static, then objectOfCall is ignored.
     * @param method
     *            Java Method either of Class O or with a parameter that is
     *            or extends O (for the objects).
     * @param arguments
     *            arguments to be passed into the call of the method
     */
    public MethodCall( java.lang.Object objectOfCall, Method method,
                       java.lang.Object... arguments ) {
        this.objectOfCall = objectOfCall;
        this.method = method;
        this.arguments = arguments;
    }
    /**
     * This is the Object whose method is called. If it is null and the
     * method is not static, the indexOfObjectArgument must be 0 to indicate
     * that the objects will be substituted such that the method is called
     * from each of them. If the method is static, then objectOfCall is
     * ignored.
     */
    public Object objectOfCall;
    /**
     * Java Method either of Class O or with a parameter that is or extends
     * O (for the objects).
     */
    public Method method;
    /**
     * arguments to be passed into the call of the method
     */
    public Object[] arguments;
    
    public Pair< Boolean, Object > invoke() {
        return invoke( true );
    }
    public Pair< Boolean, Object > invoke( boolean suppressErrors ) {
        boolean objectIsMethodCall = objectOfCall instanceof MethodCall;
        Pair< Boolean, Object > result =
                ClassUtils.runMethod( suppressErrors && !objectIsMethodCall,
                                      objectOfCall, method, arguments );
        if ( result.first == false && objectIsMethodCall ) {
            MethodCall objectMethodCall = (MethodCall)objectOfCall;
            Pair< Boolean, Object > prevResult = objectMethodCall.invoke( suppressErrors );
            if ( prevResult.first ) {
                result = ClassUtils.runMethod( suppressErrors && !objectIsMethodCall,
                                               prevResult.second, method, arguments );
            }
        }
        return result;
    }
    protected void sub( int indexOfArg, Object obj ) {
        if ( indexOfArg < 0 ) Debug.error("bad indexOfArg " + indexOfArg );
        else if ( indexOfArg == 0 ) objectOfCall = obj;
        else if ( indexOfArg > arguments.length ) Debug.error( "bad index "
                                                               + indexOfArg
                                                               + "; only "
                                                               + arguments.length
                                                               + " arguments!" );
        else arguments[indexOfArg-1] = obj;
    }
    /**
     * @param objects
     * @param methodCall
     * @param indexOfObjectArgument
     *            where in the list of arguments an object from the collection
     *            is substituted (1 to total number of args or 0 to indicate
     *            that the objects are each substituted for
     *            methodCall.objectOfCall).
     * @return the subset of objects for which the method call returns true
     */
    public static < XX > Collection<XX> filter( Collection< XX > objects,
                                                MethodCall methodCall,
                                                int indexOfObjectArgument ) {
        return methodCall.filter( objects, indexOfObjectArgument );
    }
    /**
     * @param objects
     * @param indexOfObjectArgument
     *            where in the list of arguments an object from the collection
     *            is substituted (1 to total number of args or 0 to indicate
     *            that the objects are each substituted for
     *            methodCall.objectOfCall).
     * @return the subset of objects for which the method call returns true
     */
    public < XX > Collection<XX> filter( Collection< XX > objects,
                                         int indexOfObjectArgument ) {
        Collection< XX > coll = new ArrayList< XX >( objects );
        for ( XX o : objects ) {
            sub( indexOfObjectArgument, o );
            Pair< Boolean, Object > result = invoke();
            if ( result != null && result.first && Utils.isTrue( result.second, false ) ) {
                coll.add( o );
            }
        }
        return coll;
    }
    /**
     * @param objects
     * @param methodCall
     * @param indexOfObjectArgument
     *            where in the list of arguments an object from the collection
     *            is substituted (1 to total number of args or 0 to indicate
     *            that the objects are each substituted for
     *            methodCall.objectOfCall).
     * @return the results of the methodCall on each of the objects
     */
    public static < XX > Collection< XX > map( Collection< ? > objects,
                                               MethodCall methodCall,
                                               int indexOfObjectArgument ) {
        return methodCall.map( objects, indexOfObjectArgument );
    }
    /**
     * @param objects
     * @param indexOfObjectArgument
     *            where in the list of arguments an object from the collection
     *            is substituted (1 to total number of args or 0 to indicate
     *            that the objects are each substituted for
     *            methodCall.objectOfCall).
     * @return the results of the methodCall on each of the objects
     */
    public  < XX > Collection< XX > map( Collection< ? > objects,
                                               int indexOfObjectArgument ) {
        Collection< XX > coll = new ArrayList<XX>();
        for ( Object o : objects ) {
            sub( indexOfObjectArgument, o );
            Pair< Boolean, Object > result = invoke();
            if ( result != null && result.first ) {
                coll.add( (XX)result.second );
            } else {
                coll.add( null );
            }
        }
        return coll;
    }
}
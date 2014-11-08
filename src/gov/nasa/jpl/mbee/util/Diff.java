/**
 * Copyright (c) <2013>, California Institute of Technology ("CalTech").
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
 */
package gov.nasa.jpl.mbee.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Diff represents the difference between two sets of objects of type T.
 * T may have properties of type P that each have an identifier of type ID.
 *
 * @param <T>
 * @param <P>
 * @param <ID>
 */
public interface Diff< T, P, ID > {

    public abstract boolean areDifferent();

    public abstract boolean areSame();

    public abstract Set<T> get1();

    public abstract Set<T> get2();

    public abstract T get1( ID tid );

    public abstract T get2( ID tid );

    public abstract P get1( ID tid, ID pid );

    public abstract P get2( ID tid, ID pid );

    public abstract Set< T > getRemoved();

    public abstract Set< T > getAdded();

    public abstract Set< T > getUpdated();

    public abstract Map< ID, Map< ID, P > > getRemovedProperties();

    public abstract Map< ID, Map< ID, P > > getAddedProperties();

    public abstract Map< ID, Map< ID, Pair< P, P > > > getUpdatedProperties();

    public abstract Map< ID, Map< ID, Pair< P, P > > > getPropertyChanges();

    public abstract void addPropertyIdsToIgnore( Collection<ID> ids );

    /**
     * @param ids
     *            The IDs of properties that should be left out of the property
     *            diff results. This is not used to filter the objects returned
     *            by {@link #get1()} and {@link #get2()}.
     */
    public abstract Set<ID> getPropertyIdsToIgnore();

}

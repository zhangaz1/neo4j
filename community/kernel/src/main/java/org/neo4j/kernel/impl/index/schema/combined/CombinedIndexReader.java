/*
 * Copyright (c) 2002-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel.impl.index.schema.combined;

import org.neo4j.collection.primitive.PrimitiveLongIterator;
import org.neo4j.kernel.api.exceptions.index.IndexNotApplicableKernelException;
import org.neo4j.kernel.api.schema.IndexQuery;
import org.neo4j.storageengine.api.schema.IndexReader;
import org.neo4j.storageengine.api.schema.IndexSampler;
import org.neo4j.values.storable.Value;

import static org.neo4j.kernel.impl.index.schema.combined.CombinedSchemaIndexProvider.select;

class CombinedIndexReader implements IndexReader
{
    private final IndexReader boostReader;
    private final IndexReader fallbackReader;

    CombinedIndexReader( IndexReader boostReader, IndexReader fallbackReader )
    {
        this.boostReader = boostReader;
        this.fallbackReader = fallbackReader;
    }

    @Override
    public void close()
    {
        try
        {
            boostReader.close();
        }
        finally
        {
            fallbackReader.close();
        }
    }

    @Override
    public long countIndexedNodes( long nodeId, Value... propertyValues )
    {
        return select( propertyValues, boostReader, fallbackReader ).countIndexedNodes( nodeId, propertyValues );
    }

    @Override
    public IndexSampler createSampler()
    {
        return new CombinedIndexSampler( boostReader.createSampler(), fallbackReader.createSampler() );
    }

    @Override
    public PrimitiveLongIterator query( IndexQuery... predicates ) throws IndexNotApplicableKernelException
    {
        throw new UnsupportedOperationException( "Tricky stuff, not implemented yet" );
    }

    @Override
    public boolean hasFullNumberPrecision()
    {
        // Since we know that boost reader can do this we return true
        return true;
    }
}

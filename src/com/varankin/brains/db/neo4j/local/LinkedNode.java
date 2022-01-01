package com.varankin.brains.db.neo4j.local;

import com.varankin.util.MultiIterable;
import java.util.HashMap;
import java.util.Map;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

/**
 * Объект на базе узла Neo4j, наследующий свойства и связи другого узла.
 * Наследуемый узел не подлежит изменению.
 * 
 * @author &copy; 2020 Николай Варанкин
 */
final class LinkedNode implements Node
{
    private final Node SOURCE, PATTERN;

    LinkedNode( Node source, Node pattern ) 
    {
        this. SOURCE = source;
        this.PATTERN = pattern;
    }
    
    //<editor-fold defaultstate="collapsed" desc="General">
    
    public Node getSource()
    {
        return SOURCE;
    }

    public Node getPattern() 
    {    
        return PATTERN;
    }

    @Override
    public long getId() {
        return SOURCE.getId();
    }
    
    @Override
    public GraphDatabaseService getGraphDatabase()
    {
        return SOURCE.getGraphDatabase();
    }
    
    @Override
    public void delete()
    {
        SOURCE.delete();
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Relationships">
    
    @Override
    public boolean hasRelationship()
    {
        return SOURCE.hasRelationship() || PATTERN.hasRelationship();
    }
    
    @Override
    public boolean hasRelationship( RelationshipType... rts )
    {
        return SOURCE.hasRelationship() || PATTERN.hasRelationship();
    }
    
    @Override
    public boolean hasRelationship( Direction drctn, RelationshipType... rts )
    {
        return SOURCE.hasRelationship( drctn, rts ) || PATTERN.hasRelationship( drctn, rts );
    }
    
    @Override
    public boolean hasRelationship( Direction drctn )
    {
        return SOURCE.hasRelationship( drctn ) || PATTERN.hasRelationship( drctn );
    }
    
    @Override
    public boolean hasRelationship( RelationshipType rt, Direction drctn )
    {
        return SOURCE.hasRelationship( rt, drctn ) || PATTERN.hasRelationship( rt, drctn );
    }
    
    @Override
    public Iterable<Relationship> getRelationships()
    {
        return new MultiIterable<>( SOURCE.getRelationships(), PATTERN.getRelationships() );
    }
    
    @Override
    public Iterable<Relationship> getRelationships( RelationshipType... rts )
    {
        return new MultiIterable<>( SOURCE.getRelationships( rts ), PATTERN.getRelationships( rts ) );
    }
    
    @Override
    public Iterable<Relationship> getRelationships( Direction drctn, RelationshipType... rts )
    {
        return new MultiIterable<>( SOURCE.getRelationships( drctn, rts ), PATTERN.getRelationships( drctn, rts ) );
    }
    
    @Override
    public Iterable<Relationship> getRelationships( Direction drctn )
    {
        return new MultiIterable<>( SOURCE.getRelationships( drctn ), PATTERN.getRelationships( drctn ) );
    }
    
    @Override
    public Iterable<Relationship> getRelationships( RelationshipType rt, Direction drctn )
    {
        return new MultiIterable<>( SOURCE.getRelationships( rt, drctn ), PATTERN.getRelationships( rt, drctn ) );
    }
    
    @Override
    public Relationship getSingleRelationship( RelationshipType rt, Direction drctn ) 
    {
        Relationship ssr = SOURCE .getSingleRelationship( rt, drctn );
        Relationship spr = PATTERN.getSingleRelationship( rt, drctn );
        //TODO [NeoNode.getNodeURI, Architect.getURI] if( ssr != null && spr != null ) throw new RuntimeException("Multiple relationships");
        return ssr != null ? ssr : spr;
    }
    
    @Override
    public Relationship createRelationshipTo( Node node, RelationshipType rt ) 
    {
        return SOURCE.createRelationshipTo( node, rt );
    }
    
    @Override
    public Iterable<RelationshipType> getRelationshipTypes()
    {
        return new MultiIterable<>( SOURCE.getRelationshipTypes(), PATTERN.getRelationshipTypes() );
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Degrees">
    
    @Override
    public int getDegree()
    {
        return SOURCE.getDegree() + PATTERN.getDegree();
    }
    
    @Override
    public int getDegree( RelationshipType rt )
    {
        return SOURCE.getDegree( rt ) + PATTERN.getDegree( rt );
    }
    
    @Override
    public int getDegree( Direction drctn )
    {
        return SOURCE.getDegree( drctn ) + PATTERN.getDegree( drctn );
    }
    
    @Override
    public int getDegree( RelationshipType rt, Direction drctn )
    {
        return SOURCE.getDegree( rt, drctn ) + PATTERN.getDegree( rt, drctn );
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Labels">
    
    @Override
    public void addLabel( Label label )
    {
        SOURCE.addLabel( label );
    }
    
    @Override
    public void removeLabel( Label label )
    {
        SOURCE.removeLabel( label );
    }
    
    @Override
    public boolean hasLabel( Label label )
    {
        return SOURCE.hasLabel( label ) || PATTERN.hasLabel( label );
    }
    
    @Override
    public Iterable<Label> getLabels()
    {
        return new MultiIterable<>( SOURCE.getLabels(), PATTERN.getLabels() );
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Properties">
    
    @Override
    public boolean hasProperty( String string )
    {
        return SOURCE.hasProperty( string ) || PATTERN.hasProperty( string );
    }
    
    @Override
    public Object getProperty( String string )
    {
        return SOURCE.hasProperty( string ) ? SOURCE.getProperty( string ) : PATTERN.getProperty( string );
    }
    
    @Override
    public Object getProperty( String string, Object o )
    {
        return SOURCE.hasProperty( string ) ? SOURCE.getProperty( string ) : PATTERN.getProperty( string, o );
    }
    
    @Override
    public void setProperty( String string, Object o )
    {
        SOURCE.setProperty( string, o );
    }
    
    @Override
    public Object removeProperty( String string )
    {
        return SOURCE.removeProperty( string );
    }
    
    @Override
    public Iterable<String> getPropertyKeys()
    {
        return new MultiIterable<>( SOURCE.getPropertyKeys(), PATTERN.getPropertyKeys() );
    }
    
    @Override
    public Map<String, Object> getProperties( String... strings )
    {
        Map<String, Object> map = new HashMap<>();
        map.putAll( PATTERN.getProperties( strings ) ); // сначала!
        map.putAll( SOURCE.getProperties( strings ) );
        return map;
    }
    
    @Override
    public Map<String, Object> getAllProperties()
    {
        Map<String, Object> map = new HashMap<>();
        map.putAll( PATTERN.getAllProperties() ); // сначала!
        map.putAll( SOURCE.getAllProperties() );
        return map;
    }
    
    //</editor-fold>
    
}

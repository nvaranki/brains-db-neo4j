package com.varankin.brains.db.neo4j.local;

/**
 * Связи между удаленными узлами элементов и их бывшими владельцами в Neo4j.
 * 
 * @author &copy; 2014 Николай Варанкин
 */
enum Recycle implements org.neo4j.graphdb.RelationshipType
{
    /**
     * Ссылка на бывшего владельца узла.
     */
    Бывший; 
    
}

package com.varankin.brains.db.neo4j.local;

/**
 * Связи между узлами элементов и узлами пространства имен в Neo4j.
 * 
 * <p>ВАЖНО: Для всех узлов, кроме {@linkplain com.varankin.brains.db.Архив архива}, 
 * данная связь является <b>входящей</b>!</p>
 * 
 * @author &copy; 2014 Николай Варанкин
 */
enum NameSpace implements org.neo4j.graphdb.RelationshipType
{
    /**
     * Атрибут узла в пространстве имен, отличном от пространства узла.
     */
    Атрибут, 
    
    /**
     * Ссылка на пространство имен узла.
     */
    Узел;
    
}

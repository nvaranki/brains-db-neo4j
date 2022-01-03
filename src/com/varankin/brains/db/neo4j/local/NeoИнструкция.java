package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.type.DbИнструкция;
import com.varankin.brains.db.xml.PiProcessor;
import com.varankin.brains.db.xml.type.XmlИнструкция;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import static com.varankin.brains.db.DbПреобразователь.*;

/**
 * Инструкция для обработки в Neo4j.
 * 
 * @author &copy; 2021 Николай Варанкин
 */
final class NeoИнструкция extends NeoАтрибутный implements DbИнструкция, XmlИнструкция
{
    NeoИнструкция( GraphDatabaseService сервис )
    {
        this( createNodeInNameSpace( КЛЮЧ_Э_ИНСТРУКЦИЯ, сервис ) );
    }
    
    NeoИнструкция( Node node ) 
    {
        super( КЛЮЧ_Э_ИНСТРУКЦИЯ, node );
    }
    
    @Override
    public String выполнить()
    {
        Node parentNode = Architect.getParentNode( getNode(), Связь.Инструкция );
        String п = процессор();
        PiProcessor<Node> исполнитель = п != null ? PiProcessorFactory.getInstance().get( п ) : null;
        return исполнитель != null ? исполнитель.выполнить( parentNode, код() ) : null;
    }

    @Override
    public String код()
    {
        return toStringValue( атрибут( КЛЮЧ_А_КОД, null ) );
    }

    @Override
    public void код( String инструкция )
    {
        определить( КЛЮЧ_А_КОД, trimToCharArray( инструкция ) );
    }
    
    @Override
    public String процессор()
    {
        return toStringValue( атрибут( КЛЮЧ_А_ПРОЦЕССОР, null ) );
    }

    @Override
    public void процессор( String название )
    {
        определить( КЛЮЧ_А_ПРОЦЕССОР, trimToCharArray( название ) );
    }

}

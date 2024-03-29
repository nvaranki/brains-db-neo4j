package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.type.DbТекстовыйБлок;
import com.varankin.brains.db.type.DbКлассJava;
import com.varankin.brains.db.xml.type.XmlКлассJava;
import com.varankin.brains.db.xml.АтрибутныйКлюч;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import static com.varankin.brains.db.DbПреобразователь.*;

/**
 * Код на языке программирования Java в Neo4j.
 * Выполняет функции элемента мыслительной структуры, в 
 * которую он вложен. 
 *
 * @author &copy; 2023 Николай Варанкин
 */
final class NeoКлассJava extends NeoЭлемент implements DbКлассJava, XmlКлассJava
{
    NeoКлассJava( GraphDatabaseService сервис )
    {
        this( createNodeInNameSpace( КЛЮЧ_Э_JAVA, сервис ) );
    }
    
    NeoКлассJava( Node node )
    {
        super( КЛЮЧ_Э_JAVA, node );
    }
    
    @Override
    public АтрибутныйКлюч тип() 
    {
        return КЛЮЧ_Э_JAVA;
    }

    @Override
    public String название() 
    {
        return toStringValue( атрибут( КЛЮЧ_А_КЛАСС, null ) );
    }
    
    @Override
    public void название( String значение )
    {
        определить( КЛЮЧ_А_КЛАСС, trimToCharArray( значение ) );
    }
    
    @Override
    public String код()
    {
        return DbТекстовыйБлок.текст( тексты(), "\n" );
    }

    @Override
    public String опции() 
    {
        return toStringValue( атрибут( КЛЮЧ_А_ОПЦИИ, null ) );
    }
    
    @Override
    public void опции( String значение )
    {
        определить( КЛЮЧ_А_ОПЦИИ, trimToCharArray( значение ) );
    }

    @Override
    public void код( String значение )
    {
        if( тексты().isEmpty() )
        {
            NeoТекстовыйБлок блок = new NeoТекстовыйБлок( getNode().getGraphDatabase() );
            блок.текст( значение );
            тексты().add( блок );
        }
        else for( DbТекстовыйБлок блок : тексты() )
        {
            блок.текст( значение );
            значение = null; // удаляет остальной текст, что был ранее
        }
    }

    @Override
    public Назначение назначение()
    {
        return toEnumValue( атрибут( КЛЮЧ_А_НАЗНАЧЕНИЕ, null ), Назначение.class );
    }
    
    @Override
    public void назначение( Назначение значение )
    {
        определить( КЛЮЧ_А_НАЗНАЧЕНИЕ, значение != null ? значение.ordinal() : null );
    }
    
}

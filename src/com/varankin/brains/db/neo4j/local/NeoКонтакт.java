package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.type.DbКонтакт;
import com.varankin.brains.db.type.DbСигнал;
import com.varankin.brains.db.xml.АтрибутныйКлюч;
import com.varankin.brains.db.xml.type.XmlКонтакт;
import com.varankin.brains.db.xml.type.XmlСигнал;
import com.varankin.brains.db.xml.type.XmlФрагмент;
import com.varankin.util.LoggerX;

import java.util.logging.*;
import java.util.Objects;
import org.neo4j.graphdb.*;

import static com.varankin.brains.db.neo4j.local.Architect.*;
import static com.varankin.brains.db.DbПреобразователь.*;

/**
 * Фрагмент {@linkplain Соединение соединения} для приема-передачи
 * одного {@link DbСигнал сигнала} в Neo4j.
 *
 * @author &copy; 2022 Николай Варанкин
 */
final class NeoКонтакт extends NeoЭлементВП implements DbКонтакт, XmlКонтакт
{
    private static final LoggerX LOGGER = LoggerX.getLogger( NeoКонтакт.class );
    
    NeoКонтакт( GraphDatabaseService сервис )
    {
        this( createNodeInNameSpace( КЛЮЧ_Э_КОНТАКТ, сервис ) );
    }
    
    NeoКонтакт( Node node ) 
    {
        super( КЛЮЧ_Э_КОНТАКТ, node );
    }
    
    @Override
    public АтрибутныйКлюч тип() 
    {
        return КЛЮЧ_Э_КОНТАКТ;
    }

    @Override
    public String сигнал() 
    {
        return toStringValue( атрибут( КЛЮЧ_А_СИГНАЛ, null ) );
    }
    
    @Override
    public void сигнал( String значение )
    {
        определить( КЛЮЧ_А_СИГНАЛ, trimToCharArray( значение ) );
    }
    
    @Override
    public DbСигнал сигналКонтакта() 
    {
        String название = сигнал();
        if( название != null )
        {
            Node n = getParentNode( getParentNode( getNode(), Связь.Контакт ) );
            if( Objects.equals( XmlФрагмент.КЛЮЧ_Э_ФРАГМЕНТ.НАЗВАНИЕ, ( new NeoАтрибутный( n ) {} ).тип().НАЗВАНИЕ ) )
                n = getParentNode( n ); // у фрагмента нет своих сигналов
            // поиск в произвольном владельце сигналов
            for( Relationship r : n.getRelationships( Direction.OUTGOING, Связь.Сигнал ) )
            {
                Node s = r.getEndNode();
                if( название.equals( toStringValue( s.getProperty( XmlСигнал.КЛЮЧ_А_НАЗВАНИЕ.НАЗВАНИЕ, null ) ) ) )
                    return new NeoСигнал( s );
            }
        }
        if( название != null )
            LOGGER.log( Level.SEVERE, "002001005S", название(), название );
        return null;
    }

    @Override
    public Integer приоритет() 
    {
        return toIntegerValue( атрибут( КЛЮЧ_А_ПРИОРИТЕТ, null ) );
    }
    
    @Override
    public void приоритет( Integer значение ) 
    {
        определить( КЛЮЧ_А_ПРИОРИТЕТ, значение );
    }
    
    @Override
    public Short свойства()
    {
        return toShortValue( атрибут( КЛЮЧ_А_СВОЙСТВА, null ) );
    }
    
    @Override
    public void свойства( Short значение )
    {
        определить( КЛЮЧ_А_СВОЙСТВА, значение );
    }
    
    @Override
    public String точка()
    {
        return toStringValue( атрибут( КЛЮЧ_А_ТОЧКА, null ) );
    }

    @Override
    public void точка( String значение )
    {
        определить( КЛЮЧ_А_ТОЧКА, trimToCharArray( значение ) );
    }

}

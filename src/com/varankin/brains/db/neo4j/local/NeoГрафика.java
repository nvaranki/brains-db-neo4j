package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.type.DbГрафика;
import com.varankin.brains.db.xml.XLinkActuate;
import com.varankin.brains.db.xml.XLinkShow;
import com.varankin.brains.db.xml.type.XmlГрафика;
import com.varankin.brains.db.xml.АтрибутныйКлюч;
import com.varankin.brains.db.Коллекция;
import com.varankin.brains.db.xml.ЗонныйКлюч;
import com.varankin.io.xml.svg.XmlSvg;
import com.varankin.util.LoggerX;
import java.util.Arrays;
import java.util.Collection;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

/**
 * Произвольный узел структуры данных в Neo4j, предназначенный для
 * визуального отображения.
 * 
 * @author &copy; 2022 Николай Варанкин
 */
public class NeoГрафика extends NeoУзел implements DbГрафика, XmlГрафика
{
    private static final LoggerX LOGGER = LoggerX.getLogger( NeoГрафика.class );
    private static final Collection<String> XMLNS_URI = Arrays.asList( XmlSvg.XMLNS_SVG );
    
    private final ТиповойImpl<NeoГрафика> ТИПОВОЙ;
    private final Коллекция<NeoГрафика> ГРАФИКИ;

    public NeoГрафика( GraphDatabaseService сервис, ЗонныйКлюч ключ )
    {
        this( createNodeInNameSpace( ключ, сервис ) );
    }

    public NeoГрафика( Node node )
    {
        super( node ); //TODO валидация SVG
        String uri = new NeoЗона( getNodeURI() ).uri();
        if( !XMLNS_URI.contains( uri ) )
            throw new IllegalArgumentException( LOGGER.text( "002001012S", uri, XMLNS_URI.toString() ) );
        ТИПОВОЙ = XmlSvg.SVG_ELEMENT_USE.equals( тип().НАЗВАНИЕ ) ?
                new ТиповойImpl<>( () -> xlink( ссылка(), NeoГрафика.class, положение( "." ) ) ) : null; 
        ГРАФИКИ = new КоллекцияПоСвязи<>( node, Связь.Графика, n -> new NeoГрафика( n ) );
    }

    @Override
    public АтрибутныйКлюч тип() 
    {
        return КЛЮЧ_Э_ГРАФИКА.get( getNodeName( null ) );
    }

    @Override
    public final Коллекция<DbГрафика> графики()
    {
        return (Коллекция)ГРАФИКИ;
    }

    //<editor-fold defaultstate="collapsed" desc="implements Типовой">
    
    @Override
    public NeoГрафика экземпляр() 
    { 
        return ТИПОВОЙ != null ? ТИПОВОЙ.экземпляр() : null; 
    }
    
    @Override
    public String ссылка() 
    { 
        return ТИПОВОЙ != null ? ТИПОВОЙ.ссылка() : null; 
    }
    
    @Override
    public void ссылка( String значение ) 
    { 
        if( ТИПОВОЙ != null )
            ТИПОВОЙ.ссылка( значение ); 
        else
            throw new UnsupportedOperationException();
    }
    
    @Override
    public XLinkShow вид() 
    { 
        return ТИПОВОЙ != null ? ТИПОВОЙ.вид() : null; 
    }
    
    @Override
    public void вид( XLinkShow значение )
    { 
        if( ТИПОВОЙ != null )
            ТИПОВОЙ.вид( значение );
        else
            throw new UnsupportedOperationException();
    }
    
    @Override
    public XLinkActuate реализация() 
    { 
        return ТИПОВОЙ != null ? ТИПОВОЙ.реализация() : null; 
    }
    
    @Override
    public void реализация( XLinkActuate значение )
    { 
        if( ТИПОВОЙ != null )
            ТИПОВОЙ.реализация( значение ); 
        else
            throw new UnsupportedOperationException();
    }
    
    //</editor-fold>
    
}

package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.xml.PiProcessor;
import java.util.HashMap;
import java.util.Map;
import org.neo4j.graphdb.Node;

/**
 * Фабрика процессоров инструкций XML (Processing Instructions).
 * Процессоры обрабатывает XML элементы типа {@literal <?target instruction ?> }.
 *
 * @author &copy; 2013 Николай Варанкин
 */
class PiProcessorFactory 
{
    /**
     * @return фабрика процессоров инструкций XML (Processing Instructions).
     */
    static PiProcessorFactory getInstance() 
    {
        return PiProcessorFactoryHolder.INSTANCE;
    }
    
    private static class PiProcessorFactoryHolder 
    {
        private static final PiProcessorFactory INSTANCE;
        static
        {
            INSTANCE = new PiProcessorFactory();
            INSTANCE.КАТАЛОГ.put( XPathProcessor.TARGET, new XPathProcessor() );
        }
    }
    
    private final Map<String,PiProcessor<Node>> КАТАЛОГ;

    private PiProcessorFactory() 
    {
        КАТАЛОГ = new HashMap<>();
    }
    
    /**
     * Возвращает процессор инструкций XML (Processing Instructions).
     * 
     * @param target название класса обрабатываемых инструкций.
     * @return процессор инструкций или {@code null}.
     */
    PiProcessor<Node> get( String target )
    {
        return КАТАЛОГ.get( target );
    }
    
}

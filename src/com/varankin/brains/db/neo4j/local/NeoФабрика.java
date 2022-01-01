package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.xml.Xml;
import com.varankin.brains.db.xml.XmlBrains;
import com.varankin.brains.db.xml.ЗонныйКлюч;
import com.varankin.io.xml.svg.XmlSvg;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

import static com.varankin.brains.db.neo4j.local.Связь.*;
import static com.varankin.brains.db.xml.XmlBrains.*;

/**
 * Фабрика элементов базы данных для Neo4j.
 *
 * @author &copy; 2020 Николай Варанкин
 */
final class NeoФабрика
{
    private static final NeoФабрика ФАБРИКА = new NeoФабрика();

    private NeoФабрика() {}
    
    static NeoФабрика getInstance()
    {
        return ФАБРИКА;
    }
    
    static NeoАтрибутный создать( ЗонныйКлюч ключ, GraphDatabaseService сервис )
    {
        NeoАтрибутный э;
        if( ключ.ЗОНА == null )
            if( Xml.XML_CDATA.equals( ключ.НАЗВАНИЕ ) )
            {
                э = new NeoТекстовыйБлок( сервис );
            }
            else if( Xml.PI_ELEMENT.equals( ключ.НАЗВАНИЕ ) )
            {
                э = new NeoИнструкция( сервис );
            }
            else
            {
                э = new NeoУзел( сервис, ключ );
            }
        else if( XmlBrains.XMLNS_BRAINS.equals( ключ.ЗОНА ) )
            switch( ключ.НАЗВАНИЕ )
            {
                case XmlBrains.XML_BRAINS:    э = new NeoПакет( сервис ); break;
                case XmlBrains.XML_COMPUTE:   э = new NeoРасчет( сервис ); break;
                case XmlBrains.XML_BASKET:    э = new NeoМусор( сервис ); break;
                case XmlBrains.XML_FIELD:     э = new NeoПоле( сервис ); break;
                case XmlBrains.XML_FRAGMENT:  э = new NeoФрагмент( сервис ); break;
                case XmlBrains.XML_JAVA:      э = new NeoКлассJava( сервис ); break;
                case XmlBrains.XML_JOINT:     э = new NeoСоединение( сервис ); break;
                case XmlBrains.XML_LIBRARY:   э = new NeoБиблиотека( сервис ); break;
                case XmlBrains.XML_MODULE:    э = new NeoМодуль( сервис ); break;
                case XmlBrains.XML_NOTE:      э = new NeoЗаметка( сервис ); break;
                case XmlBrains.XML_PARAMETER: э = new NeoПараметр( сервис ); break;
                case XmlBrains.XML_PIN:       э = new NeoКонтакт( сервис ); break;
                case XmlBrains.XML_POINT:     э = new NeoТочка( сервис ); break;
                case XmlBrains.XML_PROCESSOR: э = new NeoПроцессор( сервис ); break;
                case XmlBrains.XML_PROJECT:   э = new NeoПроект( сервис ); break;
                case XmlBrains.XML_SENSOR:    э = new NeoСенсор( сервис ); break;
                case XmlBrains.XML_SIGNAL:    э = new NeoСигнал( сервис ); break;
                case XmlBrains.XML_TIMELINE:  э = new NeoЛента( сервис ); break;
                default:                      э = new NeoУзел( сервис, ключ );
            }
        else if( XmlSvg.XMLNS_SVG.equals( ключ.ЗОНА ) )
        {
            // все элементы SVG
            э = new NeoГрафика( сервис, ключ );
        }
        else
        {
            э = new NeoУзел( сервис, ключ );
        }
        return э;
    }
    
    @Deprecated //TODO дубликаты по Коллекция'м
    static NeoАтрибутный создать( Node node )
    {
        if( node == null ) return null;
        NeoАтрибутный атр;
        String uri = Architect.getURI( node );
        if( XmlBrains.XMLNS_BRAINS.equals( uri ) )
            switch( Architect.getXmlEntry( node, "" ) )
            {
                case XmlBrains.XML_BRAINS:    атр = new NeoПакет( node ); break;
                case XmlBrains.XML_COMPUTE:   атр = new NeoРасчет( node ); break;
                case XmlBrains.XML_BASKET:    атр = new NeoМусор( node ); break;
                case XmlBrains.XML_FIELD:     атр = new NeoПоле( node ); break;
                case XmlBrains.XML_FRAGMENT:  атр = new NeoФрагмент( node ); break;
                case XmlBrains.XML_JAVA:      атр = new NeoКлассJava( node ); break;
                case XmlBrains.XML_JOINT:     атр = new NeoСоединение( node ); break;
                case XmlBrains.XML_LIBRARY:   атр = new NeoБиблиотека( node ); break;
                case XmlBrains.XML_MODULE:    атр = new NeoМодуль( node ); break;
                case XmlBrains.XML_NOTE:      атр = new NeoЗаметка( node ); break;
                case XmlBrains.XML_PARAMETER: атр = new NeoПараметр( node ); break;
                case XmlBrains.XML_PIN:       атр = new NeoКонтакт( node ); break;
                case XmlBrains.XML_POINT:     атр = new NeoТочка( node ); break;
                case XmlBrains.XML_PROCESSOR: атр = new NeoПроцессор( node ); break;
                case XmlBrains.XML_PROJECT:   атр = new NeoПроект( node ); break;
                case XmlBrains.XML_SENSOR:    атр = new NeoСенсор( node ); break;
                case XmlBrains.XML_SIGNAL:    атр = new NeoСигнал( node ); break;
                case XmlBrains.XML_TIMELINE:  атр = new NeoЛента( node ); break;
                default:                      атр = new NeoАтрибутный( node ) {};
            }
        else if( XmlSvg.XMLNS_SVG.equals( uri ) )
        {
            // все элементы SVG
            атр = new NeoГрафика( node );
        }
        else
        {
            атр = new NeoУзел( node );
        }
        return атр;
    }
    
    static RelationshipType связь( String name, String uri )
    {
        RelationshipType type;
        if( uri == null )
        {
            switch( name )
            {
                case Xml.XML_CDATA:   type = Текст; break;
                case Xml.PI_ELEMENT:  type = Инструкция; break;
                default: type = Прочее;//throw new SAXException( lName );
            }            
        }
        else if( XmlBrains.XMLNS_BRAINS.equals( uri ) )
        {
            switch( name )
            {
                case XML_BRAINS:    type = Пакет; break;
                case XML_COMPUTE:   type = Расчет; break;
                case XML_BASKET:    type = Мусор; break;
                case XML_FIELD:     type = Поле; break;
                case XML_FRAGMENT:  type = Фрагмент; break;
                case XML_JAVA:      type = КлассJava; break;
                case XML_JOINT:     type = Соединение; break;
                case XML_LIBRARY:   type = Библиотека; break;
                case XML_MODULE:    type = Модуль; break;
                case XML_NOTE:      type = Заметка; break;
                case XML_PARAMETER: type = Параметр; break;
                case XML_PIN:       type = Контакт; break;
                case XML_POINT:     type = Точка; break;
                case XML_PROCESSOR: type = Процессор; break;
                case XML_PROJECT:   type = Проект; break;
                case XML_SENSOR:    type = Сенсор; break;
                case XML_SIGNAL:    type = Сигнал; break;
                case XML_TIMELINE:  type = Лента; break;
                default: type = Прочее;//throw new SAXException( lName );
            }
        }
        else if( XmlSvg.XMLNS_SVG.equals( uri ) )
        {
            type = Графика;
        }
        else
        {
            type = Прочее;
        }
        return type;
    }

}

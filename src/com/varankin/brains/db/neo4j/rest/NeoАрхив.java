package com.varankin.brains.db.neo4j.rest;

import com.varankin.brains.db.type.DbАрхив;
import com.varankin.brains.db.type.DbЗона;
import com.varankin.brains.db.type.DbМусор;
import com.varankin.brains.db.type.DbПакет;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.varankin.brains.db.*;
import com.varankin.brains.db.type.DbАтрибутный;
import com.varankin.brains.db.xml.ЗонныйКлюч;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.Collection;
import java.util.Date;

/**
 * Архив мыслительных структур на удаленной базе Neo4j.
 *
 * @author &copy; 2021 Николай Варанкин
 */
public class NeoАрхив extends NeoАтрибутный implements DbАрхив
{
    WebResource resource;

    public NeoАрхив( URI uri )
    {
        resource = Client.create().resource( uri ); // ex. // http://localhost:7474/db/data/
        // check the connection to the server
        ClientResponse response = resource.get( ClientResponse.class );
        assert response.getStatus() == 200; // OK
        response.close();
        //WebResource r = resource.uri(new URI("project"));
//        расположение( uri.toString() );
    }

    @Override
    public String название() 
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void название( String значение )
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Коллекция<DbПакет> пакеты()
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Коллекция<DbЗона> namespaces()
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String расположение()
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }
    
    private void расположение( String значение )
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Коллекция<DbМусор> мусор()
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DbАтрибутный создатьНовыйЭлемент( ЗонныйКлюч ключ )
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DbЗона определитьПространствоИмен( String uri, String префикс )
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object setPropertyValue( String name, Object newValue )
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getPropertyValue( String name )
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<PropertyChangeListener> listeners()
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Date создан() 
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Date изменен() 
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void закрыть()
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

}

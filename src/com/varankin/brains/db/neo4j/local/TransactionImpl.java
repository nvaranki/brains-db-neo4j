package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.Транзакция;
import org.neo4j.graphdb.Lock;
import org.neo4j.graphdb.PropertyContainer;

/**
 * Транзакция архива мыслительных структур на основе базы Neo4j.
 *
 * @author &copy; 2020 Николай Варанкин
 */
final class TransactionImpl implements Транзакция
{
    private final org.neo4j.graphdb.Transaction transaction;

    TransactionImpl( org.neo4j.graphdb.Transaction t )
    {
        transaction = t;
    }

    @Override
    public void завершить( boolean фиксация ) 
    {
        if( фиксация )
            transaction.success();
        else
            transaction.failure();
        // PlaceboTransaction завершит только свою часть
        // TopLevelTransaction завершит всю транзакцию окончательно
        transaction.close(); 
    }

    @Override
    public Блокировка согласовать( Режим режим, Object o ) 
    {
        PropertyContainer pc;
        if( o instanceof PropertyContainer )
            pc = (PropertyContainer)o;
        else if( o instanceof NeoNode )
            pc = ((NeoNode)o).getNode();
        else
            throw new IllegalArgumentException( String.valueOf( o ) );
        
        Lock lock;
        switch( режим )
        {
            case ЧТЕНИЕ_БЕЗ_ЗАПИСИ:
                lock = transaction.acquireReadLock( pc );
                break;
                
            case ЗАПРЕТ_ДОСТУПА:
                lock = transaction.acquireWriteLock( pc );
                break;
                
            default:
                throw new IllegalArgumentException( String.valueOf( режим ) );
        }
        
        return new LockImpl( lock );
    }

    @Override
    public void close() throws Exception
    {
        // проверено на версии Neo4j 2.3.7:
        // если transaction.success() прежде не вызывалось, это равносильно 
        // тому, что сейчас вызовется transaction.failure();
        // если сейчас PlaceboTransaction, transaction.failure() разрушит TopLevelTransaction; 
        // поэтому вызов this.завершить(boolean) обязателен!
        transaction.close();
    }
    
    private static class LockImpl implements Транзакция.Блокировка
    {
        final org.neo4j.graphdb.Lock LOCK;

        LockImpl( org.neo4j.graphdb.Lock lock ) 
        {
            LOCK = lock;
        }

        @Override
        public void снять() 
        {
            LOCK.release();
        }

    }

}

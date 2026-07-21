package bank.dao;

import java.util.List;

public interface BaseDAO<T, K> {
    List<T> findAll();          // get all records
    T findById(K id);           // get one by primary key
    void insert(T entity);      // add new record
    void update(T entity);      // modify existing
    void delete(K id);          // remove by primary key
    List<T> search(String keyword); // search across fields
}
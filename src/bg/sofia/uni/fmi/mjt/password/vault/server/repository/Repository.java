package bg.sofia.uni.fmi.mjt.password.vault.server.repository;

import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.ElementAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.ElementNotFoundException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.UserDeletionException;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public interface Repository<K, V> {

    V get(K key) throws ElementNotFoundException;

    boolean contains(K key);

    void put(V elem, Writer writer) throws ElementAlreadyExistsException;

    void update(V elem, Writer writer) throws ElementNotFoundException;

    void remove(K key, Writer writer) throws ElementNotFoundException, UserDeletionException;

    Map<K, V> getAll();

    void deletePathIfExists() throws IOException;

    void refresh();

}
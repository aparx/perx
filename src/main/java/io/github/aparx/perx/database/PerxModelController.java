package io.github.aparx.perx.database;

import com.j256.ormlite.dao.Dao;

/**
 * Interface that is loadable and acts as a bridge between a database model entity and a runtime
 * object that represents or wraps around that model.
 *
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 05:20
 * @since 1.0
 */
public interface PerxModelController<T extends Dao<?, ?>> {

  void load();

  T getDao();

}

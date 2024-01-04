package io.github.aparx.perx.database;

import com.j256.ormlite.dao.Dao;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 05:20
 * @since 1.0
 */
public interface PerxModelController<TDao extends Dao<?, ?>> {

  void load();

  TDao getDao();

}

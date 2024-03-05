package at.ac.tuwien.sepr.assignment.individual.persistence.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.BreedSearchDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Breed;
import at.ac.tuwien.sepr.assignment.individual.persistence.BreedDao;
import java.lang.invoke.MethodHandles;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BreedJdbcDao implements BreedDao {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String TABLE_NAME = "breed";
  private static final String SQL_ALL =
      "SELECT * FROM " + TABLE_NAME;
  private static final String SQL_FIND_BY_IDS =
      "SELECT * FROM " + TABLE_NAME
          + " WHERE id IN (:ids)";
  private static final String SQL_SEARCH =
      "SELECT * FROM " + TABLE_NAME
          + " WHERE UPPER(name) LIKE UPPER('%'||:name||'%')";
  private static final String SQL_LIMIT_CLAUSE = " LIMIT :limit";

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public BreedJdbcDao(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public Collection<Breed> allBreeds() {
    LOG.trace("allBreeds()");
    return jdbcTemplate.query(SQL_ALL, this::mapRow);
  }

  @Override
  public Collection<Breed> findBreedsById(Set<Long> breedIds) {
    LOG.trace("findBreedsById({})", breedIds);
    return jdbcTemplate.query(SQL_FIND_BY_IDS, Map.of("ids", breedIds), this::mapRow);
  }

  @Override
  public Collection<Breed> search(BreedSearchDto searchParams) {
    String query = SQL_SEARCH;
    if (searchParams.limit() != null) {
      query += SQL_LIMIT_CLAUSE;
    }
    return jdbcTemplate.query(query, new BeanPropertySqlParameterSource(searchParams), this::mapRow);
  }

  private Breed mapRow(ResultSet resultSet, int i) throws SQLException {
    return new Breed()
        .setId(resultSet.getLong("id"))
        .setName(resultSet.getString("name"))
        ;
  }
}

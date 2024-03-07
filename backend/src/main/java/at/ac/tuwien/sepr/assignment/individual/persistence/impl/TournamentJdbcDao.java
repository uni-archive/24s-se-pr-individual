package at.ac.tuwien.sepr.assignment.individual.persistence.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentSearchDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.entity.Tournament;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepr.assignment.individual.persistence.TournamentDao;
import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.lang.invoke.MethodHandles;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.List;

@Repository
public class TournamentJdbcDao implements TournamentDao {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String TABLE_NAME = "tournament";
  private static final String SQL_SELECT_BY_ID = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
  private static final String SQL_SELECT_SEARCH = "SELECT * FROM " + TABLE_NAME + " WHERE"
      + "(:name IS NULL OR UPPER(name) LIKE UPPER('%'||:name||'%'))"
      + "AND ("
      + "(:startDate IS NULL AND :endDate IS NULL) OR"
      + "(:startDate IS NULL AND :endDate IS NOT NULL AND end_date <= :endDate) OR"
      + "(:startDate IS NOT NULL AND :endDate IS NULL AND start_date >= :startDate))"
      + "OR (:startDate IS NOT NULL AND :endDate IS NOT NULL AND start_date >= :startDate AND end_date <= :endDate)";

  private static final String SQL_LIMIT_CLAUSE = " LIMIT :limit";

  private final JdbcTemplate jdbcTemplate;
  private final NamedParameterJdbcTemplate jdbcNamed;


  public TournamentJdbcDao(
      NamedParameterJdbcTemplate jdbcNamed,
      JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    this.jdbcNamed = jdbcNamed;
  }

  @Override
  public Collection<Tournament> search(TournamentSearchDto searchParameters) {
    LOG.trace("search({})", searchParameters);
    var query = SQL_SELECT_SEARCH;
    if (searchParameters.limit() != null) {
      query += SQL_LIMIT_CLAUSE;
    }
    var params = new BeanPropertySqlParameterSource(searchParameters);

    return jdbcNamed.query(query, params, this::mapRow);
  }


  private Tournament mapRow(ResultSet result, int rownum) throws SQLException {
    return new Tournament()
        .setId(result.getLong("id"))
        .setName(result.getString("name"))
        .setStartDate(result.getDate("start_date").toLocalDate())
        .setEndDate(result.getDate("end_date").toLocalDate())
        ;
  }
}

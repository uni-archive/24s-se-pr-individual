package at.ac.tuwien.sepr.assignment.individual.persistence.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.TournamentDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentDetailParticipantDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentStandingsDto;
import at.ac.tuwien.sepr.assignment.individual.entity.BranchPosition;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.entity.Tournament;
import at.ac.tuwien.sepr.assignment.individual.entity.TournamentParticipant;
import at.ac.tuwien.sepr.assignment.individual.entity.TournamentTree;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.persistence.TournamentDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.lang.invoke.MethodHandles;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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

  private static final String SQL_CREATE_TOURNAMENT = "INSERT INTO " + TABLE_NAME + " (name, start_date, end_date) VALUES (:name, :startDate, :endDate)";
  private static final String SQL_CREATE_PARTICIPANT = "INSERT INTO tournament_participant (tournament_id, horse_id, entry_number) VALUES (?, ?, ?)";
  private static final String SQL_FIND_PARTICIPANTS_BY_TOURNAMENT_ID = "SELECT * FROM tournament_participant WHERE tournament_id = ?";
  private static final String SQL_CREATE_TOURNAMENT_TREE = "INSERT INTO tournament_tree (tournament_id, participant_id, parent_id, branch_position) VALUES (?, NULL, ?, ?)";
  private static final String SQL_FIND_BRANCHES_BY_TOURNAMENT_ID = "SELECT * FROM tournament_tree WHERE tournament_id = ?";
  private static final String SQL_UPDATE_BRANCHES = "UPDATE tournament_tree SET participant_id = ? WHERE id = ?";
// (SELECT id FROM tournament_participant WHERE horse_id = ? AND tournament_id = ?)
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

  @Override
  public Tournament create(TournamentDetailDto toCreate) throws ConflictException {
    LOG.trace("create({})", toCreate);
    var tournament = new Tournament()
        .setName(toCreate.name())
        .setStartDate(toCreate.startDate())
        .setEndDate(toCreate.endDate());
    var keyHolder = new GeneratedKeyHolder();
    var x = jdbcNamed.update(SQL_CREATE_TOURNAMENT, new BeanPropertySqlParameterSource(tournament), keyHolder);
    var id = keyHolder.getKey().longValue();

    tournament.setId(id);

    for (var participant : toCreate.participants()) {
      jdbcTemplate.update(SQL_CREATE_PARTICIPANT,
          id,
          participant.horseId(),
          participant.entryNumber());
    }

    createTree(id);

    LOG.debug("tournament: {}", x);
    return tournament;
  }


  private void createTree(Long tournamentId) {
    LOG.trace("createTree({})", tournamentId);
    createTreeBranch(tournamentId, null, BranchPosition.FINAL_WINNER, 4);
  }

  // todo javadoc
  private void createTreeBranch(Long tournamentId, Long parentId, BranchPosition branchPosition, int remaining) {
    if (remaining <= 0)
      return;

    KeyHolder keyHolder = new GeneratedKeyHolder();

    jdbcTemplate.update(
        connection -> {
          PreparedStatement ps = connection.prepareStatement(SQL_CREATE_TOURNAMENT_TREE, new String[] {"id"});
          ps.setLong(1, tournamentId);
          if (parentId != null)
            ps.setLong(2, parentId);
          else
            ps.setNull(2, Types.BIGINT);
          ps.setString(3, branchPosition.toString());
          return ps;
        },
        keyHolder);
    var id = keyHolder.getKey().longValue();

    createTreeBranch(tournamentId, id, BranchPosition.UPPER, remaining - 1);
    createTreeBranch(tournamentId, id, BranchPosition.LOWER, remaining - 1);
  }

  @Override
  public Tournament getById(long id) throws NotFoundException {
    LOG.trace("getById({})", id);
    List<Tournament> tournaments;
    tournaments = jdbcTemplate.query(SQL_SELECT_BY_ID, this::mapRow, id);

    if (tournaments.isEmpty()) {
      throw new NotFoundException("No tournament with ID %d found".formatted(id));
    }
    if (tournaments.size() > 1) {
      // This should never happen!!
      throw new FatalException("Too many tournaments with ID %d found".formatted(id));
    }

    return tournaments.get(0);
  }

  @Override
  public Collection<TournamentParticipant> getParticipantsByTournamentId(long id) {
    LOG.trace("getParticipantsByTournamentId({})", id);
    return jdbcTemplate.query(SQL_FIND_PARTICIPANTS_BY_TOURNAMENT_ID, this::mapParticipantRow, id);
  }

  @Override
  public Collection<TournamentTree> getBranchesByTournamentId(long id) {
    LOG.trace("getStandingsById({})", id);
    return jdbcTemplate.query(SQL_FIND_BRANCHES_BY_TOURNAMENT_ID, this::mapBranchRow, id);
  }

  @Override
  public void updateStandings(Collection<TournamentTree> branches) {
    var branchesList = new ArrayList<>(branches);
    jdbcTemplate.batchUpdate(SQL_UPDATE_BRANCHES, new BatchPreparedStatementSetter() {
      @Override
      public void setValues(PreparedStatement ps, int i) throws SQLException {
        var b = branchesList.get(i);
        if (b.getParticipantId() == null)
          ps.setNull(1, Types.BIGINT);
        else
          ps.setLong(1, b.getParticipantId());
        ps.setLong(2, b.getId());
      }

      @Override
      public int getBatchSize() {
        return branchesList.size();
      }
    });
  }

  private TournamentParticipant mapParticipantRow(ResultSet result, int rownum) throws SQLException {
    return new TournamentParticipant()
        .setId(result.getLong("id"))
        .setTournamentId(result.getLong("tournament_id"))
        .setHorseId(result.getLong("horse_id"))
        .setEntryNumber(result.getInt("entry_number"))
        .setRoundReached(result.getInt("round_reached"))
        ;
  }

  private TournamentTree mapBranchRow(ResultSet result, int rownum) throws SQLException {
    return new TournamentTree()
        .setId(result.getLong("id"))
        .setTournamentId(result.getLong("tournament_id"))
        .setParticipantId(result.getLong("participant_id"))
        .setParentId(result.getLong("parent_id"))
        .setBranchPosition(BranchPosition.valueOf(result.getString("branch_position")))
        ;
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

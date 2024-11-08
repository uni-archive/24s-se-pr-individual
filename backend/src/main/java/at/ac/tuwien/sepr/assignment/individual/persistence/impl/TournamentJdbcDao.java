package at.ac.tuwien.sepr.assignment.individual.persistence.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.TournamentDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentSearchDto;
import at.ac.tuwien.sepr.assignment.individual.type.BranchPosition;
import at.ac.tuwien.sepr.assignment.individual.entity.Tournament;
import at.ac.tuwien.sepr.assignment.individual.entity.TournamentParticipant;
import at.ac.tuwien.sepr.assignment.individual.entity.TournamentTree;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.persistence.TournamentDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
  private static final String SQL_CREATE_TOURNAMENT_TREE = "INSERT INTO tournament_tree (tournament_id, participant_id, parent_id,"
      + " branch_position, first_round_index) VALUES (?, NULL, ?, ?, ?)";
  private static final String SQL_FIND_BRANCHES_BY_TOURNAMENT_ID = "SELECT * FROM tournament_tree WHERE tournament_id = ?";
  private static final String SQL_FIND_FIRST_ROUND_BRANCHES_BY_TOURNAMENT_ID = "SELECT * FROM tournament_tree WHERE tournament_id = ?"
      + " AND first_round_index IS NOT NULL";
  private static final String SQL_UPDATE_BRANCHES = "UPDATE tournament_tree SET participant_id = ? WHERE id = ?";
  private static final String SQL_UPDATE_PARTICIPANTS = "UPDATE tournament_participant SET round_reached = ? WHERE id = ?";
  private static final String SQL_FIND_PARTICIPANTS_BY_HORSE_IDS = "SELECT "
      + "p.id AS id, p.tournament_id AS tournament_id, p.horse_id AS horse_id, p.entry_number AS entry_number, p.round_reached AS round_reached"
      + " FROM tournament_participant AS p LEFT JOIN tournament AS t ON t.id = p.tournament_id WHERE p.horse_id IN (:ids) AND ("
      + "start_date BETWEEN DATEADD('YEAR', -1, CURRENT_DATE) AND CURRENT_DATE OR "
      + "end_date BETWEEN DATEADD('YEAR', -1, CURRENT_DATE) AND CURRENT_DATE OR "
      + "(start_date <= DATEADD('YEAR', -1, CURRENT_DATE) AND end_date >= CURRENT_DATE)"
      + ");";

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
  @Transactional
  public Tournament create(TournamentDetailDto toCreate) throws ConflictException {
    LOG.trace("create({})", toCreate);
    var tournament = new Tournament()
        .setName(toCreate.name())
        .setStartDate(toCreate.startDate())
        .setEndDate(toCreate.endDate());
    var keyHolder = new GeneratedKeyHolder();
    final var x = jdbcNamed.update(SQL_CREATE_TOURNAMENT, new BeanPropertySqlParameterSource(tournament), keyHolder);
    var id = keyHolder.getKey().longValue();

    tournament.setId(id);

    int numPart = 0;
    for (var participant : toCreate.participants()) {
      try {
        numPart += jdbcTemplate.update(SQL_CREATE_PARTICIPANT,
            id,
            participant.horseId(),
            participant.entryNumber());
      } catch (DataIntegrityViolationException e) {
        throw new ConflictException("Could not create tournament", List.of("One or more participants do not exist"), e);
      }
    }

    createTree(id);

    LOG.debug("tournament: {}", x);
    return tournament;
  }


  private void createTree(Long tournamentId) {
    LOG.trace("createTree({})", tournamentId);
    AtomicInteger firstRoundIndex = new AtomicInteger(0);
    createTreeBranch(tournamentId, null, BranchPosition.FINAL_WINNER, 4, firstRoundIndex);
  }


  private void createTreeBranch(Long tournamentId, Long parentId, BranchPosition branchPosition, int remaining, AtomicInteger firstRoundIndex) {
    LOG.trace("createTreeBranch({}, {}, {}, {}, {})", tournamentId, parentId, branchPosition, remaining, firstRoundIndex);
    if (remaining <= 0) {
      return;
    }

    KeyHolder keyHolder = new GeneratedKeyHolder();

    jdbcTemplate.update(
        connection -> {
          PreparedStatement ps = connection.prepareStatement(SQL_CREATE_TOURNAMENT_TREE, new String[] {"id"});
          ps.setLong(1, tournamentId);
          if (parentId != null) {
            ps.setLong(2, parentId);
          } else {
            ps.setNull(2, Types.BIGINT);
          }
          ps.setString(3, branchPosition.toString());
          if (remaining == 1) {
            ps.setInt(4, firstRoundIndex.getAndIncrement());
          } else {
            ps.setNull(4, Types.INTEGER);
          }
          return ps;
        },
        keyHolder);
    var id = keyHolder.getKey().longValue();

    createTreeBranch(tournamentId, id, BranchPosition.UPPER, remaining - 1, firstRoundIndex);
    createTreeBranch(tournamentId, id, BranchPosition.LOWER, remaining - 1, firstRoundIndex);
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
  public Collection<TournamentParticipant> getParticipantsByTournamentId(long id) throws NotFoundException {
    LOG.trace("getParticipantsByTournamentId({})", id);
    var participants = jdbcTemplate.query(SQL_FIND_PARTICIPANTS_BY_TOURNAMENT_ID, this::mapParticipantRow, id);
    if (participants.isEmpty()) {
      throw new NotFoundException("No participants with tournament ID %d found".formatted(id));
    }
    return participants;
  }

  @Override
  public Collection<TournamentTree> getBranchesByTournamentId(long id) throws NotFoundException {
    LOG.trace("getBranchesByTournamentId({})", id);
    var out = jdbcTemplate.query(SQL_FIND_BRANCHES_BY_TOURNAMENT_ID, this::mapBranchRow, id);
    if (out.isEmpty()) {
      throw new NotFoundException("No branches with tournament ID %d found".formatted(id));
    }
    return out;
  }

  @Override
  public Collection<TournamentTree> getFirstRoundBranchesByTournamentId(long id) throws NotFoundException {
    LOG.trace("getFirstRoundBranchesByTournamentId({})", id);
    var out = jdbcTemplate.query(SQL_FIND_FIRST_ROUND_BRANCHES_BY_TOURNAMENT_ID, this::mapBranchRow, id);
    if (out.isEmpty()) {
      throw new NotFoundException("No branches with tournament ID %d found".formatted(id));
    }
    return out;
  }

  @Override
  public Collection<TournamentParticipant> getParticipationsForHorseIds(Collection<Long> horseIds) {
    LOG.trace("getParticipationsForHorseIds({})", horseIds);
    return jdbcNamed.query(SQL_FIND_PARTICIPANTS_BY_HORSE_IDS, Map.of("ids", horseIds), this::mapParticipantRow);
  }

  @Override
  public void updateStandings(Collection<TournamentTree> branches) throws NotFoundException, ConflictException {
    LOG.trace("updateStandings({})", branches);
    var branchesList = new ArrayList<>(branches);
    var affected = jdbcTemplate.batchUpdate(SQL_UPDATE_BRANCHES, new BatchPreparedStatementSetter() {
      @Override
      public void setValues(PreparedStatement ps, int i) throws SQLException {
        var b = branchesList.get(i);
        if (b.getParticipantId() == null) {
          ps.setNull(1, Types.BIGINT);
        } else {
          ps.setLong(1, b.getParticipantId());
        }
        ps.setLong(2, b.getId());
      }

      @Override
      public int getBatchSize() {
        return branchesList.size();
      }
    });
    var affectedTotal = Arrays.stream(affected).reduce(0, Integer::sum);
    if (affectedTotal == 0) {
      throw new NotFoundException("Branches probably do not exist. Affected rows: {}".formatted(affectedTotal));
    }

    if (affectedTotal != branches.size()) {
      throw new ConflictException("Error while updating branches", List.of("Invalid number of branches affected. Affected rows: {}".formatted(affectedTotal)));
    }

  }

  @Override
  public void updateParticipants(Collection<TournamentParticipant> participants) throws NotFoundException, ConflictException {
    LOG.trace("updateParticipants({})", participants);
    var participantsList = new ArrayList<>(participants);
    var affected = jdbcTemplate.batchUpdate(SQL_UPDATE_PARTICIPANTS, new BatchPreparedStatementSetter() {
      @Override
      public void setValues(PreparedStatement ps, int i) throws SQLException {
        var b = participantsList.get(i);
        ps.setInt(1, b.getRoundReached());
        ps.setLong(2, b.getId());
      }

      @Override
      public int getBatchSize() {
        return participantsList.size();
      }
    });
    var affectedTotal = Arrays.stream(affected).reduce(0, Integer::sum);
    if (affectedTotal == 0) {
      throw new NotFoundException("Branches probably do not exist. Affected rows: {}".formatted(affectedTotal));
    }

    if (affectedTotal != participantsList.size()) {
      throw new ConflictException("Error while updating branches", List.of("Invalid number of branches affected. Affected rows: {}".formatted(affectedTotal)));
    }

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
        .setFirstRoundIndex(result.getInt("first_round_index"));
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

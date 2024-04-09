package at.ac.tuwien.sepr.assignment.individual.persistence;

import at.ac.tuwien.sepr.assignment.individual.TestBase;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSelectionDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentCreateDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.entity.Tournament;
import at.ac.tuwien.sepr.assignment.individual.entity.TournamentParticipant;
import at.ac.tuwien.sepr.assignment.individual.entity.TournamentTree;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.mapper.TournamentMapper;
import at.ac.tuwien.sepr.assignment.individual.type.BranchPosition;
import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.Not;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@ActiveProfiles({"test", "datagen"})
// enable "test" spring profile during test execution in order to pick up configuration from application-test.yml
@SpringBootTest
public class TournamentDaoTest extends TestBase {

  @Autowired
  TournamentDao tournamentDao;
  @Autowired
  TournamentMapper tournamentMapper;

  @Test
  public void createTournamentTest() throws Exception {
    var createDto = new TournamentCreateDto(
        -123L,
        "Tournament created by test",
        LocalDate.of(2021, 12, 31),
        LocalDate.of(2022, 1, 1),
        List.of(
            new HorseSelectionDto(-1, null, null),
            new HorseSelectionDto(-2, null, null),
            new HorseSelectionDto(-3, null, null),
            new HorseSelectionDto(-4, null, null),
            new HorseSelectionDto(-5, null, null),
            new HorseSelectionDto(-6, null, null),
            new HorseSelectionDto(-7, null, null),
            new HorseSelectionDto(-8, null, null)
        )
    );
    var mappedCreateDto = tournamentMapper.createDtoToEntity(createDto);

    Tournament tournament = tournamentDao.create(mappedCreateDto);
    assertNotNull(tournament);

    Collection<TournamentParticipant> participants = tournamentDao.getParticipantsByTournamentId(tournament.getId());

    assertNotNull(participants);
    // test that participants have size 8, contain all ids from 1 to 8 and have the correct tournament id
    assertThat(participants)
        .hasSize(8)
        .extracting(TournamentParticipant::getHorseId)
        .containsExactlyInAnyOrder(-1L, -2L, -3L, -4L, -5L, -6L, -7L, -8L);

    assertThat(participants)
        .hasSize(8)
        .extracting(TournamentParticipant::getTournamentId)
        .containsOnly(tournament.getId());

    Collection<TournamentTree> branches = null;
    try {
      branches = tournamentDao.getBranchesByTournamentId(tournament.getId());
    } catch (NotFoundException e) {
      fail();
    }

    assertNotNull(branches);
    assertThat(branches)
        .hasSize(15)
        .extracting(TournamentTree::getTournamentId)
        .containsOnly(tournament.getId());
    assertThat(branches)
        .extracting(TournamentTree::getParticipantId)
        .containsOnly(0L);
  }

  @Test
  public void failCreateTournamentTest() {
    var createDto = new TournamentCreateDto(
        -123L,
        "Tournament created by test",
        LocalDate.of(2021, 12, 31),
        LocalDate.of(2022, 1, 1),
        List.of(
            new HorseSelectionDto(4000, null, null),
            new HorseSelectionDto(-2, null, null),
            new HorseSelectionDto(-3, null, null),
            new HorseSelectionDto(-4, null, null),
            new HorseSelectionDto(-5, null, null),
            new HorseSelectionDto(-6, null, null),
            new HorseSelectionDto(-7, null, null),
            new HorseSelectionDto(-8, null, null)
        )
    );
    assertThrows(ConflictException.class, () -> tournamentDao.create(tournamentMapper.createDtoToEntity(createDto)));
    assertThrows(NotFoundException.class, () -> tournamentDao.getById(-123L));
    assertThrows(NotFoundException.class, () -> tournamentDao.getParticipantsByTournamentId(-123L));
    assertThrows(NotFoundException.class, () -> tournamentDao.getBranchesByTournamentId(-123L));
  }

  @Test
  public void getFirstRoundsTest() throws Exception {
    var createDto = new TournamentCreateDto(
        -123L,
        "Tournament created by test",
        LocalDate.of(2021, 12, 31),
        LocalDate.of(2022, 1, 1),
        List.of(
            new HorseSelectionDto(-1, null, null),
            new HorseSelectionDto(-2, null, null),
            new HorseSelectionDto(-3, null, null),
            new HorseSelectionDto(-4, null, null),
            new HorseSelectionDto(-5, null, null),
            new HorseSelectionDto(-6, null, null),
            new HorseSelectionDto(-7, null, null),
            new HorseSelectionDto(-8, null, null)
        )
    );
    var mappedCreateDto = tournamentMapper.createDtoToEntity(createDto);
    Tournament tournament = null;
    try {
      tournament = tournamentDao.create(mappedCreateDto);
    } catch (ConflictException e) {
      fail();
    }
    assertNotNull(tournament);

    Collection<TournamentTree> branches = null;
    try {
      branches = tournamentDao.getFirstRoundBranchesByTournamentId(tournament.getId());
    } catch (NotFoundException e) {
      fail();
    }

    assertNotNull(branches);
    assertThat(branches).hasSize(8)
        .extracting(TournamentTree::getBranchPosition)
        .containsExactlyInAnyOrder(
            BranchPosition.LOWER,
            BranchPosition.LOWER,
            BranchPosition.LOWER,
            BranchPosition.LOWER,
            BranchPosition.UPPER,
            BranchPosition.UPPER,
            BranchPosition.UPPER,
            BranchPosition.UPPER
        );
  }

  @Test
  public void getInvalidTournamentByIdTest() {
    assertThrows(NotFoundException.class, () -> tournamentDao.getById(-123L));
  }
}

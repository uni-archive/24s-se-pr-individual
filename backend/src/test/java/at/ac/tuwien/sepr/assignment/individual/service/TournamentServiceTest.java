package at.ac.tuwien.sepr.assignment.individual.service;

import at.ac.tuwien.sepr.assignment.individual.TestBase;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSelectionDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentSearchDto;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.mapper.TournamentMapper;
import at.ac.tuwien.sepr.assignment.individual.persistence.TournamentDao;
import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles({"test", "datagen"}) // enable "test" spring profile during test execution in order to pick up configuration from application-test.yml
@SpringBootTest
public class TournamentServiceTest extends TestBase {

  @Autowired
  TournamentService tournamentService;
  @Autowired
  TournamentMapper mapper;
  @Autowired
  TournamentDao tournamentDao;

  @Test
  public void searchByOiFindsTwoTournaments() {
    var searchDto = new TournamentSearchDto("oi", null, null, null);
    var tournaments = tournamentService.search(searchDto);
    assertNotNull(tournaments);
    assertThat(tournaments)
        .extracting("id", "name", "startDate", "endDate")
        .as("ID, Name, Start Date, End Date")
        .containsExactlyInAnyOrder(
            tuple(-1L, "Tournament Yoinky", LocalDate.of(2014, 12, 15), LocalDate.of(2014, 12, 20)),
            tuple(-5L, "Tournament Foive", LocalDate.of(2021, 3, 10), LocalDate.of(2021, 3, 15))
        );
  }

  @Test
  public void getInvalidTournamentByIdTest() {
    assertThrows(NotFoundException.class, () -> tournamentService.getById(-123L));
  }

  @Test
  public void createTournamentAndGenerateFirstRounds() throws Exception {
    var createDto = new TournamentCreateDto(
        null,
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

    var tournament = tournamentService.create(createDto);
    assertNotNull(tournament);
    assertThat(tournament)
        .extracting("id", "name", "startDate", "endDate")
        .as("ID, Name, Start Date, End Date")
        .containsExactly(4L, "Tournament created by test", LocalDate.of(2021, 12, 31), LocalDate.of(2022, 1, 1));

    var emptyStandings = tournamentService.getStandingsById(tournament.id());
    assertNotNull(emptyStandings);

    var filledStandingsTree = tournamentService.generateFirstRoundMatches(tournament.id(), emptyStandings.tree());
    var firstRounds = tournamentDao.getFirstRoundBranchesByTournamentId(tournament.id());
    assertThat(firstRounds)
        .hasSize(8);
  }

  @Test
  public void failGenerateFirstRoundsForNonEmptyTournamentTest() throws Exception {
    var tree = tournamentService.getStandingsById(-1L).tree();
    assertThrows(ValidationException.class, () -> tournamentService.generateFirstRoundMatches(-1L, tree));
  }
}

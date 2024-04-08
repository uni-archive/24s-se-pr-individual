package at.ac.tuwien.sepr.assignment.individual.persistence;

import at.ac.tuwien.sepr.assignment.individual.dto.TournamentDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentStandingsDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Tournament;
import at.ac.tuwien.sepr.assignment.individual.entity.TournamentParticipant;
import at.ac.tuwien.sepr.assignment.individual.entity.TournamentTree;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for tournaments.
 * Implements access functionality to the application's persistent data store regarding tournaments.
 */
public interface TournamentDao {

  /**
   * Get the tournaments that match the given search parameters.
   * Parameters that are {@code null} are ignored.
   * The name is considered a match, if the given parameter is a substring of the field in tournament.
   *
   * @param searchParameters the parameters to use in searching.
   * @return the tournaments where all given parameters match.
   */
  Collection<Tournament> search(TournamentSearchDto searchParameters);

  /**
   * Create a new tournament in the persistent data store.
   *
   * @param toCreate the tournament to create
   * @return the created tournament
   * @throws ConflictException if there is a conflict with the data in the persistent data store
   */
  Tournament create(TournamentDetailDto toCreate) throws ConflictException;

  /**
   * Get a tournament by its ID from the persistent data store.
   *
   * @param id the ID of the tournament to get
   * @return the tournament
   * @throws NotFoundException if the Tournament with the given ID does not exist in the persistent data store
   */
  Tournament getById(long id) throws NotFoundException;

  /**
   * Get the participants of a tournament by the tournament's ID from the persistent data store.
   *
   * @param id the ID of the tournament
   * @return a collection of TournamentParticipant entities
   * @throws NotFoundException if the Tournament with the given ID does not exist in the persistent data store
   */
  Collection<TournamentParticipant> getParticipantsByTournamentId(long id) throws NotFoundException;


  /**
   * Get the branches of a tournament by the tournament's ID from the persistent data store.
   *
   * @param id the ID of the tournament
   * @return a collection of TournamentTree entities
   * @throws NotFoundException if the Tournament with the given ID does not exist in the persistent data store
   */
  Collection<TournamentTree> getBranchesByTournamentId(long id) throws NotFoundException;

  /**
   * Get the first round branches of a tournament by the tournament's ID from the persistent data store.
   *
   * @param id the ID of the tournament
   * @return a collection of TournamentTree entities
   * @throws NotFoundException if the Tournament with the given ID does not exist in the persistent data store
   */
  Collection<TournamentTree> getFirstRoundBranchesByTournamentId(long id) throws NotFoundException;


  /**
   * Update the standings of a tournament in the persistent data store.
   *
   * @param branches the branches to update
   * @throws NotFoundException if the Tournament with the given ID does not exist in the persistent data store
   * @throws ConflictException if there is a conflict with the data in the persistent data store
   */
  void updateStandings(Collection<TournamentTree> branches) throws NotFoundException, ConflictException;


  /**
   * Update the participants of a tournament in the persistent data store.
   *
   * @param participants the participants to update
   * @throws NotFoundException if the Tournament with the given ID does not exist in the persistent data store
   * @throws ConflictException if there is a conflict with the data in the persistent data store
   */
  void updateParticipants(Collection<TournamentParticipant> participants) throws NotFoundException, ConflictException;


  /**
   * Get the participations for a collection of horse IDs from the persistent data store.
   *
   * @param horseIds the IDs of the horses
   * @return a collection of TournamentParticipant entities
   */
  Collection<TournamentParticipant> getParticipationsForHorseIds(Collection<Long> horseIds);
}
